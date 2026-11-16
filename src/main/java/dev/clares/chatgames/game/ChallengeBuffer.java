package dev.clares.chatgames.game;
import java.util.ArrayList;
import java.util.List;
public final class ChallengeBuffer {
    private final List<String> lines = new ArrayList<>();
    private final long start = System.currentTimeMillis();
    private long lastReceivedAtNanos;
    public void add(String line) { add(line,System.nanoTime()); }
    public void add(String line,long receivedAtNanos) {
        if (!lines.isEmpty() && last().equals(line))return;
        if (lines.size() < 25) {lines.add(line);lastReceivedAtNanos=receivedAtNanos;}
    }
    public String text() { return String.join("\n", lines); }
    public String last() { return lines.isEmpty() ? "" : lines.getLast(); }
    public long receivedAtNanos() { return lastReceivedAtNanos; }
    /** Discard an abandoned partial prompt; this never delays a complete challenge. */
    public boolean expired(long now) { return now-start>10000; }
    public int size() { return lines.size(); }
}
