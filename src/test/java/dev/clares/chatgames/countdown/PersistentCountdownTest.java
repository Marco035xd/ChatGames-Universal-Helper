package dev.clares.chatgames.countdown;

import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.CommandDispatcher;
import dev.clares.chatgames.command.*;
import dev.clares.chatgames.config.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;

class PersistentCountdownTest {
    @TempDir Path temp;
    record Interval(String file,String start,String end,String nextStart,int startToStartSeconds,Integer endToNextSeconds) { }

    @Test void resultShowsRemainingMinutesAndSecondsInsteadOfRestartingAFullInterval() {
        AtomicLong now=new AtomicLong();CountdownClock clock=new CountdownClock(now::get,()->8,()->1);
        assertEquals("Esperando un minijuego…",clock.displayText());
        clock.observe("✯ MINIJUEGO ✯");clock.observe("Tienes 60 segundos para descifrarlo `aotP`");
        assertEquals("Minijuego en curso",clock.displayText());assertEquals(481,clock.remainingSeconds());
        now.set(3000);clock.observe("¡Jugador respondio primero `Pato` en (3.0sg)");
        assertFalse(clock.roundActive());assertEquals("Nuevo minijuego en 07:58",clock.displayText());
        assertEquals(0,clock.tick(true),"Notes must remain silent outside the last ten seconds");
        now.set(4000);assertEquals("Nuevo minijuego en 07:57",clock.displayText());
        clock.observe("y fue recompensado!");assertEquals("Nuevo minijuego en 07:57",clock.displayText());
        now.set(480000);clock.observe("Tienes 60 segundos para responder");
        assertTrue(clock.roundActive());assertEquals(481,clock.remainingSeconds());
        now.set(482000);clock.observe("La respuesta correcta era Tierra!");
        assertEquals("Nuevo minijuego en 07:59",clock.displayText());
    }

    @Test void headersDuplicateSourcesAndPlayerMessagesDoNotResetTheStartOrFinish() {
        AtomicLong now=new AtomicLong();CountdownClock clock=new CountdownClock(now::get,()->8,()->1);
        clock.observe("✯ MINIJUEGO ✯");clock.observe("Tienes 60 segundos para responder");
        now.set(150);clock.observe("Tienes 60 segundos para responder");
        now.set(800);clock.observe("¿En qué planeta vivimos?");
        assertEquals(481,clock.remainingSeconds());
        now.set(3000);clock.observe("[Jugador] Persona » respondio primero `Tierra`");
        assertTrue(clock.roundActive());
        clock.observe("Jugador respondio primero `Tierra` en (3.0sg)");
        now.set(3100);clock.observe("Jugador respondio primero `Tierra` en (3.0sg)");
        assertEquals(478,clock.remainingSeconds());assertFalse(clock.roundActive());
    }

    @Test void expiryAndDisconnectWaitForARealGameWithoutInventingAnotherCycle() {
        AtomicLong now=new AtomicLong();CountdownClock clock=new CountdownClock(now::get,()->8,()->1);
        clock.observe("Completa la palabra `Co_ín`, Tienes 60 segundos");
        now.set(471000);assertEquals(10,clock.tick(true));assertEquals("Nuevo minijuego en 00:10",clock.displayText());
        now.set(481000);assertEquals(0,clock.tick(true));assertEquals(-1,clock.remainingSeconds());
        now.set(500000);assertEquals(0,clock.tick(true));assertEquals("Esperando un minijuego…",clock.displayText());
        clock.observe("Tienes 60 segundos para Escribir la palabra: `Cojín`");assertEquals(481,clock.remainingSeconds());
        clock.reset();assertEquals(-1,clock.remainingSeconds());assertFalse(clock.roundActive());
    }

    @Test void all176MeasuredIntervalsGiveAnEstimateWithinTwoSecondsOfTheNextStart() throws Exception {
        List<Interval> pairs;
        try(var reader=new InputStreamReader(getClass().getResourceAsStream("/countdown-intervals-125.json"),StandardCharsets.UTF_8)) {
            pairs=ConfigManager.GSON.fromJson(reader,new TypeToken<List<Interval>>(){}.getType());
        }
        assertEquals(176,pairs.size());
        for(Interval interval:pairs) {
            AtomicLong now=new AtomicLong();CountdownClock clock=new CountdownClock(now::get,()->8,()->1);
            clock.observe("Tienes 60 segundos para responder");
            if(interval.endToNextSeconds!=null) {
                int roundSeconds=interval.startToStartSeconds-interval.endToNextSeconds;
                assertTrue(roundSeconds>=0 && roundSeconds<=60,interval.file);
                now.set(roundSeconds*1000L);clock.observe("Jugador respondio primero `Tierra` en (3.0sg)");
                assertEquals(481-roundSeconds,clock.remainingSeconds(),interval.file);
                assertTrue(Math.abs(clock.remainingSeconds()-interval.endToNextSeconds)<=2,interval.file);
                assertTrue(clock.displayText().startsWith("Nuevo minijuego en "));
            }
            now.set(interval.startToStartSeconds*1000L);
            clock.observe("Tienes 60 segundos para Escribir la palabra: `Cojín`");
            assertEquals(481,clock.remainingSeconds());assertTrue(clock.roundActive());
        }
    }

