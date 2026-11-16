package dev.clares.chatgames.game;

import dev.clares.chatgames.config.*;
import dev.clares.chatgames.chat.AnswerSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class ControlCenterTest {
    @TempDir Path temp;
    @Test void historyRecordsRealSendAndConfirmedAnswerWithoutCountingDuplicateSources() throws Exception {
        var config=new ConfigManager(temp);config.get().setDelay("",ModConfig.Delay.fixed(0));
        var callbacks=new LinkedBlockingQueue<Runnable>();var sent=new ArrayList<String>();
        try(var manager=manager(config,callbacks,sent)) {
            String line="Tienes 60 segundos para escribir `Prueba`";
            manager.onLog(line);barrier(manager);callbacks.poll(2,TimeUnit.SECONDS).run();
            manager.onLog(line);manager.onLog("<Otro> Resuelve 8 + 5");barrier(manager);
            assertEquals(1,manager.history().entries().size());var e=manager.history().entries().getFirst();
            assertEquals("Enviada",e.status());assertEquals("Prueba",e.answer());assertEquals(1,e.sends());assertTrue(e.sentAfterMs()>=0);
            manager.onLog("Alguien respondió primero `Prueba` en 2s");barrier(manager);
            manager.onLog("Alguien respondió primero `Prueba` en 2s");barrier(manager);
            e=manager.history().entries().getFirst();assertEquals("Ronda finalizada",e.status());assertEquals("Prueba",e.confirmedAnswer());
            assertEquals(1,manager.history().summary().answered());assertEquals(List.of("Prueba"),sent);
        }
    }
    @Test void profileApplicationInvalidatesAnAlreadyQueuedSendAndPreservesPause() throws Exception {
        var config=new ConfigManager(temp);config.get().setDelay("",ModConfig.Delay.fixed(0));
        var callbacks=new LinkedBlockingQueue<Runnable>();var sent=new ArrayList<String>();
        try(var manager=manager(config,callbacks,sent)) {
            manager.profiles().saveNew("one");manager.onLog("Tienes 60 segundos para escribir `Pendiente`");barrier(manager);
            Runnable stale=callbacks.poll(2,TimeUnit.SECONDS);assertNotNull(stale);
            manager.applyProfile("one");barrier(manager);stale.run();assertTrue(sent.isEmpty());assertNull(manager.current());
            assertEquals("Cancelada",manager.history().entries().getFirst().status());
            manager.setEnabled(false);manager.applyProfile("one");assertFalse(config.get().enabled);
        }
    }
    @Test void localPracticeDoesNotCreateSessionsSendsOrStatistics() throws Exception {
        var config=new ConfigManager(temp);var callbacks=new LinkedBlockingQueue<Runnable>();var sent=new ArrayList<String>();
        try(var manager=manager(config,callbacks,sent)) {
            assertEquals("42",manager.preview(GameType.MATH,"6 * 7").get(5,TimeUnit.SECONDS).orElseThrow());
            barrier(manager);assertNull(manager.current());assertTrue(callbacks.isEmpty());assertTrue(sent.isEmpty());assertTrue(manager.history().entries().isEmpty());
        }
    }
    @Test void boundedHistoryAveragesOnlyActualFirstSendsAndKeepsNewestFirst() {
        var history=new RoundHistory(2);var a=new GameSession();a.sentAfterMs=2100;a.guesses=1;
        history.update(a,"Enviada");var b=new GameSession();history.update(b,"Programada");
        assertEquals(2100,history.summary().averageMs());assertEquals(1,history.summary().pending());
        var c=new GameSession();history.update(c,"Sin respuesta fiable");
        assertEquals(2,history.entries().size());assertEquals(c.id,history.entries().getFirst().id());assertEquals(-1,history.summary().averageMs());
    }
    private static GameManager manager(ConfigManager config,BlockingQueue<Runnable> callbacks,List<String> sent) {
        return new GameManager(config,s->{},callbacks::add,()->true,new AnswerSender(){public boolean send(String s){sent.add(s);return true;}public boolean clickCommand(String s){fail("Unexpected command");return false;}});
    }
    private static void barrier(GameManager manager) throws Exception {
        var f=GameManager.class.getDeclaredField("queue");f.setAccessible(true);((ScheduledExecutorService)f.get(manager)).submit(()->{}).get(10,TimeUnit.SECONDS);
    }
}
