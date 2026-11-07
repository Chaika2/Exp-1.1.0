package aethereal.model;
import aethereal.ui.setting.KeybindSetting;

import java.util.function.Predicate;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class RwHelperItemBind {
    public final KeybindSetting bindSetting;
    public final Item item;
    public final String serverItemName;
    public final boolean serverItem;

    public RwHelperItemBind(KeybindSetting class663Var, Item item, String str, boolean z) {
        this.bindSetting = class663Var;
        this.item = item;
        this.serverItemName = str;
        this.serverItem = z;
    }

    public Predicate<ItemStack> getSearchPredicate() {
        if (this.item == Items.CROSSBOW) {
            return itemStack -> {
                return itemStack.getItem() == this.item && CrossbowItem.isCharged(itemStack);
            };
        }
        return (this.serverItemName == null || !this.serverItem) ? itemStack2 -> {
            return itemStack2.getItem() == this.item;
        } : itemStack3 -> {
            return itemStack3.getItem() == this.item && itemStack3.getName().getString().toLowerCase().trim().contains(this.serverItemName);
        };
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "bindSetting=" + this.bindSetting + ", " + "item=" + this.item + ", " + "serverItemName=" + this.serverItemName + ", " + "serverItem=" + this.serverItem + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.bindSetting, this.item, this.serverItemName, this.serverItem);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RwHelperItemBind)) return false;
        RwHelperItemBind o = (RwHelperItemBind) obj;
        return java.util.Objects.equals(this.bindSetting, o.bindSetting) && java.util.Objects.equals(this.item, o.item) && java.util.Objects.equals(this.serverItemName, o.serverItemName) && java.util.Objects.equals(this.serverItem, o.serverItem);
    }
public KeybindSetting bindSetting() {
        return this.bindSetting;
    }

    public Item item() {
        return this.item;
    }

    public String serverItemName() {
        return this.serverItemName;
    }

    public boolean serverItem() {
        return this.serverItem;
    }
}
