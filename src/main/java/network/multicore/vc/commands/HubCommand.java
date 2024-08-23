package network.multicore.vc.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import network.multicore.vc.VelocityCompact;
import network.multicore.vc.messages.Messages;
import network.multicore.vc.utils.Text;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class HubCommand extends AbstractCommand {
    private static final String PLAYER_ARG = "player";

    /**
     * Command usage:
     * /hub [player]
     */
    public HubCommand(ProxyServer proxy, VelocityCompact plugin) {
        super(proxy, plugin, "hub");
    }

    // TODO Test if permissions are correct
    public void register() {
        LiteralArgumentBuilder<CommandSource> hubRootNode = BrigadierCommand
                .literalArgumentBuilder(command)
                .requires(src -> src.hasPermission("vcompact.hub"))
                .executes((ctx) -> execute(ctx.getSource(), null));

        RequiredArgumentBuilder<CommandSource, String> playerNode = BrigadierCommand
                .requiredArgumentBuilder(PLAYER_ARG, StringArgumentType.word())
                .requires(src -> src.hasPermission("vcompact.hub.other"))
                .suggests((ctx, builder) -> {
                    String argument = ctx.getArguments().containsKey(PLAYER_ARG) ? ctx.getArgument(PLAYER_ARG, String.class) : "";

                    for (Player player : proxy.getAllPlayers()) {
                        String playerName = player.getUsername();

                        if (playerName.regionMatches(true, 0, argument, 0, argument.length())) builder.suggest(playerName);
                    }

                    if ("all".regionMatches(true, 0, argument, 0, argument.length())) builder.suggest("all");
                    if ("current".regionMatches(true, 0, argument, 0, argument.length())) builder.suggest("current");

                    return builder.buildFuture();
                })
                .executes((ctx) -> execute(ctx.getSource(), ctx.getArgument(PLAYER_ARG, String.class)));

        hubRootNode.then(playerNode);

        proxy.getCommandManager().register(buildMeta(), new BrigadierCommand(hubRootNode.build()));
    }

    private int execute(CommandSource src, String targetName) {
        String hubName = plugin.config().getString("hub");
        if (hubName == null || hubName.isEmpty()) {
            Text.send(plugin.messages().hubNotSet, src);
            return 0;
        }

        RegisteredServer server = proxy.getServer(hubName).orElse(null);
        if (server == null) {
            Text.send(plugin.messages().hubNotSet, src);
            return 0;
        }

        Set<Player> targets = new HashSet<>();

        if (targetName == null) {
            if (!(src instanceof Player player)) {
                Text.send(plugin.messages().notPlayer, src);
                return 0;
            }

            ServerConnection playerServer = player.getCurrentServer().orElse(null);
            if (playerServer == null || playerServer.getServer().equals(server)) {
                Text.send(plugin.messages().alreadyInHub, src);
                return 0;
            }

            targets.add(player);
        } else {
            if (targetName.equals("all")) {
                targets.addAll(proxy.getAllPlayers()
                        .stream()
                        .filter(p -> {
                            ServerConnection playerServer = p.getCurrentServer().orElse(null);
                            return playerServer != null && !playerServer.getServer().equals(server);
                        })
                        .toList()
                );

                if (targets.isEmpty()) {
                    Text.send(plugin.messages().noPlayerToSendToHub, src);
                    return 0;
                }
            } else if (targetName.equals("current")) {
                if (!(src instanceof Player player)) {
                    Text.send(plugin.messages().notPlayer, src);
                    return 0;
                }

                ServerConnection srcServer = player.getCurrentServer().orElse(null);
                if (srcServer == null) {
                    Text.send(plugin.messages().notConnected, src);
                    return 0;
                }

                if (srcServer.getServer().equals(server)) {
                    Text.send(plugin.messages().playersAlreadyInHub, src);
                    return 0;
                }

                targets.addAll(proxy.getAllPlayers()
                        .stream()
                        .filter(p -> {
                            ServerConnection playerServer = p.getCurrentServer().orElse(null);
                            return playerServer != null && playerServer.getServer().equals(srcServer);
                        })
                        .toList()
                );
            } else {
                Player target = proxy.getPlayer(targetName).orElse(null);

                if (target == null) {
                    Text.send(plugin.messages().playerNotFound, src);
                    return 0;
                }

                ServerConnection targetServer = target.getCurrentServer().orElse(null);
                if (targetServer == null || targetServer.getServer().equals(server)) {
                    Text.send(plugin.messages().playerAlreadyInHub, src);
                    return 0;
                }

                targets.add(target);
            }
        }

        if (targets.size() == 1) {
            Player target = targets.iterator().next();
            target.createConnectionRequest(server)
                    .connect()
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            Text.send(Messages.replace(plugin.messages().internalException, "{message}", throwable.getMessage()), src);
                            return;
                        }

                        if (result.isSuccessful() && !target.equals(src)) {
                            Text.send(Messages.replace(plugin.messages().hubSuccessTarget, "{player}", src), target);
                            Text.send(Messages.replace(plugin.messages().hubSuccessSource, "{player}", target), src);
                        } else {
                            Text.send(plugin.messages().hubFailure, src);
                        }
                    });
        } else {
            AtomicInteger successCount = new AtomicInteger();
            AtomicInteger failureCount = new AtomicInteger();

            List<CompletableFuture<ConnectionRequestBuilder.Result>> futures = targets.stream()
                    .map(target -> target.createConnectionRequest(server)
                            .connect()
                            .whenComplete((result, throwable) -> {
                                if (throwable != null) {
                                    Text.send(Messages.replace(plugin.messages().internalException, "{message}", throwable.getMessage()), src);
                                    failureCount.incrementAndGet();
                                    return;
                                }

                                if (result.isSuccessful() && !target.equals(src)) {
                                    Text.send(Messages.replace(plugin.messages().hubSuccessTarget, "{player}", src), target);
                                    successCount.incrementAndGet();
                                } else {
                                    Text.send(plugin.messages().hubFailure, src);
                                    failureCount.incrementAndGet();
                                }
                            }))
                    .toList();

            CompletableFuture<Void> allCompleted = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            //TODO To test if it works
            allCompleted.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    Text.send(Messages.replace(plugin.messages().internalException, "{message}", throwable.getMessage()), src);
                    return;
                }

                if (successCount.get() == 0) {
                    Text.send(plugin.messages().hubFailureMultiple, src);
                    return;
                }

                if (failureCount.get() == 0) {
                    Text.send(plugin.messages().hubSuccessMultipleSource, src);
                    return;
                }

                Text.send(Messages.replace(plugin.messages().hubPartialSuccess, new String[]{"{amount}", "{total}"}, new Object[]{successCount.get(), successCount.get() + failureCount.get()}), src);
            });
        }

        return 1;
    }
}
