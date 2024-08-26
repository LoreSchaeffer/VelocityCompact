package network.multicore.vc.utils;

public enum Permission {
    COLORS("colors"),
    COMMAND_BLOCKER_BYPASS("command-blocker.bypass"),
    COMMAND_WARNING_BYPASS("command-warning.bypass"),
    COMMAND_WARNING_RECEIVE("command-blocker.receive"),
    COMMANDSPY_RECEIVE("commandspy.receive"),
    JOIN_ATTEMPT_RECEIVE("join-attempt-failed.receive"),
    BROADCAST("broadcast"),
    GBROADCAST("gbroadcast"),
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
