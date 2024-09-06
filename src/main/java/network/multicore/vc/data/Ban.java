package network.multicore.vc.data;

import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;

@Entity
public class Ban {
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
    private Date endDate; // TODO Verify - date should be nullable
    @Column(name = "unban_date")
    private Date unbanDate;
    @ManyToOne
    @JoinColumn(name = "unban_staff", referencedColumnName = "uuid")
    private User unbanStaff;
    @Column(name = "unban_reason")
    private String unbanReason;

    public Ban(UUID uuid, String username, String ip, User staff, String reason, String server, Date endDate) {
        this.uuid = uuid;
        this.username = username;
        this.ip = ip;
        this.staff = staff;
        this.reason = reason;
        this.server = server;
        this.beginDate = new Date();
        this.endDate = endDate;
    }

    public Ban(@NotNull User user, User staff, String reason, String server, Date endDate) {
        this(user.getUniqueId(), user.getUsername(), null, staff, reason, server, endDate);
    }

    public Ban(@NotNull String ip, User staff, String reason, String server, Date endDate) {
        this(null, null, ip, staff, reason, server, endDate);
    }

    protected Ban() {
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
    public Date getUnbanDate() {
        return unbanDate;
    }

    public Ban setUnbanDate() {
        this.unbanDate = new Date();
        return this;
    }

    @Nullable
    public User getUnbanStaff() {
        return unbanStaff;
    }

    public Ban setUnbanStaff(User unbanStaff) {
        this.unbanStaff = unbanStaff;
        return this;
    }

    @Nullable
    public String getUnbanReason() {
        return unbanReason;
    }

    public Ban setUnbanReason(String unbanReason) {
        this.unbanReason = unbanReason;
        return this;
    }
}
