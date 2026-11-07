package aethereal.model;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class PlayerSnapshot {
    public final Box box;
    public final Vec3d pos;
    public final boolean verticalCollision;

    public PlayerSnapshot(Box box, Vec3d vec3d, boolean z) {
        this.box = box;
        this.pos = vec3d;
        this.verticalCollision = z;
    }

    public static PlayerSnapshot from(LivingEntity livingEntity) {
        return new PlayerSnapshot(livingEntity.getBoundingBox(), livingEntity.getPos(), livingEntity.verticalCollision);
    }
}
