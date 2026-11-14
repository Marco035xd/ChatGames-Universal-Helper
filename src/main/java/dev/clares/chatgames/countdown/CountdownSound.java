package dev.clares.chatgames.countdown;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public final class CountdownSound {
    private CountdownSound(){}
    public static void play(Minecraft client,int number,float volume) {
        float pitch=(float)Math.pow(2,(10-number-6)/12.0);
        client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HARP.value(),pitch,Math.max(0,Math.min(1,volume))));
    }
}
