package dev.clares.chatgames.game;

import com.google.gson.reflect.TypeToken;
import dev.clares.chatgames.config.*;
import dev.clares.chatgames.chat.AnswerSender;
import dev.clares.chatgames.countdown.CountdownClock;
import dev.clares.chatgames.solver.*;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;

class ServerLog131Test {
    @TempDir Path temp;
    record Round(String file,int startLine,int resultLine,String type,String question,String answer,String difficulty,String prompt,String result,double elapsed) { }
    private List<Round> rounds() throws Exception {
        try(var reader=new InputStreamReader(getClass().getResourceAsStream("/server-rounds-131.json"),StandardCharsets.UTF_8)) {
            return ConfigManager.GSON.fromJson(reader,new TypeToken<List<Round>>(){}.getType());
        }
    }
    @Test void all26ConfirmedRoundsDetectFullQuestionsAndExactAnswersIncludingEveryExpertExpression() throws Exception {
        var config=new ConfigManager(temp);var trivia=new TriviaSolver(config.dir());var words=new WordSolver(config.dir());
        var corpus=rounds();assertEquals(26,corpus.size());assertEquals(10,corpus.stream().filter(r->!r.difficulty.isEmpty()).count());
        for(var r:corpus) {
            var d=GameDetector.detectComplete(r.prompt,true).orElseThrow(()->new AssertionError(r.prompt));
            assertEquals(r.type.toUpperCase(Locale.ROOT),d.type().name());assertEquals(r.question,d.question());
            assertEquals(r.difficulty,GameDetector.difficulty(r.prompt));assertTrue(r.resultLine>r.startLine);
            String answer=switch(d.type()) {
                case MATH->MathParser.solve(d.question());case TRIVIA->trivia.answer(d.question()).orElseThrow();
                case UNSCRAMBLE->words.unscramble(d.question()).orElseThrow();case FILLOUT->words.fillout(d.question(),"es60").orElseThrow();
                case REACTION->d.question();default->throw new AssertionError(d);
            };
            assertEquals(r.answer,answer,r.file+":"+r.startLine);
            assertTrue(GameDetector.winner(r.result));assertEquals(r.answer,GameDetector.announcedAnswer(r.result).orElseThrow());
            if(!r.difficulty.isEmpty()) {
                String resultBlock="✯ MINIJUEGO ✯\n("+r.difficulty+")\n"+r.result+"\nRecibió los premios de "+r.difficulty+".";
                assertTrue(GameDetector.winner(resultBlock));assertEquals(r.answer,GameDetector.announcedAnswer(resultBlock).orElseThrow());
            }
        }
    }
    @Test void lineByLineLiveChatAndLogFallbackScheduleOnlyOnceAndNewResultsCloseTheSession() throws Exception {
        var config=new ConfigManager(temp);config.get().setDelay("",ModConfig.Delay.fixed(60000));
        var callbacks=new LinkedBlockingQueue<Runnable>();
        try(var manager=manager(config,callbacks,new ArrayList<>())) {
            int count=0;
            for(var r:rounds()) {
                manager.onLog("[CHAT GAMES] You have started a chat event in your chat!");barrier(manager);
                for(String line:r.prompt.split("\\R"))manager.onRich(Component.literal(line));barrier(manager);
                GameSession s=manager.current();assertNotNull(s,r.prompt);assertEquals(r.answer,s.answer);assertEquals(r.difficulty,s.difficulty);
                long deadline=s.scheduledSendTime,version=s.scheduleVersion;
                for(String line:r.prompt.split("\\R"))manager.onLog(line);barrier(manager);
                assertSame(s,manager.current());assertEquals(version,s.scheduleVersion);assertEquals(deadline,s.scheduledSendTime);
                manager.onRich(Component.literal("✯ MINIJUEGO ✯"));
                if(!r.difficulty.isEmpty())manager.onRich(Component.literal("("+r.difficulty+")"));
                manager.onRich(Component.literal(r.result));manager.onLog(r.result);barrier(manager);
                assertNull(manager.current());assertTrue(callbacks.isEmpty());
                var e=manager.history().entries().getFirst();assertEquals("Ronda finalizada",e.status());
                assertEquals(r.answer,e.confirmedAnswer());assertEquals(r.difficulty,e.difficulty());
                assertEquals(++count,manager.history().entries().size());
            }
        }
    }
    @Test void expertUsesConfiguredDeadlineAndWinnerInvalidatesEvenAnAlreadyQueuedSend() throws Exception {
        var config=new ConfigManager(temp);config.get().setDelay("math",ModConfig.Delay.fixed(2300));
        var callbacks=new LinkedBlockingQueue<Runnable>();var sent=new ArrayList<String>();
        try(var manager=manager(config,callbacks,sent)) {
            long before=System.currentTimeMillis();
            manager.onRich(Component.literal("✯ MINIJUEGO ✯\n(Experto)\nResuelve este problema: 765 ÷ 9 × 6 + (104 - 25)\nTienes 60 segundos para responder."));barrier(manager);
            var s=manager.current();assertEquals("589",s.answer);assertEquals("Experto",s.difficulty);assertEquals(2300,s.responseDelayMs);
            assertTrue(Math.abs(s.scheduledSendTime-before-2300)<200);
            Runnable stale=callbacks.poll(3,TimeUnit.SECONDS);assertNotNull(stale);
            manager.onRich(Component.literal("Jugador respondió 589 en 2.1 s."));barrier(manager);stale.run();
            assertTrue(sent.isEmpty());assertNull(manager.current());assertEquals(0,manager.history().entries().getFirst().sends());
        }
    }
    @Test void incompleteOrMalformedExpressionsNeverProduceAPartialAnswer() throws Exception {
        for(String expr:List.of("765 ÷ 9 × 6 + (104 -", "(960 ÷ 12 × 8 + 148", "(744 ÷ 8 - 14)) × 15", "(2 + 3) foo 4", "(2 + 3) ÷ 0")) {
            assertTrue(GameDetector.detectComplete("✯ MINIJUEGO ✯\n(Experto)\nResuelve este problema: "+expr+"\nTienes 60 segundos para responder.",true).isEmpty(),expr);
        }
        String block="✯ MINIJUEGO ✯\n(Experto)\nResuelve este problema: (960 ÷ 12 × 8 +\n148) ÷ 4";
        assertEquals("197",MathParser.solve(GameDetector.detectComplete(block,true).orElseThrow().question()));
        assertEquals("30",MathParser.solve(GameDetector.detectComplete("Resuelve este problema: ((2 + 3) × (8 - 2))",true).orElseThrow().question()));
        var equation=GameDetector.detectComplete("Resuelve este problema: 3*x + 4 = 19",true).orElseThrow();
        assertEquals(GameType.VARIABLE,equation.type());assertEquals("5",VariableSolver.solve(equation.question()).orElseThrow());
    }
    @Test void newUnquotedResultsTeachUnknownTriviaButPlayerMessagesCannotCancelOrTeach() throws Exception {
        var config=new ConfigManager(temp);config.get().setDelay("",ModConfig.Delay.fixed(60000));
        try(var manager=manager(config,new LinkedBlockingQueue<>(),new ArrayList<>())) {
            String q="¿Cuál es la respuesta nueva de prueba?";
            manager.onRich(Component.literal("✯ MINIJUEGO ✯\n(Fácil)\n"+q+"\nTienes 60 segundos para responder."));barrier(manager);
            assertNotNull(manager.current());assertEquals(GameState.FINISHED,manager.current().status);
            String fake="[Rango] Otro » Jugador respondió Inventada en 3.5 s.";
            manager.onLog(fake);barrier(manager);assertNotNull(manager.current());assertTrue(manager.solveLocal(GameType.TRIVIA,q).isEmpty());
            assertFalse(GameDetector.winner(fake));assertTrue(GameDetector.announcedAnswer(fake).isEmpty());
            manager.onRich(Component.literal("Jugador respondió Respuesta con espacios en 3,5 s."));barrier(manager);
            assertNull(manager.current());assertEquals("Respuesta con espacios",new TriviaSolver(config.dir()).answer(q).orElseThrow());
            assertTrue(GameDetector.winner("Recibió los premios de Fácil."));
        }
    }
    @Test void newResultsShowRemainingTimeWithoutMovingTheNextStartToTheResultHeader() throws Exception {
        for(var r:rounds().stream().filter(row->!row.difficulty.isEmpty()).toList()) {
            var now=new AtomicLong();var clock=new CountdownClock(now::get,()->8,()->1);
            for(String line:r.prompt.split("\\R"))clock.observe(line);
            assertTrue(clock.roundActive());assertEquals(481,clock.remainingSeconds());
            now.set(24000);clock.observe("✯ MINIJUEGO ✯");clock.observe("("+r.difficulty+")");clock.observe(r.result);
            clock.observe("Recibió los premios de "+r.difficulty+".");
            assertFalse(clock.roundActive());assertEquals(457,clock.remainingSeconds());assertEquals("Nuevo minijuego en 07:37",clock.displayText());
            now.set(480000);for(String line:r.prompt.split("\\R"))clock.observe(line);
            assertTrue(clock.roundActive());assertEquals(481,clock.remainingSeconds());
        }
    }
    @Test void upgradeMakesNewAnswersAvailableAndPreservesPersonalPreferencesAndCaches() throws Exception {
        var settings=new ConfigManager(temp);settings.get().enabled=false;settings.get().games.put("trivia",false);
        settings.get().setDelay("math",ModConfig.Delay.random(2100,2900));settings.get().countdown.intervalSeconds=9;settings.save();
        Files.writeString(settings.dir().resolve("trivia.json"),"{\"Pregunta privada\":\"Personal\"}");
        Files.writeString(settings.dir().resolve("trivia-cache.json"),"{\"Pregunta aprendida\":\"Aprendida\"}");
        Files.writeString(settings.dir().resolve("word-cache.json"),"{\"u:personal\":\"Personal\"}");
        new ProfileStore(settings).saveNew("personal");
        var saved=new HashMap<Path,String>();for(String file:List.of("trivia.json","trivia-cache.json","word-cache.json","profiles/personal.json")) {
            Path path=settings.dir().resolve(file);saved.put(path,Files.readString(path));
        }
        var reloaded=new ConfigManager(temp);var trivia=new TriviaSolver(reloaded.dir());
        for(var r:rounds())if(r.type.equals("trivia"))assertEquals(r.answer,trivia.answer(r.question).orElseThrow());
        assertFalse(reloaded.get().enabled);assertFalse(reloaded.get().game("trivia"));assertEquals(2100,reloaded.get().delayFor("math").minMs);
        assertEquals(2900,reloaded.get().delayFor("math").maxMs);assertEquals(9,reloaded.get().countdown.intervalSeconds);
        for(var entry:saved.entrySet())assertEquals(entry.getValue(),Files.readString(entry.getKey()));
    }
    private static GameManager manager(ConfigManager config,BlockingQueue<Runnable> callbacks,List<String> sent) {
        return new GameManager(config,s->{},callbacks::add,()->true,new AnswerSender(){public boolean send(String answer){sent.add(answer);return true;}public boolean clickCommand(String c){fail("Unexpected command");return false;}});
    }
    private static void barrier(GameManager manager) throws Exception {
        var f=GameManager.class.getDeclaredField("queue");f.setAccessible(true);((ScheduledExecutorService)f.get(manager)).submit(()->{}).get(10,TimeUnit.SECONDS);
    }
}
