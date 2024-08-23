package network.multicore.vc.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import network.multicore.vc.VelocityCompact;

public class PlayerPreLoginListener {
    private final VelocityCompact plugin;

    public PlayerPreLoginListener(VelocityCompact plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerPreLogin(PreLoginEvent e) {

    }
}
