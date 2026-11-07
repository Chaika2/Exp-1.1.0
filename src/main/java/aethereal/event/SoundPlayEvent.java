package aethereal.event;

import net.minecraft.client.sound.SoundInstance;

public class SoundPlayEvent extends CancellableEvent {
    public final SoundInstance soundInstance;
    public float volumeMultiplier = 1.0f;

    public SoundInstance getSoundInstance() {
        return this.soundInstance;
    }

    public float getVolumeMultiplier() {
        return this.volumeMultiplier;
    }

    public void setVolumeMultiplier(float f) {
        this.volumeMultiplier = f;
    }

    public SoundPlayEvent(SoundInstance soundInstance) {
        this.soundInstance = soundInstance;
    }
}
