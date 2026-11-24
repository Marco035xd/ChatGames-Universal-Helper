package dev.clares.chatgames.gui;

import dev.clares.chatgames.config.ConfigManager;
import dev.clares.chatgames.game.GameDifficulty;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** One category, with a base interval and three independently editable levels. */
public final class DifficultySettingsScreen extends Screen {
    private final ChatGamesScreen parent;
    private final ConfigManager settings;
    private final GameCatalog.Entry entry;
    private int x,y,w;
    private String notice="Cada nivel usa la dificultad anunciada por el servidor.";
    public DifficultySettingsScreen(ChatGamesScreen parent,ConfigManager settings,GameCatalog.Entry entry){
        super(Component.literal(entry.title()+" · tiempos por dificultad"));this.parent=parent;this.settings=settings;this.entry=entry;
    }
    @Override protected void init() {
        w=Math.min(440,width-24);x=(width-w)/2;y=Math.max(1,(height-238)/2);
        var c=settings.get();
        addRenderableWidget(GuiTheme.button("Tiempo base · "+TimeDraft.describe(c.delayFor(entry.key())),x+12,y+44,w-24,26,
            ()->minecraft.gui.setScreen(new TimeEditorScreen(this,settings,entry,"")),"Para retos sin nivel anunciado y niveles que heredan la base. No cambia los intervalos propios de Fácil, Media o Difícil."));
        int row=1;
        for(var level:GameDifficulty.values()) {
            String source=c.hasDifficultyDelay(entry.key(),level.key())?"Propio":"Usa base";
            String text=level.label()+" · "+TimeDraft.describe(c.delayFor(entry.key(),level.key()))+" · "+source;
            addRenderableWidget(GuiTheme.button(text,x+12,y+44+row*31,w-24,26,
                ()->minecraft.gui.setScreen(new TimeEditorScreen(this,settings,entry,level.key())),"Editar "+level.label()+". Admite tiempo fijo o intervalo decimal. Usar base elimina solo el tiempo propio de este nivel."));row++;
        }
        addRenderableWidget(GuiTheme.button("Volver",x+12,y+210,w-24,20,this::onClose,"Vuelve al panel. Cada intervalo se guarda desde su editor."));
    }
    public void saved(String title){notice=title+": intervalo guardado.";parent.saved(title);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta) {
        g.fill(0,0,width,height,0xC7080E19);GuiTheme.panel(g,x,y,w,238);
        GuiTheme.fit(g,font,entry.title()+" · tiempos por nivel",x+12,y+10,w-24,GuiTheme.GOLD);
        GuiTheme.fit(g,font,"Media = Normal · Difícil = Experto",x+12,y+24,w-24,GuiTheme.MUTED);
        GuiTheme.fit(g,font,"Sin nivel anunciado: usa el tiempo base.",x+12,y+177,w-24,GuiTheme.MUTED);
        GuiTheme.fit(g,font,notice,x+12,y+192,w-24,GuiTheme.GREEN);
        super.extractRenderState(g,mx,my,delta);
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
}
