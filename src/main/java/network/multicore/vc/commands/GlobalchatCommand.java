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

public class GlobalchatCommand extends AbstractCommand {
    private static final String MODE_ARG = "mode";
    private static final String PLAYER_ARG = "player";

    /**
     * /globalchat <on|off|toggle> [player]
     */
    public GlobalchatCommand() {
        super("globalchat");
    }

    @Override
    public void register() {
        if (!config.getBoolean("modules.globalchat", false)) return;

        LiteralArgumentBuilder<CommandSource> globalchatRootNode = BrigadierCommand
                .literalArgumentBuilder(command)
                .requires(src -> src.hasPermission(Permission.GLOBALCHAT.get()))
                .executes(ctx -> execute(ctx.getSource(), "toggle", null));

        RequiredArgumentBuilder<CommandSource, String> modeNode = BrigadierCommand
                .requiredArgumentBuilder(MODE_ARG, StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    String argument = ctx.getArguments().containsKey(MODE_ARG) ? ctx.getArgument(MODE_ARG, String.class) : "";

                    if ("on".regionMatches(true, 0, argument, 0, argument.length())) builder.suggest("on");
                    if ("off".regionMatches(true, 0, argument, 0, argument.length())) builder.suggest("off");
                    if ("toggle".regionMatches(true, 0, argument, 0, argument.length())) builder.suggest("toggle");

                    return builder.buildFuture();
                })
                .executes(ctx -> execute(ctx.getSource(), ctx.getArgument(MODE_ARG, String.class), null));


        RequiredArgumentBuilder<CommandSource, String> playerNode = BrigadierCommand
                .requiredArgumentBuilder(PLAYER_ARG, StringArgumentType.word())
                .requires(src -> src.hasPermission(Permission.GLOBALCHAT_OTHER.get()))
                .suggests((ctx, builder) -> {
                    String argument = ctx.getArguments().containsKey(PLAYER_ARG) ? ctx.getArgument(PLAYER_ARG, String.class) : "";

                    for (Player player : proxy.getAllPlayers()) {
                        String playerName = player.getUsername();

                        if (playerName.regionMatches(true, 0, argument, 0, argument.length())) builder.suggest(playerName);
                    }

                    return builder.buildFuture();
                })
                .executes(ctx -> execute(ctx.getSource(), ctx.getArgument(MODE_ARG, String.class), ctx.getArgument(PLAYER_ARG, String.class)));

        modeNode.then(playerNode);
        globalchatRootNode.then(modeNode);

        proxy.getCommandManager().register(buildMeta(), new BrigadierCommand(globalchatRootNode.build()));
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
                target.getSettings().setGlobalchat(true);

                if (targetPlayer != null) Text.send(messages.get("commands.globalchat.enabled"), targetPlayer);
                if (targetPlayer != src) Text.send(messages.getAndReplace("commands.globalchat.enabled-player", "player", target.getUsername()), src);
            }
            case "off" -> {
                target.getSettings().setGlobalchat(false);

                if (targetPlayer != null) Text.send(messages.get("commands.globalchat.disabled"), targetPlayer);
                if (targetPlayer != src) Text.send(messages.getAndReplace("commands.globalchat.disabled-player", "player", target.getUsername()), src);
            }
            case "toggle" -> {
                target.getSettings().setGlobalchat(!target.getSettings().hasGlobalchat());

                if (target.getSettings().hasGlobalchat()) {
                    if (targetPlayer != null) Text.send(messages.get("commands.globalchat.enabled"), targetPlayer);
                    if (targetPlayer != src) Text.send(messages.getAndReplace("commands.globalchat.enabled-player", "player", target.getUsername()), src);
                } else {
                    if (targetPlayer != null) Text.send(messages.get("commands.globalchat.disabled"), targetPlayer);
                    if (targetPlayer != src) Text.send(messages.getAndReplace("commands.globalchat.disabled-player", "player", target.getUsername()), src);
                }
            }
        }

        plugin.userRepository().save(target);

        return COMMAND_SUCCESS;
    }
}
