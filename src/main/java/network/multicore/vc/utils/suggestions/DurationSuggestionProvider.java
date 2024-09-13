package network.multicore.vc.utils.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import network.multicore.vc.utils.Messages;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DurationSuggestionProvider<S> implements SuggestionProvider<S> {
    private final String argument;
    private final Set<String> suggestions = new HashSet<>();

    public DurationSuggestionProvider(String argument) {
        this.argument = argument;

        Messages messages = Messages.get();

        String seconds = messages.get("time.second.short");
        String minutes = messages.get("time.minute.short");
        String hours = messages.get("time.hour.short");
        String days = messages.get("time.day.short");
        String years = messages.get("time.year.short");

        if (seconds == null || seconds.isBlank()) suggestions.add("s");
        else suggestions.add(seconds);
        if (minutes == null || minutes.isBlank()) suggestions.add("m");
        else suggestions.add(minutes);
        if (hours == null || hours.isBlank()) suggestions.add("h");
        else suggestions.add(hours);
        if (days == null || days.isBlank()) suggestions.add("d");
        else suggestions.add(days);
        if (years == null || years.isBlank()) suggestions.add("y");
        else suggestions.add(years);
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
        String arg = ctx.getArguments().containsKey(argument) ? ctx.getArgument(argument, String.class) : "";

        if (arg.isBlank()) {
            for (int i = 1; i < 10; i++) builder.suggest(String.valueOf(i));
        } else {
            try {
                int val = Integer.parseInt(arg);

                String unit = arg.replace(String.valueOf(val), "");

                suggestions.stream()
                        .filter(suggestion -> suggestion.regionMatches(true, 0, unit, 0, unit.length()))
                        .forEach(s -> builder.suggest(val + s));

                for (int i = 0; i < 10; i++) builder.suggest(String.valueOf(i));
            } catch (NumberFormatException ignored) {
            }
        }

        return builder.buildFuture();
    }
}
