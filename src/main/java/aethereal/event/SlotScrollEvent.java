package aethereal.event;

import net.minecraft.screen.slot.Slot;

public final class SlotScrollEvent implements Event {
    public final Slot slot;
    public final int slotId;

    public SlotScrollEvent(Slot slot, int i) {
        this.slot = slot;
        this.slotId = i;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "slot=" + this.slot + ", " + "slotId=" + this.slotId + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.slot, this.slotId);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SlotScrollEvent)) return false;
        SlotScrollEvent o = (SlotScrollEvent) obj;
        return java.util.Objects.equals(this.slot, o.slot) && java.util.Objects.equals(this.slotId, o.slotId);
    }
public Slot slot() {
        return this.slot;
    }

    public int slotId() {
        return this.slotId;
    }
}
