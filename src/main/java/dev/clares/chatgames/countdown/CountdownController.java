package dev.clares.chatgames.countdown;

import dev.clares.chatgames.config.ConfigManager;
import net.minecraft.client.Minecraft;

/** Receives both chat sources independently from whether auto-answering is enabled. */
public final class CountdownController {
    private final ConfigManager settings;
    private final CountdownClock clock;
    private boolean connected;
    public CountdownController(ConfigManager settings) {
        this.settings=settings;clock=new CountdownClock(()->System.nanoTime()/1000000,()->settings.get().countdown.intervalMinutes,()->settings.get().countdown.intervalSeconds);
    }
    public CountdownClock clock(){return clock;}
    public synchronized void joined(){clock.reset();connected=true;}
    public synchronized void disconnected(){connected=false;clock.reset();}
    public synchronized void observe(String line){if(connected)for(String part:line.split("\\R"))clock.observe(part);}
    public synchronized void tick(Minecraft client) {
        var c=settings.get().countdown;
        int beat=clock.tick(connected&&client.player!=null&&c.enabled);
        if(beat>0&&c.sound&&c.volume>0&&!(client.gui.screen() instanceof dev.clares.chatgames.gui.CountdownPreviewScreen)) {
            CountdownSound.play(client,beat,c.volume);
        }
    }
}
