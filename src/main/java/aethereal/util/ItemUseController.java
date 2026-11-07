package aethereal.util;
import aethereal.type.Mc;

import net.minecraft.util.Hand;

public class ItemUseController {
    public static final ItemUseController INSTANCE = new ItemUseController();
    public boolean useItem;

    public void useHand(Hand hand) {
        Mc class815Var = Mc.INSTANCE;
        if (class815Var.isWorldLoaded() && (!class815Var.getPlayer().isUsingItem() || !class815Var.getPlayer().getActiveHand().equals(hand))) {
            class815Var.getInteractionManager().interactItem(class815Var.getPlayer(), hand);
            class815Var.getPlayer().usingItem = true;
        }
        this.useItem = true;
    }

    public void setUseItem(boolean z) {
        this.useItem = z;
    }

    public boolean isUseItem() {
        return this.useItem;
    }
}
