package network.multicore.vc.utils;

public enum Permission {
    HUB("hub"),
    HUB_OTHER("hub.other");

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public String get() {
        return "vcompact." + permission;
    }
}
