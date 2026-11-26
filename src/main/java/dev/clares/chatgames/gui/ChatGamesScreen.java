package dev.clares.chatgames.gui;

import dev.clares.chatgames.config.*;
import dev.clares.chatgames.countdown.CountdownClock;
import dev.clares.chatgames.game.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** A private control centre; all actions use the same services as the chat commands. */
public final class ChatGamesScreen extends Screen {
    private final ConfigManager settings;
    private final GameManager games;
    private final Screen parent;
    private final CountdownClock countdown;
    private DashboardLayout layout;
    private int tab,page,filter,practiceMode;
    private String query="",profileName="",selectedProfile="",practiceText="6 * 7";
    private String notice="Tus controles de ChatGames · G para abrir";
    private String pendingDelete="";
    private ProfileStore.Profile profile;
    private CompletableFuture<Optional<String>> result;
    private String testedText="";
    private GameType testedType;
    private EditBox search;
    private java.util.List<RoundHistory.Entry> visibleHistory=java.util.List.of();
    private static final String[] TABS={"Inicio","Juegos","Historial","Perfiles","Pruebas"};
    private static final DateTimeFormatter TIME=DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final GameType[] PRACTICE={GameType.MATH,GameType.VARIABLE,GameType.TRIVIA,GameType.UNSCRAMBLE,GameType.FILLOUT,GameType.UNREVERSE,GameType.REACTION};
    private static final String[] EXAMPLES={"6 * 7","3*x + 4 = 19","¿Cuál es la capital de Alemania?","aotP","_abbi_","ssalgypS","Hola"};
    public ChatGamesScreen(ConfigManager settings,GameManager games,Screen parent,CountdownClock countdown) {
        super(Component.literal("ChatGames Infection Server · Centro de control"));this.countdown=countdown;this.settings=settings;this.games=games;this.parent=parent;
    }
    @Override protected void init() {
        layout=DashboardLayout.of(width,height);
        int x=layout.x()+12,y=layout.y(),w=layout.width()-24,nav=(w-16)/5;
        for(int i=0;i<TABS.length;i++) {
            final int target=i;
            Button b=button(TABS[i],x+i*(nav+4),y+43,nav,20,()->{tab=target;page=0;pendingDelete="";rebuildWidgets();},"Abrir "+TABS[i]);
            ((FlatButton)b).selected(i==tab);
        }
        switch(tab){case 0->home();case 1->gameList();case 2->history();case 3->profiles();case 4->practice();}
        button("Cerrar",x+w-64,layout.footer(),64,18,this::onClose,"Los cambios guardados se conservan. Escape también cierra.");
    }
    private void home() {
        int x=layout.x()+12,y=layout.y(),w=layout.width()-24,half=(w-6)/2;
        button(settings.get().enabled?"Respuestas: activadas":"Respuestas: en pausa",x,y+146,half,20,()->{
            games.setEnabled(!settings.get().enabled);notice=settings.get().enabled?"Activado para los próximos retos.":"En pausa; envíos pendientes cancelados.";rebuildWidgets();
        },"Pausa todas las respuestas sin cambiar los estados individuales.");
        button("Contador",x+half+6,y+146,half,20,()->minecraft.gui.setScreen(new CountdownSettingsScreen(this,settings,countdown)),"Panel superior, intervalo, notas y vista previa.");
        button("Tiempo general",x,y+170,half,20,()->minecraft.gui.setScreen(new TimeEditorScreen(this,settings,null)),"Los intervalos propios de cada juego se conservan.");
        button("Avisos: "+(settings.get().stats?"sí":"no"),x+half+6,y+170,(half-4)/2,20,()->{
            boolean old=settings.get().stats;settings.get().stats=!old;try{settings.save();}catch(RuntimeException e){settings.get().stats=old;throw e;}notice="Avisos locales actualizados.";rebuildWidgets();
        },"Notificación local de respuesta enviada.");
        button("HUD: "+(settings.get().hud?"sí":"no"),x+half+10+(half-4)/2,y+170,(half-4)/2,20,()->{
            boolean old=settings.get().hud;settings.get().hud=!old;try{settings.save();}catch(RuntimeException e){settings.get().hud=old;throw e;}notice="HUD actualizado.";rebuildWidgets();
        },"Panel del reto activo durante el juego.");
    }
    private void gameList() {
        int x=layout.x()+12,y=layout.y(),w=layout.width()-24;
        search=addRenderableWidget(new EditBox(font,x,y+74,w-106,20,Component.literal("Buscar minijuego")));
        search.setMaxLength(60);search.setValue(query);search.setHint(Component.literal("Buscar: trivia, matemáticas…"));
        search.setResponder(value->{query=value;page=0;rebuildWidgets();search.setFocused(true);setFocused(search);});
        button(new String[]{"Todos","Activos","Pausados"}[filter],x+w-100,y+74,100,20,()->{filter=(filter+1)%3;page=0;rebuildWidgets();},"Filtra los minijuegos por su estado individual.");
        var entries=GameFilter.find(settings.get(),query,filter);int size=layout.pageSize();page=Math.min(page,Math.max(0,(entries.size()-1)/size));
        for(int i=page*size;i<Math.min(entries.size(),(page+1)*size);i++) {
            var e=entries.get(i);int slot=i-page*size,cx=layout.cardX(slot),cy=layout.cardY(slot),cw=layout.cardWidth();
            boolean levels=settings.get().difficultyDelays.containsKey(e.key());
            var card=addRenderableWidget(new GameCard(cx,cy,cw,e,(levels?"Base: ":"")+TimeDraft.describe(settings.get().delayFor(e.key())),levels?"Por nivel":settings.get().gameDelays.containsKey(e.key())?"Propio":"General",b->minecraft.gui.setScreen(new DifficultySettingsScreen(this,settings,e))));
            StringBuilder times=new StringBuilder();
            for(var level:dev.clares.chatgames.game.GameDifficulty.values())times.append("\n").append(level.label()).append(": ").append(TimeDraft.describe(settings.get().delayFor(e.key(),level.key())));
            card.setTooltip(Tooltip.create(Component.literal(e.title()+"\n\n"+e.description()+"\n"+e.detail()+"\n"+times+"\n\nClic: editar tiempo base, Fácil, Media y Difícil.\n"+(settings.get().enabled?"":"Respuestas en pausa general."))));
            button(settings.get().game(e.key())?"Activo":"Pausado",cx+cw-58,cy+34,58,18,()->{
                games.setGameEnabled(e.type(),!settings.get().game(e.key()));notice=e.title()+": estado guardado.";rebuildWidgets();
            },"Activar o pausar "+e.title()+". Pausar cancela su envío pendiente.");
        }
        paging(entries.size(),size);
    }
    private void history() {
        var entries=games.history().entries();visibleHistory=entries;int size=Math.max(1,(layout.height()-134)/32);page=Math.min(page,Math.max(0,(entries.size()-1)/size));
        for(int i=page*size;i<Math.min(entries.size(),(page+1)*size);i++) {
            var e=entries.get(i);String line=TIME.format(Instant.ofEpochMilli(e.started()))+" · "+title(e.type())+(e.difficulty().isEmpty()?"":" · "+e.difficulty())+" · "+e.status();
            button(line,layout.x()+12,layout.y()+102+(i-page*size)*32,layout.width()-24,28,()->minecraft.gui.setScreen(new RoundDetailScreen(this,e)),
                "Reto: "+e.question()+"\nRespuesta: "+(e.answer().isEmpty()?"—":e.answer())+"\nClic: ver tiempos y resultado.");
        }
        paging(entries.size(),size);
    }
    private void profiles() {
        int x=layout.x()+12,y=layout.y(),w=layout.width()-24,left=(w-12)/2,right=x+left+12;
        EditBox name=addRenderableWidget(new EditBox(font,x,y+74,w-98,20,Component.literal("Nombre del perfil")));
        name.setMaxLength(24);name.setValue(profileName);name.setHint(Component.literal("Nombre: infection, tranquilo…"));name.setResponder(s->profileName=s);
        button("Guardar nuevo",x+w-92,y+74,92,20,()->{games.profiles().saveNew(profileName);selectProfile(profileName);notice="Perfil guardado: "+profileName;rebuildWidgets();},"Guarda tiempos, juegos, HUD, avisos y contador. No sobrescribe perfiles existentes.");
        var names=games.profiles().names();int size=Math.max(1,(layout.height()-180)/24);page=Math.min(page,Math.max(0,(names.size()-1)/size));
        for(int i=page*size;i<Math.min(names.size(),(page+1)*size);i++) {
            String n=names.get(i);Button b=button(n,x,y+102+(i-page*size)*24,left,20,()->{selectProfile(n);rebuildWidgets();},"Vista previa antes de aplicar "+n);b.active=!n.equals(selectedProfile);
        }
        button(pendingDelete.equals(selectedProfile)&&!selectedProfile.isEmpty()?"Confirmar borrado":"Eliminar perfil",x,y+layout.height()-76,left,20,()->{
            if(selectedProfile.isEmpty())return;
            if(!pendingDelete.equals(selectedProfile)){pendingDelete=selectedProfile;notice="Pulsa Confirmar borrado para eliminar solo este perfil.";}
            else{games.profiles().delete(selectedProfile);selectedProfile="";pendingDelete="";profile=null;notice="Perfil eliminado; ajustes actuales conservados.";}
            rebuildWidgets();
        },"Elimina el archivo seleccionado con confirmación. No cambia tus ajustes.").active=profile!=null;
        if(profile!=null)button("Ver ajustes: "+selectedProfile,right,y+100,left,16,()->{
            var details=new ArrayList<String>();details.add("General: "+TimeDraft.describe(profile.delay));
            for(var type:GameType.values()){String key=type.name().toLowerCase(Locale.ROOT);if(profile.games.containsKey(key)||profile.gameDelays.containsKey(key))details.add(title(type)+": "+(profile.games.getOrDefault(key,false)?"activo":"pausado")+" · "+TimeDraft.describe(profile.gameDelays.getOrDefault(key,profile.delay)));}
            profile.difficultyDelays.forEach((game,levels)->levels.forEach((level,d)->details.add(game+" · "+GameDifficulty.parse(level).orElseThrow().label()+": "+TimeDraft.describe(d))));
            details.add("Contador: "+CountdownClock.formatTime(profile.countdown.totalSeconds())+" · "+(profile.countdown.enabled?"activado":"apagado"));
            details.add("Notas: "+(profile.countdown.sound?"sí":"no")+" · Volumen: "+Math.round(profile.countdown.volume*100)+"%");
            details.add("Vista: "+(profile.countdown.fullTime?"permanente":"últimos 10 s"));
            details.add("HUD: "+(profile.hud?"sí":"no")+" · Avisos: "+(profile.stats?"sí":"no"));
            details.add("La pausa general, la IA y tus diccionarios se conservan.");
            minecraft.gui.setScreen(new InfoScreen(this,"Perfil · "+selectedProfile,details));
        },"Consulta todos los ajustes de este perfil antes de aplicarlo.");
        button("Aplicar perfil",right,y+layout.height()-76,left,20,()->{games.applyProfile(selectedProfile);notice="Perfil aplicado; envíos anteriores cancelados.";rebuildWidgets();},"Carga la vista previa. Conserva la pausa general; cancela respuestas pendientes.").active=profile!=null;
        button("Deshacer carga",right,y+layout.height()-52,left,20,()->{games.applyProfile(null);notice="Ajustes anteriores restaurados.";rebuildWidgets();},"Restaura los ajustes anteriores a la última carga. También disponible tras reiniciar.").active=games.profiles().canUndo();
        button("‹",x,y+layout.height()-52,24,20,()->{page--;rebuildWidgets();},"Perfiles anteriores").active=page>0;
        button("›",x+28,y+layout.height()-52,24,20,()->{page++;rebuildWidgets();},"Más perfiles").active=(page+1)*size<names.size();
    }
    private void selectProfile(String name){profile=games.profiles().read(name);selectedProfile=name;pendingDelete="";}
    private void practice() {
        int x=layout.x()+12,y=layout.y(),w=layout.width()-24;
        button("Tipo: "+title(PRACTICE[practiceMode]),x,y+74,w,20,()->{practiceMode=(practiceMode+1)%PRACTICE.length;result=null;rebuildWidgets();},"Cambia el tipo. Usa Ejemplo para cargar un reto de ese tipo.");
        var input=addRenderableWidget(new EditBox(font,x,y+102,w,20,Component.literal("Reto de prueba")));
        input.setMaxLength(300);input.setValue(practiceText);input.setResponder(s->{practiceText=s;result=null;});
        button("Ejemplo",x,y+130,(w-6)/2,20,()->{practiceText=EXAMPLES[practiceMode];result=null;rebuildWidgets();},"Carga un ejemplo; no lo envía al chat.");
        button("Resolver localmente",x+(w-6)/2+6,y+130,(w-6)/2,20,()->{
            if(practiceText.isBlank())throw new IllegalArgumentException("Escribe un reto primero.");
            testedText=practiceText;testedType=PRACTICE[practiceMode];result=games.preview(testedType,testedText);notice="Prueba local iniciada.";
        },"Solo usa el solucionador local. No envía mensajes al servidor ni consulta una IA.");
    }
    private void paging(int total,int size) {
        int x=layout.x()+12,y=layout.footer();
        button("‹",x,y,24,18,()->{page--;rebuildWidgets();},"Página anterior").active=page>0;
        button("›",x+28,y,24,18,()->{page++;rebuildWidgets();},"Página siguiente").active=(page+1)*size<total;
    }
    private Button button(String label,int x,int y,int w,int h,Runnable action,String hint) {
        return addRenderableWidget(GuiTheme.button(label,x,y,w,h,()->{try{action.run();}catch(RuntimeException e){notice=e instanceof IllegalArgumentException?e.getMessage():"No se pudo guardar. Revisa los archivos de configuración.";rebuildWidgets();}},hint));
    }
    public void saved(String title){notice=title+": tiempo guardado para los próximos retos.";}
    public void countdownSaved(){notice="Contador guardado.";}
    @Override public void tick(){if(tab==2&&!visibleHistory.equals(games.history().entries()))rebuildWidgets();}
    public static String title(GameType t){return GameCatalog.ENTRIES.stream().filter(e->e.type()==t).map(GameCatalog.Entry::title).findFirst().orElse(t.name());}
    public static String seconds(long ms){return ms<0?"—":String.format(Locale.ROOT,"%.3f s",ms/1000.0);}
    private void text(GuiGraphicsExtractor g,String text,int dx,int dy,int w,int color){GuiTheme.fit(g,font,text,layout.x()+dx,layout.y()+dy,w,color);}
    private void metric(GuiGraphicsExtractor g,int x,int y,int w,String label,String value) {
        g.fill(x,y,x+w,y+36,0xFF203044);g.text(font,label,x+8,y+6,GuiTheme.MUTED);GuiTheme.fit(g,font,value,x+8,y+21,w-16,GuiTheme.GREEN);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta) {
        g.fill(0,0,width,height,0xC9080E19);GuiTheme.panel(g,layout.x(),layout.y(),layout.width(),layout.height());
        int w=layout.width()-24,x=layout.x()+12,y=layout.y();
        text(g,"CHATGAMES",12,10,w,GuiTheme.GOLD);
        String badge=settings.get().enabled?"ACTIVO":"EN PAUSA";g.text(font,badge,x+w-font.width(badge),y+10,settings.get().enabled?GuiTheme.GREEN:GuiTheme.RED);
        text(g,"Infection Server · 1.4.2 · "+TABS[tab],12,24,w,GuiTheme.MUTED);
        if(tab==0) {
            var s=games.history().summary();int mw=(w-12)/3;
            metric(g,x,y+74,mw,"Retos",Integer.toString(s.rounds()));metric(g,x+mw+6,y+74,mw,"Con envío",Integer.toString(s.answered()));metric(g,x+2*(mw+6),y+74,mw,"Media envío",seconds(s.averageMs()));
            text(g,countdown.displayText(),12,120,w,GuiTheme.GREEN);
            text(g,"Estimación · intervalo "+CountdownClock.formatTime(countdown.intervalSeconds()),12,133,w,GuiTheme.MUTED);
            if(layout.height()>290) {
                text(g,"TU SESIÓN",12,208,w,GuiTheme.GOLD);
                text(g,"Historial de hasta 100 retos detectados durante esta ejecución.",12,224,w,GuiTheme.MUTED);
                text(g,"Tiempo real: desde recibir el reto completo hasta enviarlo.",12,239,w,GuiTheme.MUTED);
                text(g,"G abre este panel. H pausa o reactiva las respuestas.",12,254,w,GuiTheme.TEXT);
                text(g,"Comandos: /cgedit help · /cgedit history · /cgedit profile",12,269,w,GuiTheme.TEXT);
            }
        } else if(tab==1) {
            var list=GameFilter.find(settings.get(),query,filter);int visible=Math.min(layout.pageSize(),list.size()-page*layout.pageSize());
            for(int i=0;i<visible;i++)g.fill(layout.cardX(i),layout.cardY(i),layout.cardX(i)+layout.cardWidth(),layout.cardY(i)+52,0xFF203044);
            if(list.isEmpty())text(g,"Sin resultados. Cambia la búsqueda o el filtro.",12,112,w,GuiTheme.MUTED);
        } else if(tab==2) {
            var s=games.history().summary();text(g,s.rounds()+" retos · "+s.answered()+" con envío · media "+seconds(s.averageMs()),12,78,w,GuiTheme.GREEN);
            text(g,"Clic en una ronda: respuesta y tiempos. Enviar no confirma ganar.",12,90,w,GuiTheme.MUTED);
            if(s.rounds()==0)text(g,"El próximo reto detectado aparecerá aquí.",12,118,w,GuiTheme.MUTED);
        } else if(tab==3) {
            int left=(w-12)/2,dx=12+left+12;
            if(profile==null)text(g,"Selecciona un perfil",dx,103,left,GuiTheme.GOLD);
            if(profile!=null){text(g,"General: "+TimeDraft.describe(profile.delay),dx,117,left,GuiTheme.TEXT);text(g,profile.games.values().stream().filter(Boolean::booleanValue).count()+" activos · "+profile.gameDelays.size()+" tiempos propios",dx,131,left,GuiTheme.MUTED);}
            if(layout.height()>270&&profile!=null){text(g,"Contador: "+CountdownClock.formatTime(profile.countdown.totalSeconds()),dx,150,left,GuiTheme.TEXT);text(g,"HUD: "+(profile.hud?"sí":"no")+" · Avisos: "+(profile.stats?"sí":"no"),dx,164,left,GuiTheme.MUTED);}
        } else if(tab==4) {
            String answer=result==null?"Escribe un reto y pulsa Resolver.":!result.isDone()?"Resolviendo…":result.isCompletedExceptionally()?"No se pudo completar la prueba.":result.getNow(Optional.empty()).orElse("Sin respuesta local fiable.");
            text(g,result!=null&&result.isDone()?"Resultado: "+answer:answer,12,162,w,GuiTheme.GREEN);
            if(layout.height()>250){text(g,result==null?"Prueba privada · no envía respuestas":"Prueba: "+title(testedType)+" · "+testedText,12,181,w,GuiTheme.MUTED);text(g,"Usa tus diccionarios y el banco local. No modifica las rondas.",12,199,w,GuiTheme.MUTED);}
        }
        int offset=(tab==1||tab==2)?76:12;
        String footer=(tab==1||tab==2)?"Pág. "+(page+1)+" · "+notice:notice;
        GuiTheme.fit(g,font,footer,layout.x()+offset,layout.footer()+5,layout.width()-offset-86,GuiTheme.MUTED);
        super.extractRenderState(g,mx,my,delta);
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
}
