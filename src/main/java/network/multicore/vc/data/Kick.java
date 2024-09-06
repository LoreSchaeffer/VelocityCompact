package network.multicore.vc.data;

import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;

@Entity
public class Kick {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private UUID uuid;
    private String ip;
    @ManyToOne
    @JoinColumn(name = "staff", referencedColumnName = "uuid")
    private User staff;
    private String reason;
    private String server;
    @Column(nullable = false)
    private Date date;

    public Kick(UUID uuid, String ip, User staff, String reason, String server) {
        this.uuid = uuid;
        this.ip = ip;
        this.staff = staff;
        this.reason = reason;
        this.server = server;
        this.date = new Date();
    }

    public Kick(@NotNull User user, User staff, String reason, String server) {
        this(user.getUniqueId(), null, staff, reason, server);
    }

    public Kick(@NotNull String ip, User staff, String reason, String server) {
        this(null, ip, staff, reason, server);
    }

    protected Kick() {
    }

    public long getId() {
        return id;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    @Nullable
    public String getIp() {
        return ip;
    }

    @Nullable
    public User getStaff() {
        return staff;
    }

    @Nullable
    public String getReason() {
        return reason;
    }

    @Nullable
    public String getServer() {
        return server;
    }

    public Date getDate() {
        return date;
    }
}
