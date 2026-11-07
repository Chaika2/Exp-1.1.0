package aethereal.math;
import aethereal.util.MathUtil;
import aethereal.type.Mc;
import aethereal.util.RotationManager;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Rotation {
    public float yaw;
    public float pitch;
    public static Rotation ZERO = new Rotation(0.0f, 0.0f);

    public static Rotation playerRotation() {
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        return player == null ? ZERO : new Rotation(player.getYaw(), player.getPitch());
    }

    public Rotation random(float f) {
        return add(MathUtil.getRandom(-f, f), MathUtil.getRandom(-f, f));
    }

    public Rotation add(float f, float f2) {
        return new Rotation(this.yaw + f, MathUtil.clamp(this.pitch + f2, -90.0f, 90.0f));
    }

    public Rotation addYaw(float f) {
        this.yaw += f;
        return this;
    }

    public Rotation add(Rotation class007Var) {
        return new Rotation(this.yaw + class007Var.yaw, this.pitch + class007Var.pitch);
    }

    public static Rotation lookingAt(Vec3d vec3d, Vec3d vec3d2) {
        if (vec3d == null) {
            return playerRotation();
        }
        return RotationMath.INSTANCE.fromVec3d(vec3d.subtract(vec3d2).normalize());
    }

    public Vec3d getDirectionVector() {
        return RotationMath.INSTANCE.rotationToVector(this);
    }

    public float withFixedYaw(Rotation class007Var) {
        return class007Var.getYaw() + angleDifference(Mc.INSTANCE.getPlayer().getYaw(), class007Var.getYaw());
    }

    public static float angleDifference(float f, float f2) {
        return MathHelper.wrapDegrees(f - f2);
    }

    public float angleTo(Rotation class007Var) {
        return Math.min(rotationDeltaTo(class007Var).length(), 180.0f);
    }

    public RotationDelta rotationDeltaTo(Rotation class007Var) {
        return new RotationDelta(angleDifference(class007Var.yaw, this.yaw), angleDifference(class007Var.pitch, this.pitch));
    }

    public Rotation normalize() {
        Rotation currentRotation = RotationManager.INSTANCE.getCurrentRotation();
        double dComputeGcd = MathUtil.computeGcd();
        RotationDelta class397VarRotationDeltaTo = currentRotation.rotationDeltaTo(this);
        return new Rotation(currentRotation.yaw + ((float) (((double) ((int) (((double) class397VarRotationDeltaTo.deltaYaw()) / dComputeGcd))) * dComputeGcd)), MathUtil.clamp(currentRotation.pitch + ((float) (((double) ((int) (((double) class397VarRotationDeltaTo.deltaPitch()) / dComputeGcd))) * dComputeGcd)), -90.0f, 90.0f));
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setYaw(float f) {
        this.yaw = f;
    }

    public void setPitch(float f) {
        this.pitch = f;
    }

    public Rotation(float f, float f2) {
        this.yaw = f;
        this.pitch = f2;
    }
}
