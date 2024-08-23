package network.multicore.vc.data;

import com.google.common.base.Preconditions;
import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

@Entity
public class Kick {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "uuid", referencedColumnName = "uuid", nullable = false)
    private User user;
    private String ip;
    @ManyToOne
    @JoinColumn(name = "staff", referencedColumnName = "uuid")
    private User staff;
    private String reason;
    private String server;
    @Column(nullable = false)
    private Date date;

    public Kick(@NotNull User user, User staff, String reason, String server) {
        Preconditions.checkNotNull(user, "user");

        this.user = user;
        this.staff = staff;
        this.reason = reason;
        this.server = server;
        this.date = new Date();
    }

    public Kick(@NotNull User user, @NotNull String ip, User staff, String reason, String server) {
        Preconditions.checkNotNull(user, "user");
        Preconditions.checkNotNull(ip, "ip");

        this.user = user;
        this.ip = ip;
        this.staff = staff;
        this.reason = reason;
        this.server = server;
        this.date = new Date();
    }

    protected Kick() {
    }

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
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
