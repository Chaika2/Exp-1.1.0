package aethereal.event;

import net.minecraft.entity.effect.StatusEffect;

public class StatusEffectEvent extends CancellableEvent {
    public final StatusEffect effect;

    public StatusEffect getEffect() {
        return this.effect;
    }

    public StatusEffectEvent(StatusEffect statusEffect) {
        this.effect = statusEffect;
    }
}
