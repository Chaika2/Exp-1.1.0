package aethereal.model;
import aethereal.render.AnimatedFloat;
import aethereal.math.Easings;
import aethereal.util.WeightedEngine;

import net.minecraft.item.ItemStack;

public class ItemBindEntry {
    public int count;
    public boolean active;
    public String keyText = "";
    public ItemStack stack = ItemStack.EMPTY;
    public final AnimatedFloat animation = new AnimatedFloat(250, Easings.LINEAR);

    public ItemBindEntry() {
    }

    public void animate(WeightedEngine class141Var) {
        this.animation.destination(this.active ? 1.0f : 0.0f);
        this.animation.animate(class141Var);
    }
}
