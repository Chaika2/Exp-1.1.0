package aethereal.math;
import aethereal.type.Mc;
import aethereal.type.RotationMode;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public final class ScheduledRotation {
    public final Rotation rotation;
    public final RotationMode rotationProcessor;
    public final Vec3d vec3d;
    public final Entity entity;
    public final int resetThreshold;
    public final int ticksUntilReset;
    public final boolean clientRotation;
    public final boolean moveCorrection;
    public final boolean focusedCorrection;

    public ScheduledRotation(Rotation class007Var, RotationMode class405Var, Vec3d vec3d, Entity entity, int i, int i2, boolean z, boolean z2, boolean z3) {
        this.rotation = class007Var;
        this.rotationProcessor = class405Var;
        this.vec3d = vec3d;
        this.entity = entity;
        this.resetThreshold = i;
        this.ticksUntilReset = i2;
        this.clientRotation = z;
        this.moveCorrection = z2;
        this.focusedCorrection = z3;
    }

    public Rotation towards(Rotation class007Var, boolean z) {
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        return z ? this.rotationProcessor.process(class007Var, new Rotation(player.getYaw(), player.getPitch())) : this.rotationProcessor.process(class007Var, this.rotation, this.vec3d, this.entity);
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "rotation=" + this.rotation + ", " + "rotationProcessor=" + this.rotationProcessor + ", " + "vec3d=" + this.vec3d + ", " + "entity=" + this.entity + ", " + "resetThreshold=" + this.resetThreshold + ", " + "ticksUntilReset=" + this.ticksUntilReset + ", " + "clientRotation=" + this.clientRotation + ", " + "moveCorrection=" + this.moveCorrection + ", " + "focusedCorrection=" + this.focusedCorrection + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.rotation, this.rotationProcessor, this.vec3d, this.entity, this.resetThreshold, this.ticksUntilReset, this.clientRotation, this.moveCorrection, this.focusedCorrection);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ScheduledRotation)) return false;
        ScheduledRotation o = (ScheduledRotation) obj;
        return java.util.Objects.equals(this.rotation, o.rotation) && java.util.Objects.equals(this.rotationProcessor, o.rotationProcessor) && java.util.Objects.equals(this.vec3d, o.vec3d) && java.util.Objects.equals(this.entity, o.entity) && java.util.Objects.equals(this.resetThreshold, o.resetThreshold) && java.util.Objects.equals(this.ticksUntilReset, o.ticksUntilReset) && java.util.Objects.equals(this.clientRotation, o.clientRotation) && java.util.Objects.equals(this.moveCorrection, o.moveCorrection) && java.util.Objects.equals(this.focusedCorrection, o.focusedCorrection);
    }
public Rotation rotation() {
        return this.rotation;
    }

    public RotationMode rotationProcessor() {
        return this.rotationProcessor;
    }

    public Vec3d vec3d() {
        return this.vec3d;
    }

    public Entity entity() {
        return this.entity;
    }

    public int resetThreshold() {
        return this.resetThreshold;
    }

    public int ticksUntilReset() {
        return this.ticksUntilReset;
    }

    public boolean clientRotation() {
        return this.clientRotation;
    }

    public boolean moveCorrection() {
        return this.moveCorrection;
    }

    public boolean focusedCorrection() {
        return this.focusedCorrection;
    }
}
