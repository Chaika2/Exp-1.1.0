package aethereal.event;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class BlockCollisionEvent2 implements Event {
    public final Block block;
    public final BlockPos blockPos;

    public Block getBlock() {
        return this.block;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public BlockCollisionEvent2(Block block, BlockPos blockPos) {
        this.block = block;
        this.blockPos = blockPos;
    }
}
