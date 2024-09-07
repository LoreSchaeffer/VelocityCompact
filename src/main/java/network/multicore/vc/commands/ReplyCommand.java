package network.multicore.vc.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import network.multicore.vc.data.Mute;
import network.multicore.vc.data.MuteRepository;
import network.multicore.vc.utils.*;

import java.util.List;

public class ReplyCommand extends AbstractCommand {
    private static final String MESSAGE_ARG = "message";
    private final CensureUtils censureUtils;
    private final boolean mutePreventPrivateMessages;
    private final MuteRepository muteRepository;

    /**
     * /message <message>
     */
    public ReplyCommand() {
        super("reply");
        this.censureUtils = CensureUtils.get();
        this.mutePreventPrivateMessages = config.getBoolean("moderation.mute-prevents-private-messages", false);
        this.muteRepository = plugin.muteRepository();
    }

    @Override
    public void register() {
        if (!config.getBoolean("modules.messages", false)) return;

        LiteralArgumentBuilder<CommandSource> node = BrigadierCommand
                .literalArgumentBuilder(command)
                .requires(ctx -> ctx.hasPermission(Permission.MESSAGE.get()))
                .then(BrigadierCommand.requiredArgumentBuilder(MESSAGE_ARG, StringArgumentType.greedyString())
                        .executes((ctx) -> execute(ctx.getSource(), ctx.getArgument(MESSAGE_ARG, String.class)))
                        .build());

        proxy.getCommandManager().register(buildMeta(), new BrigadierCommand(node.build()));
    }

    private int execute(CommandSource src, String message) {
        if (!(src instanceof Player sender)) {
            Text.send(messages.get("commands.generic.not-player"), src);
            return COMMAND_FAILED;
        }

        String senderServer = sender.getCurrentServer().map(server -> server.getServerInfo().getName()).orElse(messages.get("unknown"));

        if (mutePreventPrivateMessages) {
            List<Mute> activeMutes = muteRepository.findAllActiveByUuid(sender.getUniqueId());
            activeMutes.addAll(muteRepository.findAllActiveByIp(sender.getRemoteAddress().getAddress().getHostAddress()));
            activeMutes.removeIf(mute -> mute.getServer() != null && !mute.getServer().equalsIgnoreCase(senderServer));

            //TODO To be checked
            if (!activeMutes.isEmpty()) {
                Mute mute = null;
                boolean isMuted = false;

                for (Mute m : activeMutes) {
                    if (!ModerationUtils.isExpired(m.getEndDate())) {
                        isMuted = true;

                        if (mute == null) mute = m;
                        else if (mute.getServer() == null) mute = m;
                    } else {
                        m.setUnmuteDate();
                        muteRepository.save(m);
                    }
                }

                if (isMuted) {
                    Text.send(messages.getAndReplace("moderation.mute.reminder",
                            "staff", mute.getStaff() != null ? mute.getStaff().getUsername() : messages.get("console"),
                            "server", mute.getServer() != null ? mute.getServer() : messages.get("global"),
                            "duration", mute.getEndDate() != null ? ModerationUtils.getDurationString(mute.getEndDate()) : messages.get("permanent"),
                            "reason", mute.getReason() != null ? mute.getReason() : messages.get("no-reason")
                    ), sender);
                    Text.broadcast(messages.getAndReplace("moderation.mute.muted-message-broadcast",
                            "server", senderServer,
                            "player", sender.getUsername(),
                            "message", message
                    ), Permission.MUTED_MESSAGE_RECEIVE.get());
                    return COMMAND_SUCCESS;
                }
            }
        }

        Player receiver = Cache.get().getMessenger(sender).orElse(null);
        if (receiver == null) {
            Text.send(messages.get("commands.message.no-receiver"), sender);
            return COMMAND_FAILED;
        }

        if (!receiver.isActive()) {
            Text.send(messages.get("commands.message.receiver-not-online"), sender);
            return COMMAND_FAILED;
        }

        if (censureUtils.isChatCensorshipEnabled()) {
            CensureUtils.CensureResult result = censureUtils.censure(sender, message);

            if (result.isCensored()) {
                if (result.shouldCancelMessage()) return COMMAND_SUCCESS;
                message = result.getMessage();
            }
        }

        String receiverServer = receiver.getCurrentServer().map(server -> server.getServerInfo().getName()).orElse(messages.get("unknown"));

        String formattedMessage = messages.getAndReplace("commands.message.message-format",
                "sender_server", senderServer,
                "sender", sender.getUsername(),
                "receiver_server", receiverServer,
                "receiver", receiver.getUsername(),
                "message", message
        );

        Text.send(formattedMessage, sender);
        Text.send(formattedMessage, receiver);
        if (config.getBoolean("modules.socialspy", false)) MessageCommand.sendSocialspy(senderServer, sender, receiverServer, receiver, message);

        Cache.get().setMessenger(sender, receiver);

        return COMMAND_SUCCESS;
    }

}
