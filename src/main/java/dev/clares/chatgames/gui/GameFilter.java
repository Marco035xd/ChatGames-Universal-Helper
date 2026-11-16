package dev.clares.chatgames.gui;

import dev.clares.chatgames.config.ModConfig;
import java.text.Normalizer;
import java.util.*;

public final class GameFilter {
    private static String fold(String s){return Normalizer.normalize(s,Normalizer.Form.NFD).replaceAll("\\p{M}","").toLowerCase(Locale.ROOT);}
    public static List<GameCatalog.Entry> find(ModConfig config,String query,int filter) {
        String text=fold(query.strip());
        return GameCatalog.ENTRIES.stream().filter(e->fold(e.title()+" "+e.key()).contains(text))
            .filter(e->filter==0||config.game(e.key())==(filter==1)).toList();
    }
    private GameFilter() { }
}
