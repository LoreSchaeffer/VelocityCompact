package network.multicore.vc.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;

public class PlayerConnectToServerListener extends Listener {

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerConnectToServer(ServerConnectedEvent e) {

    }
}
