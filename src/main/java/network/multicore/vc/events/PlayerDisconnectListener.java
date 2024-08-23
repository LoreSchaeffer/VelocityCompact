package network.multicore.vc.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import network.multicore.vc.VelocityCompact;

public class PlayerDisconnectListener {
    private final VelocityCompact plugin;

    public PlayerDisconnectListener(VelocityCompact plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerDisconnect(DisconnectEvent e) {

    }
}
