package network.multicore.vc.utils;

import com.google.common.base.Preconditions;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import network.multicore.vc.VelocityCompact;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CensureUtils {
    private static CensureUtils instance;
    private final boolean chatCensorshipEnabled;
    private final Set<String> censoredWords;
    private final Set<String> censoredRegex;
    private final boolean punishmentEnabled;
    private final String punishmentType;
    private final long punishmentDuration;
    private final String punishmentReason;
    private final boolean punishmentSilent;

    private CensureUtils(YamlDocument config) {
        this.chatCensorshipEnabled = config.getBoolean("modules.chat-censorship", false) && config.getBoolean("modules.moderation", false);

        this.censoredWords = config.getStringList("chat-censorship.censored-words")
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        this.censoredRegex = new HashSet<>(config.getStringList("chat-censorship.censored-regex"));
        this.punishmentType = config.getString("chat-censorship.punishment.type");
        this.punishmentDuration = config.getLong("chat-censorship.punishment.duration");
        this.punishmentReason = Messages.get().get("common.used-censored-words");
        this.punishmentSilent = config.getBoolean("chat-censorship.punishment.silent");

        if (punishmentType.equalsIgnoreCase("mute") || punishmentType.equalsIgnoreCase("gmute") || punishmentType.equalsIgnoreCase("ban") || punishmentType.equalsIgnoreCase("gban")) {
            this.punishmentEnabled = config.getBoolean("chat-censorship.punishment.enabled") && punishmentDuration > 0;
        } else {
            this.punishmentEnabled = config.getBoolean("chat-censorship.punishment.enabled");
        }
    }

    public static void init(YamlDocument config) {
        instance = new CensureUtils(config);
    }

    public static CensureUtils get() {
        if (instance == null) throw new IllegalStateException("CensureUtils has not been initialized yet!");
        return instance;
    }

    public boolean isChatCensorshipEnabled() {
        return chatCensorshipEnabled;
    }

    public CensureResult censure(@NotNull Player player, @NotNull String message) {
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkNotNull(message, "lines");

        if (player.hasPermission(Permission.CHAT_CENSORSHIP_BYPASS.get())) return new CensureResult(false, message, false);

        boolean censored = false;

        for (String word : censoredWords) {
            if (message.toLowerCase().contains(word)) {
                message = message.replaceAll("(?i)" + word, "*".repeat(word.length()));
                censored = true;
            }
        }

        for (String regex : censoredRegex) {
            if (message.matches(regex)) {
                message = "*".repeat(message.length());
                censored = true;
            }
        }

        if (censored && punishmentEnabled) {
            ProxyServer proxy = VelocityCompact.getInstance().proxy();
            ConsoleCommandSource console = proxy.getConsoleCommandSource();
            String server = player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse(null);
            String silent = punishmentSilent ? "s" : "";

            if (server == null && punishmentType.equalsIgnoreCase("mute") || punishmentType.equalsIgnoreCase("kick") || punishmentType.equalsIgnoreCase("ban")) {
                VelocityCompact.getInstance().logger().warn("Player {} was censored for using a censored word, but cannot be punished because they are not on a server.", player.getUsername());
                return new CensureResult(true, message, false);
            }

            return switch (punishmentType.toLowerCase()) {
                case "warn" -> {
                    proxy.getCommandManager().executeImmediatelyAsync(console, String.format("%swarn %s %s", silent, player.getUsername(), punishmentReason));
                    yield new CensureResult(true, message, false);
                }
                case "mute" -> {
                    proxy.getCommandManager().executeImmediatelyAsync(console, String.format("%stempmute %s %s %ds %s", silent, player.getUsername(), server, punishmentDuration, punishmentReason));
                    yield new CensureResult(true, message, false);
                }
                case "gmute" -> {
                    proxy.getCommandManager().executeImmediatelyAsync(console, String.format("%sgtempmute %s %ds %s", silent, player.getUsername(), punishmentDuration, punishmentReason));
                    yield new CensureResult(true, message, false);
                }
                case "kick" -> {
                    proxy.getCommandManager().executeImmediatelyAsync(console, String.format("%skick %s %s %s", silent, player.getUsername(), server, punishmentReason));
                    yield new CensureResult(true, message, true);
                }
                case "gkick" -> {
                    proxy.getCommandManager().executeImmediatelyAsync(console, String.format("%sgkick %s %s", silent, player.getUsername(), punishmentReason));
                    yield new CensureResult(true, message, true);
                }
                case "ban" -> {
                    proxy.getCommandManager().executeImmediatelyAsync(console, String.format("%stempban %s %s %ds %s", silent, player.getUsername(), server, punishmentDuration, punishmentReason));
                    yield new CensureResult(true, message, true);
                }
                case "gban" -> {
                    proxy.getCommandManager().executeImmediatelyAsync(console, String.format("%sgtempban %s %ds %s", silent, player.getUsername(), punishmentDuration, punishmentReason));
                    yield new CensureResult(true, message, true);
                }
                default -> {
                    VelocityCompact.getInstance().logger().error("Invalid punishment type: {}", punishmentType);
                    yield new CensureResult(true, message, false);
                }
            };
        }

        return new CensureResult(censored, message, false);
    }

    public static class CensureResult {
        private final boolean censored;
        private final String message;
        private final boolean cancelMessage;

        public CensureResult(boolean censored, String message, boolean cancelMessage) {
            this.censored = censored;
            this.message = message;
            this.cancelMessage = cancelMessage;
        }

        public boolean isCensored() {
            return censored;
        }

        public String getMessage() {
            return message;
        }

        public boolean shouldCancelMessage() {
            return cancelMessage;
        }
    }
}
