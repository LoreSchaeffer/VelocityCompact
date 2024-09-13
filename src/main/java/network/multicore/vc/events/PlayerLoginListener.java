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
import network.multicore.vc.utils.ModerationUtils;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.Text;
import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerLoginListener extends Listener {
    private final boolean ipLimiterEnabled;
    private final boolean sameIpBroadcastEnabled;
    private final boolean moderationEnabled;

    private final int maxConnectionsPerIp;
    private final Set<String> whitelistedIps;
    private final Set<String> whitelistedNicknames;
    private final Set<String> sameIpBroadcastIgnoredPlayers;

    private final UserRepository userRepository;
    private final BanRepository banRepository;
    private final Logger logger;

    public PlayerLoginListener() {
        super();

        this.ipLimiterEnabled = config.getBoolean("modules.ip-limiter", false);
        this.sameIpBroadcastEnabled = config.getBoolean("modules.same-ip-broadcast", false);
        this.moderationEnabled = config.getBoolean("modules.moderation", false);

        this.maxConnectionsPerIp = config.getInt("ip-limiter.max-connections", 5);
        this.whitelistedIps = new HashSet<>(config.getStringList("ip-limiter.whitelist.ip-addresses"));
        this.whitelistedNicknames = config.getStringList("ip-limiter.whitelist.nicknames")
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        this.sameIpBroadcastIgnoredPlayers = config.getStringList("same-ip-broadcast.ignored-players")
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
            userRepository.save(new User(player));
            logger.info("{} joined the proxy for the first time.", player.getUsername());
        } else {
            if (!Objects.equals(user.getUsername(), player.getUsername())) user.setUsername(player.getUsername());

            user.setIp(player.getRemoteAddress().getHostString())
                    .setLastLogin(new Date())
                    .setProtocolVersion(player.getProtocolVersion().getProtocol())
                    .setClientBrand(player.getClientBrand());

            userRepository.save(user);
            logger.info("{} joined the proxy.", player.getUsername());
        }

        if (moderationEnabled) {
            List<Ban> activeBans = banRepository.findAllActiveByUuid(player.getUniqueId());
            activeBans.addAll(banRepository.findAllActiveByIp(player.getRemoteAddress().getHostString()));
            ModerationUtils.removeExpiredBans(activeBans);

            if (!activeBans.isEmpty()) {
                Ban ban = activeBans.stream()
                        .filter(b -> b.getServer() == null)
                        .findAny()
                        .orElse(null);

                if (ban != null) {
                    e.setResult(ResultedEvent.ComponentResult.denied(Text.deserialize(messages.getAndReplace("moderation.disconnect.ban",
                            "staff", ban.getStaff() != null ? ban.getStaff().getUsername() : messages.get("console"),
                            "server", ban.getServer() != null ? ban.getServer() : messages.get("global"),
                            "duration", ban.getEndDate() != null ? ModerationUtils.getDurationString(ban.getEndDate()) : messages.get("permanent"),
                            "reason", ban.getReason() != null ? ban.getReason() : messages.get("no-reason")
                    ))));
                    Text.broadcast(messages.getAndReplace("common.join-attempt-failed-broadcast", "player", player.getUsername(), "reason", messages.get("banned")), Permission.JOIN_ATTEMPT_RECEIVE_BAN.get());
                    logger.info("{} tried to join, but is banned", player.getUsername());
                    return;
                }
            }
        }

        if (sameIpBroadcastEnabled && !player.hasPermission(Permission.SAME_IP_BROADCAST_BYPASS.get())) {
            new Thread(() -> {
                List<User> users = userRepository.findAllByIp(player.getRemoteAddress().getHostString());
                users.removeIf(u -> sameIpBroadcastIgnoredPlayers.contains(u.getUsername().toLowerCase()));
                if (users.size() < 2) return;

                Text.broadcast(messages.getAndReplace(
                        "common.same-ip-broadcast",
                        "players",
                        users.stream().map(User::getUsername).collect(Collectors.joining(", "))
                ), Permission.SAME_IP_BROADCAST.get());
            }).start();
        }
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
