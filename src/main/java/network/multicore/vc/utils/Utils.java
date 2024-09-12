package network.multicore.vc.utils;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.myzelyam.api.vanish.VelocityVanishAPI;
import network.multicore.vc.VelocityCompact;

import java.util.UUID;

public class Utils {

    private Utils() {
        throw new IllegalStateException("Utility class");
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

    public static boolean isFallbackServer(RegisteredServer server) {
        if (server == null) return false;
        return VelocityCompact.getInstance()
                .proxy()
                .getConfiguration()
                .getAttemptConnectionOrder()
                .stream()
                .map(String::toLowerCase)
                .anyMatch(name -> name.equalsIgnoreCase(server.getServerInfo().getName()));
    }

    public static boolean isIpv4(String ip) {
        if (!ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) return false;

        String[] parts = ip.split("\\.");
        for (String part : parts) {
            try {
                int value = Integer.parseInt(part);
                if (value > 255) return false;
            } catch (NumberFormatException ignored) {
                return false;
            }
        }

        return true;
    }

    public static boolean isVanished(CommandSource viewer, Player viewed) {
        if (!VelocityCompact.getInstance().hasPremiumVanishSupport()) return false;
        if (!(viewer instanceof Player)) return false;

        return !VelocityVanishAPI.canSee((Player) viewer, viewed);
    }
}
