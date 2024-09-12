package network.multicore.vc.commands.moderation;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import network.multicore.vc.commands.AbstractCommand;
import network.multicore.vc.data.Ban;
import network.multicore.vc.data.Kick;
import network.multicore.vc.data.Mute;
import network.multicore.vc.data.User;
import network.multicore.vc.utils.ModerationUtils;
import network.multicore.vc.utils.Permission;
import network.multicore.vc.utils.Text;
import network.multicore.vc.utils.Utils;
import network.multicore.vc.utils.suggestions.CustomSuggestionProvider;
import network.multicore.vc.utils.suggestions.IpSuggestionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LookupIp extends AbstractCommand {
    private static final String IP_ARG = "ip";
    private static final String PUNISHMENT_ARG = "punishment";
    private static final String PAGE_ARG = "page";
    private static final List<String> PUNISHMENTS = List.of("ban", "kick", "mute");

    /**
     * /lookupip <ip> [punishment] [page]
     */
    public LookupIp() {
        super("lookupip");
    }

    @Override
    public void register() {
        if (!config.getBoolean("modules.moderation", false)) return;

        LiteralArgumentBuilder<CommandSource> node = BrigadierCommand
                .literalArgumentBuilder(command)
                .requires(src -> src.hasPermission(Permission.LOOKUP.get()))
                .then(BrigadierCommand.requiredArgumentBuilder(IP_ARG, StringArgumentType.word())
                        .suggests(new IpSuggestionProvider<>(plugin, IP_ARG)) //TODO Test if it's not too slow
                        .executes((ctx) -> fullLookup(ctx.getSource(), ctx.getArgument(IP_ARG, String.class)))
                        .then(BrigadierCommand.requiredArgumentBuilder(PUNISHMENT_ARG, StringArgumentType.word())
                                .suggests(new CustomSuggestionProvider<>(PUNISHMENT_ARG, PUNISHMENTS))
                                .executes((ctx) -> punishmentLookup(ctx.getSource(), ctx.getArgument(IP_ARG, String.class), ctx.getArgument(PUNISHMENT_ARG, String.class), 1))
                                .then(BrigadierCommand.requiredArgumentBuilder(PAGE_ARG, IntegerArgumentType.integer(1))
                                        .executes((ctx) -> punishmentLookup(ctx.getSource(), ctx.getArgument(IP_ARG, String.class), ctx.getArgument(PUNISHMENT_ARG, String.class), ctx.getArgument(PAGE_ARG, Integer.class))))
                                .build()));

        proxy.getCommandManager().register(buildMeta(), new BrigadierCommand(node.build()));
    }

    private int fullLookup(CommandSource src, String ip) {
        if (!Utils.isIpv4(ip)) {
            Text.send(messages.get("commands.lookup.invalid-ip"), src);
            return COMMAND_FAILED;
        }

        List<User> users = plugin.userRepository().findAllByIp(ip);
        List<Ban> bans = plugin.banRepository().findAllActiveByIp(ip);
        List<Kick> kicks = plugin.kickRepository().findAllByIp(ip);
        List<Mute> mutes = plugin.muteRepository().findAllActiveByIp(ip);

        users.removeIf(user -> config.getStringList("same-ip-broadcast-ignored-players", List.of()).contains(user.getUsername()));
        users.removeIf(user -> proxy.getPlayer(user.getUniqueId()).map(value -> value.hasPermission(Permission.SAME_IP_BROADCAST_BYPASS.get())).orElse(false));

        if (users.isEmpty() && bans.isEmpty() && kicks.isEmpty() && mutes.isEmpty()) {
            Text.send(messages.get("commands.lookup.zero-results"), src);
            return COMMAND_SUCCESS;
        }

        List<String> lookup = new ArrayList<>();

        List<String> lookupOrder = messages.getStringList("moderation.lookup-ip.order");
        for (String row : lookupOrder) {
            switch (row) {
                case "header" -> lookup.add(messages.getAndReplace("moderation.lookup-ip.header", "ip", ip));
                case "footer" -> lookup.add(messages.getAndReplace("moderation.lookup-ip.footer", "ip", ip));
                case "players-connected" -> {
                    List<Player> players = proxy.getAllPlayers()
                            .stream()
                            .filter(p -> p.getRemoteAddress().getHostString().equals(ip))
                            .collect(Collectors.toList());

                    StringBuilder playerList = new StringBuilder();

                    if (!players.isEmpty()) {
                        players.removeIf(p -> p.hasPermission(Permission.SAME_IP_BROADCAST_BYPASS.get()) || config.getStringList("same-ip-broadcast-ignored-players", List.of()).contains(p.getUsername()));

                        for (int i = 0; i < players.size(); i++) {
                            playerList.append(players.get(i).getUsername());
                            if (i < players.size() - 1) playerList.append(messages.get("moderation.lookup-ip.players-connected-separator"));
                        }
                    }

                    lookup.add(messages.getAndReplace("moderation.lookup-ip.players-connected", "players", playerList.toString()));
                }
                case "players-with-this-ip" -> {
                    StringBuilder playerList = new StringBuilder();

                    for (int i = 0; i < users.size(); i++) {
                        playerList.append(users.get(i).getUsername());
                        if (i < users.size() - 1) playerList.append(messages.get("moderation.lookup-ip.players-with-this-ip-separator"));
                    }

                    lookup.add(messages.getAndReplace("moderation.lookup-ip.players-with-this-ip", "players", playerList.toString()));
                }
                case "punishment-status" -> {
                    List<String> statusOrder = messages.getStringList("moderation.lookup-ip.punishment-status.order");

                    if (!statusOrder.isEmpty()) lookup.add(messages.get("moderation.lookup-ip.punishment-status.title"));

                    for (String statusRow : statusOrder) {
                        switch (statusRow) {
                            case "ban" -> {
                                List<Ban> activeBans = bans.stream()
                                        .filter(b -> b.getUnbanDate() == null)
                                        .toList();

                                if (activeBans.isEmpty()) {
                                    lookup.add(messages.get("moderation.lookup-ip.punishment-status.not-banned"));
                                } else {
                                    Optional<Ban> globalBanOpt = activeBans.stream()
                                            .filter(b -> b.getServer() == null)
                                            .findFirst();

                                    if (globalBanOpt.isPresent()) {
                                        lookup.add(messages.get("moderation.lookup-ip.punishment-status.banned-global"));
                                    } else {
                                        for (Ban ban : activeBans) {
                                            lookup.add(messages.getAndReplace("moderation.lookup-ip.punishment-status.banned-server", "server", ban.getServer()));
                                        }
                                    }
                                }
                            }
                            case "mute" -> {
                                List<Mute> activeMutes = mutes.stream()
                                        .filter(m -> m.getUnmuteDate() == null)
                                        .toList();

                                if (activeMutes.isEmpty()) {
                                    lookup.add(messages.get("moderation.lookup-ip.punishment-status.not-muted"));
                                } else {
                                    Optional<Mute> globalMuteOpt = activeMutes.stream()
                                            .filter(m -> m.getServer() == null)
                                            .findFirst();

                                    if (globalMuteOpt.isPresent()) {
                                        lookup.add(messages.get("moderation.lookup-ip.punishment-status.muted-global"));
                                    } else {
                                        for (Mute mute : activeMutes) {
                                            lookup.add(messages.getAndReplace("moderation.lookup-ip.punishment-status.muted-server", "server", mute.getServer()));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                case "history" -> {
                    List<String> historyOrder = messages.getStringList("moderation.lookup-ip.history.order");

                    if (!historyOrder.isEmpty()) lookup.add(messages.get("moderation.lookup-ip.history.title"));

                    for (String historyRow : historyOrder) {
                        switch (historyRow) {
                            case "bans" -> lookup.add(messages.getAndReplace("moderation.lookup-ip.history.bans", "amount", bans.size()));
                            case "kicks" -> lookup.add(messages.getAndReplace("moderation.lookup-ip.history.kicks", "amount", kicks.size()));
                            case "mutes" -> lookup.add(messages.getAndReplace("moderation.lookup-ip.history.mutes", "amount", mutes.size()));
                        }
                    }
                }
            }
        }

        Text.send(lookup, src);

        return COMMAND_SUCCESS;
    }

    private int punishmentLookup(CommandSource src, String ip, String punishment, int page) {
        if (!PUNISHMENTS.contains(punishment.toLowerCase())) {
            Text.send(messages.getAndReplace("commands.generic.invalid-argument", "argument", punishment), src);
            return COMMAND_FAILED;
        }

        List<String> lookup = new ArrayList<>();

        switch (punishment.toLowerCase()) {
            case "ban" -> {
                List<Ban> bans = plugin.banRepository().findAllActiveByIp(ip);
                if (bans.isEmpty()) {
                    Text.send(messages.get("commands.lookup.zero-bans"), src);
                    return COMMAND_SUCCESS;
                }

                int pages = (int) Math.ceil(bans.size() / 10.0);
                if (page > pages) page = pages;

                List<Ban> pageBans = bans.subList((page - 1) * 10, Math.min(page * 10, bans.size()));

                lookup.add(messages.getAndReplace("commands.lookup.header",
                        "player", ip,
                        "page", page,
                        "total_pages", pages
                ));

                for (Ban ban : pageBans) {
                    if (ban.getUnbanDate() == null) {
                        lookup.add(messages.getAndReplace("commands.punishment-lookup.ban-active",
                                "date", messages.formatDate(ban.getBeginDate()),
                                "staff", ban.getStaff() != null ? ban.getStaff().getUsername() : messages.get("console"),
                                "server", ban.getServer() == null ? messages.get("global") : ban.getServer(),
                                "duration", ban.getEndDate() == null ? messages.get("permanent") : ModerationUtils.getDurationString(ban.getBeginDate(), ban.getEndDate()),
                                "reason", ban.getReason()
                        ));
                    } else {
                        lookup.add(messages.getAndReplace("commands.punishment-lookup.ban-active",
                                new String[]{"date", "staff", "server", "duration", "reason", "revoked_by", "revoked_date", "revoked_reason"},
                                new Object[]{
                                        messages.formatDate(ban.getBeginDate()),
                                        ban.getStaff() != null ? ban.getStaff().getUsername() : messages.get("console"),
                                        ban.getServer() == null ? messages.get("global") : ban.getServer(),
                                        ban.getEndDate() == null ? messages.get("permanent") : ModerationUtils.getDurationString(ban.getBeginDate(), ban.getEndDate()),
                                        ban.getReason(),
                                        ban.getStaff() != null ? ban.getStaff().getUsername() : messages.get("console"),
                                        messages.formatDate(ban.getUnbanDate()),
                                        ban.getUnbanReason()
                                }
                        ));
                    }
                }

                lookup.add(messages.getAndReplace("commands.lookup.footer",
                        "player", ip,
                        "page", page,
                        "total_pages", pages
                ));
            }
            case "kick" -> {
                List<Kick> kicks = plugin.kickRepository().findAllByIp(ip);
                if (kicks.isEmpty()) {
                    Text.send(messages.get("commands.lookup.zero-kicks"), src);
                    return COMMAND_SUCCESS;
                }

                int pages = (int) Math.ceil(kicks.size() / 10.0);
                if (page > pages) page = pages;

                List<Kick> pageKicks = kicks.subList((page - 1) * 10, Math.min(page * 10, kicks.size()));

                lookup.add(messages.getAndReplace("commands.lookup.header",
                        "player", ip,
                        "page", page,
                        "total_pages", pages
                ));

                for (Kick kick : pageKicks) {
                    lookup.add(messages.getAndReplace("commands.punishment-lookup.kick",
                            "date", messages.formatDate(kick.getDate()),
                            "staff", kick.getStaff() != null ? kick.getStaff().getUsername() : messages.get("console"),
                            "reason", kick.getReason()
                    ));
                }

                lookup.add(messages.getAndReplace("commands.lookup.footer",
                        "player", ip,
                        "page", page,
                        "total_pages", pages
                ));
            }
            case "mute" -> {
                List<Mute> mutes = plugin.muteRepository().findAllActiveByIp(ip);
                if (mutes.isEmpty()) {
                    Text.send(messages.get("commands.lookup.zero-mutes"), src);
                    return COMMAND_SUCCESS;
                }

                int pages = (int) Math.ceil(mutes.size() / 10.0);
                if (page > pages) page = pages;

                List<Mute> pageMutes = mutes.subList((page - 1) * 10, Math.min(page * 10, mutes.size()));

                lookup.add(messages.getAndReplace("commands.lookup.header",
                        "player", ip,
                        "page", page,
                        "total_pages", pages
                ));

                for (Mute mute : pageMutes) {
                    if (mute.getUnmuteDate() == null) {
                        lookup.add(messages.getAndReplace("commands.punishment-lookup.mute-active",
                                "date", messages.formatDate(mute.getBeginDate()),
                                "staff", mute.getStaff() != null ? mute.getStaff().getUsername() : messages.get("console"),
                                "server", mute.getServer() == null ? messages.get("global") : mute.getServer(),
                                "duration", mute.getEndDate() == null ? messages.get("permanent") : ModerationUtils.getDurationString(mute.getBeginDate(), mute.getEndDate()),
                                "reason", mute.getReason()
                        ));
                    } else {
                        lookup.add(messages.getAndReplace("commands.punishment-lookup.mute-active",
                                new String[]{"date", "staff", "server", "duration", "reason", "revoked_by", "revoked_date", "revoked_reason"},
                                new Object[]{
                                        messages.formatDate(mute.getBeginDate()),
                                        mute.getStaff() != null ? mute.getStaff().getUsername() : messages.get("console"),
                                        mute.getServer() == null ? messages.get("global") : mute.getServer(),
                                        mute.getEndDate() == null ? messages.get("permanent") : ModerationUtils.getDurationString(mute.getBeginDate(), mute.getEndDate()),
                                        mute.getReason(),
                                        mute.getStaff() != null ? mute.getStaff().getUsername() : messages.get("console"),
                                        messages.formatDate(mute.getUnmuteDate()),
                                        mute.getUnmuteReason()
                                }
                        ));
                    }
                }

                lookup.add(messages.getAndReplace("commands.lookup.footer",
                        "player", ip,
                        "page", page,
                        "total_pages", pages
                ));
            }
        }

        Text.send(lookup, src);

        return COMMAND_SUCCESS;
    }
}
