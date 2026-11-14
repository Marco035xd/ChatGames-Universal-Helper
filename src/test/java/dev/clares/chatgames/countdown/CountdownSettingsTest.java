package dev.clares.chatgames.countdown;

import dev.clares.chatgames.command.*;
import dev.clares.chatgames.config.*;
import com.mojang.brigadier.CommandDispatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CountdownSettingsTest {
    @TempDir Path temp;
    @Test void oldConfigKeepsResponseTimesAndPreferencesAndAddsTwentyFiveMinuteCounter() throws Exception {
        Path dir=temp.resolve("config/chatgames-universal-helper");Files.createDirectories(dir);
        Files.writeString(dir.resolve("config.json"),"{\"configRevision\":9,\"enabled\":false,\"stats\":true,\"games\":{\"trivia\":false},\"gameDelays\":{\"math\":{\"mode\":\"fixed\",\"minMs\":5000,\"maxMs\":5000}}}");
        ConfigManager settings=new ConfigManager(temp);assertFalse(settings.get().enabled);assertTrue(settings.get().stats);assertFalse(settings.get().game("trivia"));
        assertEquals(5000,settings.get().delayFor("math").minMs);assertEquals(25,settings.get().countdown.intervalMinutes);assertTrue(settings.get().countdown.enabled);assertTrue(settings.get().countdown.sound);
    }
    @Test void guiDraftDoesNotMutateUntilSaveAndPersistsSoundVolumeAndInterval() throws Exception {
        ConfigManager settings=new ConfigManager(temp);String before=Files.readString(settings.dir().resolve("config.json"));
        CountdownDraft draft=new CountdownDraft(settings);draft.interval="30";draft.sound=false;draft.volume=20;draft.enabled=false;
        assertEquals(before,Files.readString(settings.dir().resolve("config.json")));assertEquals(25,settings.get().countdown.intervalMinutes);
        draft.save();var saved=new ConfigManager(temp).get().countdown;assertEquals(30,saved.intervalMinutes);assertFalse(saved.sound);assertFalse(saved.enabled);assertEquals(.2f,saved.volume);
    }
    @Test void invalidCountdownSettingsDoNotSaveAndFailedSaveRollsBack() throws Exception {
        ConfigManager settings=new ConfigManager(temp);String before=Files.readString(settings.dir().resolve("config.json"));
        for(String value:List.of("","0","1441","NaN","2.5")) {
            CountdownDraft draft=new CountdownDraft(settings);draft.interval=value;assertFalse(draft.error().isEmpty());assertThrows(IllegalArgumentException.class,draft::save);
            assertEquals(before,Files.readString(settings.dir().resolve("config.json")));
        }
        CountdownDraft draft=new CountdownDraft(settings);draft.interval="30";Path path=settings.dir().resolve("config.json");Files.delete(path);Files.createDirectory(path);
        assertThrows(IllegalStateException.class,draft::save);assertEquals(25,settings.get().countdown.intervalMinutes);
    }
    @Test void realCommandTreePersistsControlsAndManualSyncDoesNotChangeGameInterval() throws Exception {
        ConfigManager settings=new ConfigManager(temp);CountdownClock clock=new CountdownClock(()->0,()->settings.get().countdown.intervalMinutes);
        CommandDispatcher<Object> commands=new CommandDispatcher<>();commands.register(CommandTree.build(input->{CountdownCommand.execute(settings,clock,input.substring(9).strip());return 1;}));
        for(String command:List.of("countdown off","countdown sound off","countdown interval 30","countdown sync 60","countdown status"))commands.execute("cgedit "+command,new Object());
        var saved=new ConfigManager(temp).get().countdown;assertEquals(30,saved.intervalMinutes);assertFalse(saved.enabled);assertFalse(saved.sound);assertEquals(60,clock.remainingSeconds());
        assertTrue(commands.getCompletionSuggestions(commands.parse("cgedit countdown ",new Object())).get().getList().stream().map(s->s.getText()).toList().containsAll(List.of("on","off","sound","sync","interval","status")));
    }
}
