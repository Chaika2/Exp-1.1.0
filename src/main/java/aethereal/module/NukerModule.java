package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.event.BlockBreakEvent;
import aethereal.util.BlockUtil;
import aethereal.ui.setting.BooleanSetting;
import aethereal.render.ColorStack;
import aethereal.type.CombatPauseManager;
import aethereal.ui.setting.ExpandableSetting;
import aethereal.Expensive;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.ModuleTab;
import aethereal.type.NukerDiggingMode;
import aethereal.type.NukerWorkMode;
import aethereal.ui.setting.NumberSetting;
import aethereal.model.OrePriorityTable;
import aethereal.event.PlayerTickEvent;
import aethereal.util.RaycastUtil;
import aethereal.config.RotationConfig;
import aethereal.util.RotationManager;
import aethereal.math.RotationMath;
import aethereal.type.SettingUnit;
import aethereal.render.ShapeRenderer;
import aethereal.type.SpeedRotationMode;
import aethereal.util.StencilBufferUtil;
import aethereal.event.WorldRenderEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;

@Aliases(aliases = {"Nuker", "Auto Mine"})
public class NukerModule extends Module {
    public final NumberSetting radiusXZ;
    public final NumberSetting radiusY;
    public final ModeSetting<NukerWorkMode> workMode;
    public final ModeSetting<NukerDiggingMode> diggingMode;
    public final ExpandableSetting rotate;
    public final NumberSetting yawSpeed;
    public final NumberSetting pitchSpeed;
    public final BooleanSetting throughWalls;
    public final BooleanSetting mineNeighbor;
    public final BooleanSetting mineDown;
    public final BooleanSetting instant;
    public final Mc mc;
    public final OrePriorityTable orePriorityTable;
    public final Predicate<BlockState> orePredicate;
    public BlockPos targetBlock;

    public NukerModule() {
        super(ModuleTab.PLAYER, "Nuker");
        this.radiusXZ = new NumberSetting(Lang.NUKER_RADIUS_XZ).currentValue(3.0f).range(1.0f, 6.0f).step(1.0f).valueUnit(SettingUnit.BLOCKS);
        this.radiusY = new NumberSetting(Lang.NUKER_RADIUS_Y).currentValue(3.0f).range(1.0f, 6.0f).step(1.0f).valueUnit(SettingUnit.BLOCKS);
        this.workMode = new ModeSetting(Lang.NUKER_WORK_MODE, Lang.NUKER_WORK_MODE_DESC).values(NukerWorkMode.class);
        this.diggingMode = new ModeSetting(Lang.NUKER_DIGGING_MODE).values(NukerDiggingMode.class);
        this.rotate = new ExpandableSetting(Lang.NUKER_ROTATE);
        this.yawSpeed = new NumberSetting(Lang.NUKER_YAW_SPEED, Lang.NUKER_YAW_SPEED_DESC).currentValue(180.0f).range(1.0f, 180.0f).step(1.0f).unit(SettingUnit.DEGREES);
        this.pitchSpeed = new NumberSetting(Lang.NUKER_PITCH_SPEED, Lang.NUKER_PITCH_SPEED_DESC).currentValue(180.0f).range(1.0f, 180.0f).step(1.0f).unit(SettingUnit.DEGREES);
        this.throughWalls = new BooleanSetting(Lang.NUKER_THROUGH_WALLS, Lang.NUKER_THROUGH_WALLS_DESC);
        this.mineNeighbor = new BooleanSetting(Lang.NUKER_MINE_NEIGHBOR, Lang.NUKER_MINE_NEIGHBOR_DESC);
        this.mineDown = new BooleanSetting(Lang.NUKER_MINE_DOWN);
        this.instant = new BooleanSetting(Lang.NUKER_INSTANT, Lang.NUKER_INSTANT_DESC);
        this.mc = Mc.INSTANCE;
        this.orePriorityTable = new OrePriorityTable();
        OrePriorityTable class532Var = this.orePriorityTable;
        Objects.requireNonNull(class532Var);
        this.orePredicate = class532Var::isOre;
        this.rotate.setSubSettings(List.of(this.yawSpeed, this.pitchSpeed));
        addSettings(this.radiusXZ, this.radiusY, this.workMode, this.diggingMode, this.rotate, this.throughWalls, this.mineNeighbor, this.mineDown, this.instant);
        register(WorldRenderEvent.class, this::onWorldRender);
        register(PlayerTickEvent.class, this::onPlayerTick);
        register(BlockBreakEvent.class, class241Var -> {
            BlockPos blockPosMethod006;
            if (this.instant.isValue() && isState() && this.mc.isWorldLoaded() && class241Var.isPost()) {
                if (CombatPauseManager.INSTANCE.shouldPauseBreaking()) {
                    this.mc.getInteractionManager().cancelBlockBreaking();
                    this.targetBlock = null;
                    return;
                }
                BlockPos blockPos = null;
                List<BlockPos> list = BlockUtil.getCube(this.mc.getPlayer().getBlockPos(), this.radiusXZ.currentValue(), this.radiusY.currentValue(), this.mineDown.isValue()).stream().filter(blockPos2 -> {
                    return canMineBlock(blockPos2, this.mc.getPlayer()) && (this.throughWalls.isValue() || RaycastUtil.isBlockVisible(blockPos2, this.mc.getPlayer().getEyePos()));
                }).sorted(getBlockComparator(this.mc.getPlayer().getBlockPos(), this.mc.getPlayer())).toList();
                if (!list.isEmpty()) {
                    blockPos = (BlockPos) list.getFirst();
                    if (this.mineNeighbor.isValue() && (blockPosMethod006 = findSofterNeighbor(blockPos, this.mc.getPlayer())) != null) {
                        blockPos = blockPosMethod006;
                    }
                }
                if (blockPos == null || this.mc.getWorld().getBlockState(blockPos).calcBlockBreakingDelta(this.mc.getPlayer(), this.mc.getWorld(), blockPos) < 1.0f) {
                    return;
                }
                this.mc.getInteractionManager().updateBlockBreakingProgress(blockPos, Direction.UP);
                this.mc.getPlayer().swingHand(Hand.MAIN_HAND);
            }
        });
    }

