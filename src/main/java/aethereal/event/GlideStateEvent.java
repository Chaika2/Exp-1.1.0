package aethereal.event;


public final class GlideStateEvent implements Event {
    public final boolean gliding;

    public GlideStateEvent(boolean z) {
        this.gliding = z;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "gliding=" + this.gliding + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.gliding);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GlideStateEvent)) return false;
        GlideStateEvent o = (GlideStateEvent) obj;
        return java.util.Objects.equals(this.gliding, o.gliding);
    }
public boolean gliding() {
        return this.gliding;
    }
}
