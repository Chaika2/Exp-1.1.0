package aethereal.event;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockCollisionEvent implements Event {
    public BlockState state;

    public BlockPos pos;

    public BlockState getState() {
        return this.state;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public void setState(BlockState blockState) {
        this.state = blockState;
    }

    public void setPos(BlockPos blockPos) {
        this.pos = blockPos;
    }

    public BlockCollisionEvent(BlockState blockState, BlockPos blockPos) {
        this.state = blockState;
        this.pos = blockPos;
    }
}
