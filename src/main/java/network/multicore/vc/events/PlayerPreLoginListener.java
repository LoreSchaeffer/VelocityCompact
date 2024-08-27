package network.multicore.vc.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import network.multicore.vc.data.Ban;
import network.multicore.vc.data.BanRepository;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.PunishmentUtils;
import network.multicore.vc.utils.Text;
import org.slf4j.Logger;

import java.util.*;

public class PlayerPreLoginListener extends Listener {
    private final boolean nicknameBlockerEnabled;
    private final Set<String> blockedNicks;
    private final Logger logger;
    private final BanRepository banRepository;

    public PlayerPreLoginListener() {
        super();

        this.nicknameBlockerEnabled = config.getBoolean("modules.nickname-blocker", false);

        this.blockedNicks = new HashSet<>(config.getStringList("blocked-nicknames"));
        this.logger = plugin.logger();
        this.banRepository = plugin.banRepository();
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerPreLogin(PreLoginEvent e) {
        if (!e.getResult().isAllowed()) return;

        String ip = e.getConnection().getRemoteAddress().getHostString();

        List<Ban> activeBans = banRepository.findAllActiveByIp(ip);
        if (!activeBans.isEmpty()) {
            Optional<Ban> banOpt = activeBans.stream()
                    .filter(ban -> ban.getServer() == null)
                    .findAny();

            if (banOpt.isPresent()) {
                Ban ban = banOpt.get();
                boolean isExpired = PunishmentUtils.isExpired(ban.getEndDate());

                if (!isExpired) {
                    e.setResult(PreLoginEvent.PreLoginComponentResult.denied(Text.deserialize(messages.getAndReplace("moderation.disconnect.ban-ip",
                            "staff", ban.getStaff() != null ? ban.getStaff().getUsername() : messages.get("console"),
                            "server", ban.getServer() != null ? ban.getServer() : messages.get("global"),
                            "duration", ban.getEndDate() != null ? PunishmentUtils.getDurationString(ban.getEndDate(), messages) : messages.get("permanent"),
                            "reason", ban.getReason() != null ? ban.getReason() : messages.get("no-reason")
                    ))));
                    Text.broadcast(messages.getAndReplace("common.join-attempt-failed-broadcast", "player", e.getUsername(), "reason", messages.get("banned")), Permission.JOIN_ATTEMPT_RECEIVE_BAN.get());
                    logger.info("{} tried to join, but is ip-banned", e.getUsername());
                    return;
                } else {
                    ban.setUnbanDate();
                    banRepository.save(ban);
                }
            }
        }

        if (nicknameBlockerEnabled) {
            String username = e.getUsername();

            if (blockedNicks.stream().anyMatch(username::matches)) {
                e.setResult(PreLoginEvent.PreLoginComponentResult.denied(Text.deserialize(messages.get("common.nickname-not-allowed"))));
                Text.broadcast(messages.getAndReplace("common.join-attempt-failed-broadcast", "player", username, "reason", messages.get("blocked-nickname")), Permission.JOIN_ATTEMPT_RECEIVE_NICKNAME.get());
                logger.info("{} tried to join with a blocked nickname", username);
            }
        }
    }
}
