package aethereal.model;
import aethereal.type.InventoryScope;


public final class ScopedSlot {
    public final int slot;
    public final InventoryScope scope;

    public ScopedSlot(int i, InventoryScope class305Var) {
        this.slot = i;
        this.scope = class305Var;
    }

    public int increasedSlot() {
        return this.slot < 9 ? this.slot + 36 : this.slot;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "slot=" + this.slot + ", " + "scope=" + this.scope + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.slot, this.scope);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ScopedSlot)) return false;
        ScopedSlot o = (ScopedSlot) obj;
        return java.util.Objects.equals(this.slot, o.slot) && java.util.Objects.equals(this.scope, o.scope);
    }
public int slot() {
        return this.slot;
    }

    public InventoryScope scope() {
        return this.scope;
    }
}
