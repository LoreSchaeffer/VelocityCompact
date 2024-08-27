package network.multicore.vc.utils;

public enum Permission {
    COLORS("colors"),
    COMMAND_BLOCKER_BYPASS("command-blocker.bypass"),
    COMMAND_WARNING_BYPASS("command-warning.bypass"),
    CHAT_CENSORSHIP_BYPASS("chat-censorship.bypass"),
    COMMAND_WARNING_RECEIVE("command-blocker.receive"),
    COMMANDSPY("commandspy"),
    COMMANDSPY_OTHER("commandspy.other"),
    SOCIALSPY("socialspy"),
    SOCIALSPY_OTHER("socialspy.other"),
    GLOBALCHAT("globalchat"),
    GLOBALCHAT_OTHER("globalchat.other"),
    JOIN_ATTEMPT_RECEIVE_NICKNAME("join-attempt-failed.receive.nickname"),
    JOIN_ATTEMPT_RECEIVE_IP_LIMITER("join-attempt-failed.receive.ip-limiter"),
    JOIN_ATTEMPT_RECEIVE_BAN("join-attempt-failed.receive.ban"),
    BROADCAST("broadcast"),
    GBROADCAST("gbroadcast"),
    HUB("hub"),
    HUB_OTHER("hub.other"),
    MESSAGE("message");

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public String get() {
        return "vcompact." + permission;
    }
}
