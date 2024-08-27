package network.multicore.vc.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import network.multicore.vc.data.Ban;
import network.multicore.vc.data.BanRepository;
import network.multicore.vc.data.User;
import network.multicore.vc.data.UserRepository;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.PunishmentUtils;
import network.multicore.vc.utils.Text;
import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerLoginListener extends Listener {
    private final boolean ipLimiterEnabled;
    private final int maxConnectionsPerIp;
    private final Set<String> whitelistedIps;
    private final Set<String> whitelistedNicknames;
    private final UserRepository userRepository;
    private final BanRepository banRepository;
    private final Logger logger;

    public PlayerLoginListener() {
        super();

        this.ipLimiterEnabled = config.getBoolean("modules.ip-limiter", false);
        this.maxConnectionsPerIp = config.getInt("ip-limiter.max-connections", 5);
        this.whitelistedIps = new HashSet<>(config.getStringList("ip-limiter.whitelist.ip-addresses"));
        this.whitelistedNicknames = config.getStringList("ip-limiter.whitelist.nicknames")
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        this.userRepository = plugin.userRepository();
        this.banRepository = plugin.banRepository();
        this.logger = plugin.logger();
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerPostLogin(LoginEvent e) {
        Player player = e.getPlayer();

        if (ipLimiterEnabled && !whitelistedNicknames.contains(player.getUsername().toLowerCase())) {
            String ip = player.getRemoteAddress().getHostString();

            if (!isIpWhitelisted(ip)) {
                int connections = (int) proxy.getAllPlayers()
                        .stream()
                        .filter(p -> p.getRemoteAddress().getHostString().equals(ip))
                        .count();

                if (connections >= maxConnectionsPerIp) {
                    e.setResult(ResultedEvent.ComponentResult.denied(Text.deserialize(messages.get("common.too-many-connections"))));
                    Text.broadcast(messages.getAndReplace("common.join-attempt-failed-broadcast", "player", player.getUsername(), "reason", messages.get("too-many-connections")), Permission.JOIN_ATTEMPT_RECEIVE_IP_LIMITER.get());
                    logger.info("{} tried to join, but there are already {} players with the same ip", player.getUsername(), connections);
                    return;
                }
            }
        }

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
        cache.addUser(user);

        List<Ban> activeBans = banRepository.findAllActiveByUuid(player.getUniqueId());
        if (!activeBans.isEmpty()) {
            Optional<Ban> banOpt = activeBans.stream()
                    .filter(ban -> ban.getServer() == null)
                    .findAny();

            if (banOpt.isPresent()) {
                Ban ban = banOpt.get();
                boolean isExpired = PunishmentUtils.isExpired(ban.getEndDate());

                if (!isExpired) {
                    e.setResult(ResultedEvent.ComponentResult.denied(Text.deserialize(messages.getAndReplace("moderation.disconnect.ban",
                            "staff", ban.getStaff() != null ? ban.getStaff().getUsername() : messages.get("console"),
                            "server", ban.getServer() != null ? ban.getServer() : messages.get("global"),
                            "duration", ban.getEndDate() != null ? PunishmentUtils.getDurationString(ban.getEndDate(), messages) : messages.get("permanent"),
                            "reason", ban.getReason() != null ? ban.getReason() : messages.get("no-reason")
                    ))));
                    Text.broadcast(messages.getAndReplace("common.join-attempt-failed-broadcast", "player", player.getUsername(), "reason", messages.get("banned")), Permission.JOIN_ATTEMPT_RECEIVE_BAN.get());
                    logger.info("{} tried to join, but is banned", player.getUsername());
                    return;
                } else {
                    ban.setUnbanDate();
                    banRepository.save(ban);
                }
            }
        }

        //TODO Inform staff if a player has the same ip of other players in a separate thread
    }

    private boolean isIpWhitelisted(String ip) {
        for (String subnet : whitelistedIps) {
            if (ip.equals(subnet)) return true;

            try {
                SubnetUtils utils = new SubnetUtils(subnet);
                if (utils.getInfo().isInRange(ip)) return true;
            } catch (IllegalArgumentException ignored) {
            }
        }

        return false;
    }
}
