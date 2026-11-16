package dev.clares.chatgames.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class ProfileStoreTest {
    @TempDir Path temp;
    @Test void profilesPersistDecimalsAndCanUndoAcrossRestartWithoutCopyingPrivateSettings() throws Exception {
        var config=new ConfigManager(temp);var c=config.get();var store=new ProfileStore(config);
        c.setDelay("trivia",ModConfig.Delay.random(2100,2900));c.games.put("random",false);c.hud=false;c.stats=true;
        c.ai.endpoint="private-endpoint";c.logPath="private-path";c.debug=true;store.saveNew("infection");
        String json=Files.readString(config.dir().resolve("profiles/infection.json"));
        assertFalse(json.contains("private"));assertFalse(json.contains("ai"));assertFalse(json.contains("debug"));
        c.setDelay("trivia",ModConfig.Delay.fixed(700));c.hud=true;c.enabled=false;c.countdown.intervalSeconds=9;config.save();
        store.load("infection");assertEquals(2100,c.delayFor("trivia").minMs);assertEquals(2900,c.delayFor("trivia").maxMs);
        assertFalse(c.hud);assertFalse(c.game("random"));assertFalse(c.enabled);assertTrue(c.debug);assertEquals("private-endpoint",c.ai.endpoint);
        var restored=new ConfigManager(temp);new ProfileStore(restored).undo();
        assertEquals(700,restored.get().delayFor("trivia").minMs);assertTrue(restored.get().hud);assertEquals(9,restored.get().countdown.intervalSeconds);
        assertFalse(restored.get().enabled);
    }
    @Test void badNamesOverwriteAndCorruptProfilesCannotChangeCurrentSettings() throws Exception {
        var config=new ConfigManager(temp);var store=new ProfileStore(config);store.saveNew("a");
        assertThrows(IllegalArgumentException.class,()->store.saveNew("a"));
        for(String name:new String[]{"../escape","A","CON.","","too-long-abcdefghijklmnopqrs"})assertThrows(IllegalArgumentException.class,()->store.saveNew(name));
        Path file=config.dir().resolve("profiles/a.json");String valid=Files.readString(file),before=Files.readString(config.dir().resolve("config.json"));
        for(String invalid:new String[]{"{}","null",valid.replace("\"minMs\": 0","\"minMs\": -5"),valid.replace("\"intervalSeconds\": 1","\"intervalSeconds\": 60")}) {
            Files.writeString(file,invalid);assertThrows(IllegalArgumentException.class,()->store.load("a"));
            assertEquals(before,Files.readString(config.dir().resolve("config.json")));assertEquals(0,config.get().delay.minMs);
        }
    }
    @Test void failedConfigWriteRestoresMemoryAndLeavesReadableProfile() throws Exception {
        var config=new ConfigManager(temp);var store=new ProfileStore(config);store.saveNew("base");
        config.get().setDelay("",ModConfig.Delay.fixed(2300));
        Path configFile=config.dir().resolve("config.json");Files.delete(configFile);Files.createDirectory(configFile);Files.writeString(configFile.resolve("blocked"),"test");
        assertThrows(IllegalStateException.class,()->store.load("base"));assertEquals(2300,config.get().delay.minMs);assertNotNull(store.read("base"));
    }
    @Test void deleteOnlyRemovesSelectedProfileAndAtomicWritesLeaveNoTemporaryFiles() throws Exception {
        var config=new ConfigManager(temp);var store=new ProfileStore(config);store.saveNew("a");store.saveNew("b");
        String before=Files.readString(config.dir().resolve("config.json"));store.delete("a");
        assertEquals(java.util.List.of("b"),store.names());assertEquals(before,Files.readString(config.dir().resolve("config.json")));
        try(var files=Files.walk(config.dir())){assertFalse(files.anyMatch(p->p.toString().endsWith(".tmp")));}
    }
}
