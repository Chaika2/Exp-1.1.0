package aethereal.event;


public final class DeathTickEvent implements Event {
    public final int ticksSinceDeath;

    public DeathTickEvent(int i) {
        this.ticksSinceDeath = i;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "ticksSinceDeath=" + this.ticksSinceDeath + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.ticksSinceDeath);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DeathTickEvent)) return false;
        DeathTickEvent o = (DeathTickEvent) obj;
        return java.util.Objects.equals(this.ticksSinceDeath, o.ticksSinceDeath);
    }
public int ticksSinceDeath() {
        return this.ticksSinceDeath;
    }
}
