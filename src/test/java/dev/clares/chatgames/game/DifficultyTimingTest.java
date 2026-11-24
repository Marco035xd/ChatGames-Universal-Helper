package dev.clares.chatgames.game;

import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.CommandDispatcher;
import dev.clares.chatgames.config.*;
import dev.clares.chatgames.gui.*;
import dev.clares.chatgames.command.*;
import dev.clares.chatgames.chat.AnswerSender;
import net.minecraft.network.chat.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class DifficultyTimingTest {
    @TempDir Path temp;
    @Test void levelsOverrideOnlyTheirCategoryAndReturnIndependentSnapshots() {
        var c=new ModConfig();c.setDelay("",ModConfig.Delay.fixed(700));c.setDelay("math",ModConfig.Delay.fixed(45000));
        c.setDifficultyDelay("math","experto",ModConfig.Delay.random(20100,29900));
        assertEquals(45000,c.delayFor("math","Fácil").minMs);assertEquals(45000,c.delayFor("math","Normal").minMs);
        assertEquals(20100,c.delayFor("MATH","Difícil").minMs);assertEquals(700,c.delayFor("trivia","Experto").minMs);
        assertEquals(45000,c.delayFor("math","sin nivel").minMs);
        c.delayFor("math","Experto").minMs=0;assertEquals(20100,c.delayFor("math","Experto").minMs);
        c.setDelay("math",ModConfig.Delay.fixed(2300));assertEquals(20100,c.delayFor("math","hard").minMs);
        c.setDifficultyDelay("math","expert",null);assertEquals(2300,c.delayFor("math","Experto").minMs);assertFalse(c.difficultyDelays.containsKey("math"));
        assertEquals(GameDifficulty.NORMAL,GameDifficulty.parse("medio").orElseThrow());
        assertEquals(GameDifficulty.NORMAL,GameDifficulty.parse("media").orElseThrow());
    }
    @Test void guiEditsAllTenCategoriesAndThreeLevelsWithDecimalsAndBaseInheritance() {
        var settings=new ConfigManager(temp);settings.get().setDelay("math",ModConfig.Delay.fixed(45000));settings.save();
        for(var e:GameCatalog.ENTRIES)for(var level:GameDifficulty.values()) {
            var draft=new TimeDraft(settings,e.key(),level.key());assertEquals(TimeDraft.Mode.INHERIT,draft.mode);
            draft.preset(2100,2900);draft.stepMinimum(1);draft.stepMaximum(-1);draft.save();
            assertEquals(2200,settings.get().delayFor(e.key(),level.key()).minMs);assertEquals(2800,settings.get().delayFor(e.key(),level.key()).maxMs);
        }
        var loaded=new ConfigManager(temp);assertEquals(45000,loaded.get().delayFor("math").sampleMillis());
        for(var e:GameCatalog.ENTRIES)for(var level:GameDifficulty.values())assertEquals(2200,loaded.get().delayFor(e.key(),level.key()).minMs);
        var inherited=new TimeDraft(loaded,"math","expert");inherited.mode=TimeDraft.Mode.INHERIT;inherited.save();
        assertEquals(45000,loaded.get().delayFor("math","Experto").sampleMillis());assertEquals(2200,loaded.get().delayFor("math","Fácil").minMs);
    }
    @Test void cancelInvalidInputAndDiskFailureKeepAllExistingIntervals() throws Exception {
        var settings=new ConfigManager(temp);settings.get().setDifficultyDelay("math","expert",ModConfig.Delay.fixed(20000));settings.save();
        String before=Files.readString(settings.dir().resolve("config.json"));
        var draft=new TimeDraft(settings,"math","expert");draft.preset(21100,29900);
        assertEquals(20000,settings.get().delayFor("math","expert").minMs);assertEquals(before,Files.readString(settings.dir().resolve("config.json")));
        draft.minimum="30";draft.maximum="20";assertThrows(IllegalArgumentException.class,draft::save);
        assertEquals(before,Files.readString(settings.dir().resolve("config.json")));
        draft.preset(2100,2900);Path path=settings.dir().resolve("config.json");Files.delete(path);Files.createDirectory(path);Files.writeString(path.resolve("blocked"),"test");
        assertThrows(IllegalStateException.class,draft::save);assertEquals(20000,settings.get().delayFor("math","expert").sampleMillis());
        var fresh=new TimeDraft(settings,"trivia","easy");fresh.preset(1100,1900);assertThrows(IllegalStateException.class,fresh::save);
        assertFalse(settings.get().hasDifficultyDelay("trivia","easy"));
    }
    @Test void commandsCompleteAndExecuteLevelRangesIncludingRandomModeAndLegacyForms() throws Exception {
        var settings=new ConfigManager(temp);CommandDispatcher<Object> d=new CommandDispatcher<>();
        d.register(CommandTree.build(command->{TimeCommand.execute(settings,command.substring(7).strip());return 1;}));
        for(String game:List.of("math","trivia","random")) {
            d.execute("cgedit settime "+game+" easy random 2,1 2.9",new Object());
            d.execute("cgedit settime "+game+" normal 5.5",new Object());
            d.execute("cgedit settime "+game+" expert random 20.1 29.9",new Object());
            assertEquals(2100,settings.get().delayFor(game,"Fácil").minMs);assertEquals(5500,settings.get().delayFor(game,"Normal").minMs);
            assertEquals(20100,settings.get().delayFor(game,"Experto").minMs);
        }
        d.execute("cgedit settime math medio 6,5",new Object());assertEquals(6500,settings.get().delayFor("math","Normal").minMs);
        d.execute("cgedit settime math dificil default",new Object());assertFalse(settings.get().hasDifficultyDelay("math","expert"));
        d.execute("cgedit settime random 0s 1s",new Object());assertEquals(1000,settings.get().delay.maxMs);
        d.execute("cgedit settime random 2s",new Object());assertEquals(2000,settings.get().delayFor("random").minMs);
        assertEquals(2100,new ConfigManager(temp).get().delayFor("random","easy").minMs);
        assertTrue(d.getCompletionSuggestions(d.parse("cgedit settime trivia ",new Object())).get().getList().stream().anyMatch(s->s.getText().equals("expert")));
        String before=Files.readString(settings.dir().resolve("config.json"));
        assertThrows(Exception.class,()->d.execute("cgedit settime math expert random 30 20",new Object()));
        assertEquals(before,Files.readString(settings.dir().resolve("config.json")));
    }
    @Test void profilesSaveLoadUndoAndLegacyProfilesPreserveTheIntendedLevelSettings() throws Exception {
        var settings=new ConfigManager(temp);var store=new ProfileStore(settings);
        settings.get().setDifficultyDelay("trivia","normal",ModConfig.Delay.fixed(5500));store.saveNew("niveles");
        settings.get().setDifficultyDelay("trivia","normal",ModConfig.Delay.fixed(2300));settings.get().setDifficultyDelay("math","expert",ModConfig.Delay.random(20000,30000));
        store.load("niveles");assertEquals(5500,settings.get().delayFor("trivia","normal").minMs);assertFalse(settings.get().hasDifficultyDelay("math","expert"));
        new ProfileStore(new ConfigManager(temp)).undo();var loaded=new ConfigManager(temp);
        assertEquals(2300,loaded.get().delayFor("trivia","normal").minMs);assertEquals(20000,loaded.get().delayFor("math","expert").minMs);
        var json=com.google.gson.JsonParser.parseString(Files.readString(settings.dir().resolve("profiles/niveles.json"))).getAsJsonObject();
        json.remove("difficultyDelays");Files.writeString(settings.dir().resolve("profiles/antiguo.json"),ConfigManager.GSON.toJson(json));
        var oldStore=new ProfileStore(loaded);oldStore.load("antiguo");assertTrue(loaded.get().difficultyDelays.isEmpty());
        json.add("difficultyDelays",com.google.gson.JsonParser.parseString("{\"math\":{\"unknown\":{\"mode\":\"fixed\",\"minMs\":1,\"maxMs\":1}}}"));
        Files.writeString(settings.dir().resolve("profiles/mal.json"),ConfigManager.GSON.toJson(json));
        assertThrows(IllegalArgumentException.class,()->oldStore.load("mal"));assertTrue(loaded.get().difficultyDelays.isEmpty());
    }
    @Test void oldConfigKeepsItsBaseTimesAndMalformedNewLevelsFallBackSafely() throws Exception {
        Path dir=temp.resolve("config/chatgames-universal-helper");Files.createDirectories(dir);
        Files.writeString(dir.resolve("config.json"),"{\"configRevision\":10,\"enabled\":false,\"games\":{\"math\":true,\"trivia\":false},\"gameDelays\":{\"math\":{\"mode\":\"fixed\",\"minMs\":45000,\"maxMs\":45000}},\"difficultyDelays\":{\"math\":{\"expert\":null,\"easy\":{\"mode\":\"fixed\",\"minMs\":-1,\"maxMs\":500},\"medio\":{\"mode\":\"fixed\",\"minMs\":5500,\"maxMs\":5500}},\"trivia\":null}}");
        var settings=new ConfigManager(temp);assertFalse(settings.get().enabled);assertFalse(settings.get().game("trivia"));
        assertEquals(45000,settings.get().delayFor("math","Experto").minMs);assertEquals(45000,settings.get().delayFor("math","Fácil").minMs);
        assertEquals(5500,settings.get().delayFor("math","Normal").minMs);
    }
    record Round(String type,String difficulty,String prompt,String result) { }
    @Test void actualServerLabelsSelectTheRightWaitAndDuplicateLogsDoNotResampleIt() throws Exception {
        var settings=new ConfigManager(temp);settings.get().setDelay("",ModConfig.Delay.fixed(60000));
        for(String game:List.of("math","trivia")) {
            settings.get().setDifficultyDelay(game,"easy",ModConfig.Delay.random(1100,1900));settings.get().setDifficultyDelay(game,"normal",ModConfig.Delay.random(5100,5900));
            settings.get().setDifficultyDelay(game,"expert",ModConfig.Delay.random(20100,20900));
        }
        List<Round> rows;
        try(var reader=new InputStreamReader(getClass().getResourceAsStream("/server-rounds-132.json"),StandardCharsets.UTF_8)){rows=ConfigManager.GSON.fromJson(reader,new TypeToken<List<Round>>(){}.getType());}
        var callbacks=new LinkedBlockingQueue<Runnable>();
        try(var manager=manager(settings,callbacks)) {
            for(var r:rows) {
                for(String line:r.prompt.split("\\R"))manager.onRich(Component.literal(line));barrier(manager);
                var session=manager.current();assertNotNull(session);long delay=session.responseDelayMs,deadline=session.scheduledSendTime,version=session.scheduleVersion;
                int low=switch(r.difficulty){case "Fácil"->1100;case "Normal"->5100;default->20100;};assertTrue(delay>=low&&delay<=low+800);
                for(String line:r.prompt.split("\\R"))manager.onLog(line);barrier(manager);
                assertSame(session,manager.current());assertEquals(delay,session.responseDelayMs);assertEquals(deadline,session.scheduledSendTime);assertEquals(version,session.scheduleVersion);
                manager.onRich(Component.literal(r.result));barrier(manager);assertNull(manager.current());
            }
            manager.onRich(Component.literal("Resuelve este problema: 88 + 12"));barrier(manager);
            assertEquals(60000,manager.current().responseDelayMs,"Without a server label, keep the base; do not guess from expression length");
            assertTrue(callbacks.isEmpty());
        }
    }
    @Test void richClickChallengesUseTheBufferedServerLevel() throws Exception {
        var settings=new ConfigManager(temp);settings.get().setDifficultyDelay("clickable","expert",ModConfig.Delay.fixed(25000));
        try(var manager=manager(settings,new LinkedBlockingQueue<>())) {
            manager.onRich(Component.literal("✯ MINIJUEGO ✯"));manager.onRich(Component.literal("(Experto)"));
            manager.onRich(Component.literal("Haz clic aquí").withStyle(s->s.withClickEvent(new ClickEvent.RunCommand("/chatgames answer prueba"))));barrier(manager);
            assertEquals(GameType.CLICKABLE,manager.current().type);assertEquals("Experto",manager.current().difficulty);assertEquals(25000,manager.current().responseDelayMs);
        }
    }
    private static GameManager manager(ConfigManager settings,BlockingQueue<Runnable> callbacks) {
        return new GameManager(settings,s->{},callbacks::add,()->true,new AnswerSender(){public boolean send(String answer){fail("Unexpected send");return false;}public boolean clickCommand(String command){fail("Unexpected click");return false;}});
    }
    private static void barrier(GameManager manager) throws Exception {var f=GameManager.class.getDeclaredField("queue");f.setAccessible(true);((ScheduledExecutorService)f.get(manager)).submit(()->{}).get(10,TimeUnit.SECONDS);}
}
