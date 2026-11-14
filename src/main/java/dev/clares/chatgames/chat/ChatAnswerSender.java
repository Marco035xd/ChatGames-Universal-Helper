package dev.clares.chatgames.chat;
import net.minecraft.client.Minecraft;

public final class ChatAnswerSender implements AnswerSender {
    private final Minecraft client;
    public ChatAnswerSender(Minecraft client) { this.client=client; }
    public boolean send(String raw) {
        if (client.player==null || client.getConnection()==null || raw==null) return false;
        String s=raw.replaceAll("[\\p{Cntrl}\\r\\n]", "").strip();
        if (s.isEmpty() || s.startsWith("/") || s.length()>256) return false;
        client.getConnection().sendChat(s);
        return true;
    }
    public boolean clickCommand(String raw) {
        if (client.player==null || client.getConnection()==null || raw==null || !raw.matches("/[a-zA-Z0-9_:-]+(?: [a-zA-Z0-9_ .:-]+)?")) return false;
        client.getConnection().sendCommand(raw.substring(1));
        return true;
    }
}
