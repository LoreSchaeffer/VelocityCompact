package network.multicore.vc.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.Text;

public class GBroadcastCommand extends AbstractCommand {
    private static final String MESSAGE_ARG = "message";

    /**
     * /gbroadcast <message>
     */
    public GBroadcastCommand() {
        super("gbroadcast");
    }

    public void register() {
        if (!config.getBoolean("modules.broadcast", false)) return;

        LiteralArgumentBuilder<CommandSource> broadcastRootNode = BrigadierCommand
                .literalArgumentBuilder(command)
                .requires(src -> src.hasPermission(Permission.GBROADCAST.get()))
                .then(BrigadierCommand.requiredArgumentBuilder(MESSAGE_ARG, StringArgumentType.greedyString())
                        .executes((ctx) -> execute(ctx.getSource(), ctx.getArgument(MESSAGE_ARG, String.class)))
                        .build());

        proxy.getCommandManager().register(buildMeta(), new BrigadierCommand(broadcastRootNode.build()));
    }

    private int execute(CommandSource src, String message) {
        if (!src.hasPermission(Permission.COLORS.get())) message = Text.stripFormatting(message);

        Text.broadcast(messages.getAndReplace("commands.broadcast.format", "message", message));

        return COMMAND_SUCCESS;
    }
}
