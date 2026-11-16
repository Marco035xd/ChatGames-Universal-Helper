package dev.clares.chatgames.gui;

import dev.clares.chatgames.config.ConfigManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public final class TimeEditorScreen extends Screen {
    private final ChatGamesScreen parent;
    private final ConfigManager settings;
    private final TimeDraft draft;
    private final String gameTitle;
    private EditBox minimum,maximum;
    private Button mode,save;
    private Button minimumDown,minimumUp,maximumDown,maximumUp;
    private static final int PANEL_HEIGHT=238;
    private int x,y,w;
    private String failure="";
    public TimeEditorScreen(ChatGamesScreen parent,ConfigManager settings,GameCatalog.Entry entry) {
        super(Component.literal("Tiempo de respuesta"));this.parent=parent;this.settings=settings;
        gameTitle=entry==null?"Tiempo general":entry.title();draft=new TimeDraft(settings,entry==null?"":entry.key());
    }
    @Override protected void init() {
        w=Math.min(440,width-24);x=(width-w)/2;y=Math.max(1,(height-PANEL_HEIGHT)/2);int inner=w-24,field=(inner-12)/2;
        mode=button("",x+12,y+40,inner,20,()->{draft.nextMode();refresh();},"Fijo: por ejemplo 2.1 s. Aleatorio: por ejemplo de 2.1 a 2.9 s. General: hereda el intervalo común.");
        minimum=addRenderableWidget(new EditBox(font,x+12,y+80,field,20,Component.literal("Tiempo mínimo en segundos")));
        maximum=addRenderableWidget(new EditBox(font,x+24+field,y+80,field,20,Component.literal("Tiempo máximo en segundos")));
        minimum.setMaxLength(12);maximum.setMaxLength(12);minimum.setTextColorUneditable(0xFF7A8B9A);maximum.setTextColorUneditable(0xFF7A8B9A);
        minimum.setValue(draft.minimum);maximum.setValue(draft.maximum);
        minimum.setResponder(s->{draft.minimum=s;failure="";refresh();});
        maximum.setResponder(s->{draft.maximum=s;failure="";refresh();});
        minimum.setTooltip(Tooltip.create(Component.literal("De 0 a 60 segundos. Acepta punto o coma: 2.1 o 2,1. Usa los botones para ajustar de 0.1 en 0.1. También acepta 500ms.")));
        maximum.setTooltip(Tooltip.create(Component.literal("Límite superior con decimales: por ejemplo 2.9 o 2,9. Elige intervalo aleatorio para editarlo. Debe ser igual o mayor que el mínimo.")));
        int stepW=(field-4)/2;
        minimumDown=button("−0.1",x+12,y+104,stepW,18,()->step(false,-1),"Resta 0.1 segundos al tiempo mínimo o fijo.");
        minimumUp=button("+0.1",x+16+stepW,y+104,stepW,18,()->step(false,1),"Suma 0.1 segundos: 2.1, 2.2, 2.3…");
        maximumDown=button("−0.1",x+24+field,y+104,stepW,18,()->step(true,-1),"Resta 0.1 segundos al límite máximo.");
        maximumUp=button("+0.1",x+28+field+stepW,y+104,stepW,18,()->step(true,1),"Suma 0.1 segundos al límite máximo.");
        int presetW=(inner-16)/5;
        for(int i=0;i<5;i++) {
            int seconds=new int[]{0,1,2,3,5}[i];
            button(seconds+" s",x+12+i*(presetW+4),y+140,presetW,18,()->preset(seconds*1000,seconds*1000),"Tiempo fijo de "+seconds+(seconds==1?" segundo.":" segundos.")+" Ajusta las décimas con −0.1 y +0.1. Pulsa Guardar para aplicarlo.");
        }
        int rangeW=(inner-8)/3;
        button("0–1 s",x+12,y+162,rangeW,18,()->preset(0,1000),"Intervalo aleatorio de 0 a 1 segundo. Pulsa Guardar para aplicarlo.");
        button("2.1–2.9 s",x+16+rangeW,y+162,rangeW,18,()->preset(2100,2900),"Intervalo aleatorio entre 2.1 y 2.9 segundos para este minijuego. Puedes editar ambos límites.");
        button("3–5 s",x+20+rangeW*2,y+162,rangeW,18,()->preset(3000,5000),"Intervalo aleatorio de 3 a 5 segundos. Pulsa Guardar para aplicarlo.");
        button("Cancelar",x+12,y+210,inner/2-4,20,this::onClose,"Vuelve sin guardar este intervalo.");
        save=button("Guardar cambios",x+12+inner/2+4,y+210,inner/2-4,20,()->{
            try{draft.save();parent.saved(gameTitle);minecraft.gui.setScreen(parent);}
            catch(IllegalArgumentException e){failure=e.getMessage();refresh();}
            catch(RuntimeException e){failure="No se pudo guardar. Puedes volver a intentarlo.";refresh();}
        },"Guarda el intervalo en tu configuración. Se conserva después de reiniciar Minecraft.");
        refresh();
    }
    private void preset(int min,int max){draft.preset(min,max);minimum.setValue(draft.minimum);maximum.setValue(draft.maximum);refresh();}
    private void step(boolean upper,int direction) {
        try {
            if(upper)draft.stepMaximum(direction);else draft.stepMinimum(direction);
            failure="";
            minimum.setValue(draft.minimum);maximum.setValue(draft.maximum);
        }catch(IllegalArgumentException e){failure=e.getMessage();}
        refresh();
    }
    private void refresh() {
        if(mode==null||minimum==null||maximum==null)return;
        mode.setMessage(Component.literal(switch(draft.mode){case FIXED->"Modo: tiempo fijo";case RANDOM->"Modo: intervalo aleatorio";case INHERIT->"Modo: usar tiempo general";}));
        minimum.setEditable(draft.mode!=TimeDraft.Mode.INHERIT);minimum.active=draft.mode!=TimeDraft.Mode.INHERIT;
        maximum.setEditable(draft.mode==TimeDraft.Mode.RANDOM);maximum.active=draft.mode==TimeDraft.Mode.RANDOM;
        if(minimumDown!=null)minimumDown.active=minimumUp.active=draft.mode!=TimeDraft.Mode.INHERIT;
        if(maximumDown!=null)maximumDown.active=maximumUp.active=draft.mode==TimeDraft.Mode.RANDOM;
        if(save!=null)save.active=draft.error().isEmpty();
    }
    private Button button(String label,int x,int y,int width,int height,Runnable action,String lore) {
        return addRenderableWidget(GuiTheme.button(label,x,y,width,height,action,lore));
    }
    @Override public void extractRenderState(GuiGraphicsExtractor c,int mx,int my,float delta) {
        c.fill(0,0,width,height,0xC7080E19);GuiTheme.panel(c,x,y,w,PANEL_HEIGHT);
        GuiTheme.fit(c,font,gameTitle+" · tiempo de respuesta",x+12,y+10,w-24,GuiTheme.GOLD);
        GuiTheme.fit(c,font,"Decimales: 2.1 o 2,1 · Ajusta de 0.1 en 0.1",x+12,y+24,w-24,GuiTheme.MUTED);
        c.text(font,draft.mode==TimeDraft.Mode.FIXED?"Segundos":"Mínimo (s)",x+12,y+68,GuiTheme.TEXT);
        c.text(font,draft.mode==TimeDraft.Mode.FIXED?"No se usa":"Máximo (s)",x+24+(w-36)/2,y+68,draft.mode==TimeDraft.Mode.RANDOM?GuiTheme.TEXT:GuiTheme.MUTED);
        c.text(font,"Opciones rápidas",x+12,y+128,GuiTheme.MUTED);
        String error=draft.error();String message=!failure.isEmpty()?failure:!error.isEmpty()?error:
            draft.mode==TimeDraft.Mode.INHERIT?"General actual: "+TimeDraft.describe(settings.get().delayFor("")):"0 s = envío en cuanto la respuesta esté lista.";
        GuiTheme.fit(c,font,message,x+12,y+190,w-24,!failure.isEmpty()||!error.isEmpty()?GuiTheme.RED:GuiTheme.GREEN);
        super.extractRenderState(c,mx,my,delta);
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
}
