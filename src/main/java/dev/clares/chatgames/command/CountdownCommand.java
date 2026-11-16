package dev.clares.chatgames.command;

import dev.clares.chatgames.config.ConfigManager;
import dev.clares.chatgames.countdown.CountdownClock;
import java.util.List;
import java.util.Locale;

public final class CountdownCommand {
    private CountdownCommand(){}
    public static List<String> execute(ConfigManager settings,CountdownClock clock,String input) {
        String[] p=input.strip().toLowerCase(Locale.ROOT).split("\\s+");var c=settings.get().countdown;
        if(input.isBlank()||input.equalsIgnoreCase("status")) {
            long left=clock.remainingSeconds();
            return List.of("Contador: "+(c.enabled?"ACTIVADO":"DESACTIVADO")+" | Notas: "+(c.sound?"sí":"no")+" | Intervalo: "+CountdownClock.formatTime(c.totalSeconds())+" | Vista: "+(c.fullTime?"permanente":"últimos 10 s"),
                clock.roundActive()?"Minijuego en curso; el contador continuará desde el inicio observado.":clock.source()+(left<0?"":" | Faltan: "+CountdownClock.formatTime(left)));
        }
        var old=settings.get().countdown;
        var next=new dev.clares.chatgames.config.ModConfig.Countdown();next.enabled=old.enabled;next.sound=old.sound;next.fullTime=old.fullTime;next.intervalMinutes=old.intervalMinutes;next.intervalSeconds=old.intervalSeconds;next.volume=old.volume;
        c=next;
        switch(p[0]) {
            case "on","off"->{if(p.length!=1)throw new IllegalArgumentException();c.enabled=p[0].equals("on");}
            case "sound"->{if(p.length!=2||!List.of("on","off").contains(p[1]))throw new IllegalArgumentException();c.sound=p[1].equals("on");}
            case "interval"->{
                if(p.length!=2&&p.length!=3)throw new IllegalArgumentException();
                int min=Integer.parseInt(p[1]),sec=p.length==3?Integer.parseInt(p[2]):0;
                if(min<0||min>1440||sec<0||sec>59||min*60L+sec<1||min*60L+sec>86400)throw new IllegalArgumentException("Usa minutos y segundos: por ejemplo interval 8 1.");
                c.intervalMinutes=min;c.intervalSeconds=sec;
            }
            case "view"->{if(p.length!=2||!List.of("full","last10").contains(p[1]))throw new IllegalArgumentException();c.fullTime=p[1].equals("full");}
            case "sync"->{if(p.length!=2)throw new IllegalArgumentException();clock.syncRemaining(Integer.parseInt(p[1]));return List.of("Contador sincronizado: próximo juego estimado en "+p[1]+" segundos.");}
            default->throw new IllegalArgumentException();
        }
        synchronized(settings.get()){settings.get().countdown=next;try{settings.save();}catch(RuntimeException e){settings.get().countdown=old;throw e;}}
        return List.of("Contador guardado: "+(c.enabled?"activo":"apagado")+" | Notas: "+(c.sound?"sí":"no")+" | Intervalo: "+CountdownClock.formatTime(c.totalSeconds())+" | Vista: "+(c.fullTime?"permanente":"últimos 10 s"));
    }
}
