package aethereal.model;
import aethereal.util.BlockRegionIterator;
import aethereal.type.Mc;

import java.util.Iterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

public class BlockRegion implements Iterable<BlockPos> {
    public final BlockPos min;
    public final BlockPos max;
    public final int volume;
    public static final BlockRegion EMPTY = new BlockRegion(BlockPos.ORIGIN, BlockPos.ORIGIN);

    public static BlockRegion quadAround(BlockPos blockPos, int i, int i2) {
        return new BlockRegion(blockPos.add(-i, -i2, -i), blockPos.add(i, i2, i));
    }

    public static BlockRegion from(BlockPos blockPos) {
        return new BlockRegion(blockPos, blockPos);
    }

    public static BlockRegion from(Chunk chunk) {
        BlockPos startPos = chunk.getPos().getStartPos();
        return new BlockRegion(new BlockPos(startPos.getX(), chunk.getBottomY(), startPos.getZ()), new BlockPos(startPos.getX() + 15, chunk.getTopYInclusive(), startPos.getZ() + 15));
    }

    public static BlockRegion fromChunkPos(int i, int i2) {
        return new BlockRegion(new BlockPos(i << 4, Mc.INSTANCE.getWorld().getBottomY(), i2 << 4), new BlockPos((i << 4) | 15, Mc.INSTANCE.getWorld().getTopYInclusive(), (i2 << 4) | 15));
    }

    public static Box getBox(BlockRegion class306Var) {
        return new Box(0.0d, 0.0d, 0.0d, ((double) (class306Var.max.getX() - class306Var.min.getX())) + 1.0d, ((double) (class306Var.max.getY() - class306Var.min.getY())) + 1.0d, ((double) (class306Var.max.getZ() - class306Var.min.getZ())) + 1.0d);
    }

    public BlockRegion(BlockPos blockPos, BlockPos blockPos2) {
        BlockPos blockPos3 = new BlockPos(Math.min(blockPos.getX(), blockPos2.getX()), Math.min(blockPos.getY(), blockPos2.getY()), Math.min(blockPos.getZ(), blockPos2.getZ()));
        BlockPos blockPos4 = new BlockPos(Math.max(blockPos.getX(), blockPos2.getX()), Math.max(blockPos.getY(), blockPos2.getY()), Math.max(blockPos.getZ(), blockPos2.getZ()));
        this.min = blockPos3;
        this.max = blockPos4;
        this.volume = (blockPos4.getX() - blockPos3.getX()) * (blockPos4.getY() - blockPos3.getY()) * (blockPos4.getZ() - blockPos3.getZ());
    }

    public int[] xBounds() {
        return new int[]{this.min.getX(), this.max.getX()};
    }

    public int[] yBounds() {
        return new int[]{this.min.getY(), this.max.getY()};
    }

    public int[] zBounds() {
        return new int[]{this.min.getZ(), this.max.getZ()};
    }

    public boolean isEmpty() {
        return this.volume == 0;
    }

    public boolean contains(BlockRegion class306Var) {
        int[] iArrMethod001 = xBounds();
        int[] iArrMethod003 = yBounds();
        int[] iArrMethod004 = zBounds();
        int[] iArr = {class306Var.min.getX(), class306Var.max.getX()};
        int[] iArr2 = {class306Var.min.getY(), class306Var.max.getY()};
        int[] iArr3 = {class306Var.min.getZ(), class306Var.max.getZ()};
        return iArr[0] >= iArrMethod001[0] && iArr[1] <= iArrMethod001[1] && iArr2[0] >= iArrMethod003[0] && iArr2[1] <= iArrMethod003[1] && iArr3[0] >= iArrMethod004[0] && iArr3[1] <= iArrMethod004[1];
    }

    public boolean contains(BlockPos blockPos) {
        return blockPos.getX() >= this.min.getX() && blockPos.getX() <= this.max.getX() && blockPos.getY() >= this.min.getY() && blockPos.getY() <= this.max.getY() && blockPos.getZ() >= this.min.getZ() && blockPos.getZ() <= this.max.getZ();
    }

    public boolean intersects(BlockRegion class306Var) {
        return overlaps(new Vec3i(class306Var.min.getX(), class306Var.min.getY(), class306Var.min.getZ()), new Vec3i(class306Var.max.getX(), class306Var.max.getY(), class306Var.max.getZ()));
    }

    public boolean overlaps(Vec3i vec3i, Vec3i vec3i2) {
        return this.max.getX() > vec3i.getX() && this.min.getX() < vec3i2.getX() && this.max.getY() > vec3i.getY() && this.min.getY() < vec3i2.getY() && this.max.getZ() > vec3i.getZ() && this.min.getZ() < vec3i2.getZ();
    }

    public Vec3d getBottomFaceCenter() {
        return new Vec3d(((double) ((this.min.getX() + this.max.getX()) + 1)) / 2.0d, this.min.getY(), ((double) ((this.min.getZ() + this.max.getZ()) + 1)) / 2.0d);
    }

    public Box getBoundingBox() {
        return new Box(this.min.getX(), this.min.getY(), this.min.getZ(), ((double) this.max.getX()) + 1.0d, ((double) this.max.getY()) + 1.0d, ((double) this.max.getZ()) + 1.0d);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BlockRegion)) {
            return false;
        }
        BlockRegion class306Var = (BlockRegion) obj;
        return this.min.equals(class306Var.min) && this.max.equals(class306Var.max);
    }

    public int hashCode() {
        return (31 * this.min.hashCode()) + this.max.hashCode();
    }

    public BlockRegion intersection(BlockRegion class306Var) {
        return new BlockRegion(new BlockPos(Math.max(this.min.getX(), class306Var.min.getX()), Math.max(this.min.getY(), class306Var.min.getY()), Math.max(this.min.getZ(), class306Var.min.getZ())), new BlockPos(Math.min(this.max.getX(), class306Var.max.getX()), Math.min(this.max.getY(), class306Var.max.getY()), Math.min(this.max.getZ(), class306Var.max.getZ())));
    }

    public BlockRegion union(BlockRegion class306Var) {
        return new BlockRegion(new BlockPos(Math.min(this.min.getX(), class306Var.min.getX()), Math.min(this.min.getY(), class306Var.min.getY()), Math.min(this.min.getZ(), class306Var.min.getZ())), new BlockPos(Math.max(this.max.getX(), class306Var.max.getX()), Math.max(this.max.getY(), class306Var.max.getY()), Math.max(this.max.getZ(), class306Var.max.getZ())));
    }

    public String toString() {
        return "[" + this.min.getX() + "," + this.min.getY() + "," + this.min.getZ() + "] -> [" + this.max.getX() + "," + this.max.getY() + "," + this.max.getZ() + "]";
    }

    @Override
    @NotNull
    public Iterator<BlockPos> iterator() {
        return new BlockRegionIterator(this);
    }
}
