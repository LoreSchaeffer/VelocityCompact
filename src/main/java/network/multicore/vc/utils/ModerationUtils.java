package network.multicore.vc.utils;

import com.google.common.base.Preconditions;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import network.multicore.vc.VelocityCompact;
import network.multicore.vc.data.Ban;
import network.multicore.vc.data.BanRepository;
import network.multicore.vc.data.Mute;
import network.multicore.vc.data.MuteRepository;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ModerationUtils {

    private ModerationUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isExpired(Date endDate) {
        if (endDate == null) return false;
        Date now = new Date();
        return endDate.before(now) || endDate.equals(now);
    }

    public static String getDurationString(@NotNull Date startDate, @NotNull Date endDate) {
        Preconditions.checkNotNull(endDate, "endDate");

        long seconds = (endDate.getTime() - startDate.getTime()) / 1000;

        long years = seconds / 31536000;
        seconds -= years * 31536000;

        long days = TimeUnit.DAYS.convert(seconds, TimeUnit.SECONDS);
        seconds -= TimeUnit.SECONDS.convert(days, TimeUnit.DAYS);

        long hours = TimeUnit.HOURS.convert(seconds, TimeUnit.SECONDS);
        seconds -= TimeUnit.SECONDS.convert(hours, TimeUnit.HOURS);

        long minutes = TimeUnit.MINUTES.convert(seconds, TimeUnit.SECONDS);
        seconds -= TimeUnit.SECONDS.convert(minutes, TimeUnit.MINUTES);

        Messages messages = Messages.get();

        return (years > 0 ? years + messages.get("time.year.short") + " " : "") +
                (days > 0 ? days + messages.get("time.day.short") + " " : "") +
                (hours > 0 ? hours + messages.get("time.hour.short") + " " : "") +
                (minutes > 0 ? minutes + messages.get("time.minute.short") + " " : "") +
                (seconds > 0 ? seconds + messages.get("time.second.short") : "");
    }

    public static String getDurationString(@NotNull Date endDate) {
        return getDurationString(new Date(), endDate);
    }

    public static Date parseDurationString(@NotNull String time, boolean before) throws IllegalArgumentException {
        Preconditions.checkNotNull(time, "time");

        Messages messages = Messages.get();
        Map<String, String> validValues = Map.of(
                "s", messages.get("time.second.short"),
                "m", messages.get("time.minute.short"),
                "h", messages.get("time.hour.short"),
                "d", messages.get("time.day.short"),
                "y", messages.get("time.year.short")
        );

        String[] split = time.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        int number;
        String timeUnit;

        if (split.length < 2) throw new IllegalArgumentException("Invalid time format");

        try {
            number = Integer.parseInt(split[0]);
            if (number < 1) throw new NumberFormatException("Invalid number");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number");
        }

        timeUnit = split[1].toLowerCase();

        if (!validValues.containsValue(timeUnit)) throw new IllegalArgumentException("Invalid time unit");

        long now = new Date().getTime();

        if (before) {
            if (timeUnit.equals(validValues.get("s"))) return new Date(now - TimeUnit.MILLISECONDS.convert(number, TimeUnit.SECONDS));
            if (timeUnit.equals(validValues.get("m"))) return new Date(now - TimeUnit.MILLISECONDS.convert(number, TimeUnit.MINUTES));
            if (timeUnit.equals(validValues.get("h"))) return new Date(now - TimeUnit.MILLISECONDS.convert(number, TimeUnit.HOURS));
            if (timeUnit.equals(validValues.get("d"))) return new Date(now - TimeUnit.MILLISECONDS.convert(number, TimeUnit.DAYS));
            if (timeUnit.equals(validValues.get("y"))) return new Date(now - (365 * TimeUnit.MILLISECONDS.convert(number, TimeUnit.DAYS)));
            else throw new IllegalStateException("Invalid time unit");
        } else {
            if (timeUnit.equals(validValues.get("s"))) return new Date(now + TimeUnit.MILLISECONDS.convert(number, TimeUnit.SECONDS));
            if (timeUnit.equals(validValues.get("m"))) return new Date(now + TimeUnit.MILLISECONDS.convert(number, TimeUnit.MINUTES));
            if (timeUnit.equals(validValues.get("h"))) return new Date(now + TimeUnit.MILLISECONDS.convert(number, TimeUnit.HOURS));
            if (timeUnit.equals(validValues.get("d"))) return new Date(now + TimeUnit.MILLISECONDS.convert(number, TimeUnit.DAYS));
            if (timeUnit.equals(validValues.get("y"))) return new Date(now + (365 * TimeUnit.MILLISECONDS.convert(number, TimeUnit.DAYS)));
            else throw new IllegalStateException("Invalid time unit");
        }
    }

    public static Date parseDurationString(@NotNull String time) throws IllegalArgumentException {
        return parseDurationString(time, false);
    }

    public static void removeExpiredBans(List<Ban> activeBans) {
        BanRepository banRepository = VelocityCompact.getInstance().banRepository();

        Iterator<Ban> iterator = activeBans.iterator();
        while (iterator.hasNext()) {
            Ban ban = iterator.next();

            if (isExpired(ban.getEndDate())) {
                ban.setUnbanDate();
                banRepository.save(ban);
                iterator.remove();
            }
        }
    }

    public static void removeExpiredMutes(List<Mute> activeMutes) {
        MuteRepository muteRepository = VelocityCompact.getInstance().muteRepository();

        Iterator<Mute> iterator = activeMutes.iterator();
        while (iterator.hasNext()) {
            Mute mute = iterator.next();

            if (isExpired(mute.getEndDate())) {
                mute.setUnmuteDate();
                muteRepository.save(mute);
                iterator.remove();
            }
        }
    }

    public static void broadcast(String player, CommandSource staff, RegisteredServer server, Date end, String reason, boolean silent, boolean console, Permission receivePermission, String punishmentName) {
        ProxyServer proxy = VelocityCompact.getInstance().proxy();
        Messages messages = Messages.get();
        YamlDocument config = VelocityCompact.getInstance().config();

        String broadcast = messages.getAndReplace("moderation.broadcast." + punishmentName,
                "player", player,
                "staff", console ? messages.get("console") : staff,
                "server", server != null ? server.getServerInfo().getName() : messages.get("global"),
                "duration", end != null ? ModerationUtils.getDurationString(end) : messages.get("permanent"),
                "reason", reason != null ? reason : messages.get("no-reason")
        );

        if (server != null && config.getBoolean("moderation.broadcast-server-punishment-only-in-server", false)) {
            if (!silent) {
                server.getPlayersConnected()
                        .stream()
                        .filter(p -> p.hasPermission(receivePermission.get()) && !p.getUsername().equalsIgnoreCase(player))
                        .forEach(p -> Text.send(broadcast, p));
            } else {
                server.getPlayersConnected()
                        .stream()
                        .filter(p -> p.hasPermission(Permission.PUNISHMENT_RECEIVE_SILENT.get()) &&
                                p.hasPermission(Permission.PUNISHMENT_RECEIVE_SILENT.get()) &&
                                !p.getUsername().equalsIgnoreCase(player))
                        .forEach(p -> Text.send(broadcast, p));
            }
        } else {
            if (!silent) {
                proxy.getAllPlayers()
                        .stream()
                        .filter(p -> p.hasPermission(receivePermission.get()) && !p.getUsername().equalsIgnoreCase(player))
                        .forEach(p -> Text.send(broadcast, p));
            } else {
                proxy.getAllPlayers()
                        .stream()
                        .filter(p -> p.hasPermission(receivePermission.get()) &&
                                p.hasPermission(Permission.PUNISHMENT_RECEIVE_SILENT.get()) &&
                                !p.getUsername().equalsIgnoreCase(player))
                        .forEach(p -> Text.send(broadcast, p));
            }
        }
    }
}
