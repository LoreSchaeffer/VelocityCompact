package network.multicore.vc.data;

import com.google.common.base.Preconditions;
import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

@Entity
public class Ban {
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
    @Column(name = "unban_date")
    private Date unbanDate;
    @ManyToOne
    @JoinColumn(name = "unban_staff", referencedColumnName = "uuid")
    private User unbanStaff;
    @Column(name = "unban_reason")
    private String unbanReason;

    public Ban(@NotNull User user, User staff, String reason, String server, Date end) {
        Preconditions.checkNotNull(user, "user");

        this.user = user;
        this.staff = staff;
        this.reason = reason;
        this.server = server;
        this.begin = new Date();
        this.end = end;
    }

    public Ban(@NotNull User user, @NotNull String ip, User staff, String reason, String server, Date end) {
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

    protected Ban() {
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
