package network.multicore.vc.utils;

public enum Permission {
    COLORS("colors"),
    COMMAND_BLOCKER_BYPASS("command-blocker.bypass"),
    COMMAND_WARNING_BYPASS("command-warning.bypass"),
    CHAT_CENSORSHIP_BYPASS("chat-censorship.bypass"),
    COMMAND_WARNING_RECEIVE("command-blocker.receive"),
    JOIN_ATTEMPT_RECEIVE_NICKNAME("join-attempt-failed.receive.nickname"),
    JOIN_ATTEMPT_RECEIVE_IP_LIMITER("join-attempt-failed.receive.ip-limiter"),
    JOIN_ATTEMPT_RECEIVE_BAN("join-attempt-failed.receive.ban"),
    MUTED_MESSAGE_RECEIVE("muted-message.receive"),
    SAME_IP_BROADCAST("same-ip-broadcast"),
    SAME_IP_BROADCAST_BYPASS("same-ip-broadcast.bypass"),
    BROADCAST("broadcast"),
    COMMANDSPY("commandspy"),
    COMMANDSPY_OTHER("commandspy.other"),
    SOCIALSPY("socialspy"),
    SOCIALSPY_OTHER("socialspy.other"),
    GLOBALCHAT("globalchat"),
    GLOBALCHAT_OTHER("globalchat.other"),
    GBROADCAST("gbroadcast"),
    HUB("hub"),
    HUB_OTHER("hub.other"),
    MESSAGE("message"),
    BAN("ban"),
    BAN_PERMANENT("ban.permanent"),
    BAN_IP("ban.ip"),
    GBAN("gban"),
    GBAN_PERMANENT("gban.permanent"),
    GBAN_IP("gban.ip"),
    UNBAN("unban"),
    UNBAN_IP("unban.ip"),
    GUNBAN("gunban"),
    GUNBAN_IP("gunban.ip"),
    KICK("kick"),
    KICK_IP("kick.ip"),
    GKICK("gkick"),
    GKICK_IP("gkick.ip"),
    MUTE("mute"),
    MUTE_PERMANENT("mute.permanent"),
    MUTE_IP("mute.ip"),
    GMUTE("gmute"),
    GMUTE_PERMANENT("gmute.permanent"),
    GMUTE_IP("gmute.ip"),
    UNMUTE("unmute"),
    UNMUTE_IP("unmute.ip"),
    GUNMUTE("gunmute"),
    GUNMUTE_IP("gunmute.ip"),
    WARN("warn"),
    LOOKUP("lookup"),
    LOOKUP_IP("lookup.ip"),
    PUNISHMENT_RECEIVE_BAN("punishment.receive.ban"),
    PUNISHMENT_RECEIVE_KICK("punishment.receive.kick"),
    PUNISHMENT_RECEIVE_MUTE("punishment.receive.mute"),
    PUNISHMENT_RECEIVE_WARN("punishment.receive.warn"),
    PUNISHMENT_RECEIVE_UNBAN("punishment.receive.unban"),
    PUNISHMENT_RECEIVE_UNMUTE("punishment.receive.unmute"),
    PUNISHMENT_RECEIVE_SILENT("punishment.receive.silent")
    ;

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public String get() {
        return "vcompact." + permission;
    }
}
