package network.multicore.vc.commands;

import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import network.multicore.vc.VelocityCompact;
import network.multicore.vc.utils.Messages;

import java.util.List;
import java.util.Optional;

public abstract class AbstractCommand {
    public static final int COMMAND_SUCCESS = 1;
    public static final int COMMAND_FAILED = 0;
    protected final String command;
    protected final ProxyServer proxy;
    protected final VelocityCompact plugin;
    protected final YamlDocument config;
    protected final Messages messages;
    protected CommandMeta meta;

    public AbstractCommand(ProxyServer proxy, VelocityCompact plugin, String command) {
        this.proxy = proxy;
        this.plugin = plugin;
        this.messages = Messages.get();
        this.command = command;
        this.config = plugin.config();
    }

    public abstract void register();

    public void unregister() {
        if (meta != null) proxy.getCommandManager().unregister(meta);
    }

    protected final CommandMeta buildMeta() {
        Optional<String[]> aliases = getAliases();

        aliases.ifPresent(strings -> {
            if (strings.length == 0) {
                meta = proxy.getCommandManager()
                        .metaBuilder(command)
                        .build();
            } else {
                meta = proxy.getCommandManager()
                        .metaBuilder(command)
                        .aliases(aliases.get())
                        .build();
            }
        });

        return meta;
    }

    protected Optional<String[]> getAliases() {
        List<String> aliases = plugin.config().getStringList("command-aliases." + command);
        if (aliases == null) return Optional.empty();
        else return Optional.of(aliases.toArray(new String[0]));
    }
}
