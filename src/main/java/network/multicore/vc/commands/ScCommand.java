package network.multicore.vc.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import network.multicore.vc.data.User;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.Text;

import java.util.List;

public class ScCommand extends AbstractCommand {
    private static final String MESSAGE_ARG = "message";

    /**
     * /sc <message>
     */
    public ScCommand() {
        super("sc");
    }

    public void register() {
        if (!config.getBoolean("modules.staffchat", false)) return;

        LiteralArgumentBuilder<CommandSource> node = BrigadierCommand
                .literalArgumentBuilder(command)
                .requires(src -> src.hasPermission(Permission.STAFFCHAT_OTHER.get()))
                .then(BrigadierCommand.requiredArgumentBuilder(MESSAGE_ARG, StringArgumentType.greedyString())
                        .executes((ctx) -> execute(ctx.getSource(), ctx.getArgument(MESSAGE_ARG, String.class)))
                        .build());

        proxy.getCommandManager().register(buildMeta(), new BrigadierCommand(node.build()));
    }

    private int execute(CommandSource src, String message) {
        if (!src.hasPermission(Permission.COLORS.get())) message = Text.stripFormatting(message);

        String format = messages.getAndReplace("commands.staffchat.format",
                "server", src instanceof Player player ? player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse(messages.get("unknown")) : messages.get("global"),
                "player", src instanceof Player player ? player.getUsername() : messages.get("console"),
                "message", message
        );

        new Thread(() -> {
            List<Player> receives = proxy.getAllPlayers()
                    .stream()
                    .filter(p -> {
                        User user = plugin.userRepository().findById(p.getUniqueId()).orElse(null);
                        return user != null && user.getSettings().hasStaffchat();
                    })
                    .toList();

            Text.send(format, receives);
            Text.send(format, src);
        }).start();

        return COMMAND_SUCCESS;
    }
}
