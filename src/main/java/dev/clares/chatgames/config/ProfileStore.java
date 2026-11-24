package dev.clares.chatgames.config;

import java.nio.file.*;
import java.util.*;

/** Portable preferences only: never copies log paths, AI credentials or learned answers. */
public final class ProfileStore {
    public static final class Profile {
        public int format=1;
        public ModConfig.Delay delay;
        public Map<String,ModConfig.Delay> gameDelays;
        public Map<String,Map<String,ModConfig.Delay>> difficultyDelays=new LinkedHashMap<>();
        public Map<String,Boolean> games;
        public ModConfig.Countdown countdown;
        public boolean hud,stats;
    }
    private final ConfigManager settings;
    private final Path directory;
    public ProfileStore(ConfigManager settings){this.settings=settings;directory=settings.dir().resolve("profiles");}
    public synchronized List<String> names() {
        if(!Files.isDirectory(directory))return List.of();
        try(var files=Files.list(directory)) {
            return files.map(p->p.getFileName().toString()).filter(n->n.matches("[a-z0-9_-]{1,24}\\.json"))
                .map(n->n.substring(0,n.length()-5)).sorted().toList();
        }catch(Exception e){throw new IllegalArgumentException("No se pudieron leer los perfiles.",e);}
    }
    private Path file(String name) {
        if(name==null||(!name.matches("[a-z0-9_-]{1,24}")||name.matches("con|prn|aux|nul|com[1-9]|lpt[1-9]")))throw new IllegalArgumentException("Nombre: 1–24 letras minúsculas, números, _ o -.");
        return directory.resolve(name+".json");
    }
    public synchronized void saveNew(String name) {
        Path file=file(name);
        if(Files.exists(file))throw new IllegalArgumentException("Ese perfil ya existe. Usa otro nombre.");
        if(names().size()>=32)throw new IllegalArgumentException("Máximo 32 perfiles. Elimina uno antes de guardar otro.");
        Profile profile=capture();validate(profile);write(file,profile);
    }
    public synchronized Profile read(String name){return readFile(file(name));}
    private Profile readFile(Path path) {
        try {
            if(Files.size(path)>131072)throw new IllegalArgumentException("El perfil es demasiado grande.");
            Profile profile=ConfigManager.GSON.fromJson(Files.readString(path),Profile.class);
            validate(profile);return profile;
        }catch(IllegalArgumentException e){throw e;}
        catch(Exception e){throw new IllegalArgumentException("No se pudo leer el perfil.",e);}
    }
    private static void delay(ModConfig.Delay d) {
        if(d==null||!("fixed".equals(d.mode)||"random".equals(d.mode))||d.minMs<0||d.maxMs<d.minMs||d.maxMs>60000)
            throw new IllegalArgumentException("El perfil contiene un tiempo inválido.");
    }
    private static void validate(Profile p) {
        if(p==null||p.format!=1||p.games==null||p.gameDelays==null||p.countdown==null)throw new IllegalArgumentException("Formato de perfil inválido.");
        delay(p.delay);
        Set<String> allowed=new java.util.HashSet<>();for(var type:dev.clares.chatgames.game.GameType.values())if(type!=dev.clares.chatgames.game.GameType.UNKNOWN)allowed.add(type.name().toLowerCase(Locale.ROOT));
        if(!allowed.containsAll(p.games.keySet())||!allowed.containsAll(p.gameDelays.keySet())||p.games.containsValue(null))
            throw new IllegalArgumentException("El perfil contiene un juego inválido.");
        p.gameDelays.values().forEach(ProfileStore::delay);
        if(p.difficultyDelays==null)p.difficultyDelays=new LinkedHashMap<>();
        if(!allowed.containsAll(p.difficultyDelays.keySet()))throw new IllegalArgumentException("Juego inválido en tiempos por dificultad.");
        p.difficultyDelays.values().forEach(levels->{
            if(levels==null)throw new IllegalArgumentException("Tiempos por dificultad inválidos.");
            levels.forEach((level,d)->{
                if(dev.clares.chatgames.game.GameDifficulty.parse(level).map(v->!v.key().equals(level)).orElse(true))throw new IllegalArgumentException("Dificultad inválida en el perfil.");
                delay(d);
            });
        });
        var c=p.countdown;
        if(c.intervalMinutes<0||c.intervalMinutes>1440||c.intervalSeconds<0||c.intervalSeconds>59||c.totalSeconds()<1||c.totalSeconds()>86400||!Float.isFinite(c.volume)||c.volume<0||c.volume>1)
            throw new IllegalArgumentException("El perfil contiene un contador inválido.");
    }
    private Profile capture() {
        ModConfig c=settings.get();Profile p=new Profile();p.delay=c.delayFor("");
        p.gameDelays=new LinkedHashMap<>();synchronized(c.gameDelays){c.gameDelays.forEach((k,v)->p.gameDelays.put(k,v.copy()));}
        p.difficultyDelays=c.copyDifficultyDelays();
        synchronized(c.games){p.games=new LinkedHashMap<>(c.games);}
        p.countdown=ConfigManager.GSON.fromJson(ConfigManager.GSON.toJson(c.countdown),ModConfig.Countdown.class);
        p.hud=c.hud;p.stats=c.stats;return p;
    }
    private void install(Profile p) {
        ModConfig c=settings.get();
        synchronized(c){c.delay=p.delay.copy();c.gameDelays.clear();p.gameDelays.forEach((k,v)->c.gameDelays.put(k,v.copy()));
            c.difficultyDelays.clear();p.difficultyDelays.forEach((game,levels)->levels.forEach((level,d)->c.setDifficultyDelay(game,level,d)));
            c.games.clear();c.games.putAll(p.games);c.countdown=p.countdown;c.hud=p.hud;c.stats=p.stats;}
    }
    /** Called while the manager holds its response lock, after invalidating old sends. */
    public synchronized void load(String name) {apply(read(name));}
    public synchronized void undo() {apply(readFile(directory.resolve(".previous.json")));}
    private void apply(Profile target) {
        Profile previous=capture();
        write(directory.resolve(".previous.json"),previous);
        install(target);
        try{settings.save();}catch(RuntimeException e){install(previous);throw e;}
    }
    public synchronized boolean canUndo(){return Files.isRegularFile(directory.resolve(".previous.json"));}
    public synchronized void delete(String name) {
        try{if(!Files.deleteIfExists(file(name)))throw new IllegalArgumentException("El perfil no existe.");}
        catch(java.io.IOException e){throw new IllegalArgumentException("No se pudo eliminar el perfil.",e);}
    }
    private static void write(Path path,Profile value) {
        try{AtomicFiles.write(path,ConfigManager.GSON.toJson(value));}
        catch(Exception e){throw new IllegalStateException("No se pudo guardar el perfil.",e);}
    }
}
