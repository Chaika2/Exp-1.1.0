package aethereal.module;
import aethereal.util.IteratorUtil;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.PlayerTickEvent;

import java.util.Comparator;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Vec3d;

public class AutoTpLootModule extends Module {
    public final Mc mc;
    public ItemEntity targetItem;
    public Vec3d returnPos;
    public boolean movingToItem;
    public boolean returning;

    public AutoTpLootModule() {
        super(ModuleTab.PLAYER, "AutoTpLoot");
        this.mc = Mc.INSTANCE;
        this.targetItem = null;
        this.returnPos = null;
        this.movingToItem = false;
        this.returning = false;
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && class130Var.isPre() && this.mc.isWorldLoaded()) {
                if (!this.mc.getPlayer().getAbilities().flying) {
                    reset();
                    return;
                }
                ClientPlayerEntity player = this.mc.getPlayer();
                if (this.targetItem == null) {
                    this.targetItem = (ItemEntity) IteratorUtil.toList(this.mc.getWorld().getEntities().iterator()).stream().filter(entity -> {
                        return entity instanceof ItemEntity;
                    }).map(entity2 -> {
                        return (ItemEntity) entity2;
                    }).filter((v0) -> {
                        return v0.isOnGround();
                    }).min(Comparator.comparingDouble(itemEntity -> {
                        return itemEntity.squaredDistanceTo(player);
                    })).orElse(null);
                    if (this.targetItem != null && this.returnPos == null) {
                        this.returnPos = player.getPos();
                        this.movingToItem = true;
                    }
                }
                if (this.targetItem != null && !this.targetItem.isAlive()) {
                    this.movingToItem = false;
                    this.returning = true;
                }
                if (this.targetItem != null || this.movingToItem || this.returning) {
                    if (this.movingToItem && this.targetItem != null) {
                        Vec3d vec3dSubtract = new Vec3d(this.targetItem.getX(), this.targetItem.getY(), this.targetItem.getZ()).subtract(player.getPos());
                        player.setVelocity(vec3dSubtract.normalize().multiply((float) Math.min(35.0d, vec3dSubtract.length() * 0.5d)));
                    }
                    if (!this.returning || this.returnPos == null) {
                        return;
                    }
                    Vec3d vec3dSubtract2 = this.returnPos.subtract(player.getPos());
                    double length = vec3dSubtract2.length();
                    player.setVelocity(vec3dSubtract2.normalize().multiply((float) Math.min(35.0d, length * 0.5d)));
                    if (length < 0.5d) {
                        reset();
                        player.setVelocity(Vec3d.ZERO);
                    }
                }
            }
        });
    }

    @Override
    public void deactivate() {
        reset();
        super.deactivate();
    }

    public void reset() {
        this.targetItem = null;
        this.returnPos = null;
        this.movingToItem = false;
        this.returning = false;
    }
}
