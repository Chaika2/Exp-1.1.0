package aethereal.model;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockUpdateEntry {
    public final BlockPos pos;
    public final BlockState state;
    public final int light;

    public BlockUpdateEntry() {
        this.pos = null;
        this.state = null;
        this.light = 0;
    }

    public BlockUpdateEntry(BlockPos blockPos, BlockState blockState, int i) {
        this.pos = blockPos;
        this.state = blockState;
        this.light = i;
    }

    public BlockPos pos() {
        return this.pos;
    }

    public BlockState state() {
        return this.state;
    }

    public int light() {
        return this.light;
    }
}
