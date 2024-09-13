package network.multicore.vc.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.Text;
import network.multicore.vc.utils.suggestions.ServerSuggestionProvider;

public class BroadcastCommand extends AbstractCommand {
    public static final String SERVER_ARG = "server";
    private static final String MESSAGE_ARG = "message";

    /**
     * /broadcast <server> <message>
     */
    public BroadcastCommand() {
        super("broadcast");
    }

    public void register() {
        if (!config.getBoolean("modules.broadcast", false)) return;

        LiteralArgumentBuilder<CommandSource> node = BrigadierCommand
                .literalArgumentBuilder(command)
                .requires(src -> src.hasPermission(Permission.BROADCAST.get()))
                .then(BrigadierCommand.requiredArgumentBuilder(SERVER_ARG, StringArgumentType.word())
                        .suggests(new ServerSuggestionProvider<>(proxy, SERVER_ARG))
                        .then(BrigadierCommand.requiredArgumentBuilder(MESSAGE_ARG, StringArgumentType.greedyString())
                                .executes((ctx) -> execute(ctx.getSource(), ctx.getArgument(SERVER_ARG, String.class), ctx.getArgument(MESSAGE_ARG, String.class)))
                                .build()));

        proxy.getCommandManager().register(buildMeta(), new BrigadierCommand(node.build()));
    }

    private int execute(CommandSource src, String serverName, String message) {
        RegisteredServer server = proxy.getServer(serverName).orElse(null);
        if (server == null) {
            Text.send(messages.get("commands.generic.server-not-found"), src);
            return COMMAND_FAILED;
        }

        if (!src.hasPermission(Permission.COLORS.get())) message = Text.stripFormatting(message);

        Text.broadcast(messages.getAndReplace("commands.broadcast.format", "message", message), server);

        boolean sendFeedback = false;
        if (!(src instanceof Player player)) {
            sendFeedback = true;
        } else {
            String playerServer = player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse(null);
            if (playerServer == null || !playerServer.equals(serverName)) {
                sendFeedback = true;
            }
        }

        if (sendFeedback) Text.send(messages.getAndReplace("commands.broadcast.feedback", "server", server.getServerInfo().getName()), src);

        return COMMAND_SUCCESS;
    }
}
