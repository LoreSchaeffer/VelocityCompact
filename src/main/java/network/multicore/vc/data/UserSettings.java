package network.multicore.vc.data;

import com.google.common.base.Preconditions;
import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Entity
public class UserSettings {
    @Id
    @Column(name = "uuid")
    private UUID uuid;
    @OneToOne
    @MapsId
    @JoinColumn(name = "uuid")
    private User user;
    @Column(columnDefinition = "boolean default false")
    private boolean commandspy;
    @Column(columnDefinition = "boolean default false")
    private boolean globalchat;
    @Column(columnDefinition = "boolean default false")
    private boolean socialspy;
    @Column(columnDefinition = "boolean default false")
    private boolean staffchat;

    public UserSettings(@NotNull User user) {
        Preconditions.checkNotNull(user, "user");

        this.uuid = user.getUniqueId();
        this.user = user;
        this.commandspy = false;
        this.globalchat = false;
        this.socialspy = false;
        this.staffchat = false;
    }

    protected UserSettings() {
    }

    public void update(UserSettings settings) {
        this.commandspy = settings.commandspy;
        this.globalchat = settings.globalchat;
        this.socialspy = settings.socialspy;
        this.staffchat = settings.staffchat;
    }

    public User getUser() {
        return user;
    }

    public boolean hasCommandspy() {
        return commandspy;
    }

    public UserSettings setCommandspy(boolean commandspy) {
        this.commandspy = commandspy;
        return this;
    }

    public boolean hasGlobalchat() {
        return globalchat;
    }

    public UserSettings setGlobalchat(boolean globalchat) {
        this.globalchat = globalchat;
        return this;
    }

    public boolean hasSocialspy() {
        return socialspy;
    }

    public UserSettings setSocialspy(boolean socialspy) {
        this.socialspy = socialspy;
        return this;
    }

    public boolean hasStaffchat() {
        return staffchat;
    }

    public UserSettings setStaffchat(boolean staffchat) {
        this.staffchat = staffchat;
        return this;
    }
}
