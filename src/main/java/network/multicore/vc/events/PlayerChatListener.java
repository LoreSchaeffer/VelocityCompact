package network.multicore.vc.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import network.multicore.vc.data.Mute;
import network.multicore.vc.data.MuteRepository;
import network.multicore.vc.utils.CensureUtils;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.ModerationUtils;
import network.multicore.vc.utils.Text;
import org.slf4j.Logger;

import java.util.List;

public class PlayerChatListener extends Listener {
    private final boolean moderationEnabled;
    private final boolean globalchatEnabled;
    private final CensureUtils censureUtils;
    private final MuteRepository muteRepository;
    private final Logger logger;

    public PlayerChatListener() {
        super();

        this.moderationEnabled = config.getBoolean("modules.moderation", false);
        this.globalchatEnabled = config.getBoolean("modules.globalchat", false);
        this.censureUtils = CensureUtils.get();
        this.muteRepository = plugin.muteRepository();
        this.logger = plugin.logger();
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerChat(PlayerChatEvent e) {
        if (!e.getResult().isAllowed()) return;

        Player player = e.getPlayer();
        String message = e.getMessage();
        String server = player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse(null);

        if (moderationEnabled) {
            List<Mute> activeMutes = muteRepository.findAllActiveByUuid(player.getUniqueId());
            activeMutes.addAll(muteRepository.findAllActiveByIp(player.getRemoteAddress().getAddress().getHostAddress()));
            ModerationUtils.removeExpiredMutes(activeMutes);

            if (!activeMutes.isEmpty()) {
                Mute mute = activeMutes.stream()
                        .filter(m -> m.getServer() == null)
                        .findAny()
                        .orElse(activeMutes.getFirst());

                e.setResult(PlayerChatEvent.ChatResult.denied());
                Text.send(messages.getAndReplace("moderation.mute.reminder",
                        "staff", mute.getStaff() != null ? mute.getStaff().getUsername() : messages.get("console"),
                        "server", mute.getServer() != null ? mute.getServer() : messages.get("global"),
                        "duration", mute.getEndDate() != null ? ModerationUtils.getDurationString(mute.getEndDate()) : messages.get("permanent"),
                        "reason", mute.getReason() != null ? mute.getReason() : messages.get("no-reason")
                ), player);
                Text.broadcast(messages.getAndReplace("moderation.mute.muted-message-broadcast",
                        "server", server != null ? server : messages.get("unknown"),
                        "player", player.getUsername(),
                        "message", message
                ), Permission.MUTED_MESSAGE_RECEIVE.get());
                logger.info("{} tried send a message to chat, but is muted. Message: {}", player.getUsername(), message);
                return;
            }
        }

        if (censureUtils.isChatCensorshipEnabled()) {
            CensureUtils.CensureResult result = censureUtils.censure(player, message);

            if (result.isCensored()) {
                if (result.shouldCancelMessage()) {
                    e.setResult(PlayerChatEvent.ChatResult.denied());
                    return;
                }

                e.setResult(PlayerChatEvent.ChatResult.message(result.getMessage()));
            }
        }

        if (globalchatEnabled) {
            globalchat(player, message);
        }
    }

    private void globalchat(Player player, String message) {
        String server = player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse(messages.get("unknown"));

        new Thread(() -> {
            List<Player> receivers = proxy.getAllPlayers()
                    .stream()
                    .filter(p -> plugin.userRepository().findById(p.getUniqueId())
                            .map(user -> user.getSettings().hasGlobalchat())
                            .orElse(false))
                    .toList();

            if (receivers.isEmpty()) return;

            String broadcast = messages.getAndReplace("common.globalchat-broadcast", "server", server, "player", player, "message", message);
            Text.send(broadcast, receivers);
        }).start();
    }
}
