package aethereal.event;
import aethereal.type.PushType;

public class PushEvent extends CancellableEvent {
    public final PushType type;

    public PushType getType() {
        return this.type;
    }

    public PushEvent(PushType class232Var) {
        this.type = class232Var;
    }
}
