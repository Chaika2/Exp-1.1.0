package aethereal.type;
import aethereal.util.BlockUtil;
import aethereal.module.CreeperFarmModule;
import aethereal.util.IteratorUtil;

import java.util.Comparator;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class CreeperFarmLootPhase {
    public final Mc mc = Mc.INSTANCE;
    public final CreeperFarmModule module;

    public boolean shouldEnter() {
        ItemEntity itemEntityMethod001 = findLootItem();
        if (itemEntityMethod001 == null) {
            return false;
        }
        Vec3d pos = itemEntityMethod001.getPos();
        return this.module.getCreepersSortedByDistance().stream().noneMatch(creeperEntity -> {
            return creeperEntity.getPos().isInRange(pos, 6.0d);
        });
    }

    public void tickLootingPhase(ClientPlayerEntity clientPlayerEntity) {
        ItemEntity itemEntityMethod001 = findLootItem();
        if (itemEntityMethod001 == null) {
            if (this.module.getCreepersSortedByDistance().isEmpty()) {
                this.module.getPhaseManager().setPhase(TpLootStage.LOADING_CHUNKS);
                return;
            } else {
                this.module.getPhaseManager().setPhase(TpLootStage.APPROACH);
                return;
            }
        }
        if (isCreeperNear(itemEntityMethod001.getPos())) {
            this.module.getPhaseManager().setPhase(TpLootStage.APPROACH);
        } else {
            if (clientPlayerEntity.getPos().isInRange(itemEntityMethod001.getPos(), 1.0d)) {
            }
        }
    }

    public ItemEntity findLootItem() {
        return (ItemEntity) IteratorUtil.toList(this.mc.getWorld().getEntities().iterator()).stream().filter(entity -> {
            return entity instanceof ItemEntity;
        }).map(entity2 -> {
            return (ItemEntity) entity2;
        }).filter(itemEntity -> {
            return itemEntity.getStack().getItem() == Items.GUNPOWDER || itemEntity.getStack().getItem() == Items.EXPERIENCE_BOTTLE;
        }).filter(itemEntity2 -> {
            return BlockUtil.isWithinRegion(itemEntity2.getBlockPos(), this.module.getRegionMin(), this.module.getRegionMax());
        }).min(Comparator.comparingDouble(itemEntity3 -> {
            return itemEntity3.distanceTo(this.mc.getPlayer());
        })).orElse(null);
    }

    public boolean isCreeperNear(Vec3d vec3d) {
        return IteratorUtil.toList(this.mc.getWorld().getEntities().iterator()).stream().filter(entity -> {
            if (entity instanceof CreeperEntity) {
                CreeperEntity creeperEntity = (CreeperEntity) entity;
                if (creeperEntity.getHealth() > 0.0f && creeperEntity.isAlive()) {
                    return true;
                }
            }
            return false;
        }).map(entity2 -> {
            return (CreeperEntity) entity2;
        }).anyMatch(creeperEntity -> {
            return creeperEntity.getPos().isInRange(vec3d, 6.0d);
        });
    }

    public CreeperFarmLootPhase(CreeperFarmModule class597Var) {
        this.module = class597Var;
    }
}
