package aethereal.util;
import aethereal.type.Mc;
import aethereal.math.Rotation;
import aethereal.math.RotationMath;
import aethereal.type.ShapeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class RaycastUtil {
    public static final Mc mc = Mc.INSTANCE;

    public static BlockHitResult raycast(double d, Rotation class007Var, boolean z) {
        Entity cameraEntity = Mc.INSTANCE.getCameraEntity();
        if (cameraEntity == null) {
            return null;
        }
        Vec3d[] vec3dArrMethod002 = getRayEndpoints(cameraEntity, class007Var, d);
        return Mc.INSTANCE.getWorld().raycast(new RaycastContext(vec3dArrMethod002[0], vec3dArrMethod002[1], RaycastContext.ShapeType.OUTLINE, z ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, cameraEntity));
    }

    public static List<Vec3d> getEyes(boolean z, Vec3d vec3d) {
        ArrayList arrayList = new ArrayList(List.of(vec3d));
        if (z) {
            double lengthY = mc.getPlayer().getBoundingBox().getLengthY();
            arrayList.add(vec3d.subtract(0.0d, MathUtil.clamp(lengthY - 0.601d, 0.0d, 2.0d), 0.0d));
            arrayList.add(vec3d.subtract(0.0d, MathUtil.clamp(lengthY - 1.799d, -2.0d, 0.0d), 0.0d));
        }
        return arrayList;
    }

    public static BlockHitResult raycast(float f, double d, Rotation class007Var, boolean z) {
        return raycast(mc.getPlayer().getCameraPosVec(f), d, class007Var, z);
    }

    public static BlockHitResult raycast(Vec3d vec3d, double d, Rotation class007Var, boolean z) {
        return mc.getWorld().raycast(new RaycastContext(vec3d, vec3d.add(RotationMath.INSTANCE.rotationToVector(class007Var)).multiply(d), RaycastContext.ShapeType.OUTLINE, z ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, mc.getPlayer()));
    }

    public static BlockHitResult raycast(Vec3d vec3d, Vec3d vec3d2, RaycastContext.ShapeType shapeType) {
        return raycast(vec3d, vec3d2, shapeType, (Entity) mc.getPlayer());
    }

    public static BlockHitResult raycast(Vec3d vec3d, Vec3d vec3d2, RaycastContext.ShapeType shapeType, RaycastContext.FluidHandling fluidHandling, Entity entity) {
        return mc.getWorld().raycast(new RaycastContext(vec3d, vec3d2, shapeType, fluidHandling, entity));
    }

    public static EntityHitResult raytraceEntity(double d, Rotation class007Var, Predicate<Entity> predicate) {
        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity == null) {
            return null;
        }
        Vec3d cameraPosVec = cameraEntity.getCameraPosVec(1.0f);
        Vec3d vec3dRotationToVector = RotationMath.INSTANCE.rotationToVector(class007Var);
        return ProjectileUtil.raycast(cameraEntity, cameraPosVec, cameraPosVec.add(vec3dRotationToVector.x * d, vec3dRotationToVector.y * d, vec3dRotationToVector.z * d), cameraEntity.getBoundingBox().stretch(vec3dRotationToVector.multiply(d)).expand(1.0d, 1.0d, 1.0d), entity -> {
            return !entity.isSpectator() && predicate.test(entity);
        }, d * d);
    }

    public static BlockHitResult raycast(Vec3d vec3d, Vec3d vec3d2, RaycastContext.ShapeType shapeType, Entity entity) {
        return mc.getWorld().raycast(new RaycastContext(vec3d, vec3d2, shapeType, RaycastContext.FluidHandling.NONE, entity));
    }

    public static boolean isBlockVisible(BlockPos blockPos, Vec3d vec3d) {
        BlockHitResult blockHitResultRaycast = mc.getWorld().raycast(new RaycastContext(vec3d, Vec3d.ofCenter(blockPos), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.getPlayer()));
        return blockHitResultRaycast != null && blockHitResultRaycast.getBlockPos().equals(blockPos);
    }

    public static boolean rayTrace(double d, Box box) {
        return rayTrace(RotationMath.INSTANCE.rotationToVector(RotationManager.INSTANCE.getCurrentRotation()), d, box);
    }

    public static boolean rayTrace(Vec3d vec3d, double d, Box box) {
        Vec3d eyePos = Mc.INSTANCE.getPlayer().getEyePos();
        return box.contains(eyePos) || box.raycast(eyePos, eyePos.add(vec3d.multiply(d))).isPresent();
    }

    public static boolean canHitEntity(double d, Rotation class007Var, Entity entity, boolean z) {
        Entity cameraEntity = Mc.INSTANCE.getCameraEntity();
        if (cameraEntity == null || entity == null || !entity.isAlive()) {
            return false;
        }
        Vec3d cameraPosVec = cameraEntity.getCameraPosVec(1.0f);
        return isEntityUnobstructed(cameraPosVec, cameraPosVec.add(RotationMath.INSTANCE.rotationToVector(class007Var).multiply(d)), cameraEntity, entity, z);
    }

    public static boolean canHitEntity(Vec3d vec3d, double d, Entity entity, boolean z) {
        Entity cameraEntity = Mc.INSTANCE.getCameraEntity();
        if (cameraEntity == null || entity == null || !entity.isAlive()) {
            return false;
        }
        Vec3d cameraPosVec = cameraEntity.getCameraPosVec(1.0f);
        return isEntityUnobstructed(cameraPosVec, cameraPosVec.add(vec3d.multiply(d)), cameraEntity, entity, z);
    }

    public static boolean rayTrace(HitPointResolver class884Var, LivingEntity livingEntity, Vec3d vec3d, Vec3d vec3d2, double d, Box box, boolean z) {
        return class884Var.hasValidPoint(livingEntity, (float) d, z) && rayTrace(vec3d, vec3d2, d, box);
    }

    public static boolean rayTrace(Vec3d vec3d, Vec3d vec3d2, double d, Box box) {
        return box.contains(vec3d) || box.raycast(vec3d, vec3d.add(vec3d2.multiply(d))).isPresent();
    }

    public static boolean isEntityUnobstructed(Vec3d vec3d, Vec3d vec3d2, Entity entity, Entity entity2, boolean z) {
        Vec3d vec3d3;
        BlockHitResult blockHitResultRaycast;
        if (entity == null || entity2 == null || !entity2.isAlive()) {
            return false;
        }
        ClientWorld world = Mc.INSTANCE.getWorld();
        Vec3d vec3dSubtract = vec3d2.subtract(vec3d);
        vec3dSubtract.lengthSquared();
        Box boxExpand = entity2.getBoundingBox().expand(entity2.getTargetingMargin());
        if (boxExpand.contains(vec3d)) {
            vec3d3 = vec3d;
        } else {
            Optional optionalRaycast = boxExpand.raycast(vec3d, vec3d2);
            if (optionalRaycast.isEmpty()) {
                return false;
            }
            vec3d3 = (Vec3d) optionalRaycast.get();
        }
        double dSquaredDistanceTo = vec3d.squaredDistanceTo(vec3d3);
        for (Entity entity3 : world.getOtherEntities(entity, entity.getBoundingBox().stretch(vec3dSubtract).expand(1.0d, 1.0d, 1.0d), entity4 -> {
            return (entity4 == entity || entity4 == entity2 || entity4.isSpectator() || !entity4.isAlive() || entity4.getRootVehicle() == entity2.getRootVehicle()) ? false : true;
        })) {
            Box boxExpand2 = entity3.getBoundingBox().expand(entity3.getTargetingMargin());
            if (!boxExpand2.expand(0.001d).intersects(boxExpand.expand(0.001d))) {
                Optional optionalRaycast2 = boxExpand2.raycast(vec3d, vec3d2);
                if (!optionalRaycast2.isEmpty() && vec3d.squaredDistanceTo((Vec3d) optionalRaycast2.get()) + 1.0E-6d < dSquaredDistanceTo) {
                    return false;
                }
            }
        }
        return z || (blockHitResultRaycast = world.raycast(new RaycastContext(vec3d, vec3d3, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity))) == null || blockHitResultRaycast.getType() == HitResult.Type.MISS || dSquaredDistanceTo <= vec3d.squaredDistanceTo(blockHitResultRaycast.getPos()) + 1.0E-6d;
    }

    public static BlockHitResult raytraceBlock(double d, Rotation class007Var, BlockPos blockPos, BlockState blockState) {
        Entity cameraEntity = Mc.INSTANCE.getCameraEntity();
        if (cameraEntity == null) {
            return null;
        }
        Vec3d[] vec3dArrMethod002 = getRayEndpoints(cameraEntity, class007Var, d);
        return Mc.INSTANCE.getWorld().raycastBlock(vec3dArrMethod002[0], vec3dArrMethod002[1], blockPos, blockState.getOutlineShape(Mc.INSTANCE.getWorld(), blockPos, ShapeContext.of(Mc.INSTANCE.getPlayer())), blockState);
    }

    public static Vec3d[] getRayEndpoints(Entity entity, Rotation class007Var, double d) {
        Vec3d eyePos = entity.getEyePos();
        return new Vec3d[]{eyePos, eyePos.add(RotationMath.INSTANCE.rotationToVector(class007Var).multiply(d))};
    }

    public RaycastUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
