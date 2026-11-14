package dev.clares.chatgames.countdown;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CountdownClockTest {
    @Test void waitsForARealPromptThenShowsTenThroughOneWithOneBeatEach() {
        AtomicLong now=new AtomicLong();CountdownClock clock=new CountdownClock(now::get,()->25);
        assertEquals(0,clock.tick(true));assertEquals(-1,clock.remainingSeconds());
        clock.observe("   You have 20 seconds to solve: `6 × 7`");
        now.set(1490000);List<Integer> beats=new ArrayList<>();
        for(int expected=10;expected>=1;expected--) {
            beats.add(clock.tick(true));assertEquals(expected,clock.seconds());
            for(int i=0;i<20;i++)assertEquals(0,clock.tick(true));
            now.addAndGet(1000);
        }
        assertEquals(List.of(10,9,8,7,6,5,4,3,2,1),beats);
        assertEquals(0,clock.tick(true));assertEquals(0,clock.seconds());assertEquals(-1,clock.remainingSeconds());
        now.addAndGet(1500000);assertEquals(0,clock.tick(true),"A missed server event must not invent a new cycle");
    }
    @Test void systemResultsPlayerChatAndCommandsDoNotStartOrResetCountdown() {
        AtomicLong now=new AtomicLong();CountdownClock clock=new CountdownClock(now::get,()->25);
        for(String line:List.of("✓ ✯ CHAT GAMES ✯ ✓","[CHAT GAMES] You have started a random chat event in your chat!","<Marco035xd> You have 20 seconds to write: `abc`","[CGH] Nuevo minijuego en 10 segundos","Marco035xd was the fastest to write `abc` and got a prize!"))clock.observe(line);
        assertEquals(-1,clock.remainingSeconds());
        clock.observe("Tienes 20 segundos para completar la palabra: `_abbi_`");now.set(1000);
        clock.observe("La respuesta correcta era Rabbit!");assertEquals(1499,clock.remainingSeconds());
    }
    @Test void triviaQuestionContainingWinnerStillAnchorsItsRealInstruction() {
        CountdownClock clock=new CountdownClock(()->0,()->25);clock.observe("You have 20 seconds to answer: `Who is the winner?`");
        assertEquals(1500,clock.remainingSeconds());
    }
    @Test void spanishTrailingDurationAndBannerTriviaAreRecognizedButResultBannerIsNot() {
        AtomicLong now=new AtomicLong();CountdownClock clock=new CountdownClock(now::get,()->25);
        clock.observe("Completa la palabra `_a_a`, Tienes 60 segundos");assertEquals(1500,clock.remainingSeconds());
        clock.reset();clock.observe("✓ ✯ CHAT GAMES ✯ ✓");clock.observe("¿Cuántos lados tiene un dado? (número)");assertEquals(1500,clock.remainingSeconds());
        now.set(4000);clock.observe("✓ ✯ CHAT GAMES ✯ ✓");clock.observe("Marco035xd was the fastest to write `6` and got a prize!");clock.observe("`6`");assertEquals(1496,clock.remainingSeconds());
    }
    @Test void escapedMultilineClientLogIsDecodedAndCanAnchorTheClock() {
        String raw="[18:04:24] [Render thread/INFO] (Minecraft) [System] [CHAT] ✓ ✯ CHAT GAMES ✯ ✓\\nTienes 20 segundos para responder:\\n¿En qué planeta vivimos?";
        String block=dev.clares.chatgames.log.LogChatParser.parse(raw).orElseThrow();assertTrue(block.contains("\n"));
        CountdownClock clock=new CountdownClock(()->0,()->25);for(String line:block.split("\\R"))clock.observe(line);assertEquals(1500,clock.remainingSeconds());
        assertTrue(dev.clares.chatgames.log.LogChatParser.parse("[18:04:24] [Render thread/INFO]:[CHAT] [CGH] mensaje local").isEmpty());
    }
    @Test void twoChatSourcesAndServerAnnouncementsDoNotReplayTheSameBeat() {
        AtomicLong now=new AtomicLong();CountdownClock clock=new CountdownClock(now::get,()->25);
        clock.observe("You have 20 seconds to unreverse: `abc`");now.set(100);clock.observe(" You have 20 seconds to unreverse: `abc` ");assertEquals(1500,clock.remainingSeconds());
        clock.observe("Next chatgame starts in 10 seconds");assertEquals(10,clock.tick(true));
        now.set(200);clock.observe("Next chatgame starts in 10 seconds");assertEquals(0,clock.tick(true));
        now.set(1100);assertEquals(9,clock.tick(true));
    }
    @Test void newRoundAndDisconnectClearVisibleCountdownAndPreviousAnchor() {
        AtomicLong now=new AtomicLong();CountdownClock clock=new CountdownClock(now::get,()->25);
        clock.syncRemaining(10);assertEquals(10,clock.tick(true));clock.observe("You have 20 seconds to fill in the word: `_abbi_`");
        assertEquals(0,clock.tick(true));assertEquals(0,clock.seconds());assertEquals(1500,clock.remainingSeconds());
        clock.reset();assertEquals(0,clock.tick(true));assertEquals(-1,clock.remainingSeconds());
    }
    @Test void hiddenCountdownAndDelayedTicksNeverBurstOldNotes() {
        AtomicLong now=new AtomicLong();CountdownClock clock=new CountdownClock(now::get,()->25);clock.syncRemaining(10);
        assertEquals(0,clock.tick(false));now.set(3000);assertEquals(7,clock.tick(true));assertEquals(0,clock.tick(true));
        now.set(10000);assertEquals(0,clock.tick(true));assertEquals(0,clock.seconds());
    }
    @Test void intervalEditsUseLastObservedStartButKeepManualSyncTarget() {
        AtomicLong now=new AtomicLong();AtomicInteger period=new AtomicInteger(25);CountdownClock clock=new CountdownClock(now::get,period::get);
        clock.observe("You have 20 seconds to solve for: `✗`");now.set(50000);period.set(1);assertEquals(10,clock.tick(true));
        clock.syncRemaining(5);period.set(20);assertEquals(5,clock.tick(true));
        assertThrows(IllegalArgumentException.class,()->clock.syncRemaining(0));assertThrows(IllegalArgumentException.class,()->clock.syncRemaining(86401));
        assertEquals(5,clock.remainingSeconds());
    }
}