    @Test void upgradingTheOldDefaultKeepsPausedGamesResponseIntervalsAndCounterSwitches() throws Exception {
        Path dir=temp.resolve("config/chatgames-universal-helper");Files.createDirectories(dir);
        Files.writeString(dir.resolve("config.json"),"{\"configRevision\":9,\"enabled\":false,\"games\":{\"trivia\":false},\"gameDelays\":{\"math\":{\"mode\":\"random\",\"minMs\":2100,\"maxMs\":2900}},\"countdown\":{\"enabled\":false,\"sound\":false,\"intervalMinutes\":25,\"volume\":0.2}}");
        var settings=new ConfigManager(temp);var c=settings.get().countdown;
        assertEquals(481,c.totalSeconds());assertTrue(c.fullTime);assertFalse(c.enabled);assertFalse(c.sound);assertEquals(.2f,c.volume);
        assertFalse(settings.get().enabled);assertFalse(settings.get().game("trivia"));
        assertEquals(2100,settings.get().delayFor("math").minMs);assertEquals(2900,settings.get().delayFor("math").maxMs);
        assertEquals(481,new ConfigManager(temp).get().countdown.totalSeconds());
    }

    @Test void customLegacyIntervalsStayWholeMinutesAndNewMinuteSecondFieldsPersist() throws Exception {
        Path dir=temp.resolve("config/chatgames-universal-helper");Files.createDirectories(dir);
        Files.writeString(dir.resolve("config.json"),"{\"configRevision\":9,\"countdown\":{\"intervalMinutes\":30}}");
        var settings=new ConfigManager(temp);assertEquals(1800,settings.get().countdown.totalSeconds());
        CountdownDraft draft=new CountdownDraft(settings);draft.interval="8";draft.seconds="1";draft.fullTime=false;draft.sound=false;draft.save();
        var saved=new ConfigManager(temp).get().countdown;
        assertEquals(481,saved.totalSeconds());assertFalse(saved.fullTime);assertFalse(saved.sound);
        CountdownDraft invalid=new CountdownDraft(settings);invalid.interval="0";invalid.seconds="0";assertThrows(IllegalArgumentException.class,invalid::save);
        invalid.interval="8";invalid.seconds="60";assertThrows(IllegalArgumentException.class,invalid::save);
        invalid.interval="1440";invalid.seconds="1";assertThrows(IllegalArgumentException.class,invalid::save);
        assertEquals(481,new ConfigManager(temp).get().countdown.totalSeconds());
    }

    @Test void commandsSaveSecondsAndDisplayModeWithoutChangingTheObservedAnchor() throws Exception {
        var settings=new ConfigManager(temp);AtomicLong now=new AtomicLong();
        var clock=new CountdownClock(now::get,()->settings.get().countdown.intervalMinutes,()->settings.get().countdown.intervalSeconds);
        CommandDispatcher<Object> commands=new CommandDispatcher<>();commands.register(CommandTree.build(input->{CountdownCommand.execute(settings,clock,input.substring(9).strip());return 1;}));
        clock.observe("Tienes 60 segundos para responder");now.set(3000);clock.observe("La respuesta correcta era Tierra!");
        for(String cmd:List.of("countdown interval 8 1","countdown view full","countdown off","countdown sound off"))commands.execute("cgedit "+cmd,new Object());
        clock.tick(false);assertEquals(478,clock.remainingSeconds());
        var saved=new ConfigManager(temp).get().countdown;assertEquals(481,saved.totalSeconds());assertTrue(saved.fullTime);assertFalse(saved.enabled);assertFalse(saved.sound);
        commands.execute("cgedit countdown view last10",new Object());assertFalse(new ConfigManager(temp).get().countdown.fullTime);
        commands.execute("cgedit countdown interval 30",new Object());assertEquals(1800,settings.get().countdown.totalSeconds());
        commands.execute("cgedit countdown sync 60",new Object());assertEquals("Nuevo minijuego en 01:00",clock.displayText());
    }
}
