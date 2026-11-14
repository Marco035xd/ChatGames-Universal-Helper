package dev.clares.chatgames.gui;

import dev.clares.chatgames.config.ConfigManager;
import dev.clares.chatgames.countdown.CountdownClock;
import dev.clares.chatgames.game.GameManager;
import net.minecraft.client.Minecraft;
import java.util.concurrent.atomic.AtomicBoolean;

public final class GuiController {
    private final ConfigManager settings;
    private final CountdownClock countdown;
    private final GameManager games;
    private final AtomicBoolean requested=new AtomicBoolean();
    public GuiController(ConfigManager settings,GameManager games,CountdownClock countdown){this.countdown=countdown;this.settings=settings;this.games=games;}
    public void requestOpen(){requested.set(true);}
    /** Open on the next minecraft tick, after ChatScreen finishes processing the command. */
    public void tick(Minecraft minecraft){if(requested.getAndSet(false))minecraft.gui.setScreen(new ChatGamesScreen(settings,games,null,countdown));}
}
