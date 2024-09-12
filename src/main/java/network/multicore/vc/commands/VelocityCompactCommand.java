package network.multicore.vc.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import network.multicore.vc.VelocityCompact;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.Text;
import network.multicore.vc.utils.suggestions.CustomSuggestionProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class VelocityCompactCommand extends AbstractCommand {
    private static final String RELOAD_ARG = "reload";
    private static final String PERMISSIONS_ARG = "permissions";
    private static final String PERMISSIONS_EXPORT_ARG = "export";
    private static final List<String> PERMISSIONS_EXPORT_TYPES = List.of("dump", "chat");

    public VelocityCompactCommand() {
        super("velocitycompact");
    }

    @Override
    public void register() {
        LiteralArgumentBuilder<CommandSource> node = BrigadierCommand
                .literalArgumentBuilder(command)
                .executes(ctx -> version(ctx.getSource()))
                .then(BrigadierCommand.literalArgumentBuilder(RELOAD_ARG)
                        .requires(src -> src.hasPermission(Permission.VELOCITYCOMPACT_RELOAD.get()))
                        .executes(ctx -> reload(ctx.getSource()))
                )
                .then(BrigadierCommand.literalArgumentBuilder(PERMISSIONS_ARG)
                        .then(BrigadierCommand.requiredArgumentBuilder(PERMISSIONS_EXPORT_ARG, StringArgumentType.word())
                                .suggests(new CustomSuggestionProvider<>(PERMISSIONS_EXPORT_ARG, PERMISSIONS_EXPORT_TYPES))
                                .executes(ctx -> permissions(ctx.getSource(), StringArgumentType.getString(ctx, PERMISSIONS_EXPORT_ARG)))
                        )
                );

        proxy.getCommandManager().register(buildMeta(), new BrigadierCommand(node.build()));
    }

    private int version(CommandSource src) {
        Text.send(String.format("<yellow>This server is running <gold><b>VelocityCompact</b><yellow> version <gold>%s<yellow> by <gold><b>%s<yellow>!", VelocityCompact.PLUGIN_VERSION, VelocityCompact.PLUGIN_AUTHOR), src);
        return COMMAND_SUCCESS;
    }

    private int reload(CommandSource src) {
        long millis = System.currentTimeMillis();
        plugin.disable();
        plugin.enable();
        Text.send(messages.getAndReplace("commands.velocitycompact.reload", "time", (System.currentTimeMillis() - millis)), src);
        return COMMAND_SUCCESS;
    }

    private int permissions(CommandSource src, String export) {
        if (!PERMISSIONS_EXPORT_TYPES.contains(export.toLowerCase())) {
            Text.send(messages.getAndReplace("commands.generic.invalid-argument", "argument", export), src);
            return COMMAND_FAILED;
        }

        List<String> permissions = Arrays.stream(Permission.values())
                .map(Permission::get)
                .toList();

        if (export.equalsIgnoreCase("dump")) {
            File dump = new File(plugin.pluginDir(), "permissions-dump.txt");

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dump), StandardCharsets.UTF_8))) {
                for (String permission : permissions) {
                    writer.write(permission);
                    writer.newLine();
                }

                Text.send(messages.getAndReplace("commands.velocitycompact.permissions-dumped", "file", dump.getPath()), src);
            } catch (Exception e) {
                Text.send(messages.getAndReplace("commands.velocitycompact.permissions-dump-failed", "file", dump.getPath()), src);
                plugin.logger().error("Failed to dump permissions", e);
            }
        } else {
            Text.send(permissions, src);
        }

        return COMMAND_SUCCESS;
    }
}
