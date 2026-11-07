package aethereal.type;
import aethereal.util.MathUtil;
import aethereal.math.Rotation;
import aethereal.math.RotationMath;

import java.security.SecureRandom;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BlockRotationMode extends RotationMode {
    public BlockRotationMode() {
        super("Block Rotation");
    }

    @Override
    public Rotation process(Rotation class007Var, Rotation class007Var2, Vec3d vec3d, Entity entity) {
        float f;
        Rotation class007VarCalculateRotationDifference = RotationMath.INSTANCE.calculateRotationDifference(class007Var2, class007Var);
        float yaw = class007VarCalculateRotationDifference.getYaw();
        float pitch = class007VarCalculateRotationDifference.getPitch();
        float fHypot = (float) Math.hypot(Math.abs(yaw), Math.abs(pitch));
        if (entity != null) {
            f = 1.0f;
        } else {
            f = new SecureRandom().nextBoolean() ? 0.4f : 0.2f;
        }
        float f2 = f;
        float fAbs = Math.abs(yaw / fHypot) * 200.0f;
        float fAbs2 = Math.abs(pitch / fHypot) * 200.0f;
        float fClamp = MathHelper.clamp(yaw, -fAbs, fAbs);
        float fClamp2 = MathHelper.clamp(pitch, -fAbs2, fAbs2);
        Rotation class007Var3 = new Rotation(class007Var.getYaw(), class007Var.getPitch());
        class007Var3.setYaw(MathHelper.lerp(Math.clamp(MathUtil.getRandom(f2, f2 + 0.2f), 0.0f, 1.0f), class007Var.getYaw(), class007Var.getYaw() + fClamp));
        class007Var3.setPitch(MathHelper.lerp(Math.clamp(MathUtil.getRandom(f2, f2 + 0.2f), 0.0f, 1.0f), class007Var.getPitch(), class007Var.getPitch() + fClamp2));
        return new Rotation(class007Var3.getYaw(), class007Var3.getPitch());
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0.0d, 0.0d, 0.0d);
    }
}
