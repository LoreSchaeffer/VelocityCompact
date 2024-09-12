package network.multicore.vc.data;

import com.google.common.base.Preconditions;
import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;

@Entity
public class Mute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private UUID uuid;
    private String username;
    private String ip;
    @ManyToOne
    @JoinColumn(name = "staff", referencedColumnName = "uuid")
    private User staff;
    private String reason;
    private String server;
    @Column(nullable = false)
    private Date beginDate;
    private Date endDate; // TODO date should be nullable
    @Column(name = "unmute_date")
    private Date unmuteDate;
    @ManyToOne
    @JoinColumn(name = "unmute_staff", referencedColumnName = "uuid")
    private User unmuteStaff;
    @Column(name = "unmute_reason")
    private String unmuteReason;

    public Mute(UUID uuid, String username, String ip, User staff, String reason, String server, Date endDate) {
        this.uuid = uuid;
        this.username = username;
        this.ip = ip;
        this.staff = staff;
        this.reason = reason;
        this.server = server;
        this.beginDate = new Date();
        this.endDate = endDate;
    }

    public Mute(@NotNull User user, User staff, String reason, String server, Date endDate) {
        this(user.getUniqueId(), user.getUsername(), null, staff, reason, server, endDate);
    }

    public Mute(@NotNull String ip, User staff, String reason, String server, Date endDate) {
        this(null, null, ip, staff, reason, server, endDate);
    }


    protected Mute() {
    }

    public long getId() {
        return id;
    }

    @Nullable
    public UUID getUniqueId() {
        return uuid;
    }

    @Nullable
    public String getUsername() {
        return username;
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
        return beginDate;
    }

    @Nullable
    public Date getEndDate() {
        return endDate;
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
