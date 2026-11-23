package dev.clares.chatgames.command;
import dev.clares.chatgames.config.*;
import dev.clares.chatgames.countdown.CountdownClock;
import dev.clares.chatgames.game.*;
import dev.clares.chatgames.log.LogWatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import java.util.Locale;
import java.util.function.Consumer;

public final class ClientCommands {
    private final ConfigManager settings; private final GameManager games; private final LogWatcher watcher; private final Consumer<String> say; private final Consumer<Component> styled; private final Runnable openGui;private final CountdownClock countdown;
    public ClientCommands(ConfigManager settings,GameManager games,LogWatcher watcher,Consumer<String> say,Consumer<Component> styled) {this(settings,games,watcher,say,styled,()->{});}
    public ClientCommands(ConfigManager settings,GameManager games,LogWatcher watcher,Consumer<String> say,Consumer<Component> styled,Runnable openGui) {this(settings,games,watcher,say,styled,openGui,new CountdownClock(()->System.nanoTime()/1000000,()->settings.get().countdown.intervalMinutes,()->settings.get().countdown.intervalSeconds));}
    public ClientCommands(ConfigManager settings,GameManager games,LogWatcher watcher,Consumer<String> say,Consumer<Component> styled,Runnable openGui,CountdownClock countdown) {this.countdown=countdown;this.settings=settings;this.games=games;this.watcher=watcher;this.say=say;this.styled=styled;this.openGui=openGui;}
    public void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(CommandTree.build(this::run,()->games.profiles().names())));
    }
    private int run(String args) {
        ModConfig c=settings.get(); String[] p=args.strip().split("\\s+",4);
        try {
            switch(p[0].toLowerCase(Locale.ROOT)) {
                case "gui","menu" -> openGui.run();
                case "help" -> {
                    int page=p.length==1?1:Integer.parseInt(p[1]);
                    if (page<1 || page>HelpPages.count()) {say.accept("Elige /cgedit help 1–"+HelpPages.count()+".");break;}
                    showHelp(page);
                }
                case "on","off","pause","resume" -> {boolean value=p[0].equalsIgnoreCase("on")||p[0].equalsIgnoreCase("resume");games.setEnabled(value);say.accept(value?"Respuestas automáticas: ACTIVADAS. Esperando el próximo juego.":"Respuestas automáticas: DESACTIVADAS. Envíos pendientes cancelados.");}
                case "profile" -> profile(p);
                case "report" -> {
                    var s=games.history().summary();say.accept("Sesión: "+s.rounds()+" retos · "+s.answered()+" con envío · "+s.pending()+" pendientes · media "+RoundHistory.seconds(s.averageMs()));
                    say.accept("Hasta 100 retos de esta ejecución. Enviar no confirma ganar.");
                }
                case "history" -> {
                    int page=p.length<2?1:Integer.parseInt(p[1]);var entries=games.history().entries();
                    int pages=Math.max(1,(entries.size()+4)/5);if(page<1||page>pages)throw new IllegalArgumentException("Elige una página entre 1 y "+pages+".");
                    say.accept("Historial · "+page+"/"+pages+" · "+entries.size()+" retos");
                    for(var e:entries.subList((page-1)*5,Math.min(page*5,entries.size())))say.accept(dev.clares.chatgames.gui.GameCatalog.title(e.type())+(e.difficulty().isEmpty()?"":" · "+e.difficulty())+" · "+e.status()+" · "+e.answer()+" · envío "+RoundHistory.seconds(e.sentAfterMs()));
                    if(entries.isEmpty())say.accept("Aún no hay retos detectados en esta ejecución.");
                }
                case "hud" -> {boolean previous=c.hud;c.hud=toggle(p,1);try{settings.save();}catch(RuntimeException e){c.hud=previous;throw e;}say.accept("HUD: "+(c.hud?"activado":"apagado"));}
                case "status" -> say.accept("Respuestas automáticas: "+(c.enabled?"ACTIVADAS":"DESACTIVADAS")+" | Avisos: "+(c.stats?"on":"off")+" | Log: "+watcher.watching()+" | Sesión: "+(games.current()==null?"ninguna":games.current().status));
                case "testlog" -> say.accept("Log: "+(watcher.watching()?"DETECTED":"WAITING")+" | Path: "+watcher.path()+" | Watching: "+watcher.watching()+" | Last chat line: "+watcher.lastChat());
                case "last" -> {GameSession s=games.last(); say.accept(s==null?"Sin desafío":"Tipo: "+s.type+" | Dificultad: "+(s.difficulty.isEmpty()?"no indicada":s.difficulty)+" | Entrada: "+s.question+" | Respuesta: "+s.answer+" | Fuente: "+s.source+" | Estado: "+s.status);}
                case "cache" -> say.accept("Entradas locales en caché: "+games.cacheSize());
                case "ai" -> say.accept("IA: "+c.ai.enabled+" / "+c.ai.provider+" / modelo "+c.ai.model+" / clave de entorno "+c.ai.apiKeyEnv);
                case "debug","stats" -> {boolean value=toggle(p,1); if(p[0].equalsIgnoreCase("debug"))c.debug=value;else c.stats=value;settings.save();say.accept(p[0].equalsIgnoreCase("stats")?"Avisos de respuesta enviada: "+(value?"on":"off")+". Para pausar las respuestas: /cgedit off":"debug: "+value);}
                case "game" -> {if(p.length<3)throw new IllegalArgumentException();GameType type=GameType.valueOf(p[1].toUpperCase(Locale.ROOT));boolean value=toggle(p,2);games.setGameEnabled(type,value);say.accept(p[1]+": "+(value?"ACTIVADO":"DESACTIVADO; envío pendiente cancelado"));}
                case "countdown" -> CountdownCommand.execute(settings,countdown,args.length()>9?args.substring(9).strip():"").forEach(say);
                case "settime" -> TimeCommand.execute(settings,args.length()>7?args.substring(7).strip():"").forEach(say);
                case "test" -> {if(p.length<3)throw new IllegalArgumentException();GameType t=GameType.valueOf(p[1].toUpperCase(Locale.ROOT));String input=args.substring(args.indexOf(p[1])+p[1].length()).strip();say.accept("Test "+t+": "+games.solveLocal(t,input).orElse("sin respuesta local"));}
                case "simulate" -> {if(p.length<2)throw new IllegalArgumentException();games.simulate(args.substring(args.indexOf(' ')+1).replaceAll("^\\\"|\\\"$", ""));}
                default -> say.accept("Comando desconocido. /cgedit help");
            }
        } catch (IllegalArgumentException ex) {say.accept(ex.getMessage()==null?"Uso inválido. /cgedit help":ex.getMessage());} catch (Exception ex) {say.accept("No se pudo completar la acción. Revisa los archivos de configuración.");}
        return 1;
    }
    private void profile(String[] p) {
        var store=games.profiles();String command=p.length>1?p[1]:"list";
        switch(command) {
            case "list" -> {say.accept("Perfiles: "+String.join(", ",store.names()));say.accept("/cgedit profile save <nombre> · show <nombre> · load <nombre> · undo");}
            case "save" -> {store.saveNew(p[2]);say.accept("Perfil guardado: "+p[2]);}
            case "show" -> {var profile=store.read(p[2]);say.accept("Perfil "+p[2]+" · General: "+dev.clares.chatgames.gui.TimeDraft.describe(profile.delay));
                for(var type:GameType.values()){String key=type.name().toLowerCase(Locale.ROOT);if(profile.games.containsKey(key)||profile.gameDelays.containsKey(key))say.accept(dev.clares.chatgames.gui.GameCatalog.title(type)+": "+(profile.games.getOrDefault(key,false)?"activo":"pausado")+" · "+dev.clares.chatgames.gui.TimeDraft.describe(profile.gameDelays.getOrDefault(key,profile.delay)));}
                say.accept("Contador: "+CountdownClock.formatTime(profile.countdown.totalSeconds())+" · HUD: "+(profile.hud?"sí":"no")+" · Avisos: "+(profile.stats?"sí":"no"));}
            case "load" -> {games.applyProfile(p[2]);say.accept("Perfil aplicado: "+p[2]+". Pendientes cancelados. Deshacer: /cgedit profile undo");}
            case "undo" -> {games.applyProfile(null);say.accept("Configuración anterior restaurada.");}
            case "delete" -> {store.read(p[2]);if(p.length<4||!p[3].equals("confirm"))say.accept("Para eliminarlo: /cgedit profile delete "+p[2]+" confirm");else {store.delete(p[2]);say.accept("Perfil eliminado: "+p[2]);}}
            default -> throw new IllegalArgumentException("Usa /cgedit profile list.");
        }
    }
    private void showHelp(int number) {
        HelpPages.Page page=HelpPages.page(number);
        styled.accept(Component.literal("ChatGames · "+page.title()+" · "+number+"/"+HelpPages.count()).withStyle(ChatFormatting.GOLD,ChatFormatting.BOLD));
        for (HelpPages.Entry entry:page.entries()) {
            styled.accept(Component.literal(entry.command()).withStyle(ChatFormatting.AQUA)
                .withStyle(style->style.withClickEvent(new net.minecraft.network.chat.ClickEvent.SuggestCommand(entry.command().split(" · ")[0]))
                    .withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(Component.literal("Clic para preparar el comando; no lo ejecuta."))))
                .append(Component.literal("\n  "+entry.description()).withStyle(ChatFormatting.GRAY)));
        }
        styled.accept(Component.literal("Más ayuda: /cgedit help 1–"+HelpPages.count()+" · G: abrir · H: pausar").withStyle(ChatFormatting.YELLOW));
    }
    private static boolean toggle(String[] p,int i) {if(p.length<=i||!p[i].equalsIgnoreCase("on")&&!p[i].equalsIgnoreCase("off"))throw new IllegalArgumentException();return p[i].equalsIgnoreCase("on");}
}
