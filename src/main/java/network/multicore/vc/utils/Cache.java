package network.multicore.vc.utils;

import com.velocitypowered.api.proxy.Player;
import network.multicore.vc.VelocityCompact;
import network.multicore.vc.data.User;

import java.util.*;

public class Cache {
    private static Cache instance;
    private final VelocityCompact plugin;
    private final Set<User> users = new HashSet<>();
    private final Map<Player, Player> messengers = new HashMap<>();

    private Cache(VelocityCompact plugin) {
        this.plugin = plugin;
    }

    public static void init(VelocityCompact plugin) {
        instance = new Cache(plugin);

        new Thread(() -> plugin.proxy()
                .getAllPlayers()
                .stream()
                .map(player -> plugin.userRepository().findById(player.getUniqueId()))
                .filter(Optional::isPresent)
                .forEach(user -> instance.users.add(user.get()))).start();
    }

    public static Cache get() {
        if (instance == null) throw new IllegalStateException("Cache not initialized");
        return instance;
    }

    public void clear() {
        users.clear();
        System.gc();
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void removeUser(User user) {
        users.remove(user);
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
