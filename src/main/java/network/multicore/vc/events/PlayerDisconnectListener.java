package network.multicore.vc.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import network.multicore.vc.VelocityCompact;
import org.slf4j.Logger;

public class PlayerDisconnectListener extends Listener {
    private final Logger logger;

    public PlayerDisconnectListener() {
        this.logger = VelocityCompact.getInstance().logger();
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerDisconnect(DisconnectEvent e) {
        cache.removeMessenger(e.getPlayer());
        logger.info("{} left the proxy", e.getPlayer().getUsername());
    }
}
