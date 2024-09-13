package network.multicore.vc.commands.moderation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.ServerInfo;
import network.multicore.vc.commands.AbstractCommand;
import network.multicore.vc.data.User;
import network.multicore.vc.data.Warn;
import network.multicore.vc.utils.ModerationUtils;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.Text;
import network.multicore.vc.utils.suggestions.CustomSuggestionProvider;
import network.multicore.vc.utils.suggestions.PlayerSuggestionProvider;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WarnCommand extends AbstractCommand {
    private static final String PLAYER_ARG = "player";
    private static final String REASON_ARG = "reason";
    private static final List<String> PUNISHMENTS = List.of("ban", "kick", "mute");

    /**
     * /warn <player> <reason>
     */
    public WarnCommand() {
        super("warn");
    }

    @Override
    public void register() {
        if (!config.getBoolean("modules.moderation", false)) return;

        LiteralArgumentBuilder<CommandSource> node = BrigadierCommand
                .literalArgumentBuilder(command)
                .requires(src -> src.hasPermission(Permission.WARN.get()))
                .then(BrigadierCommand.requiredArgumentBuilder(PLAYER_ARG, StringArgumentType.word())
                        .suggests(new PlayerSuggestionProvider<>(proxy, PLAYER_ARG))
                        .then(BrigadierCommand.requiredArgumentBuilder(REASON_ARG, StringArgumentType.greedyString())
                                .suggests(new CustomSuggestionProvider<>(REASON_ARG, config.getStringList("moderation.reason-suggestions.warn")))
                                .executes((ctx) -> execute(ctx.getSource(),
                                        ctx.getArgument(PLAYER_ARG, String.class),
                                        ctx.getArgument(REASON_ARG, String.class)))
                                .build()));

        proxy.getCommandManager().register(buildMeta(), new BrigadierCommand(node.build()));
    }

    private int execute(CommandSource src, String targetName, String reason) {
        boolean silent = reason != null && reason.contains("-s");
        boolean console = reason != null && reason.contains("-c");

        if (silent) reason = reason.replace("-s", "").trim();
        if (console) reason = reason.replace("-c", "").trim();

        if (config.getStringList("moderation.bypass.warn").stream().map(String::toLowerCase).collect(Collectors.toSet()).contains(targetName)) {
            Text.send(messages.get("commands.moderation.warn-not-allowed"), src);
            return COMMAND_FAILED;
        }

        if (src instanceof Player player && player.getUsername().equalsIgnoreCase(targetName)) {
            Text.send(messages.get("commands.moderation.punish-yourself"), src);
            return COMMAND_FAILED;
        }

        User staff = src instanceof Player player ? plugin.userRepository().findById(player.getUniqueId()).orElse(null) : null;
        if (staff == null && src instanceof Player) {
            Text.send(messages.getAndReplace("common.internal-exception", "message", "Staff user not found"), src);
            return COMMAND_FAILED;
        }

        User user = plugin.userRepository().findByUsername(targetName).orElse(null);
        if (user == null) {
            Text.send(messages.get("commands.moderation.player-not-found"), src);
            return COMMAND_FAILED;
        }

        Warn warn = new Warn(user, staff, reason);
        plugin.warnRepository().save(warn);

        Optional<Player> playerOpt = proxy.getPlayer(user.getUniqueId());
        playerOpt.ifPresent(player -> Text.send(messages.getAndReplace("moderation.target-message.warn",
                "staff", console ? messages.get("console") : src,
                "reason", warn.getReason() != null ? warn.getReason() : messages.get("no-reason")
        ), player));

        ModerationUtils.broadcast(targetName, src, null, null, reason, silent, console, Permission.PUNISHMENT_RECEIVE_WARN, "warn");

        if (config.getBoolean("moderation.warning.punishment-enabled", false)) {
            int warnAmountToPunish = config.getInt("moderation.warning.punishment-threshold", 3);
            Date firstDate;
            try {
                firstDate = ModerationUtils.parseDurationString(config.getString("moderation.warning.time", ""), true);
            } catch (IllegalArgumentException e) {
                plugin.logger().error("Invalid duration string for warning punishment time: {}", config.getString("moderation.warning.time", ""));
                return COMMAND_SUCCESS;
            }

            List<Warn> warns = plugin.warnRepository()
                    .findAllByUuid(user.getUniqueId())
                    .stream()
                    .filter(w -> w.getDate().equals(firstDate) || w.getDate().after(firstDate))
                    .toList();

            if (warns.size() >= warnAmountToPunish) {
                String punishment = config.getString("moderation.warning.punishment-type", "");
                if (!PUNISHMENTS.contains(punishment.toLowerCase())) {
                    plugin.logger().error("Invalid punishment type for warning punishment: {}", punishment);
                    return COMMAND_SUCCESS;
                }

                String durationString = config.getString("moderation.warning.punishment-duration", "");
                Date duration;

                if (durationString.isBlank()) {
                    duration = null;
                } else {
                    try {
                        duration = ModerationUtils.parseDurationString(durationString, false);
                    } catch (IllegalArgumentException e) {
                        plugin.logger().error("Invalid duration string for warning punishment duration: {}", durationString);
                        return COMMAND_SUCCESS;
                    }
                }

                boolean isGlobal = config.getBoolean("moderation.warning.punishment-global", true);
                boolean isSilent = config.getBoolean("moderation.warning.punishment-silent", false);
                String punishmentReason = messages.getAndReplace("commands.moderation.warn-auto-punish", "amount", warnAmountToPunish);
                Player target = proxy.getPlayer(user.getUniqueId()).orElse(null);
                ServerInfo targetServer = target != null ? target.getCurrentServer().map(ServerConnection::getServerInfo).orElse(null) : null;

                if (targetServer == null && !isGlobal) {
                    plugin.logger().error("Player {} cannot be punished for reaching {} warns. Target server is null and the punishment is not global", targetName, warnAmountToPunish);
                    return COMMAND_SUCCESS;
                }

                //                                                                                 [g][temp]<punish> <player>[ server][ duration][ silent][ reason]
                proxy.getCommandManager().executeImmediatelyAsync(proxy.getConsoleCommandSource(), String.format("%s%s%s %s%s%s%s%s",
                        isGlobal ? "g" : "",
                        duration == null ? "" : "temp",
                        punishment,
                        targetName,
                        !isGlobal ? " " + targetServer.getName() : "",
                        duration != null ? " " + durationString : "",
                        isSilent ? " -s" : "",
                        punishmentReason != null && !punishmentReason.isBlank() ? " " + punishmentReason : ""
                ));
            }
        }

        return COMMAND_SUCCESS;
    }
}
