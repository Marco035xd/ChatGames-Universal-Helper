package dev.clares.chatgames.game;

import com.google.gson.reflect.TypeToken;
import dev.clares.chatgames.chat.AnswerSender;
import dev.clares.chatgames.config.*;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class ImmediateTimingTest {
    @TempDir Path temp;
    record Round(String type,String question,String answer,String prompt,String style) { }

    @Test void liveAnagramUsesItsConfiguredDeadlineAndChatCannotPostponeOrDuplicateIt() throws Exception {
        ConfigManager config=new ConfigManager(temp);
        config.get().setDelay("",ModConfig.Delay.fixed(60000));
        config.get().setDelay("unscramble",ModConfig.Delay.fixed(2300));
        BlockingQueue<Runnable> callbacks=new LinkedBlockingQueue<>();List<String> sent=new CopyOnWriteArrayList<>();
        try(GameManager manager=manager(config,callbacks,sent)) {
            assertEquals("Pato",manager.solveLocal(GameType.UNSCRAMBLE,"aotP").orElseThrow());
            long receivedAt=System.currentTimeMillis();
            String prompt="Tienes 60 segundos para descifrarlo `aotP`";
            manager.onRich(Component.literal(prompt));barrier(manager);
            GameSession session=manager.current();assertNotNull(session,"A complete live prompt must be detected without waiting for the log or a quiet period");
            assertEquals(GameType.UNSCRAMBLE,session.type);assertEquals("Pato",session.answer);
            assertEquals(2300,session.responseDelayMs);
            assertTrue(Math.abs(session.scheduledSendTime-receivedAt-2300)<150);
            long scheduled=session.scheduledSendTime,version=session.scheduleVersion;
            manager.onLog("✯ MINIJUEGO ✯");manager.onLog(prompt);manager.onRich(Component.literal(prompt));
            for(int i=0;i<100;i++)manager.onRich(Component.literal("[Jugador] OtraPersona » ¿Qué se vende? "+i));
            barrier(manager);
            assertSame(session,manager.current());assertEquals(version,session.scheduleVersion);assertEquals(scheduled,session.scheduledSendTime);
            assertNull(callbacks.poll(1500,TimeUnit.MILLISECONDS),"The configured delay must still be respected");
            Runnable send=callbacks.poll(1700,TimeUnit.MILLISECONDS);assertNotNull(send);send.run();
            assertEquals(List.of("Pato"),sent);assertTrue(callbacks.isEmpty());
        }
    }

    @Test void timeSpentInTheLogicQueueCountsInsideTheConfiguredInterval() throws Exception {
        ConfigManager config=new ConfigManager(temp);config.get().setDelay("reaction",ModConfig.Delay.fixed(500));
        BlockingQueue<Runnable> callbacks=new LinkedBlockingQueue<>();List<String> sent=new CopyOnWriteArrayList<>();
        try(GameManager manager=manager(config,callbacks,sent)) {
            CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);
            queue(manager).submit(()->{entered.countDown();release.await(5,TimeUnit.SECONDS);return null;});
            assertTrue(entered.await(5,TimeUnit.SECONDS));
            manager.onRich(Component.literal("You have 20 seconds to write out: `Spyglass`"));
            try {assertFalse(release.await(650,TimeUnit.MILLISECONDS));}finally{release.countDown();}
            barrier(manager);
            assertNotNull(manager.current());assertEquals(500,manager.current().responseDelayMs);
            assertTrue(manager.current().scheduledSendTime<=System.currentTimeMillis()+100,"Do not add the configured interval again after processing");
            Runnable send=callbacks.poll(250,TimeUnit.MILLISECONDS);assertNotNull(send);send.run();
            assertEquals(List.of("Spyglass"),sent);
        }
    }

    @Test void zeroDelayAlsoWorksImmediatelyThroughTheLogFallback() throws Exception {
        ConfigManager config=new ConfigManager(temp);config.get().setDelay("",ModConfig.Delay.fixed(0));
        BlockingQueue<Runnable> callbacks=new LinkedBlockingQueue<>();List<String> sent=new CopyOnWriteArrayList<>();
        try(GameManager manager=manager(config,callbacks,sent)) {
            assertEquals("Pato",manager.solveLocal(GameType.UNSCRAMBLE,"aotP").orElseThrow());
            manager.onLog("Tienes 60 segundos para descifrarlo `aotP`");barrier(manager);
            assertNotNull(manager.current(),"No manual flush and no grouping timer should be needed");
            Runnable send=callbacks.poll(200,TimeUnit.MILLISECONDS);assertNotNull(send);send.run();
            assertEquals(List.of("Pato"),sent);
        }
    }

    @Test void wrappedTriviaWaitsForTheQuestionToFinishAndIgnoresOtherPlayers() throws Exception {
        ConfigManager config=new ConfigManager(temp);config.get().setDelay("",ModConfig.Delay.fixed(60000));
        try(GameManager manager=manager(config,new LinkedBlockingQueue<>(),new ArrayList<>())) {
            manager.onRich(Component.literal("You have 20 seconds to answer:"));
            manager.onLog("You have 20 seconds to answer:");
            manager.onRich(Component.literal("`How many eyes of ender are needed to open the end"));
            manager.onLog("`How many eyes of ender are needed to open the end");barrier(manager);
            assertNull(manager.current());
            manager.onRich(Component.literal("[Jugador] Otro » Who was the winner?"));
            manager.onLog("[Jugador] Otro » Resuelve 8 + 5");barrier(manager);assertNull(manager.current());
            manager.onRich(Component.literal("portal?`"));barrier(manager);
            assertEquals("How many eyes of ender are needed to open the end portal?",manager.current().question);
            assertEquals("12",manager.current().answer);
            manager.onRich(Component.literal("The correct answer was 12!"));barrier(manager);
            manager.onRich(Component.literal("Tienes 60 segundos para responder"));
            manager.onRich(Component.literal("¿Cómo se llama el cuerpo celeste que brilla"));barrier(manager);assertNull(manager.current());
            manager.onRich(Component.literal("en la noche y gira alrededor de la Tierra?"));barrier(manager);
            assertEquals(GameType.TRIVIA,manager.current().type);assertEquals("Luna",manager.current().answer);
        }
    }

    @Test void symbolEquationsWaitOnlyUntilTheTargetCanBeSolved() throws Exception {
        ConfigManager config=new ConfigManager(temp);config.get().setDelay("",ModConfig.Delay.fixed(60000));
        try(GameManager manager=manager(config,new LinkedBlockingQueue<>(),new ArrayList<>())) {
            manager.onRich(Component.literal("You have 20 seconds to solve for: `✗`"));
            manager.onRich(Component.literal("❅ + ❅ + ❅ = 60"));
            manager.onRich(Component.literal("✯ + ✯ + ✯ = 30"));barrier(manager);assertNull(manager.current());
            manager.onRich(Component.literal("❅ + ✯ + ✗ = 96"));barrier(manager);
            assertEquals(GameType.VARIABLE,manager.current().type);assertEquals("66",manager.current().answer);
        }
    }

    @Test void allOctoberRoundsStartFromLiveChatAndAreNotRestartedByTheirLogs() throws Exception {
        ConfigManager config=new ConfigManager(temp);config.get().setDelay("",ModConfig.Delay.fixed(60000));
        List<Round> rounds;
        try(var reader=new InputStreamReader(getClass().getResourceAsStream("/server-rounds-121.json"),StandardCharsets.UTF_8)) {
            rounds=ConfigManager.GSON.fromJson(reader,new TypeToken<List<Round>>(){}.getType());
        }
        assertEquals(328,rounds.size());
        try(GameManager manager=manager(config,new LinkedBlockingQueue<>(),new ArrayList<>())) {
            assertEquals("Pato",manager.solveLocal(GameType.UNSCRAMBLE,"aotP").orElseThrow());
            for(Round round:rounds) {
                manager.onLog("[CHAT GAMES] You have started a chat event in your chat!");
                manager.onRich(Component.literal(round.prompt));barrier(manager);
                GameSession session=manager.current();assertNotNull(session,round.prompt);
                assertEquals(round.type.toUpperCase(Locale.ROOT),session.type.name(),round.prompt);
                assertEquals(round.question,session.question,round.prompt);assertEquals(round.answer,session.answer,round.prompt);
                long version=session.scheduleVersion,scheduled=session.scheduledSendTime;
                for(String line:round.prompt.split("\\R"))manager.onLog(line);
                barrier(manager);
                assertSame(session,manager.current(),round.prompt);assertEquals(version,session.scheduleVersion);assertEquals(scheduled,session.scheduledSendTime);
            }
        }
    }

    private static GameManager manager(ConfigManager config,BlockingQueue<Runnable> callbacks,List<String> sent) {
        return new GameManager(config,s->{},callbacks::add,()->true,new AnswerSender() {
            public boolean send(String answer){sent.add(answer);return true;}
            public boolean clickCommand(String command){fail("Unexpected command");return false;}
        });
    }
    private static ScheduledExecutorService queue(GameManager manager) throws Exception {
        var field=GameManager.class.getDeclaredField("queue");field.setAccessible(true);return (ScheduledExecutorService)field.get(manager);
    }
    private static void barrier(GameManager manager) throws Exception {queue(manager).submit(()->{}).get(15,TimeUnit.SECONDS);}
}
