package network.multicore.vc.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;

public class PlayerDisconnectListener extends Listener {

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerDisconnect(DisconnectEvent e) {

    }
}
