package dev.clares.chatgames.game;

import com.google.gson.reflect.TypeToken;
import dev.clares.chatgames.chat.AnswerSender;
import dev.clares.chatgames.config.*;
import dev.clares.chatgames.solver.*;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class ServerLog132Test {
    @TempDir Path temp;
    record Round(String sourceFile,int promptLine,int resultLine,String type,String question,String answer,
                 String difficulty,String prompt,String result) { }
    private List<Round> rounds() throws Exception {
        try(var reader=new InputStreamReader(getClass().getResourceAsStream("/server-rounds-132.json"),StandardCharsets.UTF_8)) {
            return ConfigManager.GSON.fromJson(reader,new TypeToken<List<Round>>(){}.getType());
        }
    }
    @Test void bothLogsReproduce14ConfirmedRoundsWithExactQuestionsAnswersAndDifficulties() throws Exception {
        var settings=new ConfigManager(temp);var trivia=new TriviaSolver(settings.dir());var corpus=rounds();
        assertEquals(14,corpus.size());assertEquals(4,corpus.stream().filter(r->r.sourceFile.startsWith("2026-11-24")).count());
        assertEquals(7,corpus.stream().filter(r->r.type.equals("trivia")).count());
        for(var r:corpus) {
            var d=GameDetector.detectComplete(r.prompt,true).orElseThrow(()->new AssertionError(r.sourceFile+":"+r.promptLine));
            assertEquals(r.question,d.question());assertEquals(r.type.toUpperCase(Locale.ROOT),d.type().name());
            assertEquals(r.difficulty,GameDetector.difficulty(r.prompt));assertTrue(r.resultLine>r.promptLine);
            String answer=d.type()==GameType.MATH?MathParser.solve(d.question()):trivia.answer(d.question()).orElseThrow();
            assertEquals(r.answer,answer);assertEquals(r.answer,GameDetector.announcedAnswer(r.result).orElseThrow());
        }
    }
    @Test void newTriviaIsAvailableAfterUpgradeWithoutReplacingPersonalDataOrPreferences() throws Exception {
        var settings=new ConfigManager(temp);var c=settings.get();c.enabled=false;c.games.put("trivia",false);
        c.setDelay("math",ModConfig.Delay.fixed(2700));c.setDelay("trivia",ModConfig.Delay.random(2100,2900));
        c.countdown.intervalMinutes=12;c.countdown.intervalSeconds=9;c.countdown.sound=false;c.hud=false;c.stats=true;
        settings.save();new ProfileStore(settings).saveNew("guardado");
        Files.writeString(settings.dir().resolve("trivia.json"),"{\"Pregunta personal\":\"Respuesta personal\"}");
        Files.writeString(settings.dir().resolve("trivia-cache.json"),"{\"Pregunta aprendida\":\"Aprendida\"}");
        Files.writeString(settings.dir().resolve("word-cache.json"),"{\"u:personal\":\"Personal\"}");
        var files=new LinkedHashMap<Path,String>();
        for(String name:List.of("trivia.json","trivia-cache.json","word-cache.json","profiles/guardado.json")) {
            Path path=settings.dir().resolve(name);files.put(path,Files.readString(path));
        }
        var loaded=new ConfigManager(temp);var solver=new TriviaSolver(loaded.dir());
        var added=rounds().stream().filter(r->r.type.equals("trivia")&&r.sourceFile.startsWith("2026-11-24")).toList();
        assertEquals(3,added.size());
        for(var r:added)assertEquals(r.answer,solver.answer(r.question).orElseThrow());
        assertEquals("Respuesta personal",solver.answer("Pregunta personal").orElseThrow());
        assertEquals("Aprendida",solver.answer("Pregunta aprendida").orElseThrow());
        assertFalse(loaded.get().enabled);assertFalse(loaded.get().game("trivia"));assertFalse(loaded.get().hud);assertTrue(loaded.get().stats);
        assertEquals(2700,loaded.get().delayFor("math").sampleMillis());assertEquals(2100,loaded.get().delayFor("trivia").minMs);assertEquals(2900,loaded.get().delayFor("trivia").maxMs);
        assertEquals(729,loaded.get().countdown.totalSeconds());assertFalse(loaded.get().countdown.sound);
        for(var entry:files.entrySet())assertEquals(entry.getValue(),Files.readString(entry.getKey()));
        solver.put(added.getFirst().question,"Respuesta propia");
        assertEquals("Respuesta propia",new TriviaSolver(loaded.dir()).answer(added.getFirst().question).orElseThrow());
    }
    @Test void recentRoundsWorkThroughLiveChatAndLogWithoutRestartingSendsAndCloseOnServerResults() throws Exception {
        var settings=new ConfigManager(temp);settings.get().setDelay("",ModConfig.Delay.fixed(60000));
        settings.get().setDelay("trivia",ModConfig.Delay.random(2100,2900));
        var callbacks=new LinkedBlockingQueue<Runnable>();var sent=new ArrayList<String>();
        try(var manager=new GameManager(settings,s->{},callbacks::add,()->true,new AnswerSender(){
            public boolean send(String answer){sent.add(answer);return true;}
            public boolean clickCommand(String command){fail("Unexpected command");return false;}
        })) {
            int count=0;
            for(var r:rounds()) {
                for(String line:r.prompt.split("\\R"))manager.onRich(Component.literal(line));barrier(manager);
                var session=manager.current();assertNotNull(session);assertEquals(r.answer,session.answer);assertEquals(r.difficulty,session.difficulty);
                if(r.type.equals("trivia"))assertTrue(session.responseDelayMs>=2100&&session.responseDelayMs<=2900);
                long deadline=session.scheduledSendTime,version=session.scheduleVersion;
                for(String line:r.prompt.split("\\R"))manager.onLog(line);barrier(manager);
                assertSame(session,manager.current());assertEquals(deadline,session.scheduledSendTime);assertEquals(version,session.scheduleVersion);
                manager.onRich(Component.literal(r.result));manager.onLog(r.result);barrier(manager);
                assertNull(manager.current());var entry=manager.history().entries().getFirst();
                assertEquals("Ronda finalizada",entry.status());assertEquals(r.answer,entry.confirmedAnswer());assertEquals(r.difficulty,entry.difficulty());
                assertEquals(++count,manager.history().entries().size());
            }
            assertTrue(callbacks.isEmpty());assertTrue(sent.isEmpty(),"The confirmed result must cancel each pending reply");
        }
    }
    private static void barrier(GameManager manager) throws Exception {
        var field=GameManager.class.getDeclaredField("queue");field.setAccessible(true);
        ((ScheduledExecutorService)field.get(manager)).submit(()->{}).get(10,TimeUnit.SECONDS);
    }
}
