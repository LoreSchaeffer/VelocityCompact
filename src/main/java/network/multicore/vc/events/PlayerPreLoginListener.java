package network.multicore.vc.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.Text;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class PlayerPreLoginListener extends Listener {
    private final boolean nicknameBlocker;
    private final Set<String> blockedNicks;
    private final boolean ipLimiter;
    private final int maxConnectionsPerIp;
    private final Logger logger;

    public PlayerPreLoginListener() {
        super();

        this.nicknameBlocker = config.getBoolean("modules.nickname-blocker", false);
        this.blockedNicks = new HashSet<>(config.getStringList("blocked-nicknames"));
        this.ipLimiter = config.getBoolean("modules.ip-limiter", false);
        this.maxConnectionsPerIp = config.getInt("limit-players-per-ip", 5);
        this.logger = plugin.logger();
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerPreLogin(PreLoginEvent e) {
        if (!e.getResult().isAllowed()) return;

        String ip = e.getConnection().getRemoteAddress().getHostString();

        //TODO Global ip ban check

        if (nicknameBlocker) {
            String username = e.getUsername();

            if (blockedNicks.stream().anyMatch(username::matches)) {
                e.setResult(PreLoginEvent.PreLoginComponentResult.denied(Text.deserialize(messages.get("common.nickname-not-allowed"))));
                Text.broadcast(messages.getAndReplace("common.join-attempt-failed-broadcast", "player", username, "reason", messages.get("blocked-nickname")), Permission.JOIN_ATTEMPT_RECEIVE.get());
                logger.info("{} tried to join with a blocked nickname", username);
                return;
            }
        }

        if (ipLimiter) {
            int connections = (int) proxy.getAllPlayers().stream().filter(p -> p.getRemoteAddress().getHostString().equals(ip)).count();

            if (connections >= maxConnectionsPerIp) {
                e.setResult(PreLoginEvent.PreLoginComponentResult.denied(Text.deserialize(messages.get("common.too-many-connections"))));
                Text.broadcast(messages.getAndReplace("common.join-attempt-failed-broadcast", "player", e.getUsername(), "reason", messages.get("too-many-connections")), Permission.JOIN_ATTEMPT_RECEIVE.get());
                logger.info("{} tried to join, but there are already {} players with the same ip", e.getUsername(), connections);
            }
        }
    }
}
