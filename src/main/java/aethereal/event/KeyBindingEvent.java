package aethereal.event;

import net.minecraft.client.option.KeyBinding;

public class KeyBindingEvent extends CancellableEvent {
    public final KeyBinding keyBinding;

    public KeyBindingEvent(KeyBinding keyBinding) {
        this.keyBinding = keyBinding;
    }

    public KeyBinding getKeyBinding() {
        return this.keyBinding;
    }
}
