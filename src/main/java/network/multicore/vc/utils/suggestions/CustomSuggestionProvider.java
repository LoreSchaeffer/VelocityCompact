package network.multicore.vc.utils.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CustomSuggestionProvider<S> implements SuggestionProvider<S> {
    private final String argument;
    private final Set<String> suggestions;

    public CustomSuggestionProvider(String argument, String... suggestions) {
        this.argument = argument;
        this.suggestions = Set.of(suggestions);
    }

    public CustomSuggestionProvider(String argument, Collection<String> suggestions) {
        this.argument = argument;
        this.suggestions = Set.copyOf(suggestions);
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
        String arg = ctx.getArguments().containsKey(argument) ? ctx.getArgument(argument, String.class) : "";

        suggestions.stream()
                .filter(suggestion -> suggestion.regionMatches(true, 0, arg, 0, arg.length()))
                .forEach(builder::suggest);

        return builder.buildFuture();
    }
}
