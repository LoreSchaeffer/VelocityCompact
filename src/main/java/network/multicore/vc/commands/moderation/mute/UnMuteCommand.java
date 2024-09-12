package network.multicore.vc.commands.moderation.mute;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import network.multicore.vc.commands.AbstractCommand;
import network.multicore.vc.data.Mute;
import network.multicore.vc.data.User;
import network.multicore.vc.utils.ModerationUtils;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.Text;
import network.multicore.vc.utils.suggestions.CustomSuggestionProvider;
import network.multicore.vc.utils.suggestions.PlayerSuggestionProvider;
import network.multicore.vc.utils.suggestions.ServerSuggestionProvider;

import java.util.List;
import java.util.Optional;

public class UnMuteCommand extends AbstractCommand {
    private static final String PLAYER_ARG = "player";
    private static final String SERVER_ARG = "server";
    private static final String REASON_ARG = "reason";

    /**
     * /unmute <player> <server> [reason]
     */
    public UnMuteCommand() {
        super("unmute");
    }

    @Override
    public void register() {
        if (!config.getBoolean("modules.moderation", false)) return;

        LiteralArgumentBuilder<CommandSource> node = BrigadierCommand
                .literalArgumentBuilder(command)
                .requires(src -> src.hasPermission(Permission.UNMUTE.get()))
                .then(BrigadierCommand.requiredArgumentBuilder(PLAYER_ARG, StringArgumentType.word())
                        .suggests(new PlayerSuggestionProvider<>(proxy, PLAYER_ARG))
                        .then(BrigadierCommand.requiredArgumentBuilder(SERVER_ARG, StringArgumentType.word())
                                .suggests(new ServerSuggestionProvider<>(proxy, SERVER_ARG))
                                .executes((ctx) -> execute(ctx.getSource(),
                                        ctx.getArgument(PLAYER_ARG, String.class),
                                        ctx.getArgument(SERVER_ARG, String.class),
                                        null))
                                .then(BrigadierCommand.requiredArgumentBuilder(REASON_ARG, StringArgumentType.greedyString())
                                        .suggests(new CustomSuggestionProvider<>(REASON_ARG, config.getStringList("moderation.reason-suggestions.mute")))
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
            Text.send(messages.getAndReplace("common.internal-exception", "lines", "Staff user not found"), src);
            return COMMAND_FAILED;
        }

        List<Mute> activeMutes = plugin.muteRepository().findAllActiveByUsername(targetName);
        if (!activeMutes.isEmpty()) ModerationUtils.removeExpiredMutes(activeMutes);

        if (activeMutes.stream().noneMatch(m -> server.getServerInfo().getName().equals(m.getServer()))) {
            Text.send(messages.getAndReplace("commands.moderation.not-muted-server", "player", targetName), src);
            return COMMAND_FAILED;
        }

        Mute mute = activeMutes.stream()
                .filter(m -> server.getServerInfo().getName().equals(m.getServer()))
                .findFirst()
                .orElse(null);

        if (mute == null) {
            Text.send(messages.getAndReplace("commands.moderation.not-muted-server", "player", targetName), src);
            return COMMAND_FAILED;
        }

        mute.setUnmuteDate();
        mute.setUnmuteStaff(staff);
        mute.setUnmuteReason(reason);
        plugin.muteRepository().save(mute);

        Optional<Player> target = proxy.getPlayer(mute.getUniqueId());
        target.ifPresent(p -> Text.send(messages.getAndReplace("moderation.target-lines.unmute",
                "staff", console ? messages.get("console") : src,
                "server", server.getServerInfo().getName(),
                "reason", mute.getReason() != null ? mute.getReason() : messages.get("no-reason")
        ), p));

        ModerationUtils.broadcast(targetName, src, server, null, reason, silent, console, Permission.PUNISHMENT_RECEIVE_UNMUTE, "unmute");

        return COMMAND_SUCCESS;
    }
}
