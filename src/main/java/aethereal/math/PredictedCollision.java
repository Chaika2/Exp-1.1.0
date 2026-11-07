package aethereal.math;

import net.minecraft.util.math.BlockPos;

public class PredictedCollision {
    public final BlockPos pos;
    public final int tick;

    public PredictedCollision() {
        this.pos = null;
        this.tick = 0;
    }

    public PredictedCollision(BlockPos blockPos, int i) {
        this.pos = blockPos;
        this.tick = i;
    }
}
