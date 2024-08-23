package network.multicore.vc.data;

import com.google.common.base.Preconditions;
import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

@Entity
public class Mute {
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
    private Date begin;
    private Date end; // TODO date should be nullable
    @Column(name = "unmute_date")
    private Date unmuteDate;
    @ManyToOne
    @JoinColumn(name = "unmute_staff", referencedColumnName = "uuid")
    private User unmuteStaff;
    @Column(name = "unmute_reason")
    private String unmuteReason;

    public Mute(@NotNull User user, User staff, String reason, String server, Date end) {
        Preconditions.checkNotNull(user, "user");

        this.user = user;
        this.staff = staff;
        this.reason = reason;
        this.server = server;
        this.begin = new Date();
        this.end = end;
    }

    public Mute(@NotNull User user, @NotNull String ip, User staff, String reason, String server, Date end) {
        Preconditions.checkNotNull(user, "user");
        Preconditions.checkNotNull(ip, "ip");

        this.user = user;
        this.ip = ip;
        this.staff = staff;
        this.reason = reason;
        this.server = server;
        this.begin = new Date();
        this.end = end;
    }

    protected Mute() {
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

    public Date getBeginDate() {
        return begin;
    }

    @Nullable
    public Date getEndDate() {
        return end;
    }

    @Nullable
    public Date getUnmuteDate() {
        return unmuteDate;
    }

    public Mute setUnmuteDate() {
        this.unmuteDate = new Date();
        return this;
    }

    @Nullable
    public User getUnmuteStaff() {
        return unmuteStaff;
    }

    public Mute setUnmuteStaff(User unmuteStaff) {
        this.unmuteStaff = unmuteStaff;
        return this;
    }

    @Nullable
    public String getUnmuteReason() {
        return unmuteReason;
    }

    public Mute setUnmuteReason(String unmuteReason) {
        this.unmuteReason = unmuteReason;
        return this;
    }
}
