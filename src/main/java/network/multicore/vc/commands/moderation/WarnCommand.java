package network.multicore.vc.commands.moderation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import network.multicore.vc.commands.AbstractCommand;
import network.multicore.vc.data.User;
import network.multicore.vc.data.Warn;
import network.multicore.vc.utils.ModerationUtils;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.Text;
import network.multicore.vc.utils.Utils;
import network.multicore.vc.utils.suggestions.CustomSuggestionProvider;
import network.multicore.vc.utils.suggestions.PlayerSuggestionProvider;

import java.util.stream.Collectors;

public class WarnCommand extends AbstractCommand {
    private static final String PLAYER_ARG = "player";
    private static final String REASON_ARG = "reason";

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

        if (Utils.isOnline(proxy, user.getUniqueId())) {
            Player target = proxy.getPlayer(user.getUniqueId()).get();

            Text.send(messages.getAndReplace("moderation.target-message.warn",
                    "staff", console ? messages.get("console") : src,
                    "reason", warn.getReason() != null ? warn.getReason() : messages.get("no-reason")
            ), target);
        }

        ModerationUtils.broadcast(targetName, src, null, null, reason, silent, console, Permission.PUNISHMENT_RECEIVE_WARN, "warn");

        return COMMAND_SUCCESS;
    }
}
