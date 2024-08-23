package network.multicore.vc.commands;

import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.proxy.ProxyServer;
import network.multicore.vc.VelocityCompact;

import java.util.List;
import java.util.Optional;

public abstract class AbstractCommand {
    protected final String command;
    protected final ProxyServer proxy;
    protected final VelocityCompact plugin;
    protected CommandMeta meta;

    public AbstractCommand(ProxyServer proxy, VelocityCompact plugin, String command) {
        this.proxy = proxy;
        this.plugin = plugin;
        this.command = command;
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
