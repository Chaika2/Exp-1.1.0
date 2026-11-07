package aethereal.event;
import aethereal.type.BlockBreakStage;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class BlockBreakEvent implements Event {
    BlockPos pos;

    Direction direction;

    BlockBreakStage stage;

    public boolean isPost() {
        return this.stage == BlockBreakStage.POST;
    }

    public boolean isPre() {
        return this.stage == BlockBreakStage.PRE;
    }

    public BlockBreakEvent(BlockPos blockPos, Direction direction, BlockBreakStage class242Var) {
        this.pos = blockPos;
        this.direction = direction;
        this.stage = class242Var;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public BlockBreakStage getStage() {
        return this.stage;
    }
}
