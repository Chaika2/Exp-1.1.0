package aethereal.model;

import net.minecraft.item.ItemStack;

public final class PricedItemStack {
    public final int price;
    public final ItemStack stack;

    public PricedItemStack(int i, ItemStack itemStack) {
        this.price = i;
        this.stack = itemStack;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "price=" + this.price + ", " + "stack=" + this.stack + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.price, this.stack);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PricedItemStack)) return false;
        PricedItemStack o = (PricedItemStack) obj;
        return java.util.Objects.equals(this.price, o.price) && java.util.Objects.equals(this.stack, o.stack);
    }
public int price() {
        return this.price;
    }

    public ItemStack stack() {
        return this.stack;
    }
}
