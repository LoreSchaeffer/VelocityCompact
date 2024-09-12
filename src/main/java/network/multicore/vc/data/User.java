package network.multicore.vc.data;

import com.google.common.base.Preconditions;
import com.velocitypowered.api.proxy.Player;
import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Entity
public class User {
    @Id
    private UUID uuid;
    @Column(nullable = false)
    private String username;
    @Column(name = "first_login")
    private Date firstLogin;
    @Column(name = "last_login")
    private Date lastLogin;
    private String ip;
    @Column(name = "protocol_version")
    private int protocolVersion;
    @Column(name = "client_brand")
    private String clientBrand;
    @ElementCollection
    @CollectionTable(name = "username_history", joinColumns = @JoinColumn(name = "user_uuid"))
    @Column(name = "username_history")
    private List<String> usernameHistory;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private UserSettings settings;

    public User(@NotNull Player player) {
        Preconditions.checkNotNull(player, "player");

        this.uuid = player.getUniqueId();
        this.username = player.getUsername();
        this.firstLogin = new Date();
        this.lastLogin = this.firstLogin;
        this.ip = player.getRemoteAddress().getHostString();
        this.protocolVersion = player.getProtocolVersion().getProtocol();
        this.clientBrand = player.getClientBrand();
        this.settings = new UserSettings(this);
    }

    protected User() {
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public User setUsername(@NotNull String username) {
        Preconditions.checkNotNull(username, "username");

        if (usernameHistory == null) usernameHistory = new ArrayList<>();
        usernameHistory.add(this.username);
        this.username = username;
        return this;
    }

    public Date getFirstLogin() {
        return firstLogin;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public User setLastLogin(@NotNull Date lastLogin) {
        Preconditions.checkNotNull(lastLogin, "lastLogin");

        this.lastLogin = lastLogin;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public User setIp(@NotNull String ip) {
        Preconditions.checkNotNull(ip, "ip");

        this.ip = ip;
        return this;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public User setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
        return this;
    }

    public String getClientBrand() {
        return clientBrand;
    }

    public User setClientBrand(String clientBrand) {
        this.clientBrand = clientBrand != null ? clientBrand : "unknown";
        return this;
    }

    public List<String> getUsernameHistory() {
        return Collections.unmodifiableList(usernameHistory);
    }

    public UserSettings getSettings() {
        return settings;
    }

    public User setSettings(@NotNull UserSettings settings) {
        Preconditions.checkNotNull(settings, "settings");

        if (settings.getUser().getUniqueId() != this.getUniqueId()) throw new IllegalArgumentException("Settings user UUID does not match user UUID");
        if (this.settings != null) this.settings.update(settings);
        else this.settings = settings;
        return this;
    }
}
