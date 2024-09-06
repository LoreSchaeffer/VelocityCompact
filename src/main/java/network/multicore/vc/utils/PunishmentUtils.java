package network.multicore.vc.utils;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PunishmentUtils {

    private PunishmentUtils() {
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

    public static Date parseDurationString(@NotNull String time) throws IllegalArgumentException {
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

        if (timeUnit.equals(validValues.get("s"))) return new Date(now + TimeUnit.MILLISECONDS.convert(number, TimeUnit.SECONDS));
        if (timeUnit.equals(validValues.get("m"))) return new Date(now + TimeUnit.MILLISECONDS.convert(number, TimeUnit.MINUTES));
        if (timeUnit.equals(validValues.get("h"))) return new Date(now + TimeUnit.MILLISECONDS.convert(number, TimeUnit.HOURS));
        if (timeUnit.equals(validValues.get("d"))) return new Date(now + TimeUnit.MILLISECONDS.convert(number, TimeUnit.DAYS));
        if (timeUnit.equals(validValues.get("y"))) return new Date(now + (365 * TimeUnit.MILLISECONDS.convert(number, TimeUnit.DAYS)));
        else throw new IllegalStateException("Invalid time unit");
    }
}
