package network.multicore.vc.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import network.multicore.vc.utils.Text;

public class HelpCommand extends AbstractCommand {

    public HelpCommand() {
        super("help");
    }

    @Override
    public void register() {
        if (!config.getBoolean("modules.help", false)) return;

        LiteralArgumentBuilder<CommandSource> helpRootNode = BrigadierCommand
                .literalArgumentBuilder(command)
                .executes(ctx -> execute(ctx.getSource()));

        proxy.getCommandManager().register(buildMeta(), new BrigadierCommand(helpRootNode));
    }

    private int execute(CommandSource src) {
        Text.send(messages.getStringList("commands.help.messages"), src);
        return COMMAND_SUCCESS;
    }
}
