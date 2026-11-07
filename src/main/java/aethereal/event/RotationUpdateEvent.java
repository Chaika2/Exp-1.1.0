package aethereal.event;
import aethereal.type.EventPhase;


public final class RotationUpdateEvent implements Event {
    public final EventPhase stage;

    public RotationUpdateEvent(EventPhase class346Var) {
        this.stage = class346Var;
    }

    public boolean isPost() {
        return this.stage == EventPhase.POST;
    }

    public boolean isPre() {
        return this.stage == EventPhase.PRE;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "stage=" + this.stage + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.stage);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RotationUpdateEvent)) return false;
        RotationUpdateEvent o = (RotationUpdateEvent) obj;
        return java.util.Objects.equals(this.stage, o.stage);
    }
public EventPhase stage() {
        return this.stage;
    }
}
