package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

import java.util.function.Predicate;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public enum AutoSwapItemType implements DisplayNamed {
    SHIELD(Translation.clearText("Щит"), Items.SHIELD, itemStack -> {
        return itemStack.getItem() == Items.SHIELD;
    }),
    GAPPLE(Translation.clearText("Гепл"), Items.GOLDEN_APPLE, itemStack2 -> {
        return itemStack2.getItem() == Items.GOLDEN_APPLE;
    }),
    BALL(Translation.clearText("Шар"), Items.ENCHANTED_GOLDEN_APPLE, itemStack3 -> {
        return itemStack3.getItem() == Items.ENCHANTED_GOLDEN_APPLE && !itemStack3.contains(DataComponentTypes.CUSTOM_DATA);
    }),
    RUNE(Translation.clearText("Руна"), Items.FIREWORK_STAR, itemStack4 -> {
        return itemStack4.getItem() == Items.FIREWORK_STAR;
    }),
    SPHERE(Translation.clearText("Сфера"), Items.ENCHANTED_GOLDEN_APPLE, itemStack5 -> {
        return itemStack5.getItem() == Items.ENCHANTED_GOLDEN_APPLE && itemStack5.contains(DataComponentTypes.CUSTOM_DATA);
    }),
    TOTEM(Translation.clearText("Тотем"), Items.TOTEM_OF_UNDYING, itemStack6 -> {
        return itemStack6.getItem() == Items.TOTEM_OF_UNDYING;
    });

    public final Translation displayName;
    public final Item displayItem;
    public final Predicate<ItemStack> filter;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }

    public Item displayItem() {
        return this.displayItem;
    }

    public Predicate<ItemStack> filter() {
        return this.filter;
    }

    AutoSwapItemType(Translation class254Var, Item item, Predicate<ItemStack> predicate) {
        this.displayName = class254Var;
        this.displayItem = item;
        this.filter = predicate;
    }
}
