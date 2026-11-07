package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

import java.util.Collections;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public enum AutoDuelArmorType implements DisplayNamed {
    LEATHER(Lang.AUTODUEL_ARMOR_TYPE_LEATHER, List.of(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS)),
    IRON(Lang.AUTODUEL_ARMOR_TYPE_IRON, List.of(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS)),
    GOLD(Lang.AUTODUEL_ARMOR_TYPE_GOLD, List.of(Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS)),
    DIAMOND(Lang.AUTODUEL_ARMOR_TYPE_DIAMOND, List.of(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS)),
    NETHERITE(Lang.AUTODUEL_ARMOR_TYPE_NETHERITE, List.of(Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS)),
    ANY(Lang.AUTODUEL_ARMOR_TYPE_ANY, Collections.emptyList()),
    NONE(Lang.AUTODUEL_ARMOR_TYPE_NONE, Collections.emptyList());

    public final Translation displayName;
    public final List<Item> items;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }

    public List<Item> items() {
        return this.items;
    }

    AutoDuelArmorType(Translation class254Var, List list) {
        this.displayName = class254Var;
        this.items = list;
    }
}
