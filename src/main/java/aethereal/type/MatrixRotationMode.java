package aethereal.type;
import aethereal.module.AttackAuraModule;
import aethereal.Expensive;
import aethereal.util.MathUtil;
import aethereal.util.RaycastUtil;
import aethereal.math.Rotation;
import aethereal.math.RotationMath;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MatrixRotationMode extends RotationMode {
    private static final float YAW_SPEED = 52.0f;
    private static final float PITCH_SPEED = 5.0f;
    private static final float YAW_JITTER = 3.0f;
    private static final float PITCH_JITTER = 0.5f;
    private static final float JITTER_RANDOM = 0.5f;

    public MatrixRotationMode() {
        super("Matrix");
    }

    @Override
    public Rotation process(Rotation class007Var, Rotation class007Var2, Vec3d vec3d, Entity entity) {
        AttackAuraModule class878Var = (AttackAuraModule) Expensive.INSTANCE.moduleRepository().get(AttackAuraModule.class);
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        boolean zAttackNow = class878Var != null && player != null && entity != null && class878Var.canAttack(player, 0);

        float targetYaw = class007Var2.getYaw();
        float targetPitch = class007Var2.getPitch();
        if (!zAttackNow) {
            int tick = player == null ? (int) (System.currentTimeMillis() / 50L) : player.age;
            targetYaw += (float) (Math.sin(tick) * ((double) YAW_JITTER)) + MathUtil.getRandom(-JITTER_RANDOM, JITTER_RANDOM);
            targetPitch += (float) (Math.cos(tick) * ((double) PITCH_JITTER)) + MathUtil.getRandom(-JITTER_RANDOM, JITTER_RANDOM);
        }

        Rotation class007VarCalculateRotationDifference = RotationMath.INSTANCE.calculateRotationDifference(new Rotation(targetYaw, targetPitch), class007Var);
        float yaw = MathHelper.clamp(class007VarCalculateRotationDifference.getYaw(), -YAW_SPEED, YAW_SPEED);
        float pitch = zAttackNow && !canHitCurrent(class878Var, player, class007Var, entity) ? class007VarCalculateRotationDifference.getPitch() : MathHelper.clamp(class007VarCalculateRotationDifference.getPitch(), -PITCH_SPEED, PITCH_SPEED);
        return new Rotation(MathHelper.wrapDegrees(class007Var.getYaw() + yaw), MathHelper.clamp(class007Var.getPitch() + pitch, -90.0f, 90.0f));
    }

    private boolean canHitCurrent(AttackAuraModule class878Var, ClientPlayerEntity player, Rotation class007Var, Entity entity) {
        double dCurrentValue = class878Var.maxDistance.currentValue();
        if (player.isCreative()) {
            dCurrentValue = Math.max(dCurrentValue, player.getEntityInteractionRange());
        }
        return RaycastUtil.rayTrace(RotationMath.INSTANCE.rotationToVector(class007Var), dCurrentValue, entity.getBoundingBox());
    }

    @Override
    public Vec3d randomValue() {
        return Vec3d.ZERO;
    }
}
