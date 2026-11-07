package aethereal.event;

import net.minecraft.screen.slot.Slot;

public final class FocusedSlotEvent implements Event {
    public final Slot focusedSlot;

    public FocusedSlotEvent(Slot slot) {
        this.focusedSlot = slot;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "focusedSlot=" + this.focusedSlot + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.focusedSlot);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FocusedSlotEvent)) return false;
        FocusedSlotEvent o = (FocusedSlotEvent) obj;
        return java.util.Objects.equals(this.focusedSlot, o.focusedSlot);
    }
public Slot focusedSlot() {
        return this.focusedSlot;
    }
}
