package network.multicore.vc.data;

import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;

@Entity
public class Warn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private UUID uuid;
    private String username;
    @ManyToOne
    @JoinColumn(name = "staff", referencedColumnName = "uuid")
    private User staff;
    private String reason;
    @Column(nullable = false)
    private Date date;

    public Warn(@NotNull User user, User staff, String reason) {
        this.uuid = user.getUniqueId();
        this.username = user.getUsername();
        this.staff = staff;
        this.reason = reason;
        this.date = new Date();
    }

    protected Warn() {
    }

    public long getId() {
        return id;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    @Nullable
    public User getStaff() {
        return staff;
    }

    @Nullable
    public String getReason() {
        return reason;
    }

    public Date getDate() {
        return date;
    }
}
