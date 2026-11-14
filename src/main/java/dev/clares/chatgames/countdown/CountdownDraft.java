package dev.clares.chatgames.countdown;

import dev.clares.chatgames.config.*;

public final class CountdownDraft {
    private final ConfigManager settings;
    public boolean enabled,sound;
    public String interval;
    public int volume;
    public CountdownDraft(ConfigManager settings) {
        this.settings=settings;var c=settings.get().countdown;enabled=c.enabled;sound=c.sound;
        interval=Integer.toString(c.intervalMinutes);volume=Math.round(c.volume*100);
    }
    public String error() {try{value();return "";}catch(IllegalArgumentException e){return e.getMessage();}}
    private ModConfig.Countdown value() {
        int min;
        try{min=Integer.parseInt(interval.strip());}catch(NumberFormatException e){throw new IllegalArgumentException("Escribe entre 1 y 1440 minutos enteros.");}
        if(min<1||min>1440)throw new IllegalArgumentException("El intervalo debe estar entre 1 y 1440 minutos.");
        if(volume<0||volume>100)throw new IllegalArgumentException("El volumen debe estar entre 0 y 100%.");
        var c=new ModConfig.Countdown();c.enabled=enabled;c.sound=sound;c.intervalMinutes=min;c.volume=volume/100f;return c;
    }
    public void save() {
        var next=value();var config=settings.get();
        synchronized(config){var old=config.countdown;config.countdown=next;try{settings.save();}catch(RuntimeException e){config.countdown=old;throw e;}}
    }
}