    public void onWorldRender(WorldRenderEvent class016Var) {
        if (isState() && Mc.INSTANCE.isWorldLoaded()) {
            MatrixStack matrixStack = class016Var.matrixStack();
            ColorStack class115VarColorStack = Expensive.INSTANCE.drawEngine().colorStack();
            int iComputeColor = class115VarColorStack.computeColor(30, 160, 82, 45);
            int iComputeColor2 = class115VarColorStack.computeColor(StencilBufferUtil.STENCIL_MASK, 160, 82, 45);
            int iComputeColor3 = class115VarColorStack.computeColor(100, 160, 82, 45);
            if (this.targetBlock != null) {
                Box box = new Box(this.targetBlock);
                ShapeRenderer.INSTANCE.addBox(class016Var.matrixStack().peek().getPositionMatrix(), box, iComputeColor);
                ShapeRenderer.INSTANCE.addOutlineWireframe(matrixStack.peek().getPositionMatrix(), box, iComputeColor2, iComputeColor3, 3.0f);
            }
            Pair<BlockPos, BlockPos> mineRegion = BlockUtil.getMineRegion();
            BlockPos blockPos = (BlockPos) mineRegion.getLeft();
            BlockPos blockPos2 = (BlockPos) mineRegion.getRight();
            if (!this.workMode.isSelected(NukerWorkMode.ONLY_MINE) || BlockUtil.distanceToRegion(this.mc.getPlayer().getBlockPos(), blockPos, blockPos2) >= 30.0d) {
                return;
            }
            ShapeRenderer.INSTANCE.addOutline(class016Var.matrixStack().peek().getPositionMatrix(), new Box(new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), new Vec3d(blockPos2.getX() + 1, blockPos2.getY() + 1, blockPos2.getZ() + 1)), class115VarColorStack.computeColor(StencilBufferUtil.STENCIL_MASK, StencilBufferUtil.STENCIL_MASK, 0, 0), 1.0f);
        }
    }

    public void onPlayerTick(PlayerTickEvent class130Var) {
        if (isState() && this.mc.isWorldLoaded() && class130Var.isPre()) {
            ClientPlayerEntity player = this.mc.getPlayer();
            if (CombatPauseManager.INSTANCE.shouldPauseBreaking()) {
                this.mc.getInteractionManager().cancelBlockBreaking();
                this.targetBlock = null;
                return;
            }
            this.targetBlock = findTargetBlock();
            if (this.targetBlock != null) {
                swapTool(this.targetBlock);
                BlockHitResult blockHitResultRaytraceBlock = this.rotate.isValue() ? RaycastUtil.raytraceBlock(5.0d, RotationManager.INSTANCE.getCurrentRotation(), this.targetBlock, this.mc.getWorld().getBlockState(this.targetBlock)) : new BlockHitResult(this.targetBlock.toCenterPos(), Direction.UP, this.targetBlock, false);
                if (!this.rotate.isValue() || (blockHitResultRaytraceBlock != null && blockHitResultRaytraceBlock.getType() == HitResult.Type.BLOCK && blockHitResultRaytraceBlock.getBlockPos().equals(this.targetBlock))) {
                    breakBlock(player, blockHitResultRaytraceBlock);
                }
            }
        }
    }

    public BlockPos findTargetBlock() {
        ClientPlayerEntity player = this.mc.getPlayer();
        BlockPos blockPos = player.getBlockPos();
        if (this.targetBlock != null) {
            if (this.mc.getWorld().getBlockState(this.targetBlock) == null || !canMineBlock(this.targetBlock, player)) {
                this.mc.getInteractionManager().cancelBlockBreaking();
                return null;
            }
            if (this.rotate.isValue()) {
                RotationManager.INSTANCE.scheduleRotation(RotationMath.INSTANCE.fromVec3d(this.targetBlock.toCenterPos().subtract(player.getEyePos())).random(0.5f), new RotationConfig(new SpeedRotationMode(this.yawSpeed.currentValue(), this.pitchSpeed.currentValue()), false, true, false), 0, this, 1);
            }
            return this.targetBlock;
        }
        List<BlockPos> list = BlockUtil.getCube(blockPos, this.radiusXZ.currentValue(), this.radiusY.currentValue(), this.mineDown.isValue()).stream().filter(blockPos2 -> {
            return canMineBlock(blockPos2, player) && (this.throughWalls.isValue() || RaycastUtil.isBlockVisible(blockPos2, player.getEyePos()));
        }).sorted(getBlockComparator(blockPos, player)).toList();
        if (list.isEmpty()) {
            return null;
        }
        BlockPos blockPos3 = (BlockPos) list.getFirst();
        if (this.rotate.isValue()) {
            RotationManager.INSTANCE.scheduleRotation(RotationMath.INSTANCE.fromVec3d(blockPos3.toCenterPos().subtract(player.getEyePos())), new RotationConfig(new SpeedRotationMode(this.yawSpeed.currentValue(), this.pitchSpeed.currentValue()), false, true, false), 0, this, 1);
        }
        if (!this.mineNeighbor.isValue()) {
            return blockPos3;
        }
        BlockPos blockPosMethod006 = findSofterNeighbor(blockPos3, player);
        if (blockPosMethod006 != null) {
            blockPos3 = blockPosMethod006;
        }
        return blockPos3;
    }

    public BlockPos findSofterNeighbor(BlockPos blockPos, ClientPlayerEntity clientPlayerEntity) {
        float hardness = this.mc.getWorld().getBlockState(blockPos).getHardness(this.mc.getWorld(), blockPos);
        Vec3d eyePos = clientPlayerEntity.getEyePos();
        for (Direction direction : Direction.values()) {
            BlockPos blockPosOffset = blockPos.offset(direction);
            if ((!this.workMode.isSelected(NukerWorkMode.ONLY_MINE) || BlockUtil.isWithInMine(blockPosOffset)) && isBreakableBlock(blockPosOffset) && ((this.throughWalls.isValue() || RaycastUtil.isBlockVisible(blockPosOffset, eyePos)) && this.mc.getWorld().getBlockState(blockPosOffset).getHardness(this.mc.getWorld(), blockPosOffset) < hardness)) {
                return blockPosOffset;
            }
        }
        return null;
    }

    @NotNull
    public Comparator<BlockPos> getBlockComparator(BlockPos blockPos, ClientPlayerEntity clientPlayerEntity) {
        return Comparator.<BlockPos>comparingInt(blockPos2 -> {
            if (this.diggingMode.isSelected(NukerDiggingMode.ORE_PRIORITY)) {
                return this.orePriorityTable.getPriority(this.mc.getWorld().getBlockState(blockPos2));
            }
            return 0;
        }).thenComparingInt(blockPos3 -> {
            int y = blockPos3.getY() - blockPos.getY();
            if (y >= 1) {
                return 1;
            }
            return y == 0 ? 0 : 2;
        }).thenComparingDouble(blockPos4 -> {
            return clientPlayerEntity.squaredDistanceTo(blockPos4.toCenterPos());
        });
    }

    public void breakBlock(ClientPlayerEntity clientPlayerEntity, BlockHitResult blockHitResult) {
        if (clientPlayerEntity.isCreative() && this.mc.getInteractionManager().attackBlock(blockHitResult.getBlockPos(), blockHitResult.getSide())) {
            clientPlayerEntity.swingHand(Hand.MAIN_HAND);
        } else if (this.mc.getInteractionManager().updateBlockBreakingProgress(blockHitResult.getBlockPos(), blockHitResult.getSide())) {
            clientPlayerEntity.swingHand(Hand.MAIN_HAND);
            this.mc.getMinecraft().particleManager.addBlockBreakingParticles(this.targetBlock, blockHitResult.getSide());
        }
    }

    public boolean canMineBlock(BlockPos blockPos, ClientPlayerEntity clientPlayerEntity) {
        GameMode currentGameMode;
        if (this.workMode.isSelected(NukerWorkMode.ONLY_MINE) && !BlockUtil.isWithInMine(blockPos)) {
            return false;
        }
        Vec3d eyePos = clientPlayerEntity.getEyePos();
        double dClamp = clamp(eyePos.x, blockPos.getX(), blockPos.getX() + 1);
        double dClamp2 = clamp(eyePos.y, blockPos.getY(), blockPos.getY() + 1);
        double dClamp3 = clamp(eyePos.z, blockPos.getZ(), blockPos.getZ() + 1);
        double d = eyePos.x - dClamp;
        double d2 = eyePos.y - dClamp2;
        double d3 = eyePos.z - dClamp3;
        double d4 = (d * d) + (d2 * d2) + (d3 * d3);
        float blockInteractionRange = (float) clientPlayerEntity.getBlockInteractionRange();
        if (d4 > blockInteractionRange * blockInteractionRange || (currentGameMode = this.mc.getInteractionManager().getCurrentGameMode()) == GameMode.ADVENTURE || currentGameMode == GameMode.SPECTATOR) {
            return false;
        }
        return isBreakableBlock(blockPos);
    }

    public void swapTool(BlockPos blockPos) {
        AutoToolModule class576Var = (AutoToolModule) Expensive.INSTANCE.moduleRepository().get(AutoToolModule.class);
        if (class576Var == null || !class576Var.isState()) {
            return;
        }
        class576Var.swapForBlock(this.mc.getWorld().getBlockState(blockPos));
    }

    public double clamp(double d, double d2, double d3) {
        return d < d2 ? d2 : Math.min(d, d3);
    }

    public boolean isBreakableBlock(BlockPos blockPos) {
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        BlockState blockState = ((ClientWorld) Objects.requireNonNull(this.mc.getWorld())).getBlockState(blockPos);
        return (!this.diggingMode.isSelected(NukerDiggingMode.ONLY_ORE) || this.orePredicate.test(blockState)) && isNotAir(blockState) && !blockState.isTransparent() && !((!player.isCreative() && blockState.getHardness(this.mc.getWorld(), blockPos) < 0.0f) || blockState.getBlock() == Blocks.WATER || blockState.getBlock() == Blocks.LAVA || blockState.getBlock() == Blocks.BEDROCK || blockState.getBlock() == Blocks.BARRIER);
    }

    public boolean isNotAir(BlockState blockState) {
        return (blockState.isAir() || blockState.getBlock().equals(Blocks.CAVE_AIR) || blockState.getBlock().equals(Blocks.VOID_AIR)) ? false : true;
    }
}
