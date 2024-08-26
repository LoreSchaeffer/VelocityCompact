package network.multicore.vc.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import network.multicore.vc.data.User;
import network.multicore.vc.data.UserRepository;

import java.util.Date;
import java.util.Objects;

public class PlayerPostLoginListener extends Listener {
    private final UserRepository userRepository;

    public PlayerPostLoginListener() {
        super();

        this.userRepository = plugin.userRepository();
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerPostLogin(PostLoginEvent e) {
        Player player = e.getPlayer();

        User user = userRepository.findById(e.getPlayer().getUniqueId()).orElse(null);
        if (user == null) {
            user = userRepository.save(new User(player));
        } else {
            if (!Objects.equals(user.getUsername(), player.getUsername())) user.setUsername(player.getUsername());

            user.setIp(player.getRemoteAddress().getHostString())
                    .setLastLogin(new Date())
                    .setProtocolVersion(player.getProtocolVersion().getProtocol())
                    .setClientBrand(player.getClientBrand());

            userRepository.save(user);
        }

        //TODO Global ban check
    }
}
