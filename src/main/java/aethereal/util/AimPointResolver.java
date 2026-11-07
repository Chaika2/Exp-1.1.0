package aethereal.util;
import aethereal.type.Mc;
import aethereal.math.Rotation;
import aethereal.math.RotationMath;

import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class AimPointResolver {
    final Mc mc = Mc.INSTANCE;
    Random random = new SecureRandom();
    Vec3d jitterOffset = Vec3d.ZERO;

    public Pair<Vec3d, Box> computeVector(LivingEntity livingEntity, float f, Rotation class007Var, Vec3d vec3d) {
        Pair<List<Vec3d>, Box> pairGenerateCandidatePoints = generateCandidatePoints(livingEntity, f);
        Vec3d vec3dMethod004 = findClosestPoint((List) pairGenerateCandidatePoints.getLeft(), class007Var);
        applyJitter(vec3d);
        return new Pair<>((vec3dMethod004 == null ? livingEntity.getEyePos() : vec3dMethod004).add(this.jitterOffset), (Box) pairGenerateCandidatePoints.getRight());
    }

    public Pair<List<Vec3d>, Box> generateCandidatePoints(LivingEntity livingEntity, float f) {
        Box boundingBox = livingEntity.getBoundingBox();
        double lengthY = boundingBox.getLengthY() / 10.0d;
        return new Pair<>(Stream.iterate(Double.valueOf(boundingBox.minY), d -> {
            return d.doubleValue() <= boundingBox.maxY;
        }, d2 -> {
            return Double.valueOf(d2.doubleValue() + lengthY);
        }).map(d3 -> {
            return new Vec3d(boundingBox.getCenter().x, d3.doubleValue(), boundingBox.getCenter().z);
        }).filter(vec3d -> {
            return isWithinRange(((ClientPlayerEntity) Objects.requireNonNull(this.mc.getPlayer())).getEyePos(), vec3d, f);
        }).toList(), boundingBox);
    }

    public boolean hasValidPoint(LivingEntity livingEntity, float f) {
        Box boundingBox = livingEntity.getBoundingBox();
        double lengthY = boundingBox.getLengthY() / 10.0d;
        return Stream.iterate(Double.valueOf(boundingBox.minY), d -> {
            return d.doubleValue() < boundingBox.maxY;
        }, d2 -> {
            return Double.valueOf(d2.doubleValue() + lengthY);
        }).map(d3 -> {
            return new Vec3d(boundingBox.getCenter().x, d3.doubleValue(), boundingBox.getCenter().z);
        }).anyMatch(vec3d -> {
            return isWithinRange(((ClientPlayerEntity) Objects.requireNonNull(this.mc.getPlayer())).getEyePos(), vec3d, f);
        });
    }

    public boolean isWithinRange(Vec3d vec3d, Vec3d vec3d2, float f) {
        return vec3d.distanceTo(vec3d2) <= ((double) f);
    }

    public Vec3d findClosestPoint(List<Vec3d> list, Rotation class007Var) {
        return list.stream().min(Comparator.comparing(vec3d -> {
            return Double.valueOf(computeRotationDistance(((ClientPlayerEntity) Objects.requireNonNull(this.mc.getPlayer())).getEyePos(), vec3d, class007Var));
        })).orElse(null);
    }

    public boolean rayTrace(Vec3d vec3d, double d, Box box) {
        Vec3d eyePos = this.mc.getPlayer().getEyePos();
        return box.contains(eyePos) || box.raycast(eyePos, eyePos.add(vec3d.multiply(d))).isPresent();
    }

    public double computeRotationDistance(Vec3d vec3d, Vec3d vec3d2, Rotation class007Var) {
        Rotation class007VarCalculateRotationDifference = RotationMath.INSTANCE.calculateRotationDifference(RotationMath.INSTANCE.fromVec3d(vec3d2.subtract(vec3d)), class007Var);
        return Math.hypot(class007VarCalculateRotationDifference.getYaw(), class007VarCalculateRotationDifference.getPitch());
    }

    public void applyJitter(Vec3d vec3d) {
        this.jitterOffset = this.jitterOffset.add(this.random.nextGaussian(), this.random.nextGaussian(), this.random.nextGaussian()).multiply(vec3d);
    }
}
