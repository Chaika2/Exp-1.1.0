package aethereal.type;
import aethereal.module.AttackAuraModule;
import aethereal.Expensive;
import aethereal.math.Rotation;
import aethereal.math.RotationMath;

import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SmoothRotationMode extends RotationMode {
    public SmoothRotationMode() {
        super("Funtime");
    }

    @Override
    public Rotation process(Rotation class007Var, Rotation class007Var2, Vec3d vec3d, Entity entity) {
        Rotation class007VarCalculateRotationDifference = RotationMath.INSTANCE.calculateRotationDifference(class007Var2, class007Var);
        float yaw = class007VarCalculateRotationDifference.getYaw();
        float pitch = class007VarCalculateRotationDifference.getPitch();
        float fHypot = (float) Math.hypot(Math.abs(yaw), Math.abs(pitch));
        float fAbs = Math.abs(yaw / fHypot) * 60.0f;
        float fAbs2 = Math.abs(pitch / fHypot) * 40.0f;
        float fClamp = MathHelper.clamp(yaw, -fAbs, fAbs) + (shouldCrit() ? 0.0f : (float) Math.ceil(((double) randomLerp(2.0f, 5.0f)) * Math.sin(System.currentTimeMillis() / 40.0d)));
        float fClamp2 = MathHelper.clamp(pitch, -fAbs2, fAbs2);
        Rotation class007Var3 = new Rotation(class007Var.getYaw(), class007Var.getPitch());
        class007Var3.setYaw((float) MathHelper.lerp(0.6000000238418579d + (((double) ThreadLocalRandom.current().nextFloat(0.0f, 1.0f)) * 0.6000000238418579d), class007Var.getYaw(), class007Var.getYaw() + fClamp));
        class007Var3.setPitch((float) MathHelper.lerp(0.5d + (((double) ThreadLocalRandom.current().nextFloat(0.0f, 1.0f)) * 0.5d), class007Var.getPitch(), class007Var.getPitch() + fClamp2));
        return new Rotation(class007Var3.getYaw(), class007Var3.getPitch());
    }

    public boolean shouldCrit() {
        AttackAuraModule class878Var = (AttackAuraModule) Expensive.INSTANCE.moduleRepository().get(AttackAuraModule.class);
        return class878Var.target() != null && class878Var.canCrit(Mc.INSTANCE.getPlayer(), 0);
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0.0d, 0.0d, 0.0d);
    }

    public float randomLerp(float f, float f2) {
        return MathHelper.lerp(new SecureRandom().nextFloat(), f, f2);
    }
}
