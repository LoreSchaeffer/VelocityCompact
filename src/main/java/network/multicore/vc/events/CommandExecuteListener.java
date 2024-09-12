package network.multicore.vc.events;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.Text;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandExecuteListener extends Listener {
    private final boolean commandBlockerEnabled;
    private final boolean commandWarningEnabled;
    private final boolean commandspyEnabled;

    private final Set<String> blockedCommands;
    private final Set<String> commandsWarning;
    private final Set<String> csServerList;
    private final boolean csIsWhitelist;
    private final Set<String> csPlayerBypassList;
    private final Set<String> csIgnoredCommands;
    private final Logger logger;

    public CommandExecuteListener() {
        super();

        this.commandBlockerEnabled = config.getBoolean("modules.command-blocker", false);
        this.commandWarningEnabled = config.getBoolean("modules.command-warning", false);
        this.commandspyEnabled = config.getBoolean("modules.commandspy", false);

        this.blockedCommands = config
                .getStringList("blocked-commands")
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        this.commandsWarning = config
                .getStringList("command-warning")
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        this.csServerList = config
                .getStringList("commandspy.server-list")
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        this.csIsWhitelist = config.getBoolean("commandspy.list-is-whitelist", false);
        this.csPlayerBypassList = new HashSet<>(config.getStringList("commandspy.player-bypass"));
        this.csIgnoredCommands = config
                .getStringList("commandspy.ignored-commands")
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        if (config.getBoolean("modules.socialspy", false)) {
            this.csIgnoredCommands.add("message");
            this.csIgnoredCommands.add("reply");
            this.csIgnoredCommands.addAll(config.getStringList("command-aliases.message"));
            this.csIgnoredCommands.addAll(config.getStringList("command-aliases.reply"));
        }
        this.logger = plugin.logger();
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onCommandExecuted(CommandExecuteEvent e) {
        if (!e.getResult().isAllowed()) return;

        CommandSource src = e.getCommandSource();
        String command = e.getCommand().trim();

        if (commandBlockerEnabled && src instanceof Player player && isCommandBlocked(src, command)) {
            e.setResult(CommandExecuteEvent.CommandResult.denied());
            Text.send("common.command-blocked", src);

            String server = player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse(messages.get("unknown"));
            Text.broadcast(messages.getAndReplace("common.command-blocked-broadcast", "server", server, "player", player, "command", command), Permission.COMMAND_WARNING_BYPASS.get());
            logger.info("{}:{} tried to use the command: {}", server, player.getUsername(), command);
            return;
        }

        if (commandWarningEnabled && src instanceof Player player && shouldWarnStaff(src, command)) {
            e.setResult(CommandExecuteEvent.CommandResult.denied());
            Text.send("common.command-warning", src);

            String server = player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse(messages.get("unknown"));
            Text.broadcast(messages.getAndReplace("common.command-warning-broadcast", "server", server, "player", player, "command", command), Permission.COMMAND_WARNING_RECEIVE.get());
            logger.info("{}:{} used the command: {}", server, player.getUsername(), command);
        }

        if (commandspyEnabled && src instanceof Player player) {
            commandspy(player, command);
        }
    }

    private boolean isCommandBlocked(CommandSource src, String command) {
        if (src.hasPermission(Permission.COMMAND_BLOCKER_BYPASS.get())) return false;
        return blockedCommands.stream().anyMatch(blockedCommand -> command.equalsIgnoreCase(blockedCommand) || command.toLowerCase().startsWith(blockedCommand + " "));
    }

    private boolean shouldWarnStaff(CommandSource src, String command) {
        if (src.hasPermission(Permission.COMMAND_WARNING_BYPASS.get())) return false;
        return commandsWarning.stream().anyMatch(blockedCommand -> command.equalsIgnoreCase(blockedCommand) || command.toLowerCase().startsWith(blockedCommand + " "));
    }

    private void commandspy(Player player, String command) {
        if (csIgnoredCommands.stream().anyMatch(cmd -> command.equalsIgnoreCase(cmd) || command.toLowerCase().startsWith(cmd + " "))) return;
        if (csPlayerBypassList.contains(player.getUsername())) return;

        String server = player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse(messages.get("unknown")).toLowerCase();

        if (csIsWhitelist && !csServerList.contains(server)) return;
        else if (!csIsWhitelist && csServerList.contains(server)) return;

        new Thread(() -> {
            List<Player> receivers = proxy.getAllPlayers()
                    .stream()
                    .filter(p -> plugin.userRepository().findById(p.getUniqueId())
                            .map(user -> user.getSettings().hasCommandspy())
                            .orElse(false))
                    .toList();

            if (receivers.isEmpty()) return;

            String broadcast = messages.getAndReplace("common.commandspy-broadcast", "server", server, "player", player, "command", command);
            Text.send(broadcast, receivers);
        }).start();
    }
}
