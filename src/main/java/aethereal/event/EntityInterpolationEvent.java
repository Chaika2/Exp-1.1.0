package aethereal.event;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class EntityInterpolationEvent extends CancellableEvent {
    public final Vec3d original;
    public final Entity target;
    public final float tickDelta;
    public Vec3d changedVector = Vec3d.ZERO;

    public Vec3d original() {
        return this.original;
    }

    public Entity target() {
        return this.target;
    }

    public float tickDelta() {
        return this.tickDelta;
    }

    public Vec3d changedVector() {
        return this.changedVector;
    }

    public EntityInterpolationEvent(Vec3d vec3d, Entity entity, float f) {
        this.original = vec3d;
        this.target = entity;
        this.tickDelta = f;
    }

    public EntityInterpolationEvent changedVector(Vec3d vec3d) {
        this.changedVector = vec3d;
        return this;
    }
}
