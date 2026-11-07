package aethereal.event;


public final class SelectedSlotChangeEvent implements Event {
    public final int newSelectedSlot;

    public SelectedSlotChangeEvent(int i) {
        this.newSelectedSlot = i;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "newSelectedSlot=" + this.newSelectedSlot + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.newSelectedSlot);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SelectedSlotChangeEvent)) return false;
        SelectedSlotChangeEvent o = (SelectedSlotChangeEvent) obj;
        return java.util.Objects.equals(this.newSelectedSlot, o.newSelectedSlot);
    }
public int newSelectedSlot() {
        return this.newSelectedSlot;
    }
}
