package network.multicore.vc.utils;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
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

    public static String getDurationString(@NotNull Date startDate, @NotNull Date endDate, @NotNull Messages messages) {
        Preconditions.checkNotNull(endDate, "endDate");
        Preconditions.checkNotNull(messages, "messages");

        long seconds = (endDate.getTime() - startDate.getTime()) / 1000;

        long years = seconds / 31536000;
        seconds -= years * 31536000;

        long days = TimeUnit.DAYS.convert(seconds, TimeUnit.SECONDS);
        seconds -= TimeUnit.SECONDS.convert(days, TimeUnit.DAYS);

        long hours = TimeUnit.HOURS.convert(seconds, TimeUnit.SECONDS);
        seconds -= TimeUnit.SECONDS.convert(hours, TimeUnit.HOURS);

        long minutes = TimeUnit.MINUTES.convert(seconds, TimeUnit.SECONDS);
        seconds -= TimeUnit.SECONDS.convert(minutes, TimeUnit.MINUTES);

        return (years > 0 ? years + messages.get("time.year.short") + " " : "") +
                (days > 0 ? days + messages.get("time.day.short") + " " : "") +
                (hours > 0 ? hours + messages.get("time.hour.short") + " " : "") +
                (minutes > 0 ? minutes + messages.get("time.minute.short") + " " : "") +
                (seconds > 0 ? seconds + messages.get("time.second.short") : "");
    }

    public static String getDurationString(@NotNull Date endDate, @NotNull Messages messages) {
        return getDurationString(new Date(), endDate, messages);
    }
}
