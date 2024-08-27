package network.multicore.vc.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import network.multicore.vc.utils.CensureUtils;
import network.multicore.vc.utils.Text;

import java.util.List;

public class PlayerChatListener extends Listener {
    private final boolean globalchatEnabled;
    private final CensureUtils censureUtils;

    public PlayerChatListener() {
        super();

        this.globalchatEnabled = config.getBoolean("modules.globalchat", false);
        this.censureUtils = CensureUtils.get();
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerChat(PlayerChatEvent e) {
        if (!e.getResult().isAllowed()) return;

        Player player = e.getPlayer();
        String message = e.getMessage();

        // TODO Check for globalmute and servermute

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
