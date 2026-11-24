package dev.clares.chatgames.command;

import dev.clares.chatgames.config.*;
import dev.clares.chatgames.game.GameType;
import dev.clares.chatgames.game.GameDifficulty;
import java.util.*;

/** Validates the complete interval before replacing any saved setting. */
public final class TimeCommand {
    private TimeCommand() { }
    public static List<String> execute(ConfigManager settings,String input) {
        ModConfig config=settings.get();
        if(input.isBlank()) {
            List<String> lines=new ArrayList<>();
            lines.add("Tiempo general: "+describe(config.delayFor(""))+". Los modos con tiempo propio conservan su intervalo.");
            synchronized(config.gameDelays) {
                config.gameDelays.forEach((type,d)->{if(d!=null)lines.add(type.toUpperCase(Locale.ROOT)+": "+describe(d));});
            }
            config.copyDifficultyDelays().forEach((game,levels)->levels.forEach((level,d)->lines.add(game.toUpperCase(Locale.ROOT)+" · "+GameDifficulty.parse(level).orElseThrow().label()+": "+describe(d))));
            return lines;
        }
        String[] p=input.strip().split("\\s+");
        String type="",level="";int start=0;
        try {
            GameType mode=GameType.valueOf(p[0].toUpperCase(Locale.ROOT));
            // The legacy three-token form "random 2s 3s" sets the general interval.
            if(mode!=GameType.UNKNOWN && !(mode==GameType.RANDOM && p.length==3 && GameDifficulty.parse(p[1]).isEmpty())) {type=mode.name().toLowerCase(Locale.ROOT);start=1;}
        }catch(IllegalArgumentException ignored) { }
        if(!type.isEmpty()&&p.length>start) {
            var difficulty=GameDifficulty.parse(p[start]);
            if(difficulty.isPresent()){level=difficulty.get().key();start++;}
        }
        String label=type.isEmpty()?"Tiempo general":type.toUpperCase(Locale.ROOT)+(level.isEmpty()?"":" · "+GameDifficulty.parse(level).orElseThrow().label());
        if(p.length==start) {
            List<String> lines=new ArrayList<>();lines.add(label+": "+describe(config.delayFor(type,level)));
            if(!type.isEmpty()&&level.isEmpty())for(var difficulty:GameDifficulty.values())lines.add(difficulty.label()+": "+describe(config.delayFor(type,difficulty.key()))+(config.hasDifficultyDelay(type,difficulty.key())?" (propio)":" (usa base)"));
            return lines;
        }
        ModConfig.Delay value;
        if(p[start].equalsIgnoreCase("default")) {
            if(type.isEmpty() || p.length!=start+1)throw new IllegalArgumentException();
            persist(settings,type,level,null);
            return List.of(label+(level.isEmpty()?": usa el tiempo general (":": usa el tiempo base (")+describe(config.delayFor(type,level))+").");
        }
        if(p[start].equalsIgnoreCase("random")) {
            if(p.length!=start+3)throw new IllegalArgumentException();
            int min=parseDuration(p[start+1]),max=parseDuration(p[start+2]);
            if(min>max)throw new IllegalArgumentException("El mínimo supera al máximo");
            value=ModConfig.Delay.random(min,max);
        }else {
            if(p.length!=start+1)throw new IllegalArgumentException();
            value=ModConfig.Delay.fixed(parseDuration(p[start]));
        }
        persist(settings,type,level,value);
        return List.of(label+": "+describe(value)+". Se aplica a las próximas respuestas.");
    }
    private static void persist(ConfigManager settings,String type,String level,ModConfig.Delay next) {
        ModConfig c=settings.get();
        synchronized(c) {
            ModConfig.Delay previous=level.isEmpty()?(type.isEmpty()?c.delay.copy():c.gameDelays.get(type)):c.difficultyDelays.getOrDefault(type,Map.of()).get(level);
            replace(c,type,level,next);
            try{settings.save();}catch(RuntimeException e){replace(c,type,level,previous);throw e;}
        }
    }
    private static void replace(ModConfig c,String type,String level,ModConfig.Delay value){if(level.isEmpty())c.setDelay(type,value);else c.setDifficultyDelay(type,level,value);}
    public static int parseDuration(String value) {
        String s=value.strip().replace(',','.').toLowerCase(Locale.ROOT);
        double n=Double.parseDouble(s.replaceAll("(ms|s)$",""));
        double millis=n*(s.endsWith("ms")?1:1000);
        if(!Double.isFinite(millis) || millis<0 || millis>60000)throw new IllegalArgumentException();
        return (int)Math.round(millis);
    }
    private static String seconds(int ms) {return java.math.BigDecimal.valueOf(ms,3).stripTrailingZeros().toPlainString()+" s";}
    private static String describe(ModConfig.Delay d) {
        return "fixed".equalsIgnoreCase(d.mode)?seconds(d.minMs)+" fijo":seconds(d.minMs)+"–"+seconds(d.maxMs)+" al azar";
    }
}
