package network.multicore.vc.utils;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.UUID;

public class Utils {

    private Utils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isOnline(ProxyServer proxy, UUID id) {
        return proxy.getPlayer(id).isPresent();
    }

    public static boolean isOnline(ProxyServer proxy, String name) {
        return proxy.getPlayer(name).isPresent();
    }

    public static boolean isOnline(RegisteredServer server, UUID id) {
        return server.getPlayersConnected()
                .stream()
                .anyMatch(player -> player.getUniqueId().equals(id));
    }

    public static boolean isOnline(RegisteredServer server, String name) {
        return server.getPlayersConnected()
                .stream()
                .anyMatch(player -> player.getUsername().equalsIgnoreCase(name));
    }
}
