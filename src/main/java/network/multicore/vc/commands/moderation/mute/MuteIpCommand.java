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
import network.multicore.vc.utils.Utils;
import network.multicore.vc.utils.suggestions.CustomSuggestionProvider;
import network.multicore.vc.utils.suggestions.PlayerSuggestionProvider;
import network.multicore.vc.utils.suggestions.ServerSuggestionProvider;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MuteIpCommand extends AbstractCommand {
    private static final String PLAYER_ARG = "player";
    private static final String SERVER_ARG = "server";
    private static final String REASON_ARG = "reason";

    /**
     * /muteip <player|ip> <server> <duration> [reason]
     */
    public MuteIpCommand() {
        super("muteip");
    }

    @Override
    public void register() {
        if (!config.getBoolean("modules.moderation", false)) return;

        LiteralArgumentBuilder<CommandSource> node = BrigadierCommand
                .literalArgumentBuilder(command)
                .requires(src -> src.hasPermission(Permission.MUTE_IP.get()) && src.hasPermission(Permission.MUTE_PERMANENT.get()))
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
            Text.send(messages.getAndReplace("common.internal-exception", "message", "Staff user not found"), src);
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

        List<User> targets = plugin.userRepository().findAllByIp(ip);

        boolean bypass = targets.stream().anyMatch(u -> config.getStringList("moderation.bypass.mute")
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet())
                .contains(u.getUsername())
        );
        if (bypass) {
            Text.send(messages.get("commands.moderation.mute-ip-not-allowed"), src);
            return COMMAND_FAILED;
        }

        if (src instanceof Player player && targets.stream().anyMatch(u -> u.getUniqueId().equals(player.getUniqueId()))) {
            Text.send(messages.get("commands.moderation.punish-yourself"), src);
            return COMMAND_FAILED;
        }

        Mute mute = new Mute(ip, staff, reason, server.getServerInfo().getName(), null);
        plugin.muteRepository().save(mute);

        if (targets.isEmpty()) {
            List<Mute> activeMutes = plugin.muteRepository().findAllActiveByIp(ip);
            if (!activeMutes.isEmpty()) ModerationUtils.removeExpiredMutes(activeMutes);

            if (activeMutes.stream().anyMatch(b -> server.getServerInfo().getName().equals(b.getServer()))) {
                Text.send(messages.getAndReplace("commands.moderation.already-muted-server", "player", targetNameIp), src);
                return COMMAND_FAILED;
            }

            if (activeMutes.stream().anyMatch(b -> b.getServer() == null)) {
                Text.send(messages.get("commands.moderation.already-muted-global"), src);
                return COMMAND_FAILED;
            }

            ModerationUtils.broadcast(ip, src, server, null, reason, silent, console, Permission.PUNISHMENT_RECEIVE_MUTE, "mute-ip");
        } else {
            for (User target : targets) {
                List<Mute> activeMutes = plugin.muteRepository().findAllActiveByUsername(target.getUsername());
                if (!activeMutes.isEmpty()) ModerationUtils.removeExpiredMutes(activeMutes);

                if (activeMutes.stream().anyMatch(b -> server.getServerInfo().getName().equals(b.getServer()))) {
                    Text.send(messages.getAndReplace("commands.moderation.already-muted-server", "player", target.getUsername()), src);
                    continue;
                }

                if (activeMutes.stream().anyMatch(b -> b.getServer() == null)) {
                    Text.send(messages.get("commands.moderation.already-muted-global"), src);
                    continue;
                }

                Optional<Player> player = proxy.getPlayer(target.getUniqueId());
                player.ifPresent(p -> Text.send(messages.getAndReplace("moderation.target-message.mute-ip",
                        "staff", console ? messages.get("console") : src,
                        "server", server.getServerInfo().getName(),
                        "duration", messages.get("permanent"),
                        "reason", mute.getReason() != null ? mute.getReason() : messages.get("no-reason")
                ), p));

                ModerationUtils.broadcast(target.getUsername(), src, server, null, reason, silent, console, Permission.PUNISHMENT_RECEIVE_MUTE, "mute-ip");
            }
        }

        return COMMAND_SUCCESS;
    }
}
