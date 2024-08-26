package network.multicore.vc.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.Text;

public class BroadcastCommand extends AbstractCommand {
    private static final String MESSAGE_ARG = "message";

    /**
     * /broadcast <message>
     */
    public BroadcastCommand() {
        super("broadcast");
    }

    public void register() {
        if (!config.getBoolean("modules.broadcast", false)) return;

        LiteralArgumentBuilder<CommandSource> broadcastRootNode = BrigadierCommand
                .literalArgumentBuilder(command)
                .requires(src -> src.hasPermission(Permission.BROADCAST.get()))
                .then(BrigadierCommand.requiredArgumentBuilder(MESSAGE_ARG, StringArgumentType.greedyString())
                        .executes((ctx) -> execute(ctx.getSource(), ctx.getArgument(MESSAGE_ARG, String.class)))
                        .build());

        proxy.getCommandManager().register(buildMeta(), new BrigadierCommand(broadcastRootNode.build()));
    }

    private int execute(CommandSource src, String message) {
        if (!(src instanceof Player player)) {
            Text.send(messages.get("commands.generic.not-player"), src);
            return COMMAND_FAILED;
        }

        ServerConnection server = player.getCurrentServer().orElse(null);
        if (server == null) {
            Text.send(messages.get("commands.generic.not-connected-self"), player);
            return COMMAND_FAILED;
        }

        if (!src.hasPermission(Permission.COLORS.get())) message = Text.stripFormatting(Text.stripLegacyFormatting(message));

        Text.broadcast(message, server.getServer());

        return COMMAND_SUCCESS;
    }
}
