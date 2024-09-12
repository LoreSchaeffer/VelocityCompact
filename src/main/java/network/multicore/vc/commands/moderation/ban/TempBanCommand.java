package network.multicore.vc.commands.moderation.ban;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import network.multicore.vc.commands.AbstractCommand;
import network.multicore.vc.data.Ban;
import network.multicore.vc.data.User;
import network.multicore.vc.utils.*;
import network.multicore.vc.utils.suggestions.CustomSuggestionProvider;
import network.multicore.vc.utils.suggestions.DurationSuggestionProvider;
import network.multicore.vc.utils.suggestions.PlayerSuggestionProvider;
import network.multicore.vc.utils.suggestions.ServerSuggestionProvider;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TempBanCommand extends AbstractCommand {
    private static final String PLAYER_ARG = "player";
    private static final String SERVER_ARG = "server";
    private static final String DURATION_ARG = "duration";
    private static final String REASON_ARG = "reason";

    /**
     * /tempban <player> <server> <duration> [reason]
     */
    public TempBanCommand() {
        super("tempban");
    }

    @Override
    public void register() {
        if (!config.getBoolean("modules.moderation", false)) return;

        LiteralArgumentBuilder<CommandSource> node = BrigadierCommand
                .literalArgumentBuilder(command)
                .requires(src -> src.hasPermission(Permission.BAN.get()))
                .then(BrigadierCommand.requiredArgumentBuilder(PLAYER_ARG, StringArgumentType.word())
                        .suggests(new PlayerSuggestionProvider<>(proxy, PLAYER_ARG))
                        .then(BrigadierCommand.requiredArgumentBuilder(SERVER_ARG, StringArgumentType.word())
                                .suggests(new ServerSuggestionProvider<>(proxy, SERVER_ARG))
                                .then(BrigadierCommand.requiredArgumentBuilder(DURATION_ARG, StringArgumentType.word())
                                        .suggests(new DurationSuggestionProvider<>(DURATION_ARG))
                                        .executes((ctx) -> execute(ctx.getSource(),
                                                ctx.getArgument(PLAYER_ARG, String.class),
                                                ctx.getArgument(SERVER_ARG, String.class),
                                                ctx.getArgument(DURATION_ARG, String.class),
                                                null))
                                        .then(BrigadierCommand.requiredArgumentBuilder(REASON_ARG, StringArgumentType.greedyString())
                                                .suggests(new CustomSuggestionProvider<>(REASON_ARG, config.getStringList("moderation.reason-suggestions.ban")))
                                                .executes((ctx) -> execute(ctx.getSource(),
                                                        ctx.getArgument(PLAYER_ARG, String.class),
                                                        ctx.getArgument(SERVER_ARG, String.class),
                                                        ctx.getArgument(DURATION_ARG, String.class),
                                                        ctx.getArgument(REASON_ARG, String.class)))
                                                .build()))));

        proxy.getCommandManager().register(buildMeta(), new BrigadierCommand(node.build()));
    }

    private int execute(CommandSource src, String targetName, String serverName, String duration, String reason) {
        RegisteredServer server = proxy.getServer(serverName).orElse(null);
        if (server == null) {
            Text.send(messages.get("commands.generic.server-not-found"), src);
            return COMMAND_FAILED;
        }

        Date end;
        try {
            end = ModerationUtils.parseDurationString(duration);
        } catch (IllegalArgumentException e) {
            Text.send(messages.get("commands.generic.invalid-duration"), src);
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

        if (config.getStringList("moderation.bypass.ban").stream().map(String::toLowerCase).collect(Collectors.toSet()).contains(targetName)) {
            Text.send(messages.get("commands.moderation.ban-not-allowed"), src);
            return COMMAND_FAILED;
        }

        if (src instanceof Player player && player.getUsername().equalsIgnoreCase(targetName)) {
            Text.send(messages.get("commands.moderation.punish-yourself"), src);
            return COMMAND_FAILED;
        }

        User staff = src instanceof Player player ? plugin.userRepository().findById(player.getUniqueId()).orElse(null) : null;
        if (staff == null && src instanceof Player) {
            Text.send(messages.getAndReplace("common.internal-exception", "lines", "Staff user not found"), src);
            return COMMAND_FAILED;
        }

        List<Ban> activeBans = plugin.banRepository().findAllActiveByUsername(targetName);
        if (!activeBans.isEmpty()) ModerationUtils.removeExpiredBans(activeBans);

        if (activeBans.stream().anyMatch(b -> server.getServerInfo().getName().equals(b.getServer()))) {
            Text.send(messages.getAndReplace("commands.moderation.already-banned-server", "player", targetName), src);
            return COMMAND_FAILED;
        }

        if (activeBans.stream().anyMatch(b -> b.getServer() == null)) {
            Text.send(messages.get("commands.moderation.already-banned-global"), src);
            return COMMAND_FAILED;
        }

        User user = plugin.userRepository().findByUsername(targetName).orElse(null);
        if (user == null) {
            Ban ban = new Ban(null, targetName, null, staff, reason, server.getServerInfo().getName(), end);
            plugin.banRepository().save(ban);
        } else {
            Ban ban = new Ban(user, staff, reason, server.getServerInfo().getName(), end);
            plugin.banRepository().save(ban);

            if (Utils.isOnline(server, user.getUniqueId())) {
                Player target = proxy.getPlayer(user.getUniqueId()).get();

                if (Utils.isFallbackServer(target.getCurrentServer().map(ServerConnection::getServer).orElse(null))) {
                    target.disconnect(Text.deserialize(messages.getAndReplace("moderation.disconnect.ban",
                            "player", targetName,
                            "staff", console ? messages.get("console") : src,
                            "server", server.getServerInfo().getName(),
                            "duration", ModerationUtils.getDurationString(end),
                            "reason", ban.getReason() != null ? ban.getReason() : messages.get("no-reason")
                    )));
                } else {
                    FallbackConnection connection = new FallbackConnection(target);
                    connection.connect().whenComplete((result, throwable) -> {
                        if (throwable != null || !result.isSuccessful()) {
                            target.disconnect(Text.deserialize(messages.getAndReplace("moderation.disconnect.ban",
                                    "player", targetName,
                                    "staff", console ? messages.get("console") : src,
                                    "server", server.getServerInfo().getName(),
                                    "duration", ModerationUtils.getDurationString(end),
                                    "reason", ban.getReason() != null ? ban.getReason() : messages.get("no-reason")
                            )));
                        } else {
                            Text.send(messages.getAndReplace("moderation.target-lines.ban",
                                    "staff", console ? messages.get("console") : src,
                                    "server", server.getServerInfo().getName(),
                                    "duration", ModerationUtils.getDurationString(end),
                                    "reason", ban.getReason() != null ? ban.getReason() : messages.get("no-reason")
                            ), target);
                        }
                    });
                }
            }
        }

        ModerationUtils.broadcast(targetName, src, server, end, reason, silent, console, Permission.PUNISHMENT_RECEIVE_BAN, "ban");

        return COMMAND_SUCCESS;
    }
}
