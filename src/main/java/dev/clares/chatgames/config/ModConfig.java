package dev.clares.chatgames.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import dev.clares.chatgames.game.GameDifficulty;

public final class ModConfig {
    public int configRevision = 0;
    public volatile boolean enabled = true;
    public boolean debug = false;
    public boolean stats = false;
    public boolean hud = true;
    public String logPath = "auto";
    public Delay delay = new Delay();
    public Map<String, Delay> gameDelays = new LinkedHashMap<>();
    public Map<String,Map<String,Delay>> difficultyDelays = new LinkedHashMap<>();
    public Map<String, Boolean> games = new LinkedHashMap<>();
    public Ai ai = new Ai();
    public volatile Countdown countdown = new Countdown();
    public int guessDelayMs = 2500;
    public int maxGuesses = 7;
    public ModConfig() {
        for (String s : new String[]{"math","trivia","reaction","random","unreverse","unscramble","variable","fillout","clickable","hoverable","guess_the_number"}) games.put(s, true);
    }
    public boolean game(String type) { return games.getOrDefault(type.toLowerCase(), false); }
    public synchronized Delay delayFor(String type) {
        Delay chosen=gameDelays.get(type.toLowerCase(Locale.ROOT));
        return (chosen==null?delay:chosen).copy();
    }
    public synchronized Delay delayFor(String type,String difficulty) {
        String key=GameDifficulty.parse(difficulty).map(GameDifficulty::key).orElse("");
        Delay chosen=difficultyDelays.getOrDefault(type.toLowerCase(Locale.ROOT),Map.of()).get(key);
        return chosen==null?delayFor(type):chosen.copy();
    }
    public synchronized boolean hasDifficultyDelay(String type,String difficulty) {
        return difficultyDelays.getOrDefault(type.toLowerCase(Locale.ROOT),Map.of()).containsKey(difficulty);
    }
    public synchronized void setDifficultyDelay(String type,String difficulty,Delay value) {
        String key=GameDifficulty.parse(difficulty).orElseThrow(()->new IllegalArgumentException("Dificultad inválida.")).key();
        String game=type.toLowerCase(Locale.ROOT);
        if(value==null){var levels=difficultyDelays.get(game);if(levels!=null){levels.remove(key);if(levels.isEmpty())difficultyDelays.remove(game);}}
        else difficultyDelays.computeIfAbsent(game,k->new LinkedHashMap<>()).put(key,value.copy());
    }
    public synchronized Map<String,Map<String,Delay>> copyDifficultyDelays() {
        Map<String,Map<String,Delay>> result=new LinkedHashMap<>();
        difficultyDelays.forEach((game,levels)->{Map<String,Delay> copy=new LinkedHashMap<>();levels.forEach((level,d)->copy.put(level,d.copy()));result.put(game,copy);});
        return result;
    }
    public synchronized void setDelay(String type,Delay value) {
        if(type.isEmpty())delay=value;
        else if(value==null)gameDelays.remove(type);
        else gameDelays.put(type,value);
    }
    public static final class Delay {
        public String mode = "random";
        public int minMs = 0, maxMs = 1000;
        public static Delay random(int min,int max) {Delay d=new Delay();d.mode="random";d.minMs=min;d.maxMs=max;return d;}
        public static Delay fixed(int ms) {Delay d=random(ms,ms);d.mode="fixed";return d;}
        public Delay copy() {Delay d=random(minMs,maxMs);d.mode=mode;return d;}
        public long sampleMillis() {
            long low=Math.max(0,Math.min(60000,minMs));
            long high=Math.max(low,Math.min(60000,maxMs));
            return "fixed".equalsIgnoreCase(mode)?low:ThreadLocalRandom.current().nextLong(low,high+1);
        }
    }
    public static final class Countdown {
        public boolean enabled=true, sound=true;
        public boolean fullTime=true;
        public volatile int intervalMinutes=8, intervalSeconds=1;
        public float volume=0.6f;
        public int totalSeconds(){return intervalMinutes*60+intervalSeconds;}
    }
    public static final class Ai { public boolean enabled = false; public String provider = "disabled"; public String endpoint = ""; public String model = ""; public String apiKeyEnv = "CGH_API_KEY"; public int timeoutMs = 4000; }
}
