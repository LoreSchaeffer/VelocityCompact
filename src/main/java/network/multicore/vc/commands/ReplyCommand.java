package network.multicore.vc.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.Text;

public class ReplyCommand extends AbstractCommand {
    private static final String MESSAGE_ARG = "message";

    /**
     * /message <message>
     */
    public ReplyCommand() {
        super("reply");
    }

    @Override
    public void register() {
        if (!config.getBoolean("modules.messages", false)) return;

        LiteralArgumentBuilder<CommandSource> replyRootNode = BrigadierCommand
                .literalArgumentBuilder(command)
                .requires(ctx -> ctx.hasPermission(Permission.MESSAGE.get()))
                .then(BrigadierCommand.requiredArgumentBuilder(MESSAGE_ARG, StringArgumentType.greedyString())
                        .executes((ctx) -> execute(ctx.getSource(), ctx.getArgument(MESSAGE_ARG, String.class)))
                        .build());

        proxy.getCommandManager().register(buildMeta(), new BrigadierCommand(replyRootNode.build()));
    }

    private int execute(CommandSource src, String message) {
        if (!(src instanceof Player sender)) {
            Text.send(messages.get("commands.generic.not-player"), src);
            return COMMAND_FAILED;
        }

        // TODO Check for active mutes if set so in the config

        Player receiver = plugin.getMessenger(sender).orElse(null);
        if (receiver == null) {
            Text.send(messages.get("commands.message.no-receiver"), sender);
            return COMMAND_FAILED;
        }

        if (!receiver.isActive()) {
            Text.send(messages.get("commands.message.receiver-not-online"), sender);
            return COMMAND_FAILED;
        }

        String senderServer = sender.getCurrentServer().map(server -> server.getServerInfo().getName()).orElse(messages.get("unknown"));
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
        MessageCommand.sendSocialspy(senderServer, sender, receiverServer, receiver, message);

        plugin.setMessenger(sender, receiver);

        return COMMAND_SUCCESS;
    }

}
