package dev.clares.chatgames.log;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LogChatParser {
    // Supports the launcher format and Loom's development logger used by Minecraft 26.2.
    private static final Pattern CHAT = Pattern.compile("^\\[[\\d:.]+\\] \\[[^]]+\\]\\s*:?\\s*(?:\\([^)]*\\)\\s*)?(?:\\[[^]]+\\]\\s*)?\\[CHAT\\]\\s*(.*)$");
    private LogChatParser() { }
    public static Optional<String> parse(String line) {
        Matcher m = CHAT.matcher(line);
        if(!m.matches()||m.group(1).startsWith("[CGH]"))return Optional.empty();
        // ChatComponent escapes line breaks before logging; detectors need the original block.
        return Optional.of(m.group(1).replace("\\r","\r").replace("\\n","\n"));
    }
}
