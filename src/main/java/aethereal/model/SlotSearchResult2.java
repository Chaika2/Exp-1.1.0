package aethereal.model;

import net.minecraft.item.ItemStack;

public final class SlotSearchResult2 {
    public final ScopedSlot slotReference;
    public final ItemStack stack;

    public SlotSearchResult2(ScopedSlot class246Var, ItemStack itemStack) {
        this.slotReference = class246Var;
        this.stack = itemStack;
    }

    public boolean found() {
        return this.slotReference != null;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "slotReference=" + this.slotReference + ", " + "stack=" + this.stack + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.slotReference, this.stack);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SlotSearchResult2)) return false;
        SlotSearchResult2 o = (SlotSearchResult2) obj;
        return java.util.Objects.equals(this.slotReference, o.slotReference) && java.util.Objects.equals(this.stack, o.stack);
    }
public ScopedSlot slotReference() {
        return this.slotReference;
    }

    public ItemStack stack() {
        return this.stack;
    }
}
