package dev.clares.chatgames.hud;

import dev.clares.chatgames.config.ConfigManager;
import dev.clares.chatgames.countdown.CountdownClock;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class CountdownHud {
    private CountdownHud(){}
    public static void register(CountdownClock clock,ConfigManager settings) {
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("chatgames_universal_helper","countdown"),(g,t)->{
            Minecraft client=Minecraft.getInstance();
            if(client.player==null||client.gui.hud.isHidden()||!settings.get().countdown.enabled)return;
            if(settings.get().countdown.fullTime) {
                drawCompact(g,client,clock.displayText(),clock.roundActive()?"Al terminar verás el tiempo restante":clock.remainingSeconds()<0?"Se sincroniza con el próximo reto":"Estimado · intervalo "+CountdownClock.formatTime(clock.intervalSeconds()));
            }
            int number=clock.seconds();if(number>0)draw(g,client,number,clock.source().equals("Anuncio del servidor")?"Próximo evento":clock.source().contains("manual")?"Ajuste manual":"Estimado · "+CountdownClock.formatTime(clock.intervalSeconds()));
        });
    }
    public static void drawCompact(GuiGraphicsExtractor g,Minecraft client,String text,String caption) {
        int cx=g.guiWidth()/2,y=34,w=Math.min(g.guiWidth()-12,Math.max(210,Math.max(client.font.width(text),client.font.width(caption))+20));
        g.fill(cx-w/2,y,cx+w/2,y+29,0xDC172535);
        g.fill(cx-w/2,y,cx+w/2,y+2,0xFF7DE1D2);
        g.centeredText(client.font,text,cx,y+6,0xFFF3F4F7);
        g.centeredText(client.font,caption,cx,y+18,0xFFA9BBC9);
    }
    public static void draw(GuiGraphicsExtractor g,Minecraft client,int number,String caption) {
        int cx=g.guiWidth()/2,y=Math.max(70,g.guiHeight()/5),w=166;
        g.fill(cx-w/2-2,y-2,cx+w/2+2,y+70,0xB8070D17);
        g.fill(cx-w/2,y,cx+w/2,y+68,0xE31B2A3D);
        int color=number<=3?0xFFFFB76F:0xFF7DE1D2;
        g.fill(cx-w/2,y,cx+w/2,y+2,color);
        g.centeredText(client.font,"Nuevo minijuego en",cx,y+9,0xFFF3F4F7);
        g.pose().pushMatrix();g.pose().translate(cx,y+24);g.pose().scale(3);
        String digit=Integer.toString(number);g.text(client.font,digit,-client.font.width(digit)/2,0,color);
        g.pose().popMatrix();g.centeredText(client.font,caption,cx,y+55,0xFFA9BBC9);
        int filled=Math.round(w*number/10f);g.fill(cx-w/2,y+66,cx-w/2+filled,y+68,color);
    }
}
