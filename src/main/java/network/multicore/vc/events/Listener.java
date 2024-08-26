package network.multicore.vc.events;

import com.velocitypowered.api.proxy.ProxyServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import network.multicore.vc.VelocityCompact;
import network.multicore.vc.utils.Messages;

public abstract class Listener {
    protected final VelocityCompact plugin;
    protected final ProxyServer proxy;
    protected final YamlDocument config;
    protected final Messages messages;

    public Listener() {
        this.plugin = VelocityCompact.getInstance();
        this.proxy = this.plugin.proxy();
        this.messages = Messages.get();
        this.config = plugin.config();
    }
}
