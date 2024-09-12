package network.multicore.vc.utils;

import com.velocitypowered.api.proxy.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Cache {
    private static Cache instance;
    private final Map<Player, Player> messengers = new HashMap<>();

    private Cache() {
    }

    public static Cache get() {
        if (instance == null) instance = new Cache();
        return instance;
    }

    public void clear() {
        System.gc();
    }

    public void setMessenger(Player sender, Player receiver) {
        messengers.put(sender, receiver);
        messengers.put(receiver, sender);
    }

    public Optional<Player> getMessenger(Player player) {
        return Optional.ofNullable(messengers.get(player));
    }

    public void removeMessenger(Player player) {
        messengers.remove(player);
        messengers.entrySet().removeIf(entry -> entry.getValue().equals(player));
    }
}
