package network.multicore.vc.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import network.multicore.vc.VelocityCompact;
import network.multicore.vc.utils.Messages;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.Text;

import java.util.List;

public class MessageCommand extends AbstractCommand {
    private static final String PLAYER_ARG = "player";
    private static final String MESSAGE_ARG = "message";

    /**
     * /message <target> <message>
     */
    public MessageCommand() {
        super("message");
    }

    @Override
    public void register() {
        if (!config.getBoolean("modules.messages", false)) return;

        LiteralArgumentBuilder<CommandSource> messageRootNode = BrigadierCommand
                .literalArgumentBuilder(command)
                .requires(ctx -> ctx.hasPermission(Permission.MESSAGE.get()))
                .then(BrigadierCommand.requiredArgumentBuilder(PLAYER_ARG, StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            String argument = ctx.getArguments().containsKey(PLAYER_ARG) ? ctx.getArgument(PLAYER_ARG, String.class) : "";

                            for (Player player : proxy.getAllPlayers()) {
                                String playerName = player.getUsername();

                                if (playerName.regionMatches(true, 0, argument, 0, argument.length())) builder.suggest(playerName);
                            }

                            return builder.buildFuture();
                        })
                        .then(BrigadierCommand.requiredArgumentBuilder(MESSAGE_ARG, StringArgumentType.greedyString())
                                .executes((ctx) -> execute(ctx.getSource(), ctx.getArgument(PLAYER_ARG, String.class), ctx.getArgument(MESSAGE_ARG, String.class)))
                                .build()));

        proxy.getCommandManager().register(buildMeta(), new BrigadierCommand(messageRootNode.build()));
    }

    private int execute(CommandSource src, String receiverName, String message) {
        if (!(src instanceof Player sender)) {
            Text.send(messages.get("commands.generic.not-player"), src);
            return COMMAND_FAILED;
        }

        // TODO Check for active mutes if set so in the config

        Player receiver = proxy.getPlayer(receiverName).orElse(null);
        if (receiver == null) {
            Text.send(messages.get("commands.generic.player-not-found"), sender);
            return COMMAND_FAILED;
        }

        if (receiver.equals(sender)) {
            Text.send(messages.get("commands.message.message-yourself"), sender);
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
        sendSocialspy(senderServer, sender, receiverServer, receiver, message);

        plugin.setMessenger(sender, receiver);

        return COMMAND_SUCCESS;
    }

    public static void sendSocialspy(String senderServer, Player sender, String receiverServer, Player receiver, String message) {
        VelocityCompact plugin = VelocityCompact.getInstance();

        new Thread(() -> {
            List<Player> receivers = plugin.proxy().getAllPlayers()
                    .stream()
                    .filter(p -> plugin.userRepository().findById(p.getUniqueId())
                            .map(user -> user.getSettings().hasSocialspy())
                            .orElse(false))
                    .toList();

            if (receivers.isEmpty()) return;

            String broadcast = Messages.get().getAndReplace("common.socialspy-broadcast",
                    "sender_server", senderServer,
                    "sender", sender.getUsername(),
                    "receiver_server", receiverServer,
                    "receiver", receiver.getUsername()
            );

            Text.send(broadcast, receivers);
        }).start();
    }

}
