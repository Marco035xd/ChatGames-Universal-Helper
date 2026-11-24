package dev.clares.chatgames.gui;

import dev.clares.chatgames.config.*;
import dev.clares.chatgames.command.TimeCommand;

/** Edits are isolated from the live configuration until save succeeds. */
public final class TimeDraft {
    public enum Mode { FIXED, RANDOM, INHERIT }
    public final String key;
    public final String difficulty;
    private final ConfigManager settings;
    public Mode mode;
    public String minimum,maximum;
    public TimeDraft(ConfigManager settings,String key) {
        this(settings,key,"");
    }
    public TimeDraft(ConfigManager settings,String key,String difficulty) {
        this.settings=settings;this.key=key;
        this.difficulty=difficulty.isEmpty()?"":dev.clares.chatgames.game.GameDifficulty.parse(difficulty).orElseThrow(()->new IllegalArgumentException("Dificultad inválida.")).key();
        if(key.isEmpty()&&!this.difficulty.isEmpty())throw new IllegalArgumentException("Elige un minijuego para editar su dificultad.");
        ModConfig config=settings.get();ModConfig.Delay d=config.delayFor(key,this.difficulty);
        boolean own=this.difficulty.isEmpty()?config.gameDelays.containsKey(key):config.hasDifficultyDelay(key,this.difficulty);
        mode=!key.isEmpty()&&!own?Mode.INHERIT:
            "fixed".equalsIgnoreCase(d.mode)?Mode.FIXED:Mode.RANDOM;
        minimum=seconds(d.minMs);maximum=seconds(d.maxMs);
    }
    public void nextMode(){mode=switch(mode){case FIXED->Mode.RANDOM;case RANDOM->key.isEmpty()?Mode.FIXED:Mode.INHERIT;case INHERIT->Mode.FIXED;};}
    public void preset(int min,int max){mode=min==max?Mode.FIXED:Mode.RANDOM;minimum=seconds(min);maximum=seconds(max);}
    public void stepMinimum(int direction) {
        if(mode==Mode.INHERIT)return;
        minimum=step(minimum,direction);
        if(mode==Mode.FIXED)maximum=minimum;
    }
    public void stepMaximum(int direction) {
        if(mode==Mode.RANDOM)maximum=step(maximum,direction);
    }
    private static String step(String value,int direction) {
        if(direction!=-1 && direction!=1)throw new IllegalArgumentException("El paso debe ser de una décima.");
        try {
            int millis=TimeCommand.parseDuration(value);
            return seconds(Math.clamp(millis+direction*100,0,60000));
        }catch(IllegalArgumentException e){throw new IllegalArgumentException("Escribe un tiempo válido entre 0 y 60 segundos.");}
    }
    public ModConfig.Delay value() {
        if(mode==Mode.INHERIT){if(key.isEmpty())throw new IllegalArgumentException("El tiempo general no puede heredarse.");return null;}
        int min,max;
        try{min=TimeCommand.parseDuration(minimum);max=mode==Mode.FIXED?min:TimeCommand.parseDuration(maximum);}
        catch(IllegalArgumentException e){throw new IllegalArgumentException("Escribe un tiempo válido entre 0 y 60 segundos.");}
        if(min>max)throw new IllegalArgumentException("El mínimo no puede superar al máximo.");
        return mode==Mode.FIXED?ModConfig.Delay.fixed(min):ModConfig.Delay.random(min,max);
    }
    public String error(){try{value();return "";}catch(IllegalArgumentException e){return e.getMessage();}}
    public void save() {
        ModConfig.Delay next=value();ModConfig c=settings.get();
        synchronized(c) {
            ModConfig.Delay old=difficulty.isEmpty()?(key.isEmpty()?c.delay.copy():c.gameDelays.get(key)):c.difficultyDelays.getOrDefault(key,java.util.Map.of()).get(difficulty);
            replace(c,next);
            try{settings.save();}catch(RuntimeException e){replace(c,old);throw e;}
        }
    }
    private void replace(ModConfig c,ModConfig.Delay value){if(difficulty.isEmpty())c.setDelay(key,value);else c.setDifficultyDelay(key,difficulty,value);}
    public static String seconds(int ms){return java.math.BigDecimal.valueOf(ms,3).stripTrailingZeros().toPlainString();}
    public static String describe(ModConfig.Delay d){return "fixed".equalsIgnoreCase(d.mode)||d.minMs==d.maxMs?seconds(d.minMs)+" s":seconds(d.minMs)+"–"+seconds(d.maxMs)+" s";}
}
