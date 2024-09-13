package network.multicore.vc.commands.moderation.ban;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import network.multicore.vc.commands.AbstractCommand;
import network.multicore.vc.data.Ban;
import network.multicore.vc.data.User;
import network.multicore.vc.utils.ModerationUtils;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.Text;
import network.multicore.vc.utils.suggestions.CustomSuggestionProvider;
import network.multicore.vc.utils.suggestions.PlayerSuggestionProvider;
import network.multicore.vc.utils.suggestions.ServerSuggestionProvider;

import java.util.List;
import java.util.Optional;

public class UnBanCommand extends AbstractCommand {
    private static final String PLAYER_ARG = "player";
    private static final String SERVER_ARG = "server";
    private static final String REASON_ARG = "reason";

    /**
     * /unban <player> <server> [reason]
     */
    public UnBanCommand() {
        super("unban");
    }

    @Override
    public void register() {
        if (!config.getBoolean("modules.moderation", false)) return;

        LiteralArgumentBuilder<CommandSource> node = BrigadierCommand
                .literalArgumentBuilder(command)
                .requires(src -> src.hasPermission(Permission.UNBAN.get()))
                .then(BrigadierCommand.requiredArgumentBuilder(PLAYER_ARG, StringArgumentType.word())
                        .suggests(new PlayerSuggestionProvider<>(proxy, PLAYER_ARG))
                        .then(BrigadierCommand.requiredArgumentBuilder(SERVER_ARG, StringArgumentType.word())
                                .suggests(new ServerSuggestionProvider<>(proxy, SERVER_ARG))
                                .executes((ctx) -> execute(ctx.getSource(),
                                        ctx.getArgument(PLAYER_ARG, String.class),
                                        ctx.getArgument(SERVER_ARG, String.class),
                                        null))
                                .then(BrigadierCommand.requiredArgumentBuilder(REASON_ARG, StringArgumentType.greedyString())
                                        .suggests(new CustomSuggestionProvider<>(REASON_ARG, config.getStringList("moderation.reason-suggestions.ban")))
                                        .executes((ctx) -> execute(ctx.getSource(),
                                                ctx.getArgument(PLAYER_ARG, String.class),
                                                ctx.getArgument(SERVER_ARG, String.class),
                                                ctx.getArgument(REASON_ARG, String.class)))
                                        .build())));

        proxy.getCommandManager().register(buildMeta(), new BrigadierCommand(node.build()));
    }

    private int execute(CommandSource src, String targetName, String serverName, String reason) {
        RegisteredServer server = proxy.getServer(serverName).orElse(null);
        if (server == null) {
            Text.send(messages.get("commands.generic.server-not-found"), src);
            return COMMAND_FAILED;
        }

        if ((reason == null || reason.isBlank()) && config.getBoolean("moderation.revoke-needs-reason", false)) {
            Text.send(messages.get("commands.moderation.reason-needed"), src);
            return COMMAND_FAILED;
        }

        boolean silent = reason != null && reason.contains("-s");
        boolean console = reason != null && reason.contains("-c");

        if (silent) reason = reason.replace("-s", "").trim();
        if (console) reason = reason.replace("-c", "").trim();

        if (src instanceof Player player && player.getUsername().equalsIgnoreCase(targetName)) {
            Text.send(messages.get("commands.moderation.revoke-yourself"), src);
            return COMMAND_FAILED;
        }

        User staff = src instanceof Player player ? plugin.userRepository().findById(player.getUniqueId()).orElse(null) : null;
        if (staff == null && src instanceof Player) {
            Text.send(messages.getAndReplace("common.internal-exception", "message", "Staff user not found"), src);
            return COMMAND_FAILED;
        }

        List<Ban> activeBans = plugin.banRepository().findAllActiveByUsername(targetName);
        if (!activeBans.isEmpty()) ModerationUtils.removeExpiredBans(activeBans);

        if (activeBans.stream().noneMatch(b -> server.getServerInfo().getName().equals(b.getServer()))) {
            Text.send(messages.getAndReplace("commands.moderation.not-banned-server", "player", targetName), src);
            return COMMAND_FAILED;
        }

        Ban ban = activeBans.stream()
                .filter(b -> server.getServerInfo().getName().equals(b.getServer()))
                .findFirst()
                .orElse(null);

        if (ban == null) {
            Text.send(messages.getAndReplace("commands.moderation.not-banned-server", "player", targetName), src);
            return COMMAND_FAILED;
        }

        ban.setUnbanDate();
        ban.setUnbanStaff(staff);
        ban.setUnbanReason(reason);
        plugin.banRepository().save(ban);

        Optional<Player> target = proxy.getPlayer(ban.getUniqueId());
        target.ifPresent(p -> Text.send(messages.getAndReplace("moderation.target-message.unban",
                "staff", console ? messages.get("console") : src,
                "server", server.getServerInfo().getName(),
                "reason", ban.getReason() != null ? ban.getReason() : messages.get("no-reason")
        ), p));

        ModerationUtils.broadcast(targetName, src, server, null, reason, silent, console, Permission.PUNISHMENT_RECEIVE_UNBAN, "unban");

        return COMMAND_SUCCESS;
    }
}
