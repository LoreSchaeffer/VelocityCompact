package network.multicore.vc.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import network.multicore.vc.utils.FallbackConnection;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.Text;
import network.multicore.vc.utils.suggestions.PlayerSuggestionProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class HubCommand extends AbstractCommand {
    private static final String PLAYER_ARG = "player";

    /**
     * /hub [player]
     */
    public HubCommand() {
        super("hub");
    }

    public void register() {
        if (!config.getBoolean("modules.hub", false)) return;

        LiteralArgumentBuilder<CommandSource> rootNode = BrigadierCommand
                .literalArgumentBuilder(command)
                .requires(src -> src.hasPermission(Permission.HUB.get()))
                .executes(ctx -> execute(ctx.getSource(), null));

        RequiredArgumentBuilder<CommandSource, String> playerNode = BrigadierCommand
                .requiredArgumentBuilder(PLAYER_ARG, StringArgumentType.word())
                .requires(src -> src.hasPermission(Permission.HUB_OTHER.get()))
                .suggests(new PlayerSuggestionProvider<>(proxy, PLAYER_ARG, "all", "current"))
                .executes(ctx -> execute(ctx.getSource(), ctx.getArgument(PLAYER_ARG, String.class)));

        rootNode.then(playerNode);

        proxy.getCommandManager().register(buildMeta(), new BrigadierCommand(rootNode.build()));
    }

    private int execute(CommandSource src, String targetName) {
        String hubName = plugin.config().getString("hub");
        if (hubName == null || hubName.isEmpty()) {
            Text.send(messages.get("commands.hub.invalid-hub"), src);
            return COMMAND_FAILED;
        }

        RegisteredServer hub = proxy.getServer(hubName).orElse(null);
        if (hub == null) {
            Text.send(messages.get("commands.hub.invalid-hub"), src);
            return COMMAND_FAILED;
        }

        Set<Player> targets = new HashSet<>();

        if (targetName == null) {
            if (!(src instanceof Player player)) {
                Text.send(messages.get("commands.generic.not-player"), src);
                return COMMAND_FAILED;
            }

            ServerConnection targetServer = player.getCurrentServer().orElse(null);
            if (targetServer == null) {
                Text.send(messages.get("commands.hub.not-sent-to-hub-player"), src);
                return COMMAND_FAILED;
            } else if (targetServer.getServer().equals(hub)) {
                Text.send(messages.get("commands.hub.already-connected-player"), src);
                return COMMAND_FAILED;
            }

            targets.add(player);
        } else {
            if (targetName.equals("all")) {
                targets.addAll(proxy.getAllPlayers()
                        .stream()
                        .filter(p -> {
                            ServerConnection playerServer = p.getCurrentServer().orElse(null);
                            return playerServer != null && !playerServer.getServer().equals(hub);
                        })
                        .toList()
                );

                if (targets.isEmpty()) {
                    Text.send(messages.get("commands.hub.no-player-to-send"), src);
                    return COMMAND_FAILED;
                }
            } else if (targetName.equals("current")) {
                if (!(src instanceof Player player)) {
                    Text.send(messages.get("commands.generic.not-player"), src);
                    return COMMAND_FAILED;
                }

                ServerConnection srcServer = player.getCurrentServer().orElse(null);
                if (srcServer == null) {
                    Text.send(messages.get("commands.generic.not-connected-self"), src);
                    return COMMAND_FAILED;
                } else if (srcServer.getServer().equals(hub)) {
                    Text.send(messages.get("commands.hub.already-connected-players"), src);
                    return COMMAND_FAILED;
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
                    Text.send(messages.get("commands.generic.player-not-found"), src);
                    return COMMAND_FAILED;
                }

                ServerConnection targetServer = target.getCurrentServer().orElse(null);
                if (targetServer == null) {
                    Text.send(messages.get("commands.hub.not-sent-to-hub-player"), src);
                    return COMMAND_FAILED;
                } else if (targetServer.getServer().equals(hub)) {
                    Text.send(messages.get("commands.hub.already-connected-player"), src);
                    return COMMAND_FAILED;
                }

                targets.add(target);
            }
        }

        if (targets.size() == 1) {
            Player target = targets.iterator().next();
            target.createConnectionRequest(hub)
                    .connect()
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            Text.send(messages.getAndReplace("common.internal-exception", "message", throwable.getMessage()), src);
                            return;
                        }

                        if (result.isSuccessful() && !target.equals(src)) {
                            Text.send(messages.getAndReplace("commands.hub.sent-to-hub-target", "player", src), target);
                            Text.send(messages.getAndReplace("commands.hub.sent-to-hub-player", "player", target), src);
                        } else {
                            Text.send(messages.get("commands.hub.not-sent-to-hub-player"), src);
                        }
                    });
        } else {
            AtomicInteger successCount = new AtomicInteger();
            AtomicInteger failureCount = new AtomicInteger();

            List<CompletableFuture<ConnectionRequestBuilder.Result>> futures = targets.stream()
                    .map(target -> {
                        FallbackConnection connection = new FallbackConnection(target);
                        return connection.connect().whenComplete((result, throwable) -> {
                            if (throwable != null || !result.isSuccessful()) failureCount.incrementAndGet();

                            if (!target.equals(src)) {
                                successCount.incrementAndGet();
                                Text.send(messages.getAndReplace("commands.hub.sent-to-hub-target", "player", src), target);
                            }
                        });
                    })
                    .toList();

            CompletableFuture<Void> allCompleted = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            //TODO To be tested
            allCompleted.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    Text.send(messages.getAndReplace("common.internal-exception", "message", throwable.getMessage()), src);
                    return;
                }

                if (successCount.get() == 0) {
                    Text.send(messages.get("commands.hub.not-sent-to-hub-players"), src);
                    return;
                }

                if (failureCount.get() == 0) {
                    Text.send(messages.getAndReplace("commands.hub-sent-to-hub-players", "amount", successCount.get()), src);
                    return;
                }

                Text.send(messages.getAndReplace("commands.hub.sent-to-hub-partial-players", "amount", successCount.get(), "total", successCount.get() + failureCount.get()), src);
            });
        }

        return COMMAND_SUCCESS;
    }
}
