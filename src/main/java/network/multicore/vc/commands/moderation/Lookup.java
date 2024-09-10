package network.multicore.vc.commands.moderation;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import network.multicore.vc.commands.AbstractCommand;
import network.multicore.vc.data.*;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.Text;
import network.multicore.vc.utils.suggestions.CustomSuggestionProvider;
import network.multicore.vc.utils.suggestions.PlayerSuggestionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Lookup extends AbstractCommand {
    private static final String PLAYER_ARG = "player";
    private static final String PUNISHMENT_ARG = "punishment";
    private static final String PAGE_ARG = "page";

    /**
     * /lookup <player> [punishment] [page]
     */
    public Lookup() {
        super("lookup");
    }

    @Override
    public void register() {
        if (!config.getBoolean("modules.moderation", false)) return;

        LiteralArgumentBuilder<CommandSource> node = BrigadierCommand
                .literalArgumentBuilder(command)
                .requires(src -> src.hasPermission(Permission.LOOKUP.get()))
                .then(BrigadierCommand.requiredArgumentBuilder(PLAYER_ARG, StringArgumentType.word())
                        .suggests(new PlayerSuggestionProvider<>(proxy, PLAYER_ARG))
                        .executes((ctx) -> fullLookup(ctx.getSource(), ctx.getArgument(PLAYER_ARG, String.class)))
                        .then(BrigadierCommand.requiredArgumentBuilder(PUNISHMENT_ARG, StringArgumentType.word())
                                .suggests(new CustomSuggestionProvider<>(PUNISHMENT_ARG, "ban", "kick", "mute", "warn"))
                                .executes((ctx) -> punishmentLookup(ctx.getSource(), ctx.getArgument(PLAYER_ARG, String.class), ctx.getArgument(PUNISHMENT_ARG, String.class), 1))
                                .then(BrigadierCommand.requiredArgumentBuilder(PAGE_ARG, IntegerArgumentType.integer(1))
                                        .executes((ctx) -> punishmentLookup(ctx.getSource(), ctx.getArgument(PLAYER_ARG, String.class), ctx.getArgument(PUNISHMENT_ARG, String.class), ctx.getArgument(PAGE_ARG, Integer.class))))
                                .build()));

        proxy.getCommandManager().register(buildMeta(), new BrigadierCommand(node.build()));
    }

    private int fullLookup(CommandSource src, String targetName) {
        Optional<User> userOpt = plugin.userRepository().findByUsername(targetName);
        if (userOpt.isEmpty()) {
            Text.send(messages.get("commands.generic.player-not-found"), src);
            return COMMAND_FAILED;
        }

        User user = userOpt.get();
        List<Ban> bans = plugin.banRepository().findAllByUuid(user.getUniqueId());
        List<Kick> kicks = plugin.kickRepository().findAllByUuid(user.getUniqueId());
        List<Mute> mutes = plugin.muteRepository().findAllByUuid(user.getUniqueId());
        List<Warn> warns = plugin.warnRepository().findAllByUuid(user.getUniqueId());

        List<String> lookup = new ArrayList<>();

        List<String> lookupOrder = messages.getStringList("moderation.lookup.order");
        for (String row : lookupOrder) {
            switch (row) {
                case "header" -> lookup.add(messages.getAndReplace("moderation.lookup.header", "player", user.getUsername()));
                case "footer" -> lookup.add(messages.getAndReplace("moderation.lookup.footer", "player", user.getUsername()));
                case "first-login" -> lookup.add(messages.getAndReplace("moderation.lookup.first-login", "date", messages.formatDate(user.getFirstLogin())));
                case "last-login" -> lookup.add(messages.getAndReplace("moderation.lookup.last-login", "date", messages.formatDate(user.getLastLogin())));
                case "last-ip" -> {
                    if (src.hasPermission(Permission.LOOKUP_IP.get())) lookup.add(messages.getAndReplace("moderation.lookup.last-ip", "ip", user.getIp()));
                }
                case "uuid" -> lookup.add(messages.getAndReplace("moderation.lookup.uuid", "uuid", user.getUniqueId().toString()));
                case "status" -> {
                    Optional<Player> playerOpt = proxy.getPlayer(user.getUniqueId());
                    if (playerOpt.isEmpty()) {
                        lookup.add(messages.get("moderation.lookup.status-offline"));
                    } else {
                        String server = playerOpt.get().getCurrentServer().map(s -> s.getServerInfo().getName()).orElse(messages.get("unknown"));
                        lookup.add(messages.getAndReplace("moderation.lookup.status-online", "server", server));
                    }
                }
                case "punishment-status" -> {
                    List<String> statusOrder = messages.getStringList("moderation.lookup.punishment-status.order");

                    if (!statusOrder.isEmpty()) lookup.add(messages.get("moderation.lookup.punishment-status.title"));

                    for (String statusRow : statusOrder) {
                        switch (statusRow) {
                            case "ban" -> {
                                List<Ban> activeBans = bans.stream()
                                        .filter(b -> b.getUnbanDate() == null)
                                        .toList();

                                if (activeBans.isEmpty()) {
                                    lookup.add(messages.get("moderation.lookup.punishment-status.not-banned"));
                                } else {
                                    Optional<Ban> globalBanOpt = activeBans.stream()
                                            .filter(b -> b.getServer() == null)
                                            .findFirst();

                                    if (globalBanOpt.isPresent()) {
                                        lookup.add(messages.get("moderation.lookup.punishment-status.banned-global"));
                                    } else {
                                        for (Ban ban : activeBans) {
                                            lookup.add(messages.getAndReplace("moderation.lookup.punishment-status.banned-server", "server", ban.getServer()));
                                        }
                                    }
                                }
                            }
                            case "mute" -> {
                                List<Mute> activeMutes = mutes.stream()
                                        .filter(m -> m.getUnmuteDate() == null)
                                        .toList();

                                if (activeMutes.isEmpty()) {
                                    lookup.add(messages.get("moderation.lookup.punishment-status.not-muted"));
                                } else {
                                    Optional<Mute> globalMuteOpt = activeMutes.stream()
                                            .filter(m -> m.getServer() == null)
                                            .findFirst();

                                    if (globalMuteOpt.isPresent()) {
                                        lookup.add(messages.get("moderation.lookup.punishment-status.muted-global"));
                                    } else {
                                        for (Mute mute : activeMutes) {
                                            lookup.add(messages.getAndReplace("moderation.lookup.punishment-status.muted-server", "server", mute.getServer()));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                case "history" -> {
                    List<String> historyOrder = messages.getStringList("moderation.lookup.history.order");

                    if (!historyOrder.isEmpty()) lookup.add(messages.get("moderation.lookup.history.title"));

                    for (String historyRow : historyOrder) {
                        switch (historyRow) {
                            case "bans" -> lookup.add(messages.getAndReplace("moderation.lookup.history.bans", "amount", bans.size()));
                            case "kicks" -> lookup.add(messages.getAndReplace("moderation.lookup.history.kicks", "amount", kicks.size()));
                            case "mutes" -> lookup.add(messages.getAndReplace("moderation.lookup.history.mutes", "amount", mutes.size()));
                            case "warns" -> lookup.add(messages.getAndReplace("moderation.lookup.history.warns", "amount", warns.size()));
                        }
                    }
                }
            }
        }

        Text.send(lookup, src);

        return COMMAND_SUCCESS;
    }

    private int punishmentLookup(CommandSource src, String targetName, String punishment, int page) {

        return COMMAND_SUCCESS;
    }
}
