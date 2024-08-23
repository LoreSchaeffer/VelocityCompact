package network.multicore.vc.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import network.multicore.vc.VelocityCompact;

public class PlayerPostLoginListener {
    private final VelocityCompact plugin;

    public PlayerPostLoginListener(VelocityCompact plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerPostLogin(PostLoginEvent e) {

    }
}
