package aethereal.math;

import java.util.Optional;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ElytraSimulation {
    public final ClientPlayerEntity player;
    public double x;
    public double y;
    public double z;
    public double velocityX;
    public double velocityY;
    public double velocityZ;
    public final float yaw;
    public int tickCount;

    public ElytraSimulation(ClientPlayerEntity clientPlayerEntity, double d, double d2, double d3, double d4, double d5, double d6, float f) {
        this.player = clientPlayerEntity;
        this.x = d;
        this.y = d2;
        this.z = d3;
        this.velocityX = d4;
        this.velocityY = d5;
        this.velocityZ = d6;
        this.yaw = f;
    }

    public static ElytraSimulation fromPlayer(ClientPlayerEntity clientPlayerEntity) {
        return new ElytraSimulation(clientPlayerEntity, clientPlayerEntity.getX(), clientPlayerEntity.getY(), clientPlayerEntity.getZ(), clientPlayerEntity.getVelocity().x, clientPlayerEntity.getVelocity().y, clientPlayerEntity.getVelocity().z, clientPlayerEntity.getYaw());
    }

    public void simulateStep(Vec3d vec3d) {
        double d = 0.08d;
        if ((this.velocityY <= 0.0d) && hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            d = 0.01d;
        }
        float pitch = this.player.getPitch() * 0.017453292f;
        double dSqrt = Math.sqrt((vec3d.x * vec3d.x) + (vec3d.z * vec3d.z));
        double dSqrt2 = Math.sqrt((this.velocityX * this.velocityX) + (this.velocityZ * this.velocityZ));
        double length = vec3d.length();
        float fCos = MathHelper.cos(pitch);
        float fMin = (float) (((double) (fCos * fCos)) * Math.min(1.0d, length / 0.4d));
        Vec3d vec3dAdd = new Vec3d(this.velocityX, this.velocityY, this.velocityZ).add(0.0d, d * ((-1.0d) + (((double) fMin) * 0.75d)), 0.0d);
        if (vec3dAdd.y < 0.0d && dSqrt > 0.0d) {
            double d2 = vec3dAdd.y * (-0.1d) * ((double) fMin);
            vec3dAdd = vec3dAdd.add((vec3d.x * d2) / dSqrt, d2, (vec3d.z * d2) / dSqrt);
        }
        if (pitch < 0.0f && dSqrt > 0.0d) {
            double d3 = dSqrt2 * ((double) (-MathHelper.sin(pitch))) * 0.04d;
            vec3dAdd = vec3dAdd.add(((-vec3d.x) * d3) / dSqrt, d3 * 3.2d, ((-vec3d.z) * d3) / dSqrt);
        }
        if (dSqrt > 0.0d) {
            vec3dAdd = vec3dAdd.add((((vec3d.x / dSqrt) * dSqrt2) - vec3dAdd.x) * 0.1d, 0.0d, (((vec3d.z / dSqrt) * dSqrt2) - vec3dAdd.z) * 0.1d);
        }
        Vec3d vec3dAdd2 = vec3dAdd.add(Entity.movementInputToVelocity(new Vec3d(((double) this.player.input.movementSideways) * 0.98d, 0.0d, ((double) this.player.input.movementForward) * 0.98d), 0.02f, this.yaw));
        float velocityMultiplier = this.player.getVelocityMultiplier();
        this.velocityX = vec3dAdd2.x * 0.9900000095367432d * ((double) velocityMultiplier);
        this.velocityY = vec3dAdd2.y * 0.9800000190734863d;
        this.velocityZ = vec3dAdd2.z * 0.9900000095367432d * ((double) velocityMultiplier);
        this.x += this.velocityX;
        this.y += this.velocityY;
        this.z += this.velocityZ;
        this.tickCount++;
    }

    public boolean hasStatusEffect(RegistryEntry<StatusEffect> registryEntry) {
        StatusEffectInstance statusEffect = this.player.getStatusEffect(registryEntry);
        return statusEffect != null && statusEffect.getDuration() >= this.tickCount;
    }

    public PredictedCollision findCollision(int i) {
        Vec3d rotationVector = this.player.getRotationVector();
        for (int i2 = 0; i2 < i; i2++) {
            Vec3d vec3d = new Vec3d(this.x, this.y, this.z);
            simulateStep(rotationVector);
            Vec3d vec3d2 = new Vec3d(this.x, this.y, this.z);
            ClientPlayerEntity clientPlayerEntity = this.player;
            Optional optionalFindSupportingBlockPos = clientPlayerEntity.getWorld().findSupportingBlockPos(clientPlayerEntity, clientPlayerEntity.getDimensions(EntityPose.STANDING).getBoxAt(vec3d).stretch(vec3d2.subtract(vec3d)));
            if (optionalFindSupportingBlockPos.isPresent()) {
                return new PredictedCollision((BlockPos) optionalFindSupportingBlockPos.get(), i2);
            }
        }
        return null;
    }
}
