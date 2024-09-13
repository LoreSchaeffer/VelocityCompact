package network.multicore.vc.commands.moderation.ban;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import network.multicore.vc.commands.AbstractCommand;
import network.multicore.vc.data.Ban;
import network.multicore.vc.data.User;
import network.multicore.vc.utils.ModerationUtils;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.Text;
import network.multicore.vc.utils.Utils;
import network.multicore.vc.utils.suggestions.CustomSuggestionProvider;
import network.multicore.vc.utils.suggestions.PlayerSuggestionProvider;

import java.util.List;
import java.util.Optional;

public class GUnBanIpCommand extends AbstractCommand {
    private static final String PLAYER_ARG = "player";
    private static final String REASON_ARG = "reason";

    /**
     * /gunbanip <player|ip> [reason]
     */
    public GUnBanIpCommand() {
        super("gunbanip");
    }

    @Override
    public void register() {
        if (!config.getBoolean("modules.moderation", false)) return;

        LiteralArgumentBuilder<CommandSource> node = BrigadierCommand
                .literalArgumentBuilder(command)
                .requires(src -> src.hasPermission(Permission.GUNBAN_IP.get()))
                .then(BrigadierCommand.requiredArgumentBuilder(PLAYER_ARG, StringArgumentType.word())
                        .suggests(new PlayerSuggestionProvider<>(proxy, PLAYER_ARG))
                        .executes((ctx) -> execute(ctx.getSource(),
                                ctx.getArgument(PLAYER_ARG, String.class),
                                null))
                        .then(BrigadierCommand.requiredArgumentBuilder(REASON_ARG, StringArgumentType.greedyString())
                                .suggests(new CustomSuggestionProvider<>(REASON_ARG, config.getStringList("moderation.reason-suggestions.ban")))
                                .executes((ctx) -> execute(ctx.getSource(),
                                        ctx.getArgument(PLAYER_ARG, String.class),
                                        ctx.getArgument(REASON_ARG, String.class)))
                                .build()));

        proxy.getCommandManager().register(buildMeta(), new BrigadierCommand(node.build()));
    }

    private int execute(CommandSource src, String targetNameIp, String reason) {
        if ((reason == null || reason.isBlank()) && config.getBoolean("moderation.revoke-needs-reason", false)) {
            Text.send(messages.get("commands.moderation.reason-needed"), src);
            return COMMAND_FAILED;
        }

        boolean silent = reason != null && reason.contains("-s");
        boolean console = reason != null && reason.contains("-c");

        if (silent) reason = reason.replace("-s", "").trim();
        if (console) reason = reason.replace("-c", "").trim();

        User staff = src instanceof Player player ? plugin.userRepository().findById(player.getUniqueId()).orElse(null) : null;
        if (staff == null && src instanceof Player) {
            Text.send(messages.getAndReplace("common.internal-exception", "message", "Staff user not found"), src);
            return COMMAND_FAILED;
        }

        String ip;
        if (Utils.isIpv4(targetNameIp)) {
            ip = targetNameIp;
        } else {
            ip = proxy.getPlayer(targetNameIp).map(p -> p.getRemoteAddress().getHostString()).orElse(null);

            if (ip == null) {
                List<Ban> activeBans = plugin.banRepository()
                        .findAllActiveByUsername(targetNameIp)
                        .stream()
                        .filter(b -> b.getServer() == null)
                        .toList();

                if (activeBans.isEmpty()) {
                    Text.send(messages.get("commands.moderation.not-banned-global"), src);
                    return COMMAND_FAILED;
                }

                Ban ban = activeBans.getFirst();
                ip = ban.getIp();
            }
        }

        List<Ban> activeBans = plugin.banRepository().findAllActiveByIp(ip);
        if (!activeBans.isEmpty()) ModerationUtils.removeExpiredBans(activeBans);

        if (activeBans.stream().noneMatch(b -> b.getServer() == null)) {
            Text.send(messages.getAndReplace("commands.moderation.not-banned-global", "player", targetNameIp), src);
            return COMMAND_FAILED;
        }

        Ban ban = activeBans.stream()
                .filter(b -> b.getServer() == null)
                .findFirst()
                .orElse(null);

        if (ban == null) {
            Text.send(messages.getAndReplace("commands.moderation.not-banned-server", "player", targetNameIp), src);
            return COMMAND_FAILED;
        }

        ban.setUnbanDate();
        ban.setUnbanStaff(staff);
        ban.setUnbanReason(reason);
        plugin.banRepository().save(ban);

        List<User> targets = plugin.userRepository().findAllByIp(ip);
        targets.forEach(target -> {
            Optional<Player> p = proxy.getPlayer(target.getUniqueId());
            p.ifPresent(player -> {
                Text.send(messages.getAndReplace("moderation.target-message.unban-ip",
                        "staff", console ? messages.get("console") : src,
                        "server", messages.get("global"),
                        "reason", ban.getReason() != null ? ban.getReason() : messages.get("no-reason")
                ), player);
            });
        });

        ModerationUtils.broadcast(targetNameIp, src, null, null, reason, silent, console, Permission.PUNISHMENT_RECEIVE_UNBAN, "unban-ip");

        return COMMAND_SUCCESS;
    }
}
