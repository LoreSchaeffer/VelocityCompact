package network.multicore.vc.utils.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import network.multicore.vc.VelocityCompact;
import network.multicore.vc.data.Ban;
import network.multicore.vc.data.Kick;
import network.multicore.vc.data.Mute;
import network.multicore.vc.data.User;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class IpSuggestionProvider<S> implements SuggestionProvider<S> {
    private final VelocityCompact plugin;
    private final String argument;

    public IpSuggestionProvider(VelocityCompact plugin, String argument) {
        this.plugin = plugin;
        this.argument = argument;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
        String arg = ctx.getArguments().containsKey(argument) ? ctx.getArgument(argument, String.class) : "";

        plugin.userRepository()
                .findAll()
                .stream()
                .map(User::getIp)
                .filter(ip -> ip.regionMatches(true, 0, arg, 0, arg.length()))
                .forEach(builder::suggest);

        plugin.banRepository()
                .findAll()
                .stream()
                .map(Ban::getIp)
                .filter(ip -> Objects.nonNull(ip) && ip.regionMatches(true, 0, arg, 0, arg.length()))
                .forEach(builder::suggest);

        plugin.kickRepository()
                .findAll()
                .stream()
                .map(Kick::getIp)
                .filter(ip -> Objects.nonNull(ip) && ip.regionMatches(true, 0, arg, 0, arg.length()))
                .forEach(builder::suggest);

        plugin.muteRepository()
                .findAll()
                .stream()
                .map(Mute::getIp)
                .filter(ip -> Objects.nonNull(ip) && ip.regionMatches(true, 0, arg, 0, arg.length()))
                .forEach(builder::suggest);

        return builder.buildFuture();
    }
}
