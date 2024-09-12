package network.multicore.vc.utils;

import java.util.List;

public record AnnouncerMessage(List<String> servers, boolean isWhitelist, List<String> lines) {
}
