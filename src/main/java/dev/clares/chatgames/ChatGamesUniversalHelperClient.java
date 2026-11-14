package dev.clares.chatgames;

import dev.clares.chatgames.command.ClientCommands;
import dev.clares.chatgames.config.ConfigManager;
import dev.clares.chatgames.game.GameManager;
import dev.clares.chatgames.hud.ChatGamesHud;
import dev.clares.chatgames.hud.CountdownHud;
import dev.clares.chatgames.countdown.CountdownController;
import dev.clares.chatgames.gui.GuiController;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import dev.clares.chatgames.log.LogWatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class ChatGamesUniversalHelperClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        Minecraft client=Minecraft.getInstance();
        Path dir=FabricLoader.getInstance().getGameDir();
        ConfigManager config=new ConfigManager(dir);
        Consumer<String> feedback=msg -> client.execute(() -> {if(client.player!=null)client.gui.hud.getChat().addClientSystemMessage(Component.literal("[CGH] "+msg));});
        GameManager manager=new GameManager(config,client,feedback);
        CountdownController countdown=new CountdownController(config);
        Path log="auto".equalsIgnoreCase(config.get().logPath)?dir.resolve("logs/latest.log"):Path.of(config.get().logPath);
        LogWatcher watcher=new LogWatcher(log,line->{countdown.observe(line);manager.onLog(line);}); watcher.start();
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {if(!overlay){countdown.observe(message.getString());manager.onRich(message);}});
        ClientPlayConnectionEvents.JOIN.register((handler,sender,c) -> {countdown.joined();manager.resetServer(handler.getConnection().getRemoteAddress().toString());});
        ClientPlayConnectionEvents.DISCONNECT.register((handler,c) -> {countdown.disconnected();manager.resetServer("");});
        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> {watcher.close();manager.close();});
        Consumer<Component> styledFeedback=msg -> client.execute(() -> {
            if(client.player!=null)client.gui.hud.getChat().addClientSystemMessage(Component.literal("[CGH] ").append(msg));
        });
        GuiController gui=new GuiController(config,manager,countdown.clock());
        KeyMapping openGui=KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.chatgames_universal_helper.open_gui",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_G,
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("chatgames_universal_helper","main"))));
        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            // Consume all queued presses, including ones received while another screen is open.
            while(openGui.consumeClick()) {
                if(c.player!=null && c.gui.screen()==null && c.isWindowActive())gui.requestOpen();
            }
            gui.tick(c);
        });
        ClientTickEvents.END_CLIENT_TICK.register(countdown::tick);
        new ClientCommands(config,manager,watcher,feedback,styledFeedback,gui::requestOpen,countdown.clock()).register();
        ChatGamesHud.register(manager,config);
        CountdownHud.register(countdown.clock(),config);
    }
}
