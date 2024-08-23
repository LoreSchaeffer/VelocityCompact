package network.multicore.vc.messages;

import com.google.gson.annotations.SerializedName;
import com.velocitypowered.api.command.CommandSource;
import network.multicore.vc.utils.Text;
import network.multicore.vc.utils.json.JsonConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MessagesOld extends JsonConfig {
    @SerializedName("already_in_server")
    public String alreadyInServer;
    @SerializedName("already_in_server_other")
    public String alreadyInServerOther;
    @SerializedName("command_blocked")
    public String commandBlocked;
    @SerializedName("command_disabled")
    public String commandDisabled;
    @SerializedName("command_disabled_for")
    public String commandDisabledFor;
    @SerializedName("command_enabled")
    public String commandEnabled;
    @SerializedName("command_enabled_for")
    public String commandEnabledFor;
    @SerializedName("command_mode")
    public String commandMode;
    @SerializedName("command_mode_for")
    public String commandModeFor;
    @SerializedName("console_chat")
    public String consoleChat;
    @SerializedName("console_chat_blocked")
    public String consoleChatBlocked;
    @SerializedName("console_command")
    public String consoleCommand;
    @SerializedName("console_command_blocked")
    public String consoleCommandBlocked;
    public String exception;
    @SerializedName("hub_not_set")
    public String hubNotSet;
    @SerializedName("incorrect_usage")
    public String incorrectUsage;
    @SerializedName("insufficient_perm")
    public String insufficientPerm;
    @SerializedName("internal_error")
    public String internalError;
    @SerializedName("ip_list_kick")
    public String ipListKick;
    @SerializedName("ip_not_found")
    public String ipNotFound;
    @SerializedName("message_yourself")
    public String messageYourself;
    @SerializedName("nickname_not_allowed")
    public String nicknameNotAllowed;
    @SerializedName("nickname_not_allowed_log")
    public String nicknameNotAllowedLog;
    @SerializedName("no_player_found")
    public String noPlayerFound;
    @SerializedName("not_existing_server")
    public String notExistingServer;
    @SerializedName("not_player")
    public String notPlayer;
    @SerializedName("not_saved")
    public String notSaved;
    public String offline;
    public String online;
    @SerializedName("player_join_proxy")
    public String playerJoinProxy;
    @SerializedName("player_join_proxy_first")
    public String playerJoinProxyFirst;
    @SerializedName("player_join_server")
    public String playerJoinServer;
    @SerializedName("player_not_found")
    public String playerNotFound;
    @SerializedName("player_quit_proxy")
    public String playerQuitProxy;
    @SerializedName("player_send_to_receiver")
    public String playerSendToReceiver;
    @SerializedName("player_send_to_sender")
    public String playerSendToSender;
    @SerializedName("plugin_reloaded")
    public String pluginReloaded;
    @SerializedName("players_with_ip")
    public String playersWithIp;
    @SerializedName("server_filter_add")
    public String serverFilterAdd;
    @SerializedName("server_filter_add_for")
    public String serverFilterAddFor;
    @SerializedName("server_filter_emptied")
    public String serverFilterEmptied;
    @SerializedName("server_filter_emptied_for")
    public String serverFilterEmptiedFor;
    @SerializedName("server_filter_list")
    public String serverFilterList;
    @SerializedName("server_filter_remove")
    public String serverFilterRemove;
    @SerializedName("server_filter_remove_for")
    public String serverFilterRemoveFor;

    public List<String> help;
    public List<String> ping;

    @SerializedName("formats")
    public Formats FORMATS;
    @SerializedName("moderation")
    public Moderation MODERATION;
    @SerializedName("usages")
    public Usages USAGES;

    @Override
    public MessagesOld init() {
        if (alreadyInServer == null) alreadyInServer = "<red>You are already connected to this server!";
        if (alreadyInServerOther == null) alreadyInServerOther = "<red>{player} is already connected to this server!";
        if (commandBlocked == null) commandBlocked = "<red>This command has been blocked!";
        if (commandDisabled == null) commandDisabled = "<dark_green>{command} <dark_red>disabled<dark_green>!";
        if (commandDisabledFor == null) commandDisabledFor = "<dark_green>{command} <dark_red>disabled <dark_green>for <yellow>{player}<dark_green>!";
        if (commandEnabled == null) commandEnabled = "<dark_green>{command} <green>enabled<dark_green>!";
        if (commandEnabledFor == null) commandEnabledFor = "<dark_green>{command} <green>enabled <dark_green>for <yellow>{player}<dark_green>!";
        if (commandMode == null) commandMode = "<aqua>{command} <yellow>mode set to <aqua>{mode}<yellow>!";
        if (commandModeFor == null) commandModeFor = "<aqua>{command} <yellow>mode set to <aqua>{mode} <yellow>for <aqua>{player}<yellow>!";
        if (consoleChat == null) consoleChat = "<aqua>CHAT: <yellow>{server}:{player} > <reset>{message}";
        if (consoleChatBlocked == null) consoleChatBlocked = "<red>CHAT MUTED: <yellow>{server}:{player} > <reset>{message}";
        if (consoleCommand == null) consoleCommand = "<aqua>CMD: <yellow>{server}:{player} > <reset>{command}";
        if (consoleCommandBlocked == null) consoleCommandBlocked = "<red>CMD BLOCKED: <yellow>{server}:{player} > <reset>{command}";
        if (exception == null) exception = "<dark_red>BungeeCompact encountered an exception! <white>{exception}";
        if (hubNotSet == null) hubNotSet = "<red>Hub server not set!";
        if (incorrectUsage == null) incorrectUsage = "<red>Incorrect usage!";
        if (insufficientPerm == null) insufficientPerm = "<red>Insufficient permissions!";
        if (internalError == null) internalError = "<red>Internal error!";
        if (ipListKick == null) ipListKick = "<red>You have been disconnected!\n<yellow>There are too many accounts with your IP.";
        if (ipNotFound == null) ipNotFound = "<red>IP not found";
        if (messageYourself == null) messageYourself = "<red>You can not send a message to yourself!";
        if (nicknameNotAllowed == null) nicknameNotAllowed = "<dark_red>{player} <red>is not allowed as a nickname!";
        if (nicknameNotAllowedLog == null) nicknameNotAllowedLog = "<red>{player}'s join was blocked due to an invalid nickname.";
        if (noPlayerFound == null) noPlayerFound = "<red>No player found!";
        if (notExistingServer == null) notExistingServer = "<red>This is not a valid server!";
        if (notPlayer == null) notPlayer = "<red>You need to be a player to use this command!";
        if (notSaved == null) notSaved = "<red>Failed to save to storage!";
        if (offline == null) offline = "<red>OFFLINE";
        if (online == null) online = "<dark_green>ONLINE";
        if (playerJoinProxy == null) playerJoinProxy = "<green>{player} joined the proxy.";
        if (playerJoinProxyFirst == null) playerJoinProxyFirst = "<aqua>{player} joined the proxy for the first time.";
        if (playerJoinServer == null) playerJoinServer = "<yellow>{player} joined the server {server}.";
        if (playerNotFound == null) playerNotFound = "<red>Player not found!";
        if (playerQuitProxy == null) playerQuitProxy = "<red>{player} left the proxy.";
        if (playerSendToReceiver == null) playerSendToReceiver = "<dark_green>You were sent to {server} by {player}!";
        if (playerSendToSender == null) playerSendToSender = "<dark_green>You sent {player} to {server}!";
        if (pluginReloaded == null) pluginReloaded = "<dark_green>BungeeCompact reloaded in {time}ms!";
        if (playersWithIp == null) playersWithIp = "<gold>[<aqua>{players}<gold>] <yellow>have the same ip address.";
        if (serverFilterAdd == null) serverFilterAdd = "<yellow>Added <aqua>{server} <yellow>to <aqua>{command} {mode}<yellow>!";
        if (serverFilterAddFor == null) serverFilterAddFor = "<yellow>Added <aqua>{server} <yellow>to <aqua>{command} {mode} <yellow>for <aqua>{player}<yellow>!";
        if (serverFilterEmptied == null) serverFilterEmptied = "<aqua>{command} {mode} <yellow>emptied!";
        if (serverFilterEmptiedFor == null) serverFilterEmptiedFor = "<aqua>{command} {mode} <yellow>emptied for <aqua>{player}<yellow>!";
        if (serverFilterList == null) serverFilterList = "<aqua>{command} {mode} <yellow>servers: <aqua>{list}";
        if (serverFilterRemove == null) serverFilterRemove = "<yellow>Removed <aqua>{server} <yellow>from <aqua>{command} {mode}<yellow>!";
        if (serverFilterRemoveFor == null) serverFilterRemoveFor = "<yellow>Removed <aqua>{server} <yellow>from <aqua>{command} {mode} <yellow>for <green>{player}<yellow>!";

        if (help == null) help = Arrays.asList(
                "<blue>Contact the staff of the server to get help!",
                "<dark_aqua>This is a sample help message,",
                "<aqua>edit the language files to change this message!"
        );

        if (ping == null) ping = Arrays.asList(
                "<aqua><b>Ping of server <yellow>{server}",
                "<aqua><b>Status: {status}",
                "<aqua><b>Players: <yellow>{online}<aqua><b>/<yellow>{max}"
        );

        if (FORMATS == null) FORMATS = new Formats().init();
        if (MODERATION == null) MODERATION = new Moderation().init();
        if (USAGES == null) USAGES = new Usages().init();

        return this;
    }

    public static class Formats extends JsonConfig {
        public String broadcast;
        public String commandspy;
        public String commandwarn;
        public String globalchat;
        public String msg;
        public String socialspy;
        public String staffchat;
        @SerializedName("muted_msg")
        public String mutedMsg;

        @Override
        public Formats init() {
            if (broadcast == null) broadcast = "<dark_red><b>BROADCAST <dark_aqua>● <yellow>{message}";
            if (commandspy == null) commandspy = "<dark_blue><b>CSPY <blue>{server}<gold>:<blue><b>{player} <dark_aqua>● <reset>{command}";
            if (commandwarn == null) commandwarn = "<dark_aqua><b>WARN <blue>{server}<gold>:<blue><b>{player} <dark_aqua>● <aqua>used the command <yellow>{command}";
            if (globalchat == null) globalchat = "<gold>{server}<red>:<gold><b>{player} <dark_aqua>● <reset>{message}";
            if (msg == null) msg = "<aqua>{sender_server}<red>:<yellow><b>{sender} <red>> <aqua>{receiver_server}<red>:<yellow><b>{receiver} <dark_aqua>● <reset>{message}";
            if (socialspy == null)
                socialspy = "<dark_blue><b>SSPY <blue>{sender_server}<gold>:<blue><b>{sender} <red>> <blue>{receiver_server}<gold>:<blue><b>{receiver} <dark_aqua>● <reset>{message}";
            if (staffchat == null) staffchat = "<gold><b>STAFF <aqua>{server}<red>:<yellow><b>{player} <dark_aqua>● <reset>{message}";
            if (mutedMsg == null) mutedMsg = "<red>MUTED <blue>{server}<gold>:<blue><b>{player} <dark_aqua>● <gray>{message}";

            return this;
        }
    }

    public static class Moderation extends JsonConfig {
        public String console;
        @SerializedName("no_reason")
        public String noReason;
        public String online;
        public String offline;
        public String none;
        @SerializedName("not_banned_status")
        public String notBannedStatus;
        @SerializedName("not_muted_status")
        public String notMutedStatus;
        public String hidden;
        public String longest;
        public String permanent;
        public String unknown;
        public String global;
        public String active;
        public String ended;
        @SerializedName("reason_needed")
        public String reasonNeeded;
        @SerializedName("invalid_duration")
        public String invalidDuration;
        @SerializedName("already_banned")
        public String alreadyBanned;
        @SerializedName("already_banned_in_server")
        public String alreadyBannedInServer;
        @SerializedName("already_muted")
        public String alreadyMuted;
        @SerializedName("already_muted_in_server")
        public String alreadyMutedInServer;
        @SerializedName("not_banned")
        public String notBanned;
        @SerializedName("not_banned_in_server")
        public String notBannedInServer;
        @SerializedName("not_muted")
        public String notMuted;
        @SerializedName("not_muted_in_server")
        public String notMutedInServer;
        @SerializedName("id_not_found")
        public String idNotFound;
        @SerializedName("ban_not_allowed")
        public String banNotAllowed;
        @SerializedName("kick_not_allowed")
        public String kickNotAllowed;
        @SerializedName("mute_not_allowed")
        public String muteNotAllowed;
        @SerializedName("warn_not_allowed")
        public String warnNotAllowed;
        @SerializedName("you_are_muted")
        public String youAreMuted;
        @SerializedName("you_are_muted_server")
        public String youAreMutedServer;
        @SerializedName("lookup_header")
        public String lookupHeader;
        @SerializedName("lookup_id_header")
        public String lookupIdHeader;

        @SerializedName("ban_disconnect")
        public String banDisconnect;
        @SerializedName("ban_broadcast")
        public String banBroadcast;
        @SerializedName("ban_broadcast_target")
        public String banBroadcastTarget;
        @SerializedName("ban_ip_disconnect")
        public String banipDisconnect;
        @SerializedName("ban_ip_broadcast")
        public String banipBroadcast;
        @SerializedName("ban_ip_broadcast_target")
        public String banipBroadcastTarget;
        @SerializedName("tempban_disconnect")
        public String tempbanDisconnect;
        @SerializedName("tempban_broadcast")
        public String tempbanBroadcast;
        @SerializedName("tempban_broadcast_target")
        public String tempbanBroadcastTarget;
        @SerializedName("tempbanip_disconnect")
        public String tempbanipDisconnect;
        @SerializedName("tempban_ip_broadcast")
        public String tempbanipBroadcast;
        @SerializedName("tempban_ip_broadcast_target")
        public String tempbanipBroadcastTarget;
        @SerializedName("gban_disconnect")
        public String gbanDisconnect;
        @SerializedName("gban_broadcast")
        public String gbanBroadcast;
        @SerializedName("gbanip_disconnect")
        public String gbanipDisconnect;
        @SerializedName("gbanip_broadcast")
        public String gbanipBroadcast;
        @SerializedName("gtempban_disconnect")
        public String gtempbanDisconnect;
        @SerializedName("gtempban_broadcast")
        public String gtempbanBroadcast;
        @SerializedName("gtempbanip_disconnect")
        public String gtempbanipDisconnect;
        @SerializedName("gtempbanip_broadcast")
        public String gtempbanipBroadcast;
        @SerializedName("unban_broadcast")
        public String unbanBroadcast;
        @SerializedName("unban_broadcast_target")
        public String unbanBroadcastTarget;
        @SerializedName("unbanip_broadcast")
        public String unbanipBroadcast;
        @SerializedName("unbanip_broadcast_target")
        public String unbanipBroadcastTarget;
        @SerializedName("gunban_broadcast")
        public String gunbanBroadcast;
        @SerializedName("gunban_broadcast_target")
        public String gunbanBroadcastTarget;
        @SerializedName("gunbanip_broadcast")
        public String gunbanipBroadcast;
        @SerializedName("gunbanip_broadcast_target")
        public String gunbanipBroadcastTarget;

        @SerializedName("kick_disconnect")
        public String kickDisconnect;
        @SerializedName("kick_broadcast")
        public String kickBroadcast;
        @SerializedName("kickip_disconnect")
        public String kickipDisconnect;
        @SerializedName("kickip_broadcast")
        public String kickipBroadcast;
        @SerializedName("kick_broadcast_target")
        public String kickBroadcastTarget;
        @SerializedName("kickip_broadcast_target")
        public String kickipBroadcastTarget;
        @SerializedName("gkick_disconnect")
        public String gkickDisconnect;
        @SerializedName("gkick_broadcast")
        public String gkickBroadcast;
        @SerializedName("gkickip_disconnect")
        public String gkickipDisconnect;
        @SerializedName("gkickip_broadcast")
        public String gkickipBroadcast;

        @SerializedName("mute_broadcast")
        public String muteBroadcast;
        @SerializedName("mute_broadcast_target")
        public String muteBroadcastTarget;
        @SerializedName("muteip_broadcast")
        public String muteipBroadcast;
        @SerializedName("muteip_broadcast_target")
        public String muteipBroadcastTarget;
        @SerializedName("tempmute_broadcast")
        public String tempmuteBroadcast;
        @SerializedName("tempmute_broadcast_target")
        public String tempmuteBroadcastTarget;
        @SerializedName("tempmuteip_broadcast")
        public String tempmuteipBroadcast;
        @SerializedName("tempmuteip_broadcast_target")
        public String tempmuteipBroadcastTarget;
        @SerializedName("gmute_broadcast")
        public String gmuteBroadcast;
        @SerializedName("gmute_broadcast_target")
        public String gmuteBroadcastTarget;
        @SerializedName("gmuteip_broadcast")
        public String gmuteipBroadcast;
        @SerializedName("gmuteip_broadcast_target")
        public String gmuteipBroadcastTarget;
        @SerializedName("gtempmute_broadcast")
        public String gtempmuteBroadcast;
        @SerializedName("gtempmute_broadcast_target")
        public String gtempmuteBroadcastTarget;
        @SerializedName("gtempmuteip_broadcast")
        public String gtempmuteipBroadcast;
        @SerializedName("gtempmuteip_broadcast_target")
        public String gtempmuteipBroadcastTarget;
        @SerializedName("unmute_broadcast")
        public String unmuteBroadcast;
        @SerializedName("unmute_broadcast_target")
        public String unmuteBroadcastTarget;
        @SerializedName("unmuteip_broadcast")
        public String unmuteipBroadcast;
        @SerializedName("unmuteip_broadcast_target")
        public String unmuteipBroadcastTarget;
        @SerializedName("gunmute_broadcast")
        public String gunmuteBroadcast;
        @SerializedName("gunmute_broadcast_target")
        public String gunmuteBroadcastTarget;
        @SerializedName("gunmuteip_broadcast")
        public String gunmuteipBroadcast;
        @SerializedName("gunmuteip_broadcast_target")
        public String gunmuteipBroadcastTarget;

        @SerializedName("warn_broadcast")
        public String warnBroadcast;
        @SerializedName("warn_broadcast_target")
        public String warnBroadcastTarget;

        public List<String> lookup;
        @SerializedName("lookup_ip")
        public List<String> lookupIp;
        @SerializedName("lookup_ban")
        public String lookupBan;
        @SerializedName("lookup_kick")
        public String lookupKick;
        @SerializedName("lookup_mute")
        public String lookupMute;
        @SerializedName("lookup_warn")
        public String lookupWarn;
        @SerializedName("lookup_ban_id")
        public List<String> lookupBanId;
        @SerializedName("lookup_kick_id")
        public List<String> lookupKickId;
        @SerializedName("lookup_mute_id")
        public List<String> lookupMuteId;
        @SerializedName("lookup_warn_id")
        public List<String> lookupWarnId;

        @SerializedName("ban_reason_suggestions")
        public List<String> banReasonSuggestions;
        @SerializedName("kick_reason_suggestions")
        public List<String> kickReasonSuggestions;
        @SerializedName("mute_reason_suggestions")
        public List<String> muteReasonSuggestions;
        @SerializedName("warn_reason_suggestions")
        public List<String> warnReasonSuggestions;

        @Override
        public Moderation init() {
            if (console == null) console = "Console";
            if (noReason == null) noReason = "No reason";
            if (online == null) online = "<dark_green><b>Online <gray>in server <yellow>{server}";
            if (offline == null) offline = "<dark_red><b>Offline";
            if (none == null) none = "<yellow>none";
            if (notBannedStatus == null) notBannedStatus = "<yellow>not banned";
            if (notMutedStatus == null) notMutedStatus = "<yellow>not muted";
            if (hidden == null) hidden = "hidden";
            if (longest == null) longest = "<gray>(server longest)";
            if (permanent == null) permanent = "permanent";
            if (unknown == null) unknown = "unknown";
            if (global == null) global = "global";
            if (active == null) active = "<red>Active";
            if (ended == null) ended = "<dark_green>Ended";
            if (reasonNeeded == null) reasonNeeded = "<red>You must add a reason to use this command.";
            if (invalidDuration == null) invalidDuration = "<red>Invalid duration!";
            if (alreadyBanned == null) alreadyBanned = "<red>This player is already banned!";
            if (alreadyBannedInServer == null) alreadyBannedInServer = "<red>This player is already banned in this server!";
            if (alreadyMuted == null) alreadyMuted = "<red>This player is already muted!";
            if (alreadyMutedInServer == null) alreadyMutedInServer = "<red>This player is already muted in this server!";
            if (notBanned == null) notBanned = "<red>This player is not banned!";
            if (notBannedInServer == null) notBannedInServer = "<red>This player is not banned in this server!";
            if (notMuted == null) notMuted = "<red>This player is not muted!";
            if (notMutedInServer == null) notMutedInServer = "<red>This player is not muted in this server!";
            if (idNotFound == null) idNotFound = "<red>ID not found!";
            if (banNotAllowed == null) banNotAllowed = "<red>You can't ban this player!";
            if (kickNotAllowed == null) kickNotAllowed = "<red>You can't kick this player!";
            if (muteNotAllowed == null) muteNotAllowed = "<red>You can't mute this player!";
            if (warnNotAllowed == null) warnNotAllowed = "<red>You can't warn this player!";
            if (youAreMuted == null) youAreMuted = "<red>You are muted!";
            if (youAreMutedServer == null) youAreMutedServer = "<red>You are muted in this server!";
            if (lookupHeader == null) lookupHeader = "<blue><st>---- <gold>Lookup <yellow>{player} <white>- <yellow>{type} <white>- <yellow>Page {page} <blue><st>----";
            if (lookupIdHeader == null) lookupIdHeader = "<blue><st>---- <gold>Lookup <yellow>{id} <white>- <yellow>{type} <blue><st>----";

            if (banDisconnect == null)
                banDisconnect = "<red>You are banned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red>Server <dark_aqua><b>» <gray>{server}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red>Reason <dark_aqua><b>» <gray>{reason}";
            if (banBroadcast == null)
                banBroadcast = "<gold><b>{player} <gray>has been <yellow><i>banned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (banBroadcastTarget == null)
                banBroadcastTarget = "<red>You are <yellow><i>banned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (banipDisconnect == null)
                banipDisconnect = "<red>You are ip-banned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red>Server <dark_aqua><b>» <gray>{server}\n<red>Reason <dark_aqua><b>» <gray>{reason}";
            if (banipBroadcast == null)
                banipBroadcast = "<gold><b>{player} <gray>has been <yellow><i>ip-banned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (banipBroadcastTarget == null)
                banipBroadcastTarget = "<red>You are <yellow><i>ip-banned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (tempbanDisconnect == null)
                tempbanDisconnect = "<red>You are banned temporary\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red>Server <dark_aqua><b>» <gray>{server}\n<red>Duration <dark_aqua><b>» <gray>{duration}\n<red>Reason <dark_aqua><b>» <gray>{reason}";
            if (tempbanBroadcast == null)
                tempbanBroadcast = "<gold><b>{player} <gray>has been <yellow><i>banned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (tempbanBroadcastTarget == null)
                tempbanBroadcastTarget = "<red>You are <yellow><i>banned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (tempbanipDisconnect == null)
                tempbanipDisconnect = "<red>You are ip-banned temporary\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red>Server <dark_aqua><b>» <gray>{server}\n<red>Duration <dark_aqua><b>» <gray>{duration}\n<red>Reason <dark_aqua><b>» <gray>{reason}";
            if (tempbanipBroadcast == null)
                tempbanipBroadcast = "<gold><b>{player} <gray>has been <yellow><i>ip-banned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (tempbanipBroadcastTarget == null)
                tempbanipBroadcastTarget = "<red>You are <yellow><i>ip-banned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gbanDisconnect == null)
                gbanDisconnect = "<red>You are banned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red>Reason <dark_aqua><b>» <gray>{reason}";
            if (gbanBroadcast == null)
                gbanBroadcast = "<gold><b>{player} <gray>has been <yellow><i>banned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gbanipDisconnect == null)
                gbanipDisconnect = "<red>You are ip-banned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red>Reason <dark_aqua><b>» <gray>{reason}";
            if (gbanipBroadcast == null)
                gbanipBroadcast = "<gold><b>{player} <gray>has been <yellow><i>ip-banned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gtempbanDisconnect == null)
                gtempbanDisconnect = "<red>You are banned temporary\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red>Duration <dark_aqua><b>» <gray>{duration}\n<red>Reason <dark_aqua><b>» <gray>{reason}";
            if (gtempbanBroadcast == null)
                gtempbanBroadcast = "<gold><b>{player} <gray>has been <yellow><i>banned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gtempbanipDisconnect == null)
                gtempbanipDisconnect = "<red>You are ip-banned temporary\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red>Duration <dark_aqua><b>» <gray>{duration}\n<red>Reason <dark_aqua><b>» <gray>{reason}";
            if (gtempbanipBroadcast == null)
                gtempbanipBroadcast = "<gold><b>{player} <gray>has been <yellow><i>ip-banned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (unbanBroadcast == null)
                unbanBroadcast = "<gold><b>{player} <gray>has been <yellow><i>unbanned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (unbanBroadcastTarget == null)
                unbanBroadcastTarget = "<gray>You have been <yellow><i>unbanned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (unbanipBroadcast == null)
                unbanipBroadcast = "<gold><b>{player} <gray>has been <yellow><i>ip-unbanned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (unbanipBroadcastTarget == null)
                unbanipBroadcastTarget = "<gray>You have been <yellow><i>ip-unbanned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gunbanBroadcast == null)
                gunbanBroadcast = "<gold><b>{player} <gray>has been <yellow><i>unbanned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gunbanBroadcastTarget == null)
                gunbanBroadcastTarget = "<gray>You have been <yellow><i>unbanned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gunbanipBroadcast == null)
                gunbanipBroadcast = "<gold><b>{player} <gray>has been <yellow><i>ip-unbanned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gunbanipBroadcastTarget == null)
                gunbanipBroadcastTarget = "<gray>You have been <yellow><i>ip-unbanned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";

            if (kickDisconnect == null)
                kickDisconnect = "<red>You are kicked\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red>Server <dark_aqua><b>» <gray>{server}\n<red>Reason <dark_aqua><b>» <gray>{reason}";
            if (kickBroadcast == null)
                kickBroadcast = "<gold><b>{player} <gray>has been <yellow><i>kicked\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (kickipDisconnect == null)
                kickipDisconnect = "<red>You are ip-kicked\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red>Server <dark_aqua><b>» <gray>{server}\n<red>Reason <dark_aqua><b>» <gray>{reason}";
            if (kickipBroadcast == null)
                kickipBroadcast = "<gold><b>{player} <gray>has been <yellow><i>ip-kicked\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (kickBroadcastTarget == null)
                kickBroadcastTarget = "<gray>You have been <yellow><i>kicked\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (kickipBroadcastTarget == null)
                kickipBroadcastTarget = "<gray>You have been <yellow><i>ip-kicked\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gkickDisconnect == null) gkickDisconnect = "<red>You are kicked\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red>Reason <dark_aqua><b>» <gray>{reason}";
            if (gkickBroadcast == null)
                gkickBroadcast = "<gold><b>{player} <gray>has been <yellow><i>kicked\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gkickipDisconnect == null) gkickipDisconnect = "<red>You are ip-kicked\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red>Reason <dark_aqua><b>» <gray>{reason}";
            if (gkickipBroadcast == null)
                gkickipBroadcast = "<gold><b>{player} <gray>has been <yellow><i>ip-kicked\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";

            if (muteBroadcast == null)
                muteBroadcast = "<gold><b>{player} <gray>has been <yellow><i>muted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (muteBroadcastTarget == null)
                muteBroadcastTarget = "<gray>You have been <yellow><i>muted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (muteipBroadcast == null)
                muteipBroadcast = "<gold><b>{player} <gray>has been <yellow><i>ip-muted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (muteipBroadcastTarget == null)
                muteipBroadcastTarget = "<gray>You have been <yellow><i>ip-muted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (tempmuteBroadcast == null)
                tempmuteBroadcast = "<gold><b>{player} <gray>has been <yellow><i>muted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (tempmuteBroadcastTarget == null)
                tempmuteBroadcastTarget = "<gray>You have been <yellow><i>muted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (tempmuteipBroadcast == null)
                tempmuteipBroadcast = "<gold><b>{player} <gray>has been <yellow><i>ip-muted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (tempmuteipBroadcastTarget == null)
                tempmuteipBroadcastTarget = "<gray>You have been <yellow><i>ip-muted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gmuteBroadcast == null)
                gmuteBroadcast = "<gold><b>{player} <gray>has been <yellow><i>muted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gmuteBroadcastTarget == null)
                gmuteBroadcastTarget = "<gray>You have been <yellow><i>muted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gmuteipBroadcast == null)
                gmuteipBroadcast = "<gold><b>{player} <gray>has been <yellow><i>ip-muted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gmuteipBroadcastTarget == null)
                gmuteipBroadcastTarget = "<gray>You have been <yellow><i>ip-muted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gtempmuteBroadcast == null)
                gtempmuteBroadcast = "<gold><b>{player} <gray>has been <yellow><i>muted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gtempmuteBroadcastTarget == null)
                gtempmuteBroadcastTarget = "<gray>You have been <yellow><i>muted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gtempmuteipBroadcast == null)
                gtempmuteipBroadcast = "<gold><b>{player} <gray>has been <yellow><i>ip-muted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gtempmuteipBroadcastTarget == null)
                gtempmuteipBroadcastTarget = "<gray>You have been <yellow><i>ip-muted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Duration <dark_aqua><b>» <yellow>{duration}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (unmuteBroadcast == null)
                unmuteBroadcast = "<gold><b>{player} <gray>has been <yellow><i>unmuted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (unmuteBroadcastTarget == null)
                unmuteBroadcastTarget = "<gray>You have been <yellow><i>unmuted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (unmuteipBroadcast == null)
                unmuteipBroadcast = "<gold><b>{player} <gray>has been <yellow><i>ip-unmuted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (unmuteipBroadcastTarget == null)
                unmuteipBroadcastTarget = "<gray>You have been <yellow><i>ip-unmuted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Server <dark_aqua><b>» <yellow>{server}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gunmuteBroadcast == null)
                gunmuteBroadcast = "<gold><b>{player} <gray>has been <yellow><i>unmuted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gunmuteBroadcastTarget == null)
                gunmuteBroadcastTarget = "<gray>You have been <yellow><i>unmuted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gunmuteipBroadcast == null)
                gunmuteipBroadcast = "<gold><b>{player} <gray>has been <yellow><i>ip-unmuted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (gunmuteipBroadcastTarget == null)
                gunmuteipBroadcastTarget = "<gray>You have been <yellow><i>ip-unmuted\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";

            if (warnBroadcast == null)
                warnBroadcast = "<gold><b>{player} <gray>has been <yellow><i>warned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";
            if (warnBroadcastTarget == null)
                warnBroadcastTarget = "<gray>You have been <yellow><i>warned\n\n<red><b>Staff <dark_aqua><b>» <yellow>{staff}\n<red><b>Reason <dark_aqua><b>» <gray>{reason}";

            if (lookup == null) lookup = Arrays.asList(
                    "<blue><st>----<reset> <gold>Lookup <yellow>{player} <blue><st>----",
                    "{connection_status}",
                    "<yellow>First login <dark_aqua><b>» <yellow>{first_login}",
                    "<yellow>Last login <dark_aqua><b>» <yellow>{last_login}",
                    "<yellow>Last IP <dark_aqua><b>» <yellow>{ip}",
                    "<yellow>Status <dark_aqua>:",
                    "  <red>Ban <gray>end <dark_aqua><b>» {ban_status}",
                    "  <red>Banned <gray>by <dark_aqua><b>» {ban_staff}",
                    "  <red>Mute <gray>end <dark_aqua><b>» {mute_status}",
                    "  <red>Muted <gray>by <dark_aqua><b>» {mute_staff}",
                    "<yellow>History <dark_aqua>:",
                    "  <gray>{bans} <yellow>bans",
                    "  <gray>{kicks} <yellow>kicks",
                    "  <gray>{mutes} <yellow>mutes",
                    "  <gray>{warns} <yellow>warns",
                    "<yellow>Nickname history <dark_aqua><b>» <aqua>[<yellow>{nickname_history}<aqua>]",
                    "<yellow>UUID <dark_aqua><b>» <yellow>{uuid}",
                    "<blue><st>---- <gold>Lookup <yellow>{player} <blue><st>----"
            );
            if (lookupIp == null) lookupIp = Arrays.asList(
                    "<blue><st>----<reset> <gold>Lookup <yellow>{ip} <blue><st>----",
                    "<yellow>Location <dark_aqua><b>» <yellow>{location}",
                    "<yellow>Players <gray>with this ip <dark_aqua><b>» <aqua>[<yellow>{players}<aqua>]",
                    "<yellow>Status <dark_aqua>:",
                    "  <red>Ban <gray>end <dark_aqua><b>» {ban_status}",
                    "  <red>Banned <gray>by <dark_aqua><b>» {ban_staff}",
                    "  <red>Mute <gray>end <dark_aqua><b>» {mute_status}",
                    "  <red>Muted <gray>by <dark_aqua><b>» {mute_staff}",
                    "<yellow>History <dark_aqua>:",
                    "  <gray>{bans} <yellow>bans",
                    "  <gray>{mutes} <yellow>mutes",
                    "<blue><st>---- <gold>Lookup <yellow>{ip} <blue><st>----");
            if (lookupBan == null)
                lookupBan = "<light_purple>{id} <yellow>Begin: <green>{begin} <yellow>End: <green>{end} <yellow>Server: <green>{server} <yellow>Staff: <green>{staff} <yellow>Reason: <green>{reason}";
            if (lookupKick == null) lookupKick = "<light_purple>{id} <yellow>Date: <green>{date} <yellow>Server: <green>{server} <yellow>Staff: <green>{staff} <yellow>Reason: <green>{reason}";
            if (lookupMute == null)
                lookupMute = "<light_purple>{id} <yellow>Begin: <green>{begin} <yellow>End: <green>{end} <yellow>Server: <green>{server} <yellow>Staff: <green>{staff} <yellow>Reason: <green>{reason}";
            if (lookupWarn == null) lookupWarn = "<light_purple>{id} <yellow>Date: <green>{date} <yellow>Staff: <green>{staff} <yellow>Reason: <green>{reason}";
            if (lookupBanId == null) lookupBanId = Arrays.asList(
                    "<yellow>Begin: <green>{begin}",
                    "<yellow>End: <green>{end}",
                    "<yellow>Server: <green>{server}",
                    "<yellow>Staff: <green>{staff}",
                    "<yellow>Reason: <green>{reason}",
                    "<yellow>State: {state}",
                    "<yellow>Unban date: <green>{unban_date}",
                    "<yellow>Unban staff: <green>{unban_staff}",
                    "<yellow>Unban reason: <green>{unban_reason}");
            if (lookupKickId == null) lookupKickId = Arrays.asList(
                    "<yellow>Date: <green>{date}",
                    "<yellow>Server: <green>{server}",
                    "<yellow>Staff: <green>{staff}",
                    "<yellow>Reason: <green>{reason}");
            if (lookupMuteId == null) lookupMuteId = Arrays.asList(
                    "<yellow>Begin: <green>{begin}",
                    "<yellow>End: <green>{end}",
                    "<yellow>Server: <green>{server}",
                    "<yellow>Staff: <green>{staff}",
                    "<yellow>Reason: <green>{reason}",
                    "<yellow>State: {state}",
                    "<yellow>Unmute date: <green>{unmute_date}",
                    "<yellow>Unmute staff: <green>{unmute_staff}",
                    "<yellow>Unmute reason: <green>{unmute_reason}");
            if (lookupWarnId == null) lookupWarnId = Arrays.asList(
                    "<yellow>Date: <green>{date}",
                    "<yellow>Staff: <green>{staff}",
                    "<yellow>Reason: <green>{reason}");

            if (banReasonSuggestions == null) banReasonSuggestions = Arrays.asList(
                    "Hacks",
                    "Spam"
            );
            if (kickReasonSuggestions == null) kickReasonSuggestions = Arrays.asList(
                    "Ignores the staff",
                    "Low TPS"
            );
            if (muteReasonSuggestions == null) muteReasonSuggestions = Arrays.asList(
                    "Use bad words",
                    "Insults",
                    "Flame"
            );
            if (warnReasonSuggestions == null) warnReasonSuggestions = Arrays.asList(
                    "Moderate your language",
                    "Does not respect others"
            );

            return this;
        }
    }

    public static class Usages extends JsonConfig {
        public List<String> ban;
        public List<String> banip;
        public List<String> broadcast;
        public List<String> commandspy;
        public List<String> gban;
        public List<String> gbanip;
        public List<String> gbroadcast;
        public List<String> gkick;
        public List<String> gkickip;
        public List<String> globalchat;
        public List<String> gmute;
        public List<String> gmuteip;
        public List<String> gtempban;
        public List<String> gtempbanip;
        public List<String> gtempmute;
        public List<String> gtempmuteip;
        public List<String> gunban;
        public List<String> gunbanip;
        public List<String> gunmute;
        public List<String> gunmuteip;
        public List<String> kick;
        public List<String> kickip;
        public List<String> lookup;
        public List<String> lookupip;
        public List<String> msg;
        public List<String> mute;
        public List<String> muteip;
        public List<String> r;
        public List<String> socialspy;
        public List<String> staffchat;
        public List<String> tempban;
        public List<String> tempbanip;
        public List<String> tempmute;
        public List<String> tempmuteip;
        public List<String> unban;
        public List<String> unbanip;
        public List<String> unmute;
        public List<String> unmuteip;
        public List<String> warn;

        @Override
        public Usages init() {
            if (ban == null) ban = Collections.singletonList("<aqua>/ban <player> <server> [reason] [-s] [-h]");
            if (banip == null) banip = Collections.singletonList("<aqua>/banip <player|ip> <server> [reason] [-s] [-h]");
            if (broadcast == null) broadcast = Collections.singletonList("<aqua>/broadcast [server] <message>");
            if (commandspy == null) commandspy = Arrays.asList(
                    "<aqua>/commandspy <on|off|toggle> [player]",
                    "<aqua>/commandspy mode <whitelist|blacklist> [player]",
                    "<aqua>/commandspy list [player]",
                    "<aqua>/commandspy list empty [player]",
                    "<aqua>/commandspy list <add|remove> <server> [player]");
            if (gban == null) gban = Collections.singletonList("<aqua>/gban <player> [reason] [-s] [-h]");
            if (gbanip == null) gbanip = Collections.singletonList("<aqua>/gbanip <player|ip> [reason] [-s] [-h]");
            if (gbroadcast == null) gbroadcast = Collections.singletonList("<aqua>/gbroadcast <message>");
            if (gkick == null) gkick = Collections.singletonList("<aqua>/gkick <player> [reason] [-s] [-h]");
            if (gkickip == null) gkickip = Collections.singletonList("<aqua>/gkickip <player|ip> [reason] [-s] [-h]");
            if (globalchat == null) globalchat = Arrays.asList(
                    "<aqua>/globalchat <on|off|toggle> [player]",
                    "<aqua>/globalchat mode <whitelist|blacklist> [player]",
                    "<aqua>/globalchat list [player]",
                    "<aqua>/globalchat list empty [player",
                    "<aqua>/globalchat list <add|remove> <server> [player]");
            if (gmute == null) gmute = Collections.singletonList("<aqua>/gmute <player> [reason] [-s] [-h]");
            if (gmuteip == null) gmuteip = Collections.singletonList("<aqua>/gmuteip <player|ip> [reason] [-s] [-h]");
            if (gtempban == null) gtempban = Collections.singletonList("<aqua>/gtempban <player> <duration> [reason] [-s] [-h]");
            if (gtempbanip == null) gtempbanip = Collections.singletonList("<aqua>/gtempbanip <player|ip> <duration> [reason] [-s] [-h]");
            if (gtempmute == null) gtempmute = Collections.singletonList("<aqua>/gtempmute <player> <duration> [reason] [-s] [-h]");
            if (gtempmuteip == null) gtempmuteip = Collections.singletonList("<aqua>/gtempmuteip <player|ip> <duration> [reason] [-s] [-h]");
            if (gunban == null) gunban = Collections.singletonList("<aqua>/gunban <player> [reason] [-s] [-h]");
            if (gunbanip == null) gunbanip = Collections.singletonList("<aqua>/gunbanip <player|ip> [reason] [-s] [-h]");
            if (gunmute == null) gunmute = Collections.singletonList("<aqua>/gunmute <player> [reason] [-s] [-h]");
            if (gunmuteip == null) gunmuteip = Collections.singletonList("<aqua>/gunmuteip <player|ip> [reason] [-s] [-h]");
            if (kick == null) kick = Collections.singletonList("<aqua>/kick <player> <server> [reason] [-s] [-h]");
            if (kickip == null) kickip = Collections.singletonList("<aqua>/kickip <player|ip> <server> [reason] [-s] [-h]");
            if (lookup == null) lookup = Collections.singletonList("<aqua>/lookup <player|ip|ban|kick|mute|warn> [ban|kick|mute|warn] [page]");
            if (lookupip == null) lookupip = Collections.singletonList("<aqua>/lookupip <player|ip> [ban|kick|mute|warn] [page]");
            if (msg == null) msg = Collections.singletonList("/msg <player> <message>");
            if (mute == null) mute = Collections.singletonList("<aqua>/mute <player> <server> [reason] [-s] [-h]");
            if (muteip == null) muteip = Collections.singletonList("<aqua>/muteip <ip|player> <server> [reason] [-s] [-h]");
            if (r == null) r = Collections.singletonList("<aqua>/r <message>");
            if (socialspy == null) socialspy = Arrays.asList(
                    "<aqua>/socialspy <on|off|toggle> [player]",
                    "<aqua>/socialspy mode <whitelist|blacklist> [player]",
                    "<aqua>/socialspy list [player]",
                    "<aqua>/socialspy list empty [player",
                    "<aqua>/socialspy list <add|remove> <server> [player]");
            if (staffchat == null) staffchat = Arrays.asList(
                    "<aqua>/staffchat <on|off|toggle> [player]",
                    "<aqua>/staffchat <message>");
            if (tempban == null) tempban = Collections.singletonList("<aqua>/tempban <player> <duration> <server> [reason] [-s] [-h]");
            if (tempbanip == null) tempbanip = Collections.singletonList("<aqua>/tempbanip <player|ip> <duration> <server> [reason] [-s] [-h]");
            if (tempmute == null) tempmute = Collections.singletonList("<aqua>/tempmute <player> <duration> <server> [reason] [-s] [-h]");
            if (tempmuteip == null) tempmuteip = Collections.singletonList("<aqua>/tempmuteip <player|ip> <duration> <server> [reason] [-s] [-h]");
            if (unban == null) unban = Collections.singletonList("<aqua>/unban <player> <server> [reason] [-s] [-h]");
            if (unbanip == null) unbanip = Collections.singletonList("<aqua>/unbanip <player|ip> <server> [reason] [-s] [-h]");
            if (unmute == null) unmute = Collections.singletonList("<aqua>/unmute <player> <server> [reason] [-s] [-h]");
            if (unmuteip == null) unmuteip = Collections.singletonList("<aqua>/unmuteip <player|ip> <server> [reason] [-s] [-h]");
            if (warn == null) warn = Collections.singletonList("<aqua>/warn <player> [reason] [-s] [-h]");

            return this;
        }
    }

    public static String replace(String str, String[] target, String[] replacement) {
        for (int i = 0; i < target.length; i++) {
            str = str.replace(target[i], replacement[i]);
        }

        return str;
    }

    public static void sendIncorrectUsage(String error, List<String> usage, CommandSource target) {
        Text.send(error, target);
        Text.send(usage, target);
    }
}
