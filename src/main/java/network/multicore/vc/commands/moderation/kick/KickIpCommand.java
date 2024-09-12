package network.multicore.vc.commands.moderation.kick;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import network.multicore.vc.commands.AbstractCommand;
import network.multicore.vc.data.Kick;
import network.multicore.vc.data.User;
import network.multicore.vc.utils.*;
import network.multicore.vc.utils.suggestions.CustomSuggestionProvider;
import network.multicore.vc.utils.suggestions.PlayerSuggestionProvider;
import network.multicore.vc.utils.suggestions.ServerSuggestionProvider;

import java.util.List;
import java.util.stream.Collectors;

public class KickIpCommand extends AbstractCommand {
    private static final String PLAYER_ARG = "player";
    private static final String SERVER_ARG = "server";
    private static final String REASON_ARG = "reason";

    /**
     * /kickip <player|ip> <server> [reason]
     */
    public KickIpCommand() {
        super("kick");
    }

    @Override
    public void register() {
        if (!config.getBoolean("modules.moderation", false)) return;

        LiteralArgumentBuilder<CommandSource> node = BrigadierCommand
                .literalArgumentBuilder(command)
                .requires(src -> src.hasPermission(Permission.KICK.get()))
                .then(BrigadierCommand.requiredArgumentBuilder(PLAYER_ARG, StringArgumentType.word())
                        .suggests(new PlayerSuggestionProvider<>(proxy, PLAYER_ARG))
                        .then(BrigadierCommand.requiredArgumentBuilder(SERVER_ARG, StringArgumentType.word())
                                .suggests(new ServerSuggestionProvider<>(proxy, SERVER_ARG))
                                .executes((ctx) -> execute(ctx.getSource(),
                                        ctx.getArgument(PLAYER_ARG, String.class),
                                        ctx.getArgument(SERVER_ARG, String.class),
                                        null))
                                .then(BrigadierCommand.requiredArgumentBuilder(REASON_ARG, StringArgumentType.greedyString())
                                        .suggests(new CustomSuggestionProvider<>(REASON_ARG, config.getStringList("moderation.reason-suggestions.kick")))
                                        .executes((ctx) -> execute(ctx.getSource(),
                                                ctx.getArgument(PLAYER_ARG, String.class),
                                                ctx.getArgument(SERVER_ARG, String.class),
                                                ctx.getArgument(REASON_ARG, String.class)))
                                        .build())));

        proxy.getCommandManager().register(buildMeta(), new BrigadierCommand(node.build()));
    }

    private int execute(CommandSource src, String targetNameIp, String serverName, String reason) {
        RegisteredServer server = proxy.getServer(serverName).orElse(null);
        if (server == null) {
            Text.send(messages.get("commands.generic.server-not-found"), src);
            return COMMAND_FAILED;
        }

        if ((reason == null || reason.isBlank()) && config.getBoolean("moderation.punishment-needs-reason", false)) {
            Text.send(messages.get("commands.moderation.reason-needed"), src);
            return COMMAND_FAILED;
        }

        boolean silent = reason != null && reason.contains("-s");
        boolean console = reason != null && reason.contains("-c");

        if (silent) reason = reason.replace("-s", "").trim();
        if (console) reason = reason.replace("-c", "").trim();

        User staff = src instanceof Player player ? plugin.userRepository().findById(player.getUniqueId()).orElse(null) : null;
        if (staff == null && src instanceof Player) {
            Text.send(messages.getAndReplace("common.internal-exception", "lines", "Staff user not found"), src);
            return COMMAND_FAILED;
        }

        String ip;
        if (Utils.isIpv4(targetNameIp)) {
            ip = targetNameIp;
        } else {
            ip = proxy.getPlayer(targetNameIp).map(p -> p.getRemoteAddress().getHostString()).orElse(null);

            if (ip == null) {
                User user = plugin.userRepository().findByUsername(targetNameIp).orElse(null);
                if (user == null) {
                    Text.send(messages.get("commands.generic.player-not-found"), src);
                    return COMMAND_FAILED;
                } else {
                    ip = user.getIp();
                }
            }
        }

        String finalIp = ip;
        List<Player> targets = server.getPlayersConnected()
                .stream()
                .filter(p -> p.getRemoteAddress().getHostString().equals(finalIp))
                .toList();

        boolean bypass = targets.stream().anyMatch(p -> config.getStringList("moderation.bypass.kick")
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet())
                .contains(p.getUsername())
        );
        if (bypass) {
            Text.send(messages.get("commands.moderation.kick-not-allowed"), src);
            return COMMAND_FAILED;
        }

        if (src instanceof Player player && targets.stream().anyMatch(p -> p.getUniqueId().equals(player.getUniqueId()))) {
            Text.send(messages.get("commands.moderation.punish-yourself"), src);
            return COMMAND_FAILED;
        }

        if (targets.isEmpty()) {
            Text.send(messages.get("commands.moderation.none-to-be-kicked"), src);
            return COMMAND_FAILED;
        }

        for (Player target : targets) {
            Kick kick = new Kick(target, ip, staff, reason, server.getServerInfo().getName());
            plugin.kickRepository().save(kick);

            if (Utils.isFallbackServer(target.getCurrentServer().map(ServerConnection::getServer).orElse(null))) {
                target.disconnect(Text.deserialize(messages.getAndReplace("moderation.disconnect.kick",
                        "player", targetNameIp,
                        "staff", console ? messages.get("console") : src,
                        "server", server.getServerInfo().getName(),
                        "duration", messages.get("permanent"),
                        "reason", kick.getReason() != null ? kick.getReason() : messages.get("no-reason")
                )));
            } else {
                FallbackConnection connection = new FallbackConnection(target);
                connection.connect().whenComplete((result, throwable) -> {
                    if (throwable != null || !result.isSuccessful()) {
                        target.disconnect(Text.deserialize(messages.getAndReplace("moderation.disconnect.kick",
                                "player", targetNameIp,
                                "staff", console ? messages.get("console") : src,
                                "server", server.getServerInfo().getName(),
                                "duration", messages.get("permanent"),
                                "reason", kick.getReason() != null ? kick.getReason() : messages.get("no-reason")
                        )));
                    } else {
                        Text.send(messages.getAndReplace("moderation.target-lines.kick",
                                "staff", console ? messages.get("console") : src,
                                "server", server.getServerInfo().getName(),
                                "duration", messages.get("permanent"),
                                "reason", kick.getReason() != null ? kick.getReason() : messages.get("no-reason")
                        ), target);
                    }
                });
            }

            ModerationUtils.broadcast(targetNameIp, src, server, null, reason, silent, console, Permission.PUNISHMENT_RECEIVE_KICK, "kick-ip");
        }

        return COMMAND_SUCCESS;
    }
}
