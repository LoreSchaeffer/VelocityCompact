package network.multicore.vc.messages;

import java.util.Arrays;
import java.util.List;

public class MessagesOld {

    public static class Moderation {
        public String lookupHeader;
        public String lookupIdHeader;

        public List<String> lookup;
        public List<String> lookupIp;
        public String lookupBan;
        public String lookupKick;
        public String lookupMute;
        public String lookupWarn;
        public List<String> lookupBanId;
        public List<String> lookupKickId;
        public List<String> lookupMuteId;
        public List<String> lookupWarnId;

        public Moderation init() {
            if (lookupHeader == null) lookupHeader = "<blue><st>---- <gold>Lookup <yellow>{player} <white>- <yellow>{type} <white>- <yellow>Page {page} <blue><st>----";
            if (lookupIdHeader == null) lookupIdHeader = "<blue><st>---- <gold>Lookup <yellow>{id} <white>- <yellow>{type} <blue><st>----";

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

            return this;
        }
    }
}
