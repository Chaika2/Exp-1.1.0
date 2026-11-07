package aethereal.model;

import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

public class MissHitResult extends HitResult {
    public static final MissHitResult INSTANCE = new MissHitResult();

    public MissHitResult() {
        super(new Vec3d(0.0d, 0.0d, 0.0d));
    }

    public HitResult.Type getType() {
        return HitResult.Type.MISS;
    }
}
