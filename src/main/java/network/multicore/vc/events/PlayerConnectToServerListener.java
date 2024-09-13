package network.multicore.vc.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import network.multicore.vc.data.Ban;
import network.multicore.vc.data.BanRepository;
import network.multicore.vc.utils.ModerationUtils;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.Text;
import network.multicore.vc.utils.Utils;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

public class PlayerConnectToServerListener extends Listener {
    private final boolean moderationEnabled;
    private final BanRepository banRepository;
    private final Logger logger;

    public PlayerConnectToServerListener() {
        super();

        this.moderationEnabled = config.getBoolean("modules.moderation", false);
        this.banRepository = plugin.banRepository();
        this.logger = plugin.logger();
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerConnectToServer(ServerConnectedEvent e) {
        if (moderationEnabled) {
            Player player = e.getPlayer();
            RegisteredServer server = e.getServer();

            List<Ban> activeBans = banRepository.findAllActiveByUuid(player.getUniqueId());
            activeBans.addAll(banRepository.findAllActiveByIp(player.getRemoteAddress().getHostString()));
            ModerationUtils.removeExpiredBans(activeBans);

            if (!activeBans.isEmpty()) {
                Ban ban = activeBans.stream()
                        .filter(b -> server.getServerInfo().getName().equalsIgnoreCase(b.getServer()))
                        .findAny()
                        .orElse(null);

                if (ban != null) {
                    if (Utils.isFallbackServer(server)) {
                        player.disconnect(Text.deserialize(messages.getAndReplace("moderation.disconnect.ban",
                                "staff", ban.getStaff() != null ? ban.getStaff().getUsername() : messages.get("console"),
                                "server", ban.getServer() != null ? ban.getServer() : messages.get("global"),
                                "duration", ban.getEndDate() != null ? ModerationUtils.getDurationString(ban.getEndDate()) : messages.get("permanent"),
                                "reason", ban.getReason() != null ? ban.getReason() : messages.get("no-reason")
                        )));
                    } else {
                        Optional<RegisteredServer> previousServer = e.getPreviousServer();

                        if (previousServer.isPresent()) {
                            player.createConnectionRequest(previousServer.get()).connect()
                                    .whenComplete((result, throwable) -> {
                                        if (throwable != null || !result.isSuccessful()) {
                                            logger.error("Failed to connect player {} to previous server", player.getUsername(), throwable);

                                            player.disconnect(Text.deserialize(messages.getAndReplace("moderation.disconnect.ban",
                                                    "staff", ban.getStaff() != null ? ban.getStaff().getUsername() : messages.get("console"),
                                                    "server", ban.getServer() != null ? ban.getServer() : messages.get("global"),
                                                    "duration", ban.getEndDate() != null ? ModerationUtils.getDurationString(ban.getEndDate()) : messages.get("permanent"),
                                                    "reason", ban.getReason() != null ? ban.getReason() : messages.get("no-reason")
                                            )));
                                        }
                                    });
                        } else {
                            player.disconnect(Text.deserialize(messages.getAndReplace("moderation.disconnect.ban",
                                    "staff", ban.getStaff() != null ? ban.getStaff().getUsername() : messages.get("console"),
                                    "server", ban.getServer() != null ? ban.getServer() : messages.get("global"),
                                    "duration", ban.getEndDate() != null ? ModerationUtils.getDurationString(ban.getEndDate()) : messages.get("permanent"),
                                    "reason", ban.getReason() != null ? ban.getReason() : messages.get("no-reason")
                            )));
                        }
                    }

                    Text.broadcast(messages.getAndReplace("common.join-attempt-failed-broadcast-server",
                            "player", player.getUsername(),
                            "reason", messages.get("banned"),
                            "server", server.getServerInfo().getName()
                    ), Permission.JOIN_ATTEMPT_RECEIVE_BAN.get());
                    logger.info("{} tried to join server {}, but is banned", player.getUsername(), server.getServerInfo().getName());
                }
            }
        }

        logger.info("Player {} connected to server {}", e.getPlayer().getUsername(), e.getServer().getServerInfo().getName());
    }
}
