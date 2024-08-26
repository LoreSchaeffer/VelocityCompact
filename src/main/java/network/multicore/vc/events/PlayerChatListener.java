package network.multicore.vc.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;

public class PlayerChatListener extends Listener {

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerChat(PlayerChatEvent e) {
        if (!e.getResult().isAllowed()) return;

        Player player = e.getPlayer();
        String message = e.getMessage();

        // TODO Check for globalmute and servermute

        if (config.getBoolean("modules.word-censure", false)) {

        }

        if (config.getBoolean("modules.globalchat", false)) {

        }
    }
}
