package dev.clares.chatgames.hud;
import dev.clares.chatgames.config.ConfigManager;
import dev.clares.chatgames.game.*;
import dev.clares.chatgames.gui.GameCatalog;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import java.util.Locale;

public final class ChatGamesHud {
    public static void register(GameManager manager,ConfigManager config) {
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("chatgames_universal_helper","status"),(g,tickCounter) -> {
            var client=Minecraft.getInstance();
            if(!config.get().hud||client.player==null||client.gui.hud.isHidden()||client.gui.screen()!=null)return;
            GameSession s=manager.current();if(s==null||s.status==GameState.FINISHED||s.status==GameState.CANCELLED)return;
            int x=8,y=72,w=Math.min(220,g.guiWidth()-16);
            // Below the compact countdown, away from the latest chat lines and hotbar.
            g.fill(x,y,x+w,y+49,0xE6172535);g.fill(x,y,x+2,y+49,0xFF7DE1D2);
            g.text(client.font,GameCatalog.title(s.type),x+8,y+6,0xFF9FE8CB);
            String question=s.question.replaceAll("\\s+"," ")+(s.answer.isBlank()?"":" → "+s.answer);
            g.text(client.font,client.font.plainSubstrByWidth(question,w-16),x+8,y+18,0xFFE8F1F5);
            long left=Math.max(0,s.scheduledSendTime-System.currentTimeMillis());
            String status=s.status==GameState.WAITING?String.format(Locale.ROOT,"Envío en %.1f s",left/1000.0):s.status==GameState.ANSWERED?"Enviada · "+RoundHistory.seconds(s.sentAfterMs):"Resolviendo…";
            g.text(client.font,client.font.plainSubstrByWidth(status,w-16),x+8,y+32,0xFFFFCF70);
            if(s.status==GameState.WAITING) {
                double progress=s.responseDelayMs<=0?1:1-Math.min(1,left/(double)s.responseDelayMs);
                g.fill(x,y+47,x+w,y+49,0xFF324657);g.fill(x,y+47,x+(int)(w*progress),y+49,0xFF7DE1D2);
            }
        });
    }
}
