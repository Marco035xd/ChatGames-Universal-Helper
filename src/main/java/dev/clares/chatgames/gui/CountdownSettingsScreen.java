package dev.clares.chatgames.gui;

import dev.clares.chatgames.config.ConfigManager;
import dev.clares.chatgames.countdown.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public final class CountdownSettingsScreen extends Screen {
    private final ChatGamesScreen parent;
    private final CountdownClock clock;
    private final CountdownDraft draft;
    private Button enabled,sound,fullTime,minus,plus,save;
    private EditBox interval,seconds;
    private static final int PANEL_HEIGHT=238;
    private int x,y,w;
    private String failure="";
    public CountdownSettingsScreen(ChatGamesScreen parent,ConfigManager settings,CountdownClock clock) {
        super(Component.literal("Contador del próximo minijuego"));this.parent=parent;this.clock=clock;draft=new CountdownDraft(settings);
    }
    @Override protected void init() {
        w=Math.min(440,width-24);x=(width-w)/2;y=Math.max(1,(height-PANEL_HEIGHT)/2);int inner=w-24,left=inner/2-4,field=(left-8)/2;
        enabled=button("",x+12,y+40,inner/2-4,20,()->{draft.enabled=!draft.enabled;refresh();},"Activa el contador privado. Tras el resultado muestra los minutos y segundos hasta el próximo juego. Es independiente de las respuestas automáticas.");
        sound=button("",x+12+inner/2+4,y+40,inner/2-4,20,()->{draft.sound=!draft.sound;refresh();},"Una nota musical por número, solo para ti; sin paquetes de recursos.");
        fullTime=button("",x+12,y+64,inner,18,()->{draft.fullTime=!draft.fullTime;refresh();},"Permanente: muestra el tiempo arriba durante toda la espera. Solo últimos 10 s: muestra el aviso final.");
        interval=addRenderableWidget(new EditBox(font,x+12,y+104,field,20,Component.literal("Minutos entre inicios de minijuegos")));
        interval.setMaxLength(4);interval.setValue(draft.interval);interval.setResponder(s->{draft.interval=s;failure="";refresh();});
        seconds=addRenderableWidget(new EditBox(font,x+20+field,y+104,field,20,Component.literal("Segundos adicionales del intervalo")));
        seconds.setMaxLength(2);seconds.setValue(draft.seconds);seconds.setResponder(s->{draft.seconds=s;failure="";refresh();});
        interval.setTooltip(Tooltip.create(Component.literal("Intervalo entre los INICIOS, no desde el ganador. Logs de Infection: aproximadamente 8 minutos y 1 segundo.")));
        seconds.setTooltip(Tooltip.create(Component.literal("De 0 a 59. Por ejemplo: 8 minutos y 1 segundo = 08:01.")));
        button("Probar 10 s",x+12+inner/2+4,y+104,inner/2-4,20,()->minecraft.gui.setScreen(new CountdownPreviewScreen(this,draft.sound,draft.volume/100f,draft.fullTime)),"Prueba el panel y las notas. No modifica el próximo evento ni guarda cambios.");
        minus=button("−",x+12,y+136,30,18,()->{draft.volume=Math.max(0,draft.volume-10);refresh();},"Baja el volumen de las notas un 10%.");
        plus=button("+",x+w-42,y+136,30,18,()->{draft.volume=Math.min(100,draft.volume+10);refresh();},"Sube el volumen de las notas un 10%.");
        button("Cancelar",x+12,y+210,inner/2-4,20,this::onClose,"Vuelve sin guardar los cambios del contador.");
        save=button("Guardar cambios",x+12+inner/2+4,y+210,inner/2-4,20,()->{
            try{draft.save();parent.countdownSaved();minecraft.gui.setScreen(parent);}catch(RuntimeException e){failure="No se pudo guardar el contador.";refresh();}
        },"Guarda intervalo, visibilidad, sonido y volumen. No cambia los tiempos de respuesta.");
        refresh();
    }
    private Button button(String label,int x,int y,int width,int height,Runnable action,String lore) {
        return addRenderableWidget(Button.builder(Component.literal(label),b->action.run()).bounds(x,y,width,height).tooltip(Tooltip.create(Component.literal(lore))).build());
    }
    private void refresh() {
        if(enabled==null||sound==null)return;
        enabled.setMessage(Component.literal(draft.enabled?"Contador: activado":"Contador: apagado").withStyle(draft.enabled?ChatFormatting.GREEN:ChatFormatting.RED));
        sound.setMessage(Component.literal(draft.sound?"Notas: activadas":"Notas: silenciadas").withStyle(draft.sound?ChatFormatting.GREEN:ChatFormatting.GRAY));
        if(fullTime!=null)fullTime.setMessage(Component.literal(draft.fullTime?"Vista: permanente (minutos:segundos)":"Vista: solo últimos 10 segundos"));
        if(minus!=null)minus.active=draft.volume>0;if(plus!=null)plus.active=draft.volume<100;if(save!=null)save.active=draft.error().isEmpty();
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta) {
        g.fill(0,0,width,height,0xC7080E19);GuiTheme.panel(g,x,y,w,PANEL_HEIGHT);
        GuiTheme.fit(g,font,"Nuevo minijuego · contador privado",x+12,y+10,w-24,GuiTheme.GOLD);
        GuiTheme.fit(g,font,"Intervalo entre inicios; se sincroniza con el reto.",x+12,y+24,w-24,GuiTheme.MUTED);
        g.text(font,"Minutos",x+12,y+92,GuiTheme.TEXT);g.text(font,"Segundos",seconds.getX(),y+92,GuiTheme.TEXT);
        g.centeredText(font,"Volumen: "+draft.volume+"%",x+w/2,y+141,GuiTheme.TEXT);
        long left=clock.remainingSeconds();String status=clock.roundActive()?"Minijuego en curso; después verás la cuenta.":left<0?"Esperando un reto para sincronizar.":"Próximo juego estimado: "+CountdownClock.formatTime(left);
        GuiTheme.fit(g,font,status,x+12,y+166,w-24,GuiTheme.MUTED);
        String error=draft.error();String message=!failure.isEmpty()?failure:!error.isEmpty()?error:"El servidor puede adelantar o retrasar el inicio.";
        GuiTheme.fit(g,font,message,x+12,y+188,w-24,!failure.isEmpty()||!error.isEmpty()?GuiTheme.RED:GuiTheme.MUTED);
        super.extractRenderState(g,mx,my,delta);
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
}
