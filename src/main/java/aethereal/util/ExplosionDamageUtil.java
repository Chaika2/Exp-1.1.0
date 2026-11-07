package aethereal.util;
import aethereal.internal.DifficultyIndexMap;
import aethereal.type.ShapeType;

import java.util.Arrays;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionImpl;

public class ExplosionDamageUtil {
    public static float getExplosionDamageFromEntity(LivingEntity livingEntity, Entity entity) {
        if (entity instanceof EndCrystalEntity) {
            return getDamageFromExplosion(livingEntity, entity.getPos(), 6.0f, 12.0f, 144.0f, null, null, null, null);
        }
        if (entity instanceof TntEntity) {
            return getDamageFromExplosion(livingEntity, entity.getPos().add(0.0d, 0.0625d, 0.0d), 4.0f, 8.0f, 64.0f, null, null, null, null);
        }
        if (entity instanceof TntMinecartEntity) {
            float f = 4.0f + (5.0f * 1.5f);
            float f2 = f * 2.0f;
            return getDamageFromExplosion(livingEntity, entity.getPos(), f, f2, f2 * f2, null, null, null, null);
        }
        if (!(entity instanceof CreeperEntity)) {
            return 0.0f;
        }
        CreeperEntity creeperEntity = (CreeperEntity) entity;
        float f3 = creeperEntity.explosionRadius * (creeperEntity.isCharged() ? 2.0f : 1.0f);
        float f4 = f3 * 2.0f;
        return getDamageFromExplosion(livingEntity, entity.getPos(), f3, f4, f4 * f4, null, null, null, null);
    }

    public static float getDamageFromExplosion(LivingEntity livingEntity, Vec3d vec3d, float f, float f2, float f3, BlockPos[] blockPosArr, BlockPos blockPos, Float f4, Box box) {
        World world = livingEntity.getWorld();
        if (livingEntity.squaredDistanceTo(vec3d) > f3 || world.getDifficulty() == Difficulty.PEACEFUL) {
            return 0.0f;
        }
        double exposureToExplosion = ((double) (blockPosArr != null || f4 != null || blockPos != null || box != null ? getExposureToExplosion(livingEntity, vec3d, blockPosArr, blockPos, f4, box) : ExplosionImpl.calculateReceivedDamage(vec3d, livingEntity))) * (1.0d - (Math.sqrt(livingEntity.squaredDistanceTo(vec3d)) / ((double) f2)));
        double d = ((((exposureToExplosion * exposureToExplosion) + exposureToExplosion) / 2.0d) * 7.0d * ((double) f2)) + 1.0d;
        if (d == 0.0d) {
            return 0.0f;
        }
        return getEffectiveDamage(livingEntity, world.getDamageSources().explosion((Explosion) null), (float) d, false);
    }

    public static float getExposureToExplosion(LivingEntity livingEntity, Vec3d vec3d, BlockPos[] blockPosArr, BlockPos blockPos, Float f, Box box) {
        Box boundingBox = box != null ? box : livingEntity.getBoundingBox();
        ShapeContext shapeContextOf = ShapeContext.of(livingEntity);
        double d = 1.0d / (((boundingBox.maxX - boundingBox.minX) * 2.0d) + 1.0d);
        double d2 = 1.0d / (((boundingBox.maxY - boundingBox.minY) * 2.0d) + 1.0d);
        double d3 = 1.0d / (((boundingBox.maxZ - boundingBox.minZ) * 2.0d) + 1.0d);
        double dFloor = (1.0d - (Math.floor(1.0d / d) * d)) / 2.0d;
        double dFloor2 = (1.0d - (Math.floor(1.0d / d3) * d3)) / 2.0d;
        if (d < 0.0d || d2 < 0.0d || d3 < 0.0d) {
            return 0.0f;
        }
        int i = 0;
        int i2 = 0;
        World world = livingEntity.getWorld();
        double d4 = 0.0d;
        while (true) {
            double d5 = d4;
            if (d5 > 1.0d) {
                return i / i2;
            }
            double d6 = 0.0d;
            while (true) {
                double d7 = d6;
                if (d7 <= 1.0d) {
                    double d8 = 0.0d;
                    while (true) {
                        double d9 = d8;
                        if (d9 <= 1.0d) {
                            if (isRayExposed(world.raycast(new RaycastContext(new Vec3d(MathHelper.lerp(d5, boundingBox.minX, boundingBox.maxX) + dFloor, MathHelper.lerp(d7, boundingBox.minY, boundingBox.maxY), MathHelper.lerp(d9, boundingBox.minZ, boundingBox.maxZ) + dFloor2), vec3d, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, shapeContextOf)), blockPosArr, blockPos, f, world)) {
                                i++;
                            }
                            i2++;
                            d8 = d9 + d3;
                        } else {
                            break;
                        }
                    }
                    d6 = d7 + d2;
                } else {
                    break;
                }
            }
            d4 = d5 + d;
        }
    }

    public static boolean isRayExposed(HitResult hitResult, BlockPos[] blockPosArr, BlockPos blockPos, Float f, World world) {
        if (hitResult.getType() == HitResult.Type.MISS) {
            return true;
        }
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        BlockPos blockPos2 = new BlockPos((int) hitResult.getPos().x, (int) hitResult.getPos().y, (int) hitResult.getPos().z);
        if (blockPosArr != null && Arrays.asList(blockPosArr).contains(blockPos2)) {
            return true;
        }
        if (blockPos == null || blockPos2.equals(blockPos)) {
            return f != null && world.getBlockState(blockPos2).getBlock().getBlastResistance() > f.floatValue();
        }
        return true;
    }

    public static float getEffectiveDamage(LivingEntity livingEntity, DamageSource damageSource, float f, boolean z) {
        World world = livingEntity.getWorld();
        if (livingEntity.isAlwaysInvulnerableTo(damageSource) || livingEntity.isDead()) {
            return 0.0f;
        }
        float fMin = f;
        if (livingEntity instanceof PlayerEntity) {
            PlayerAbilities abilities = ((PlayerEntity) livingEntity).getAbilities();
            DamageSource damageSourceOutOfWorld = world.getDamageSources().outOfWorld();
            if (abilities.invulnerable && !damageSource.getType().deathMessageType().equals(damageSourceOutOfWorld.getType().deathMessageType())) {
                return 0.0f;
            }
            if (damageSource.isScaledWithDifficulty()) {
                switch (DifficultyIndexMap.difficultyOrdinals[world.getDifficulty().ordinal()]) {
                    case 1:
                        fMin = 0.0f;
                        break;
                    case 2:
                        fMin = Math.min((fMin / 2.0f) + 1.0f, fMin);
                        break;
                    case 3:
                        fMin *= 1.5f;
                        break;
                }
            }
        }
        if (fMin == 0.0f) {
            return 0.0f;
        }
        if (damageSource == world.getDamageSources().onFire() && livingEntity.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
            return 0.0f;
        }
        if (z || !livingEntity.blockedByShield(damageSource)) {
            return livingEntity.modifyAppliedDamage(damageSource, livingEntity.applyArmorToDamage(damageSource, fMin));
        }
        return 0.0f;
    }
}
