package network.multicore.vc.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import network.multicore.vc.VelocityCompact;

public class PlayerChatListener {
    private final VelocityCompact plugin;

    public PlayerChatListener(VelocityCompact plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerChat(PlayerChatEvent e) {

    }
}
