package network.multicore.vc.data;

import com.google.common.base.Preconditions;
import com.velocitypowered.api.proxy.Player;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.UUID;

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

    public User(@NotNull Player player) {
        Preconditions.checkNotNull(player, "player");

        this.uuid = player.getUniqueId();
        this.username = player.getUsername();
        this.firstLogin = new Date();
        this.lastLogin = this.firstLogin;
        this.ip = player.getRemoteAddress().getAddress().getHostAddress();
        this.protocolVersion = player.getProtocolVersion().getProtocol();
    }

    protected User() {
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public Date getFirstLogin() {
        return firstLogin;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public User setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public User setIp(String ip) {
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
}
