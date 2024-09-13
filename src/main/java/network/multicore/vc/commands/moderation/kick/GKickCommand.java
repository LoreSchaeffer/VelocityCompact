package network.multicore.vc.commands.moderation.kick;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import network.multicore.vc.commands.AbstractCommand;
import network.multicore.vc.data.Kick;
import network.multicore.vc.data.User;
import network.multicore.vc.utils.*;
import network.multicore.vc.utils.suggestions.CustomSuggestionProvider;
import network.multicore.vc.utils.suggestions.PlayerSuggestionProvider;

import java.util.Optional;
import java.util.stream.Collectors;

public class GKickCommand extends AbstractCommand {
    private static final String PLAYER_ARG = "player";
    private static final String REASON_ARG = "reason";

    /**
     * /gkick <player> [reason]
     */
    public GKickCommand() {
        super("gkick");
    }

    @Override
    public void register() {
        if (!config.getBoolean("modules.moderation", false)) return;

        LiteralArgumentBuilder<CommandSource> node = BrigadierCommand
                .literalArgumentBuilder(command)
                .requires(src -> src.hasPermission(Permission.GKICK.get()))
                .then(BrigadierCommand.requiredArgumentBuilder(PLAYER_ARG, StringArgumentType.word())
                        .suggests(new PlayerSuggestionProvider<>(proxy, PLAYER_ARG))
                        .then(BrigadierCommand.requiredArgumentBuilder(REASON_ARG, StringArgumentType.greedyString())
                                .suggests(new CustomSuggestionProvider<>(REASON_ARG, config.getStringList("moderation.reason-suggestions.kick")))
                                .executes((ctx) -> execute(ctx.getSource(),
                                        ctx.getArgument(PLAYER_ARG, String.class),
                                        ctx.getArgument(REASON_ARG, String.class)))
                                .build()));

        proxy.getCommandManager().register(buildMeta(), new BrigadierCommand(node.build()));
    }

    private int execute(CommandSource src, String targetName, String reason) {
        Optional<Player> targetOpt = proxy.getPlayer(targetName);
        if (targetOpt.isEmpty()) {
            Text.send(messages.get("commands.generic.player-not-found"), src);
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

        if (config.getStringList("moderation.bypass.kick").stream().map(String::toLowerCase).collect(Collectors.toSet()).contains(targetName)) {
            Text.send(messages.get("commands.moderation.kick-not-allowed"), src);
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

        Player target = targetOpt.get();

        Kick kick = new Kick(target, staff, reason, null);
        plugin.kickRepository().save(kick);

        if (Utils.isFallbackServer(target.getCurrentServer().map(ServerConnection::getServer).orElse(null))) {
            target.disconnect(Text.deserialize(messages.getAndReplace("moderation.disconnect.kick",
                    "player", targetName,
                    "staff", console ? messages.get("console") : src,
                    "server", messages.get("global"),
                    "duration", messages.get("permanent"),
                    "reason", kick.getReason() != null ? kick.getReason() : messages.get("no-reason")
            )));
        } else {
            FallbackConnection connection = new FallbackConnection(target);
            connection.connect().whenComplete((result, throwable) -> {
                if (throwable != null || !result.isSuccessful()) {
                    target.disconnect(Text.deserialize(messages.getAndReplace("moderation.disconnect.kick",
                            "player", targetName,
                            "staff", console ? messages.get("console") : src,
                            "server", messages.get("global"),
                            "duration", messages.get("permanent"),
                            "reason", kick.getReason() != null ? kick.getReason() : messages.get("no-reason")
                    )));
                } else {
                    Text.send(messages.getAndReplace("moderation.target-message.kick",
                            "staff", console ? messages.get("console") : src,
                            "server", messages.get("global"),
                            "duration", messages.get("permanent"),
                            "reason", kick.getReason() != null ? kick.getReason() : messages.get("no-reason")
                    ), target);
                }
            });
        }

        ModerationUtils.broadcast(targetName, src, null, null, reason, silent, console, Permission.PUNISHMENT_RECEIVE_KICK, "kick");

        return COMMAND_SUCCESS;
    }
}
