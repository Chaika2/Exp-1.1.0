package aethereal.internal;

import net.minecraft.entity.EquipmentSlot;

public class EquipmentSlotIndexMap {
    public static final int[] slotOrdinals = new int[EquipmentSlot.values().length];

    static {
        try {
            slotOrdinals[EquipmentSlot.HEAD.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            slotOrdinals[EquipmentSlot.CHEST.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            slotOrdinals[EquipmentSlot.LEGS.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            slotOrdinals[EquipmentSlot.FEET.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
    }
}
