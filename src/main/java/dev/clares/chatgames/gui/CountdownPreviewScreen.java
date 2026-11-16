package dev.clares.chatgames.gui;

import dev.clares.chatgames.countdown.*;
import dev.clares.chatgames.hud.CountdownHud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** A ten-second demo with its own clock. Never changes the actual server estimate. */
public final class CountdownPreviewScreen extends Screen {
    private final Screen parent;
    private final boolean sound;
    private final float volume;
    private final boolean fullTime;
    private final CountdownClock preview=new CountdownClock(()->System.nanoTime()/1000000,()->8,()->1);
    public CountdownPreviewScreen(Screen parent,boolean sound,float volume,boolean fullTime) {
        super(Component.literal("Vista previa del contador"));this.parent=parent;this.sound=sound;this.volume=volume;this.fullTime=fullTime;preview.syncRemaining(10);
    }
    @Override public void tick() {
        int beat=preview.tick(true);
        if(beat>0&&sound&&volume>0)CountdownSound.play(minecraft,beat,volume);
        if(preview.remainingSeconds()<0)onClose();
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta) {
        g.fill(0,0,width,height,0x50080E19);
        if(fullTime)CountdownHud.drawCompact(g,minecraft,"Nuevo minijuego en "+CountdownClock.formatTime(preview.remainingSeconds()),"Vista previa · minutos:segundos");
        int number=preview.seconds();if(number>0)CountdownHud.draw(g,minecraft,number,"Vista previa");
        g.centeredText(font,"Prueba privada · no modifica el próximo evento",width/2,height-36,GuiTheme.MUTED);
        g.centeredText(font,"Escape: volver",width/2,height-22,GuiTheme.MUTED);
        super.extractRenderState(g,mx,my,delta);
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
}
