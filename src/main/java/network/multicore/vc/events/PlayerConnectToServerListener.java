package network.multicore.vc.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import network.multicore.vc.VelocityCompact;

public class PlayerConnectToServerListener {
    private final VelocityCompact plugin;

    public PlayerConnectToServerListener(VelocityCompact plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerConnectToServer(ServerConnectedEvent e) {

    }
}
