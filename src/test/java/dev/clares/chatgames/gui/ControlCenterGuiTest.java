package dev.clares.chatgames.gui;

import dev.clares.chatgames.config.ModConfig;
import dev.clares.chatgames.command.CommandTree;
import com.mojang.brigadier.CommandDispatcher;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ControlCenterGuiTest {
    @Test void allCardsFitBetweenSearchAndFooterAtSupportedScales() {
        for(int[] size:List.of(new int[]{320,240},new int[]{427,240},new int[]{640,360},new int[]{960,540})) {
            var l=DashboardLayout.of(size[0],size[1]);assertTrue(l.x()>=0);assertTrue(l.y()+l.height()<=size[1]);
            for(int i=0;i<l.pageSize();i++){assertTrue(l.cardY(i)>=l.y()+98);assertTrue(l.cardY(i)+52<l.footer());assertTrue(l.cardX(i)+l.cardWidth()<=size[0]);}
            assertTrue(l.y()+190<l.footer(),"Home controls cannot overlap footer");
        }
    }
    @Test void searchesIgnoreAccentsAndRespectGameFilter() {
        var config=new ModConfig();assertEquals("math",GameFilter.find(config,"MATEMATICAS",0).getFirst().key());
        config.games.put("trivia",false);assertTrue(GameFilter.find(config,"trivia",1).isEmpty());
        assertEquals("trivia",GameFilter.find(config,"trivia",2).getFirst().key());assertTrue(GameFilter.find(config,"zzzz",0).isEmpty());
    }
    @Test void newCommandsValidateArgumentsAndSuggestSavedProfileNames() throws Exception {
        var executed=new ArrayList<String>();CommandDispatcher<Object> d=new CommandDispatcher<>();
        d.register(CommandTree.build(c->{executed.add(c);return 1;},()->List.of("infection","tranquilo")));
        for(String command:List.of("profile save infection","profile show infection","profile load infection","profile delete infection confirm","profile undo","history 2","report","pause","resume","hud off")){
            d.execute("cgedit "+command,new Object());assertEquals(command,executed.getLast());
        }
        assertEquals(List.of("infection"),d.getCompletionSuggestions(d.parse("cgedit profile load in",new Object())).get().getList().stream().map(s->s.getText()).toList());
        assertThrows(Exception.class,()->d.execute("cgedit history 0",new Object()));
        assertThrows(Exception.class,()->d.execute("cgedit profile delete infection wrong",new Object()));
    }
}
