package aethereal.util;
import aethereal.model.BlockRegion;
import aethereal.type.Mc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class BlockUtil {
    public static final Block[] fallDamageBlockingBlocks = {Blocks.WATER, Blocks.COBWEB, Blocks.POWDER_SNOW, Blocks.HAY_BLOCK, Blocks.SLIME_BLOCK};
    static BlockPos mineRegionMin = new BlockPos(-71, 77, 9);
    static BlockPos mineRegionMax = new BlockPos(-53, 86, 27);

    public static boolean checkBlockIntersection(Box box, Predicate<Block> predicate) {
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        ClientWorld world = Mc.INSTANCE.getWorld();
        if (player == null || world == null) {
            return false;
        }
        for (BlockPos blockPos : getBoundingRegion(box)) {
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState != null && predicate.test(blockState.getBlock()) && box.intersects(new Box(blockPos))) {
                return true;
            }
        }
        return false;
    }

    public static BlockHitResult createHitResult(BlockPos blockPos, Direction direction) {
        return new BlockHitResult(Vec3d.ofCenter(blockPos).add(Vec3d.of(direction.getVector()).multiply(0.5d)), direction, blockPos, false);
    }

    public static List<BlockPos> getAllBlockPositions(BlockPos blockPos, BlockPos blockPos2) {
        ArrayList arrayList = new ArrayList();
        int iMin = Math.min(blockPos.getX(), blockPos2.getX());
        int iMax = Math.max(blockPos.getX(), blockPos2.getX());
        int iMin2 = Math.min(blockPos.getY(), blockPos2.getY());
        int iMax2 = Math.max(blockPos.getY(), blockPos2.getY());
        int iMin3 = Math.min(blockPos.getZ(), blockPos2.getZ());
        int iMax3 = Math.max(blockPos.getZ(), blockPos2.getZ());
        for (int i = iMin; i <= iMax; i++) {
            for (int i2 = iMin2; i2 <= iMax2; i2++) {
                for (int i3 = iMin3; i3 <= iMax3; i3++) {
                    arrayList.add(new BlockPos(i, i2, i3));
                }
            }
        }
        return arrayList;
    }

    public static List<BlockPos> getCube(BlockPos blockPos, float f) {
        return getCube(blockPos, f, f, true);
    }

    public static List<BlockPos> getCube(BlockPos blockPos, float f, float f2) {
        return getCube(blockPos, f, f2, true);
    }

    public static List<BlockPos> getCube(BlockPos blockPos, float f, float f2, boolean z) {
        ArrayList arrayList = new ArrayList();
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z2 = blockPos.getZ();
        int i = z ? y - ((int) f2) : y;
        for (int i2 = x - ((int) f); i2 <= x + f; i2++) {
            for (int i3 = z2 - ((int) f); i3 <= z2 + f; i3++) {
                for (int i4 = i; i4 < y + f2; i4++) {
                    arrayList.add(new BlockPos(i2, i4, i3));
                }
            }
        }
        return arrayList;
    }

    public static List<BlockPos> searchBlocksInCuboid(Vec3d vec3d, float f) {
        ArrayList arrayList = new ArrayList();
        int iFloor = (int) Math.floor(vec3d.x - ((double) f));
        int iFloor2 = (int) Math.floor(vec3d.y - ((double) f));
        int iFloor3 = (int) Math.floor(vec3d.z - ((double) f));
        int iCeil = (int) Math.ceil(vec3d.x + ((double) f));
        int iCeil2 = (int) Math.ceil(vec3d.y + ((double) f));
        int iCeil3 = (int) Math.ceil(vec3d.z + ((double) f));
        for (int i = iFloor; i <= iCeil; i++) {
            for (int i2 = iFloor2; i2 <= iCeil2; i2++) {
                for (int i3 = iFloor3; i3 <= iCeil3; i3++) {
                    arrayList.add(new BlockPos(i, i2, i3));
                }
            }
        }
        return arrayList;
    }

    public static List<BlockPos> searchBlocksInCuboid(Vec3d vec3d, float f, BiPredicate<BlockPos, BlockState> biPredicate) {
        ArrayList arrayList = new ArrayList();
        for (BlockPos blockPos : searchBlocksInCuboid(vec3d, f)) {
            if (biPredicate.test(blockPos, Mc.INSTANCE.getWorld().getBlockState(blockPos))) {
                arrayList.add(blockPos);
            }
        }
        return arrayList;
    }

    public static boolean isFallDamageBlocking(World world, BlockPos blockPos) {
        if (blockPos == null) {
            return false;
        }
        return Arrays.asList(fallDamageBlockingBlocks).contains(world.getBlockState(blockPos).getBlock());
    }

    public static boolean isJumpBlockedByCeiling(ClientPlayerEntity clientPlayerEntity, World world) {
        return !clientPlayerEntity.isTouchingWater() && !clientPlayerEntity.isClimbing() && !clientPlayerEntity.getAbilities().flying && clientPlayerEntity.isOnGround() && isBlockAboveHead(clientPlayerEntity, world) && (!canMoveUp(clientPlayerEntity, world));
    }

    public static Vec3d getNearestPoint(Vec3d vec3d, Box box) {
        return new Vec3d(Math.max(box.minX, Math.min(vec3d.x, box.maxX)), Math.max(box.minY, Math.min(vec3d.y, box.maxY)), Math.max(box.minZ, Math.min(vec3d.z, box.maxZ)));
    }

    public static boolean hasBlockAbove(ClientPlayerEntity clientPlayerEntity) {
        return !IteratorUtil.toList(clientPlayerEntity.getWorld().getCollisions(clientPlayerEntity, clientPlayerEntity.getBoundingBox().offset(0.0d, 0.1d, 0.0d)).iterator()).isEmpty();
    }

    public static double distanceToRegion(BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3) {
        int iMin = Math.min(blockPos2.getX(), blockPos3.getX());
        int iMax = Math.max(blockPos2.getX(), blockPos3.getX());
        int iMin2 = Math.min(blockPos2.getY(), blockPos3.getY());
        int iMax2 = Math.max(blockPos2.getY(), blockPos3.getY());
        int iMin3 = Math.min(blockPos2.getZ(), blockPos3.getZ());
        int iMax3 = Math.max(blockPos2.getZ(), blockPos3.getZ());
        int iClamp = MathHelper.clamp(blockPos.getX(), iMin, iMax);
        int iClamp2 = MathHelper.clamp(blockPos.getY(), iMin2, iMax2);
        int iClamp3 = MathHelper.clamp(blockPos.getZ(), iMin3, iMax3);
        double x = blockPos.getX() - iClamp;
        double y = blockPos.getY() - iClamp2;
        double z = blockPos.getZ() - iClamp3;
        return Math.sqrt((x * x) + (y * y) + (z * z));
    }

    public static boolean isWithInMine(BlockPos blockPos) {
        return isWithinRegion(blockPos, mineRegionMin, mineRegionMax);
    }

    public static boolean isWithinRegion(BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3) {
        return (blockPos.getX() >= Math.min(blockPos2.getX(), blockPos3.getX()) && blockPos.getX() <= Math.max(blockPos2.getX(), blockPos3.getX())) && (blockPos.getY() >= Math.min(blockPos2.getY(), blockPos3.getY()) && blockPos.getY() <= Math.max(blockPos2.getY(), blockPos3.getY())) && (blockPos.getZ() >= Math.min(blockPos2.getZ(), blockPos3.getZ()) && blockPos.getZ() <= Math.max(blockPos2.getZ(), blockPos3.getZ()));
    }

    public static Pair<BlockPos, BlockPos> getMineRegion() {
        return new Pair<>(mineRegionMin, mineRegionMax);
    }

    public static boolean isBlockAboveHead(ClientPlayerEntity clientPlayerEntity, World world) {
        return world.getCollisions(clientPlayerEntity, getHeadClearanceBox(clientPlayerEntity, !clientPlayerEntity.isOnGround() ? 1.899999976158142d : 2.5d)).iterator().hasNext();
    }

    public static boolean canMoveUp(ClientPlayerEntity clientPlayerEntity, World world) {
        return !world.getCollisions(clientPlayerEntity, getHeadClearanceBox(clientPlayerEntity, !(clientPlayerEntity.getPose() == EntityPose.CROUCHING && Mc.INSTANCE.getGameOptions().jumpKey.isPressed()) ? ((double) clientPlayerEntity.getHeight()) + 0.2d : ((double) clientPlayerEntity.getHeight()) + 0.05d)).iterator().hasNext();
    }

    public static Box getHeadClearanceBox(ClientPlayerEntity clientPlayerEntity, double d) {
        return new Box(clientPlayerEntity.getX() - 0.3d, clientPlayerEntity.getY() + ((double) clientPlayerEntity.getHeight()), clientPlayerEntity.getZ() - 0.3d, clientPlayerEntity.getX() + 0.3d, clientPlayerEntity.getY() + d, clientPlayerEntity.getZ() + 0.3d);
    }

    public static BlockRegion getBoundingRegion(Box box) {
        return new BlockRegion(BlockPos.ofFloored(box.minX, box.minY, box.minZ), new BlockPos(MathHelper.ceil(box.maxX), MathHelper.ceil(box.maxY), MathHelper.ceil(box.maxZ)));
    }

    public BlockUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
