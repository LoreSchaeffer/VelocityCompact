package network.multicore.vc.utils.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ServerSuggestionProvider<S> implements SuggestionProvider<S> {
    private final ProxyServer proxy;
    private final String argument;
    private final Set<String> additionalSuggestions;

    public ServerSuggestionProvider(ProxyServer proxy, String argument, String... additionalSuggestions) {
        this.proxy = proxy;
        this.argument = argument;
        this.additionalSuggestions = Set.of(additionalSuggestions);
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
        String arg = ctx.getArguments().containsKey(argument) ? ctx.getArgument(argument, String.class) : "";

        proxy.getAllServers()
                .stream()
                .map(server -> server.getServerInfo().getName())
                .filter(serverName -> serverName.regionMatches(true, 0, arg, 0, arg.length()))
                .forEach(builder::suggest);

        additionalSuggestions.stream()
                .filter(suggestion -> suggestion.regionMatches(true, 0, arg, 0, arg.length()))
                .forEach(builder::suggest);

        return builder.buildFuture();
    }
}
