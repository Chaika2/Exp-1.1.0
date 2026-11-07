package aethereal.event;
import aethereal.type.OverlayEffectType;

public class OverlayEffectEvent extends CancellableEvent {
    OverlayEffectType type;

    public OverlayEffectEvent(OverlayEffectType class070Var) {
        this.type = class070Var;
    }

    public OverlayEffectType getType() {
        return this.type;
    }
}
