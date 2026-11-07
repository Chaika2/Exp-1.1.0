package ru.expensive.mixin;

import aethereal.accessor.ArmorItemAccessor;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ArmorItem.class})
public abstract class ArmorItemMixin implements ArmorItemAccessor {

    @Unique
    private ArmorMaterial armorMaterial;

    @Unique
    private EquipmentType type;

    @Inject(method = {"<init>"}, at = {@At("RETURN")})
    public void hookCatchArgs(ArmorMaterial armorMaterial, EquipmentType equipmentType, Item.Settings settings, CallbackInfo callbackInfo) {
        this.armorMaterial = armorMaterial;
        this.type = equipmentType;
    }

    @Override
    public ArmorMaterial expensive_ru$getMaterial() {
        return this.armorMaterial;
    }

    @Override
    public EquipmentType expensive_ru$getType() {
        return this.type;
    }
}
