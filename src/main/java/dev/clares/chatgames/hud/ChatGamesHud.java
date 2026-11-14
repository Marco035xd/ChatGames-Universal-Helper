package dev.clares.chatgames.hud;
import dev.clares.chatgames.config.ConfigManager;
import dev.clares.chatgames.game.*;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class ChatGamesHud {
    public static void register(GameManager manager,ConfigManager config) {
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("chatgames_universal_helper","status"),(context,tickCounter) -> {
            if (!config.get().hud || Minecraft.getInstance().player==null) return;
            GameSession s=manager.current();if(s==null || s.status==GameState.FINISHED || s.status==GameState.CANCELLED)return;
            var client=Minecraft.getInstance();
            context.text(client.font,"CGH · "+s.type,8,8,0xFF88FFCC);
            context.text(client.font,s.question.length()>42?s.question.substring(0,42)+"…":s.question,8,20,0xFFFFFFFF);
            if(!s.answer.isBlank())context.text(client.font,"→ "+s.answer+" · "+(s.status==GameState.WAITING?Math.max(0,(s.scheduledSendTime-System.currentTimeMillis())/1000.0)+"s":s.status),8,32,0xFFFFDB79);
        });
    }
}
