package aethereal.util;
import aethereal.type.Mc;
import aethereal.math.Rotation;
import aethereal.math.RotationMath;
import aethereal.type.ShapeType;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class HitPointResolver {
    public final Mc wrapper = Mc.INSTANCE;
    public final Random random = new SecureRandom();
    public Vec3d offset = Vec3d.ZERO;

    public Pair<Vec3d, Box> computeVector(LivingEntity livingEntity, float f, Rotation class007Var, Vec3d vec3d, boolean z) {
        Pair<List<Vec3d>, Box> pairGenerateCandidatePoints = generateCandidatePoints(livingEntity, f, z);
        randomizeOffset(vec3d);
        Vec3d vec3dMethod003 = findBestPoint((List) pairGenerateCandidatePoints.getLeft(), class007Var);
        return new Pair<>((vec3dMethod003 == null ? livingEntity.getEyePos() : vec3dMethod003).add(this.offset), (Box) pairGenerateCandidatePoints.getRight());
    }

    public Pair<Vec3d, Box> computeVector(Box box, float f, Rotation class007Var, Vec3d vec3d, boolean z) {
        Pair<List<Vec3d>, Box> pairGenerateCandidatePointsForBox = generateCandidatePointsForBox(box, f, z);
        Vec3d vec3dMethod003 = findBestPoint((List) pairGenerateCandidatePointsForBox.getLeft(), class007Var);
        randomizeOffset(vec3d);
        return new Pair<>((vec3dMethod003 == null ? getClosestVec(this.wrapper.getPlayer().getEyePos(), box) : vec3dMethod003).add(this.offset), (Box) pairGenerateCandidatePointsForBox.getRight());
    }

    public Pair<List<Vec3d>, Box> generateCandidatePointsForBox(Box box, float f, boolean z) {
        double lengthY = box.getLengthY() / 10.0d;
        Vec3d eyePos = this.wrapper.getPlayer().getEyePos();
        ArrayList arrayList = new ArrayList();
        double d = box.minY;
        while (true) {
            double d2 = d;
            if (d2 > box.maxY) {
                break;
            }
            Vec3d vec3d = new Vec3d(box.getCenter().x, d2, box.getCenter().z);
            if (isReachable(eyePos, vec3d, f, z)) {
                arrayList.add(vec3d);
            }
            d = d2 + lengthY;
        }
        if (arrayList.isEmpty()) {
            Vec3d closestVec = getClosestVec(eyePos, box);
            if (isReachable(eyePos, closestVec, f, z)) {
                arrayList.add(closestVec);
            }
        }
        return new Pair<>(arrayList, box);
    }

    public Pair<List<Vec3d>, Box> generateCandidatePoints(LivingEntity livingEntity, float f, boolean z) {
        Box boundingBox = livingEntity.getBoundingBox();
        double lengthY = boundingBox.getLengthY() / 10.0d;
        Vec3d eyePos = this.wrapper.getPlayer().getEyePos();
        ArrayList arrayList = new ArrayList();
        double d = boundingBox.minY;
        while (true) {
            double d2 = d;
            if (d2 > boundingBox.maxY) {
                break;
            }
            Vec3d vec3d = new Vec3d(boundingBox.getCenter().x, d2, boundingBox.getCenter().z);
            if (isReachable(eyePos, vec3d, f, z)) {
                arrayList.add(vec3d);
            }
            d = d2 + lengthY;
        }
        if (arrayList.isEmpty()) {
            Vec3d closestVec = getClosestVec(eyePos, boundingBox);
            if (isReachable(eyePos, closestVec, f, z)) {
                arrayList.add(closestVec);
            }
        }
        return new Pair<>(arrayList, boundingBox);
    }

    public Vec3d getClosestVec(Vec3d vec3d, Box box) {
        return new Vec3d(MathUtil.clamp(vec3d.x, box.minX, box.maxX), MathUtil.clamp(vec3d.y, box.minY, box.maxY), MathUtil.clamp(vec3d.z, box.minZ, box.maxZ));
    }

    public boolean hasValidPoint(LivingEntity livingEntity, float f, boolean z) {
        if (livingEntity == null) {
            return false;
        }
        Box boundingBox = livingEntity.getBoundingBox();
        double lengthY = boundingBox.getLengthY() / 10.0d;
        Vec3d eyePos = this.wrapper.getPlayer().getEyePos();
        double d = boundingBox.minY;
        while (true) {
            double d2 = d;
            if (d2 >= boundingBox.maxY) {
                return isReachable(eyePos, getClosestVec(eyePos, boundingBox), f, z);
            }
            if (isReachable(eyePos, new Vec3d(boundingBox.getCenter().x, d2, boundingBox.getCenter().z), f, z)) {
                return true;
            }
            d = d2 + lengthY;
        }
    }

    public boolean isReachable(Vec3d vec3d, Vec3d vec3d2, float f, boolean z) {
        return vec3d.distanceTo(vec3d2) <= ((double) f) && (z || !RaycastUtil.raycast(vec3d, vec3d2, RaycastContext.ShapeType.COLLIDER).getType().equals(HitResult.Type.BLOCK));
    }

    public Vec3d findBestPoint(List<Vec3d> list, Rotation class007Var) {
        return list.stream().min(Comparator.comparing(vec3d -> {
            return Double.valueOf(computeRotationCost(this.wrapper.getPlayer().getEyePos(), vec3d, class007Var));
        })).orElse(null);
    }

    public double computeRotationCost(Vec3d vec3d, Vec3d vec3d2, Rotation class007Var) {
        Rotation class007VarCalculateRotationDifference = RotationMath.INSTANCE.calculateRotationDifference(RotationMath.INSTANCE.fromVec3d(vec3d2.subtract(vec3d)), class007Var);
        return Math.hypot(class007VarCalculateRotationDifference.getYaw(), class007VarCalculateRotationDifference.getPitch());
    }

    public void randomizeOffset(Vec3d vec3d) {
        this.offset = this.offset.add(this.random.nextGaussian(), this.random.nextGaussian(), this.random.nextGaussian()).multiply(vec3d);
    }

    public Mc getWrapper() {
        return this.wrapper;
    }

    public Random getRandom() {
        return this.random;
    }

    public Vec3d getOffset() {
        return this.offset;
    }
}
