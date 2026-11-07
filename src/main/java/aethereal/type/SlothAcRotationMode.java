package aethereal.type;
import aethereal.module.AttackAuraModule;
import aethereal.Expensive;
import aethereal.math.Rotation;
import aethereal.math.RotationMath;
import aethereal.math.Stopwatch;

import java.security.SecureRandom;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SlothAcRotationMode extends RotationMode {
    public SlothAcRotationMode() {
        super("SlothAC");
    }

    @Override
    public Rotation process(Rotation class007Var, Rotation class007Var2, Vec3d vec3d, Entity entity) {
        char c;
        Rotation class007Var3;
        AttackAuraModule class878Var = (AttackAuraModule) Expensive.INSTANCE.moduleRepository().get(AttackAuraModule.class);
        Stopwatch class314VarAttackTimer = class878Var.attackTimer();
        int iHitCounter = class878Var.hitCounter();
        if (class878Var.canAttack(Mc.INSTANCE.getPlayer(), 0)) {
            c = 0;
        } else {
            c = new SecureRandom().nextBoolean() ? (char) 26214 : (char) 52429;
        }
        Rotation class007VarCalculateRotationDifference = RotationMath.INSTANCE.calculateRotationDifference(class007Var2, class007Var);
        float yaw = class007VarCalculateRotationDifference.getYaw();
        float pitch = class007VarCalculateRotationDifference.getPitch();
        float fHypot = (float) Math.hypot(Math.abs(yaw), Math.abs(pitch));
        float fAbs = Math.abs(yaw / fHypot) * 180.0f;
        float fAbs2 = Math.abs(pitch / fHypot) * 180.0f;
        float fClamp = MathHelper.clamp(yaw, -fAbs, fAbs);
        float fClamp2 = MathHelper.clamp(pitch, -fAbs2, fAbs2);
        float fClamp3 = Math.clamp(randomLerp(0.75f, 0.95f), 0.0f, 1.0f);
        if (class878Var.target() != null && class878Var.canCrit(Mc.INSTANCE.getPlayer(), 0)) {
            return new Rotation(MathHelper.lerp(fClamp3, class007Var.getYaw(), class007Var.getYaw() + fClamp), MathHelper.lerp(fClamp3, class007Var.getPitch(), class007Var.getPitch() + fClamp2));
        }
        float fCurrentTimeMillis = (System.currentTimeMillis() % 2000) / 75.0f;
        float fCurrentTimeMillis2 = (System.currentTimeMillis() % 1000) / 75.0f;
        switch (iHitCounter % 4) {
            case 0:
                class007Var3 = new Rotation((float) Math.cos(fCurrentTimeMillis), (float) Math.sin(fCurrentTimeMillis2));
                break;
            case 1:
                class007Var3 = new Rotation((float) Math.sin(fCurrentTimeMillis), (float) Math.cos(fCurrentTimeMillis2));
                break;
            case 2:
                class007Var3 = new Rotation((float) Math.sin(fCurrentTimeMillis), (float) (-Math.cos(fCurrentTimeMillis2)));
                break;
            default:
                class007Var3 = new Rotation((float) (-Math.cos(fCurrentTimeMillis)), (float) Math.sin(fCurrentTimeMillis2));
                break;
        }
        Rotation class007Var4 = class007Var3;
        boolean z = (class314VarAttackTimer.hasElapsed(800L) && entity == null) ? false : true;
        return new Rotation(MathHelper.lerp(fClamp3, class007Var.getYaw(), class007Var.getYaw() + fClamp) + (z ? 8.0f * class007Var4.getYaw() : 0.0f), MathHelper.lerp(fClamp3, class007Var.getPitch(), class007Var.getPitch() + fClamp2) + (z ? 8.0f * class007Var4.getPitch() : 0.0f));
    }

    public static float lerp(float f, float f2, float f3) {
        return f3;
    }

    public float randomLerp(float f, float f2) {
        return MathHelper.lerp(new SecureRandom().nextFloat(), f, f2);
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0.1d, 0.10000000149011612d, 0.10000000149011612d);
    }
}
