package dev.clares.chatgames.game;

import java.util.*;

/** Bounded session diagnostics. No raw chat, player identities or server addresses. */
public final class RoundHistory {
    public record Entry(String id,long started,GameType type,String question,String answer,String source,
                        String status,long configuredMs,long sentAfterMs,int sends,String confirmedAnswer,String difficulty) { }
    public record Summary(int rounds,int answered,int pending,int unknown,long averageMs) { }
    public static String seconds(long ms){return ms<0?"—":String.format(Locale.ROOT,"%.3f s",ms/1000.0);}
    private final LinkedHashMap<String,Entry> entries=new LinkedHashMap<>();
    private final int limit;
    public RoundHistory(){this(100);}
    public RoundHistory(int limit){this.limit=Math.max(1,limit);}
    public synchronized void update(GameSession s,String status) {
        Entry old=entries.get(s.id);
        entries.put(s.id,new Entry(s.id,s.startTime,s.type,s.question,s.answer,s.source,status,s.responseDelayMs,
            s.sentAfterMs,s.guesses,old==null?"":old.confirmedAnswer(),s.difficulty));
        while(entries.size()>limit)entries.remove(entries.keySet().iterator().next());
    }
    public synchronized void finish(GameSession s,String confirmed) {
        Entry old=entries.get(s.id);
        if(old==null)return;
        entries.put(s.id,new Entry(old.id(),old.started(),old.type(),old.question(),old.answer(),old.source(),
            "Ronda finalizada",old.configuredMs(),old.sentAfterMs(),old.sends(),confirmed.isEmpty()?old.confirmedAnswer():confirmed,old.difficulty()));
    }
    public synchronized List<Entry> entries(){var copy=new ArrayList<>(entries.values());Collections.reverse(copy);return List.copyOf(copy);}
    public synchronized Summary summary() {
        int sent=0,pending=0,unknown=0;long total=0;
        for(var e:entries.values()){
            if(e.sends()>0){sent++;total+=e.sentAfterMs();}
            if(e.status().equals("Programada")||e.status().equals("Resolviendo"))pending++;
            if(e.status().equals("Sin respuesta fiable"))unknown++;
        }
        return new Summary(entries.size(),sent,pending,unknown,sent==0?-1:total/sent);
    }
}
