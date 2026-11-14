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
    private Button enabled,sound,minus,plus,save;
    private EditBox interval;
    private int x,y,w;
    private String failure="";
    public CountdownSettingsScreen(ChatGamesScreen parent,ConfigManager settings,CountdownClock clock) {
        super(Component.literal("Aviso de próximo minijuego"));this.parent=parent;this.clock=clock;draft=new CountdownDraft(settings);
    }
    @Override protected void init() {
        w=Math.min(440,width-24);x=(width-w)/2;y=Math.max(2,(height-218)/2);int inner=w-24;
        enabled=button("",x+12,y+40,inner/2-4,20,()->{draft.enabled=!draft.enabled;refresh();},"Muestra la cuenta 10…1 en pantalla. Es independiente de las respuestas automáticas.");
        sound=button("",x+12+inner/2+4,y+40,inner/2-4,20,()->{draft.sound=!draft.sound;refresh();},"Una nota musical por número, solo para ti; sin paquetes de recursos.");
        interval=addRenderableWidget(new EditBox(font,x+12,y+82,inner/2-4,20,Component.literal("Intervalo entre juegos en minutos")));
        interval.setMaxLength(4);interval.setValue(draft.interval);interval.setResponder(s->{draft.interval=s;failure="";refresh();});
        button("Probar aviso",x+12+inner/2+4,y+82,inner/2-4,20,()->minecraft.gui.setScreen(new CountdownPreviewScreen(this,draft.sound,draft.volume/100f)),"Prueba los diez números y las notas. No modifica el próximo evento ni guarda cambios.");
        minus=button("−",x+12,y+114,30,20,()->{draft.volume=Math.max(0,draft.volume-10);refresh();},"Baja el volumen de las notas un 10%.");
        plus=button("+",x+w-42,y+114,30,20,()->{draft.volume=Math.min(100,draft.volume+10);refresh();},"Sube el volumen de las notas un 10%.");
        button("Cancelar",x+12,y+188,inner/2-4,20,this::onClose,"Vuelve sin guardar los cambios del contador.");
        save=button("Guardar cambios",x+12+inner/2+4,y+188,inner/2-4,20,()->{
            try{draft.save();parent.saved("Contador");minecraft.gui.setScreen(parent);}catch(RuntimeException e){failure="No se pudo guardar el contador.";refresh();}
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
        if(minus!=null)minus.active=draft.volume>0;if(plus!=null)plus.active=draft.volume<100;if(save!=null)save.active=draft.error().isEmpty();
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta) {
        g.fill(0,0,width,height,0xC7080E19);GuiTheme.panel(g,x,y,w,218);
        GuiTheme.fit(g,font,"Nuevo minijuego · aviso de 10 segundos",x+12,y+10,w-24,GuiTheme.GOLD);
        GuiTheme.fit(g,font,"Cuenta visible y notas musicales privadas.",x+12,y+24,w-24,GuiTheme.MUTED);
        g.text(font,"Intervalo (minutos)",x+12,y+70,GuiTheme.TEXT);
        g.centeredText(font,"Volumen: "+draft.volume+"%",x+w/2,y+120,GuiTheme.TEXT);
        long left=clock.remainingSeconds();String status=left<0?"Esperando un reto para sincronizar.":"Próximo juego estimado: "+left/60+"m "+left%60+"s";
        GuiTheme.fit(g,font,status,x+12,y+148,w-24,GuiTheme.MUTED);
        String error=draft.error();String message=!failure.isEmpty()?failure:!error.isEmpty()?error:"El servidor puede adelantar o retrasar el inicio.";
        GuiTheme.fit(g,font,message,x+12,y+169,w-24,!failure.isEmpty()||!error.isEmpty()?GuiTheme.RED:GuiTheme.MUTED);
        super.extractRenderState(g,mx,my,delta);
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
}
