package aethereal.model;

import net.minecraft.screen.slot.Slot;

public final class SlotReference {
    public final Slot slot;
    public final int slotId;

    public SlotReference(Slot slot, int i) {
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
        if (!(obj instanceof SlotReference)) return false;
        SlotReference o = (SlotReference) obj;
        return java.util.Objects.equals(this.slot, o.slot) && java.util.Objects.equals(this.slotId, o.slotId);
    }
public Slot slot() {
        return this.slot;
    }

    public int slotId() {
        return this.slotId;
    }
}
