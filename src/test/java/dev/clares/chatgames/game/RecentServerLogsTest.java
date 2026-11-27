package dev.clares.chatgames.game;

import com.google.gson.reflect.TypeToken;
import dev.clares.chatgames.chat.AnswerSender;
import dev.clares.chatgames.config.*;
import dev.clares.chatgames.solver.*;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.io.TempDir;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class RecentServerLogsTest {
    @TempDir Path temp;
    record Round(String sourceFile,int promptLine,int resultLine,String type,String question,String answer,
                 String difficulty,String prompt,String result) { }
    record Expected(Map<String,Integer> files,int trivia,int math,int experts) { }
    private Expected expected(String version) {
        return switch(version) {
            case "141" -> new Expected(Map.of("2026-11-24-2.log.gz",17,"2026-11-24-3.log.gz",6),12,11,4);
            case "142" -> new Expected(Map.of("2026-11-25-2.log.gz",1,"2026-11-26-1.log.gz",6),4,3,1);
            case "143" -> new Expected(Map.of("2026-11-26-2.log.gz",16),11,5,4);
            default -> throw new IllegalArgumentException("Unknown corpus: "+version);
        };
    }
    private List<Round> rounds(String version) throws Exception {
        try(var reader=new InputStreamReader(getClass().getResourceAsStream("/server-rounds-"+version+".json"),StandardCharsets.UTF_8)) {
            return ConfigManager.GSON.fromJson(reader,new TypeToken<List<Round>>(){}.getType());
        }
    }
    @ParameterizedTest @ValueSource(strings={"141","142","143"})
    void completeRoundsKeepTheirConfirmedAnswersAndLevels(String version) throws Exception {
        var settings=new ConfigManager(temp);var trivia=new TriviaSolver(settings.dir());var corpus=rounds(version);
        var expected=expected(version);
        assertEquals(expected.files().values().stream().mapToInt(Integer::intValue).sum(),corpus.size());
        for(var entry:expected.files().entrySet())assertEquals(entry.getValue().longValue(),corpus.stream().filter(r->r.sourceFile.equals(entry.getKey())).count());
        assertEquals(expected.trivia(),corpus.stream().filter(r->r.type.equals("trivia")).count());
        assertEquals(expected.math(),corpus.stream().filter(r->r.type.equals("math")).count());
        assertEquals(expected.experts(),corpus.stream().filter(r->r.difficulty.equals("Experto")).count());
        for(var r:corpus) {
            var d=GameDetector.detectComplete(r.prompt,true).orElseThrow(()->new AssertionError(r.sourceFile+":"+r.promptLine));
            assertEquals(r.question,d.question());assertEquals(r.type.toUpperCase(Locale.ROOT),d.type().name());
            assertEquals(r.difficulty,GameDetector.difficulty(r.prompt));assertTrue(r.resultLine>r.promptLine);
            String answer=d.type()==GameType.MATH?MathParser.solve(d.question()):trivia.answer(d.question()).orElseThrow();
            assertEquals(r.answer,answer);assertEquals(r.answer,GameDetector.announcedAnswer(r.result).orElseThrow());
        }
        assertTrue(trivia.answer("Plata").isEmpty(),"An orphan result must not create a question");
    }
    @ParameterizedTest @ValueSource(strings={"141","142","143"})
    void newBankReachesExistingInstallationsWithoutChangingLevelTimesOrPersonalFiles(String version) throws Exception {
        var settings=new ConfigManager(temp);var c=settings.get();c.enabled=false;c.games.put("trivia",false);
        c.setDelay("math",ModConfig.Delay.fixed(45000));c.setDelay("trivia",ModConfig.Delay.random(2700,2900));
        c.setDifficultyDelay("math","expert",ModConfig.Delay.random(20000,30000));
        c.setDifficultyDelay("trivia","normal",ModConfig.Delay.fixed(7500));
        c.countdown.intervalMinutes=12;c.countdown.intervalSeconds=9;c.countdown.sound=false;c.hud=false;c.stats=true;
        settings.save();new ProfileStore(settings).saveNew("niveles");
        var added=rounds(version).stream().filter(r->r.type.equals("trivia")).toList();
        Files.writeString(settings.dir().resolve("trivia.json"),ConfigManager.GSON.toJson(Map.of("Pregunta personal","Respuesta personal",added.getFirst().question,"Respuesta propia")));
        Files.writeString(settings.dir().resolve("trivia-cache.json"),"{\"Pregunta aprendida\":\"Aprendida\"}");
        Files.writeString(settings.dir().resolve("word-cache.json"),"{\"u:personal\":\"Personal\"}");
        var files=new LinkedHashMap<Path,String>();
        for(String name:List.of("config.json","trivia.json","trivia-cache.json","word-cache.json","profiles/niveles.json")) {
            Path path=settings.dir().resolve(name);files.put(path,Files.readString(path));
        }
        var loaded=new ConfigManager(temp);var solver=new TriviaSolver(loaded.dir());
        for(var r:added.subList(1,added.size()))assertEquals(r.answer,solver.answer(r.question).orElseThrow());
        assertEquals("Respuesta propia",solver.answer(added.getFirst().question).orElseThrow());
        assertEquals("Respuesta personal",solver.answer("Pregunta personal").orElseThrow());assertEquals("Aprendida",solver.answer("Pregunta aprendida").orElseThrow());
        assertFalse(loaded.get().enabled);assertFalse(loaded.get().game("trivia"));assertFalse(loaded.get().hud);assertTrue(loaded.get().stats);
        assertEquals(45000,loaded.get().delayFor("math").sampleMillis());assertEquals(2700,loaded.get().delayFor("trivia").minMs);assertEquals(2900,loaded.get().delayFor("trivia").maxMs);
        assertEquals(20000,loaded.get().delayFor("math","Experto").minMs);assertEquals(30000,loaded.get().delayFor("math","Experto").maxMs);
        assertEquals(7500,loaded.get().delayFor("trivia","Normal").sampleMillis());
        assertEquals(729,loaded.get().countdown.totalSeconds());assertFalse(loaded.get().countdown.sound);
        for(var entry:files.entrySet())assertEquals(entry.getValue(),Files.readString(entry.getKey()));
    }
    @ParameterizedTest @ValueSource(strings={"141","142","143"})
    void liveAndLogCopiesUseTheirLevelIntervalAndServerResultsCancelPendingReplies(String version) throws Exception {
        var settings=new ConfigManager(temp);settings.get().setDelay("",ModConfig.Delay.fixed(60000));
        for(String game:List.of("math","trivia"))for(var level:GameDifficulty.values())
            settings.get().setDifficultyDelay(game,level.key(),ModConfig.Delay.fixed(10000*(level.ordinal()+1)+(game.equals("trivia")?1000:0)));
        var callbacks=new LinkedBlockingQueue<Runnable>();
        try(var manager=new GameManager(settings,s->{},callbacks::add,()->true,new AnswerSender(){
            public boolean send(String answer){fail("The server result must cancel the reply");return false;}
            public boolean clickCommand(String command){fail("Unexpected command");return false;}
        })) {
            int count=0;
            for(var r:rounds(version)) {
                for(String line:r.prompt.split("\\R"))manager.onRich(Component.literal(line));barrier(manager);
                var session=manager.current();assertNotNull(session);assertEquals(r.answer,session.answer);assertEquals(r.difficulty,session.difficulty);
                assertEquals(settings.get().delayFor(r.type,r.difficulty).minMs,session.responseDelayMs);
                long deadline=session.scheduledSendTime,scheduledVersion=session.scheduleVersion;
                for(String line:r.prompt.split("\\R"))manager.onLog(line);barrier(manager);
                assertSame(session,manager.current());assertEquals(deadline,session.scheduledSendTime);assertEquals(scheduledVersion,session.scheduleVersion);
                manager.onRich(Component.literal(r.result));manager.onLog(r.result);barrier(manager);
                assertNull(manager.current());var entry=manager.history().entries().getFirst();
                assertEquals("Ronda finalizada",entry.status());assertEquals(r.answer,entry.confirmedAnswer());assertEquals(r.difficulty,entry.difficulty());
                assertEquals(++count,manager.history().entries().size());
            }
            assertTrue(callbacks.isEmpty());
        }
    }
    private static void barrier(GameManager manager) throws Exception {
        var field=GameManager.class.getDeclaredField("queue");field.setAccessible(true);
        ((ScheduledExecutorService)field.get(manager)).submit(()->{}).get(10,TimeUnit.SECONDS);
    }
}
