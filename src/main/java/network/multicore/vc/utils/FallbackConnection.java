package network.multicore.vc.utils;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import network.multicore.vc.VelocityCompact;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FallbackConnection {
    private static final ConnectionRequestBuilder.Result CONNECTION_CANCELLED = new ConnectionRequestBuilder.Result() {
        @Override
        public ConnectionRequestBuilder.Status getStatus() {
            return ConnectionRequestBuilder.Status.CONNECTION_CANCELLED;
        }

        @Override
        public Optional<Component> getReasonComponent() {
            return Optional.empty();
        }

        @Override
        public RegisteredServer getAttemptedConnection() {
            return null;
        }
    };
    private final ProxyServer proxy;
    private final Player player;
    private final List<RegisteredServer> serversToTry;

    public FallbackConnection(Player player) {
        this.player = player;
        this.proxy = VelocityCompact.getInstance().proxy();
        this.serversToTry = proxy.getConfiguration()
                .getAttemptConnectionOrder()
                .stream()
                .filter(serverName -> proxy.getServer(serverName).isPresent())
                .map(serverName -> proxy.getServer(serverName).get())
                .toList();
    }

    public CompletableFuture<ConnectionRequestBuilder.Result> connect() {
        if (serversToTry.isEmpty()) return CompletableFuture.completedFuture(CONNECTION_CANCELLED);

        CompletableFuture<ConnectionRequestBuilder.Result> result = new CompletableFuture<>();
        connectToNextServer(0, result);
        return result;
    }

    private void connectToNextServer(int index, CompletableFuture<ConnectionRequestBuilder.Result> result) {
        if (index >= serversToTry.size()) {
            result.complete(CONNECTION_CANCELLED);
            return;
        }

        RegisteredServer server = serversToTry.get(index);
        if (server.getServerInfo().getName().equalsIgnoreCase(player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse(null))) {
            connectToNextServer(index + 1, result);
            return;
        }

        player.createConnectionRequest(server)
                .connect()
                .whenComplete((connectionResult, throwable) -> {
                    if (throwable != null || !connectionResult.isSuccessful()) connectToNextServer(index + 1, result);
                    else result.complete(connectionResult);
                });
    }
}
