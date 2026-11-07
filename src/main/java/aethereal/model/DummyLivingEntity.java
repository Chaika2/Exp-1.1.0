package aethereal.model;
import aethereal.util.PlayerSnapshotManager;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.world.World;

public class DummyLivingEntity extends LivingEntity {
    public DummyLivingEntity(PlayerSnapshotManager class369Var, EntityType entityType, World world) {
        super(entityType, world);
    }

    public Iterable<ItemStack> getArmorItems() {
        return null;
    }

    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return null;
    }

    public void equipStack(EquipmentSlot slot, ItemStack stack) {
    }

    public Arm getMainArm() {
        return null;
    }
}
