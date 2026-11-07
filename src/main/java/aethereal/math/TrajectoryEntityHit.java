package aethereal.math;
import aethereal.type.TrajectoryCalculator;

import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

public class TrajectoryEntityHit extends HitResult {
    public TrajectoryEntityHit(TrajectoryCalculator class261Var, Vec3d vec3d) {
        super(vec3d);
    }

    public HitResult.Type getType() {
        return HitResult.Type.ENTITY;
    }
}
