package network.multicore.vc.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import network.multicore.vc.data.User;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.Text;
import network.multicore.vc.utils.suggestions.CustomSuggestionProvider;
import network.multicore.vc.utils.suggestions.PlayerSuggestionProvider;

public class CommandspyCommand extends AbstractCommand {
    private static final String MODE_ARG = "mode";
    private static final String PLAYER_ARG = "player";

    /**
     * /commandspy <on|off|toggle> [player]
     */
    public CommandspyCommand() {
        super("commandspy");
    }

    @Override
    public void register() {
        if (!config.getBoolean("modules.commandspy", false)) return;

        LiteralArgumentBuilder<CommandSource> rootNode = BrigadierCommand
                .literalArgumentBuilder(command)
                .requires(src -> src.hasPermission(Permission.COMMANDSPY.get()))
                .executes(ctx -> execute(ctx.getSource(), "toggle", null));

        RequiredArgumentBuilder<CommandSource, String> modeNode = BrigadierCommand
                .requiredArgumentBuilder(MODE_ARG, StringArgumentType.word())
                .suggests(new CustomSuggestionProvider<>(MODE_ARG, "on", "off", "toggle"))
                .executes(ctx -> execute(ctx.getSource(), ctx.getArgument(MODE_ARG, String.class), null));


        RequiredArgumentBuilder<CommandSource, String> playerNode = BrigadierCommand
                .requiredArgumentBuilder(PLAYER_ARG, StringArgumentType.word())
                .requires(src -> src.hasPermission(Permission.COMMANDSPY_OTHER.get()))
                .suggests(new PlayerSuggestionProvider<>(proxy, PLAYER_ARG))
                .executes(ctx -> execute(ctx.getSource(), ctx.getArgument(MODE_ARG, String.class), ctx.getArgument(PLAYER_ARG, String.class)));

        modeNode.then(playerNode);
        rootNode.then(modeNode);

        proxy.getCommandManager().register(buildMeta(), new BrigadierCommand(rootNode.build()));
    }

    private int execute(CommandSource src, String mode, String targetName) {
        if (!(src instanceof Player) && targetName == null) {
            Text.send(messages.get("commands.generic.not-player"), src);
            return COMMAND_FAILED;
        }

        User target;
        if (targetName != null) {
            target = plugin.userRepository().findByUsername(targetName).orElse(null);
            if (target == null) {
                Text.send(messages.get("commands.generic.player-not-found"), src);
                return COMMAND_FAILED;
            }
        } else {
            target = plugin.userRepository().findById(((Player) src).getUniqueId()).orElse(null);
            if (target == null) {
                Text.send(messages.getAndReplace("common.internal-exception", "message", "User executing the command not found in the database"), src);
                return COMMAND_FAILED;
            }
        }

        Player targetPlayer = proxy.getPlayer(target.getUniqueId()).orElse(null);

        switch (mode.toLowerCase()) {
            case "on" -> {
                target.getSettings().setCommandspy(true);

                if (targetPlayer != null) Text.send(messages.getAndReplace("commands.generic.enabled-self", "feature", "COMMANDSPY"), targetPlayer);
                if (targetPlayer != src) Text.send(messages.getAndReplace("commands.generic.enabled-player", "feature", "COMMANDSPY", "player", target.getUsername()), src);
            }
            case "off" -> {
                target.getSettings().setCommandspy(false);

                if (targetPlayer != null) Text.send(messages.getAndReplace("commands.generic.disabled-self", "feature", "COMMANDSPY"), targetPlayer);
                if (targetPlayer != src) Text.send(messages.getAndReplace("commands.generic.disabled-self", "feature", "COMMANDSPY", "player", target.getUsername()), src);
            }
            case "toggle" -> {
                target.getSettings().setCommandspy(!target.getSettings().hasCommandspy());

                if (target.getSettings().hasCommandspy()) {
                    if (targetPlayer != null) Text.send(messages.getAndReplace("commands.generic.enabled-self", "feature", "COMMANDSPY"), targetPlayer);
                    if (targetPlayer != src) Text.send(messages.getAndReplace("commands.generic.enabled-player", "feature", "COMMANDSPY", "player", target.getUsername()), src);
                } else {
                    if (targetPlayer != null) Text.send(messages.getAndReplace("commands.generic.disabled-self", "feature", "COMMANDSPY"), targetPlayer);
                    if (targetPlayer != src) Text.send(messages.getAndReplace("commands.generic.disabled-self", "feature", "COMMANDSPY", "player", target.getUsername()), src);
                }
            }
        }

        plugin.userRepository().save(target);

        return COMMAND_SUCCESS;
    }
}
