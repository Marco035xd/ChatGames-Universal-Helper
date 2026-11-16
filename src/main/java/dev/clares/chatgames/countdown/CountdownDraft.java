package dev.clares.chatgames.countdown;

import dev.clares.chatgames.config.*;

public final class CountdownDraft {
    private final ConfigManager settings;
    public boolean enabled,sound,fullTime;
    public String interval,seconds;
    public int volume;
    public CountdownDraft(ConfigManager settings) {
        this.settings=settings;var c=settings.get().countdown;enabled=c.enabled;sound=c.sound;
        fullTime=c.fullTime;interval=Integer.toString(c.intervalMinutes);seconds=Integer.toString(c.intervalSeconds);volume=Math.round(c.volume*100);
    }
    public String error() {try{value();return "";}catch(IllegalArgumentException e){return e.getMessage();}}
    private ModConfig.Countdown value() {
        int min,sec;
        try{min=Integer.parseInt(interval.strip());sec=Integer.parseInt(seconds.strip());}catch(NumberFormatException e){throw new IllegalArgumentException("Escribe minutos y segundos enteros.");}
        if(min<0||min>1440||sec<0||sec>59||min*60L+sec<1||min*60L+sec>86400)throw new IllegalArgumentException("Usa de 1 segundo a 24 horas; segundos entre 0 y 59.");
        if(volume<0||volume>100)throw new IllegalArgumentException("El volumen debe estar entre 0 y 100%.");
        var c=new ModConfig.Countdown();c.enabled=enabled;c.sound=sound;c.fullTime=fullTime;c.intervalMinutes=min;c.intervalSeconds=sec;c.volume=volume/100f;return c;
    }
    public void save() {
        var next=value();var config=settings.get();
        synchronized(config){var old=config.countdown;config.countdown=next;try{settings.save();}catch(RuntimeException e){config.countdown=old;throw e;}}
    }
}
