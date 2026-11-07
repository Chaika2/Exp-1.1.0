package aethereal.type;
import aethereal.math.Rotation;
import aethereal.math.RotationMath;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SpeedRotationMode extends RotationMode {
    float yawSpeed;
    float pitchSpeed;

    public SpeedRotationMode(float f, float f2) {
        super("Linear Rotation");
        this.yawSpeed = f;
        this.pitchSpeed = f2;
    }

    @Override
    public Rotation process(Rotation class007Var, Rotation class007Var2, Vec3d vec3d, Entity entity) {
        Rotation class007VarCalculateRotationDifference = RotationMath.INSTANCE.calculateRotationDifference(class007Var2, class007Var);
        float yaw = class007VarCalculateRotationDifference.getYaw();
        float pitch = class007VarCalculateRotationDifference.getPitch();
        float fHypot = (float) Math.hypot(Math.abs(yaw), Math.abs(pitch));
        float fAbs = Math.abs(yaw / fHypot) * this.yawSpeed;
        float fAbs2 = Math.abs(pitch / fHypot) * this.pitchSpeed;
        return new Rotation(class007Var.getYaw() + MathHelper.clamp(yaw, -fAbs, fAbs), class007Var.getPitch() + MathHelper.clamp(pitch, -fAbs2, fAbs2));
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0.0d, 0.0d, 0.0d);
    }
}
