package dev.clares.chatgames.game;

import java.text.Normalizer;
import java.util.*;

/** Stable preference keys; display labels also explain the server's terminology. */
public enum GameDifficulty {
    EASY("easy","Fácil"), NORMAL("normal","Media (Normal)"), EXPERT("expert","Difícil (Experto)");
    private final String key,label;
    GameDifficulty(String key,String label){this.key=key;this.label=label;}
    public String key(){return key;}
    public String label(){return label;}
    public static Optional<GameDifficulty> parse(String name) {
        if(name==null)return Optional.empty();
        String value=Normalizer.normalize(name.strip(),Normalizer.Form.NFD).replaceAll("\\p{M}","").toLowerCase(Locale.ROOT);
        return Optional.ofNullable(switch(value){case "easy","facil"->EASY;case "normal","medium","media","medio"->NORMAL;case "expert","experto","hard","dificil"->EXPERT;default->null;});
    }
}
