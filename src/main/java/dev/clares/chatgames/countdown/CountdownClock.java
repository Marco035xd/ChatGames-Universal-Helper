package dev.clares.chatgames.countdown;

import dev.clares.chatgames.game.GameDetector;
import java.text.Normalizer;
import java.util.Locale;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.regex.*;

/** A client estimate anchored by real prompts; never invents a recurring server event. */
public final class CountdownClock {
    private static final Pattern START=Pattern.compile("^(?:\\[chat\\s*games]\\s*)?[¡✓✯*#\\s]*(?:you have|tienes)\\s+\\d+\\s+(?:seconds|segundos)\\b.*");
    private static final Pattern ANNOUNCEMENT=Pattern.compile("^(?:\\[chat\\s*games]\\s*)?(?:el )?(?:next (?:chat ?game|minigame)|(?:nuevo|proximo) (?:chat ?game|minijuego))(?: starts?| comenzara| empieza| iniciara)?\\s+(?:in|en)\\s+(\\d+)\\s*(seconds?|segundos?|minutes?|minutos?|s|min)[!. ]*$");
    private static final Pattern TRAILING_DURATION=Pattern.compile("^(?:completa(?:r)? la palabra|fill in the word).*\\b(?:tienes|you have)\\s+\\d+\\s+(?:segundos|seconds).*",Pattern.CASE_INSENSITIVE);
    private static final Pattern HEADER=Pattern.compile("^[^\\p{L}\\p{N}]*(?:chat\\s*games|minijuego)[^\\p{L}\\p{N}]*$");
    private static final Pattern DURATION=Pattern.compile("(?:you have|tienes)\\s+(\\d{1,4})\\s+(?:seconds|segundos)");
    private long bannerAt=-1;
    private final LongSupplier time;
    private final IntSupplier minutes,extraSeconds;
    private long nextAt=-1,lastStart=-1,period=-1;
    private String lastSignal="";
    private long lastSignalAt=-1;
    private String lastPrompt="",source="Esperando un minijuego";
    private int beat,shown;
    private boolean activeRound;
    private long roundEndsAt=-1;
    public CountdownClock(LongSupplier time,IntSupplier minutes){this(time,minutes,()->0);}
    public CountdownClock(LongSupplier time,IntSupplier minutes,IntSupplier extraSeconds){this.time=time;this.minutes=minutes;this.extraSeconds=extraSeconds;}
    private long periodMillis(){return Math.max(1,Math.min(86400,minutes.getAsInt()*60L+extraSeconds.getAsInt()))*1000;}
    public synchronized void observe(String raw) {
        String line=Normalizer.normalize(raw.replaceAll("§[0-9a-fk-orxA-FK-ORX]",""),Normalizer.Form.NFD)
            .replaceAll("\\p{M}","").strip().toLowerCase(Locale.ROOT).replaceAll("\\s+"," ");
        if(line.startsWith("[cgh]")||GameDetector.playerChat(line)||GameDetector.administrative(line))return;
        Matcher announcement=ANNOUNCEMENT.matcher(line);
        if(announcement.matches()) {
            long signalAt=time.getAsLong();
            if(line.equals(lastSignal)&&lastSignalAt>=0&&signalAt-lastSignalAt<3000)return;
            lastSignal=line;lastSignalAt=signalAt;
            try {
                long seconds=Long.parseLong(announcement.group(1))*(announcement.group(2).startsWith("min")?60:1);
                if(seconds>0&&seconds<=86400)anchor(seconds,"Anuncio del servidor");
            }catch(NumberFormatException ignored){}
            return;
        }
        long now=time.getAsLong();
        boolean instruction=START.matcher(line).matches()||TRAILING_DURATION.matcher(line).matches();
        if(!instruction) {
            if(HEADER.matcher(line).matches()){bannerAt=now;return;}
            boolean question=line.matches("(?s)^[`\"‘]?(?:¿|what\\b|which\\b|who\\b|how\\b|where\\b|when\\b).*");
            if(!question&&GameDetector.winner(line)){bannerAt=-1;activeRound=false;return;}
            if(bannerAt<0||now-bannerAt>5000||line.isBlank()||line.startsWith("<")||!question&&GameDetector.detect(line,true).isEmpty())return;
        }
        bannerAt=-1;
        // Header, instruction and wrapped question may all describe the same start.
        if(lastStart>=0&&now-lastStart<3000)return;
        lastPrompt=line;lastStart=now;period=periodMillis();nextAt=now+period;beat=shown=0;source="Estimado por intervalo";
        activeRound=true;
        Matcher duration=DURATION.matcher(line);
        long roundSeconds=duration.find()?Math.min(3600,Long.parseLong(duration.group(1))):60;
        roundEndsAt=now+roundSeconds*1000;
    }
    private void anchor(long seconds,String source) {
        nextAt=time.getAsLong()+seconds*1000;lastStart=-1;period=periodMillis();beat=shown=0;this.source=source;activeRound=false;roundEndsAt=-1;
    }
    public synchronized void syncRemaining(int seconds) {
        if(seconds<1||seconds>86400)throw new IllegalArgumentException("Usa entre 1 y 86400 segundos.");
        anchor(seconds,"Sincronización manual (estimada)");
    }
    /** Returns at most one NEW musical beat. Skipped seconds are never replayed in a burst. */
    public synchronized int tick(boolean visible) {
        long now=time.getAsLong(),currentPeriod=periodMillis();
        if(activeRound&&roundEndsAt>=0&&now>=roundEndsAt)activeRound=false;
        if(period!=currentPeriod) {
            period=currentPeriod;
            if(lastStart>=0){nextAt=lastStart+period;beat=shown=0;}
        }
        if(nextAt>=0&&now>=nextAt){nextAt=lastStart=-1;shown=beat=0;source="Esperando el próximo minijuego";}
        long remaining=nextAt<0?-1:nextAt-now;
        shown=visible&&remaining>0&&remaining<=10000?(int)((remaining+999)/1000):0;
        if(shown>0&&shown!=beat){beat=shown;return shown;}
        return 0;
    }
    public synchronized int seconds(){return shown;}
    public synchronized long remainingSeconds(){return nextAt<0?-1:Math.max(0,(nextAt-time.getAsLong()+999)/1000);}
    public synchronized String source(){return source;}
    public synchronized boolean roundActive(){return activeRound;}
    public synchronized long intervalSeconds(){return periodMillis()/1000;}
    public synchronized String displayText(){return activeRound?"Minijuego en curso":remainingSeconds()<0?"Esperando un minijuego…":"Nuevo minijuego en "+formatTime(remainingSeconds());}
    public static String formatTime(long seconds){return String.format(Locale.ROOT,"%02d:%02d",Math.max(0,seconds)/60,Math.max(0,seconds)%60);}
    public synchronized void reset(){nextAt=lastStart=period=roundEndsAt=-1;activeRound=false;beat=shown=0;lastPrompt=lastSignal="";lastSignalAt=bannerAt=-1;source="Esperando un minijuego";}
}
