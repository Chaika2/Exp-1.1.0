package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.config.RotationConfig;
import aethereal.event.PlayerTickEvent;
import aethereal.Lang;
import aethereal.math.RotationMath;
import aethereal.math.Stopwatch;
import aethereal.type.AutoFarmPumpkinMode;
import aethereal.type.Mc;
import aethereal.type.SettingUnit;
import aethereal.type.SpeedRotationMode;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.ExpandableSetting;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.setting.NumberSetting;
import aethereal.util.BlockUtil;
import aethereal.util.PlayerActionUtil;
import aethereal.util.RaycastUtil;
import aethereal.util.RotationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

@Aliases(aliases = {"Auto Farm", "Autofarm", "Auto Plant", "Planter", "Фермер", "Ферма", "Посадка"})
public class AutoFarmModule extends Module {
    public final Mc mc;
    public final BooleanSetting plantWheat;
    public final BooleanSetting plantPumpkin;
    public final ModeSetting<AutoFarmPumpkinMode> pumpkinMode;
    public final BooleanSetting plantCarrot;
    public final BooleanSetting plantBeetroot;
    public final BooleanSetting plantNetherWart;
    public final BooleanSetting plantChorus;
    public final NumberSetting chorusSpacing;
    public final NumberSetting radius;
    public final NumberSetting delay;
    public final BooleanSetting throughWalls;
    public final ExpandableSetting rotate;
    public final NumberSetting yawSpeed;
    public final NumberSetting pitchSpeed;

    private final Stopwatch timer;
    private int savedSlot;
    private int lastUsedSlot;

    private static final Predicate<BlockState> ON_FARMLAND = state -> {
        return state.isOf(Blocks.FARMLAND);
    };
    private static final Predicate<BlockState> ON_SOUL_SAND = state -> {
        return state.isOf(Blocks.SOUL_SAND);
    };
    private static final Predicate<BlockState> ON_END_STONE = state -> {
        return state.isOf(Blocks.END_STONE);
    };
    private static final Predicate<BlockState> ON_SOIL = state -> {
        return state.isOf(Blocks.DIRT) || state.isOf(Blocks.GRASS_BLOCK) || state.isOf(Blocks.COARSE_DIRT) || state.isOf(Blocks.PODZOL) || state.isOf(Blocks.MYCELIUM) || state.isOf(Blocks.ROOTED_DIRT) || state.isOf(Blocks.DIRT_PATH);
    };

    public AutoFarmModule() {
        super(ModuleTab.PLAYER, "Auto Farm");
        this.mc = Mc.INSTANCE;
        this.plantWheat = new BooleanSetting(Lang.AUTOFARM_WHEAT).setValue(true);
        this.plantPumpkin = new BooleanSetting(Lang.AUTOFARM_PUMPKIN).setValue(true);
        this.pumpkinMode = new ModeSetting(Lang.AUTOFARM_PUMPKIN_MODE).values(AutoFarmPumpkinMode.class).visible(() -> {
            return Boolean.valueOf(this.plantPumpkin.isValue());
        });
        this.plantCarrot = new BooleanSetting(Lang.AUTOFARM_CARROT);
        this.plantBeetroot = new BooleanSetting(Lang.AUTOFARM_BEETROOT);
        this.plantNetherWart = new BooleanSetting(Lang.AUTOFARM_NETHER_WART);
        this.plantChorus = new BooleanSetting(Lang.AUTOFARM_CHORUS);
        this.chorusSpacing = new NumberSetting(Lang.AUTOFARM_CHORUS_SPACING, Lang.AUTOFARM_CHORUS_SPACING_DESC).currentValue(2.0f).range(1.0f, 4.0f).step(1.0f).valueUnit(SettingUnit.BLOCKS).visible(() -> {
            return Boolean.valueOf(this.plantChorus.isValue());
        });
        this.radius = new NumberSetting(Lang.AUTOFARM_RADIUS, Lang.AUTOFARM_RADIUS_DESC).currentValue(4.0f).range(1.0f, 6.0f).step(1.0f).valueUnit(SettingUnit.BLOCKS);
        this.delay = new NumberSetting(Lang.AUTOFARM_DELAY, Lang.AUTOFARM_DELAY_DESC).currentValue(1.0f).range(0.0f, 20.0f).step(1.0f).unit(SettingUnit.TICKS);
        this.throughWalls = new BooleanSetting(Lang.AUTOFARM_THROUGH_WALLS, Lang.AUTOFARM_THROUGH_WALLS_DESC);
        this.rotate = new ExpandableSetting(Lang.AUTOFARM_ROTATE, Lang.AUTOFARM_ROTATE_DESC).setValue(true);
        this.yawSpeed = new NumberSetting(Lang.AUTOFARM_YAW_SPEED, Lang.AUTOFARM_YAW_SPEED_DESC).currentValue(180.0f).range(1.0f, 180.0f).step(1.0f).unit(SettingUnit.DEGREES);
        this.pitchSpeed = new NumberSetting(Lang.AUTOFARM_PITCH_SPEED, Lang.AUTOFARM_PITCH_SPEED_DESC).currentValue(180.0f).range(1.0f, 180.0f).step(1.0f).unit(SettingUnit.DEGREES);
        this.rotate.setSubSettings(List.of(this.yawSpeed, this.pitchSpeed));
        this.timer = new Stopwatch();
        this.savedSlot = -1;
        this.lastUsedSlot = -1;
        addSettings(this.plantWheat, this.plantPumpkin, this.pumpkinMode, this.plantCarrot, this.plantBeetroot, this.plantNetherWart, this.plantChorus, this.chorusSpacing, this.radius, this.delay, this.throughWalls, this.rotate);
        register(PlayerTickEvent.class, this::onPlayerTick);
    }

    public void onPlayerTick(PlayerTickEvent class130Var) {
        if (!isState() || !this.mc.isWorldLoaded() || !class130Var.isPre()) {
            return;
        }
        if (this.mc.getCurrentScreen() != null) {
            return;
        }
        ClientPlayerEntity player = this.mc.getPlayer();
        ClientPlayerInteractionManager interactionManager = this.mc.getInteractionManager();
        if (player == null || interactionManager == null || player.isSpectator() || interactionManager.getCurrentGameMode() == GameMode.SPECTATOR) {
            return;
        }
        if (this.timer.getElapsedTime(TimeUnit.MILLISECONDS) < (long) (this.delay.currentValue() * 50.0f)) {
            return;
        }
        List<PlantJob> jobs = buildJobs(player);
        if (jobs.isEmpty()) {
            return;
        }
        PlantTarget target = findTarget(player, jobs);
        if (target == null) {
            return;
        }
        if (this.rotate.isValue()) {
            RotationManager.INSTANCE.scheduleRotation(RotationMath.INSTANCE.fromVec3d(target.click().subtract(player.getEyePos())).random(0.5f), new RotationConfig(new SpeedRotationMode(this.yawSpeed.currentValue(), this.pitchSpeed.currentValue()), false, true, false), 0, this, 1);
            BlockHitResult aimed = RaycastUtil.raytraceBlock(5.0d, RotationManager.INSTANCE.getCurrentRotation(), target.pos(), this.mc.getWorld().getBlockState(target.pos()));
            if (aimed == null || aimed.getType() != HitResult.Type.BLOCK || !aimed.getBlockPos().equals(target.pos()) || aimed.getSide() != Direction.UP) {
                // Still turning to the spot: keep the rotation task alive and retry next tick.
                return;
            }
        }
        plant(player, interactionManager, target);
    }

    /**
     * Builds the list of planting jobs that can actually run right now: every enabled crop
     * that has a matching item sitting in the hotbar. Jobs are ordered by priority, so a
     * farmland seed always wins over a pumpkin block trying to claim the same spot.
     */
    public List<PlantJob> buildJobs(ClientPlayerEntity clientPlayerEntity) {
        List<PlantJob> list = new ArrayList<>();
        addJob(list, clientPlayerEntity, this.plantWheat, stack -> {
            return stack.isOf(Items.WHEAT_SEEDS);
        }, ON_FARMLAND);
        if (this.plantPumpkin.isValue()) {
            if (this.pumpkinMode.isSelected(AutoFarmPumpkinMode.SEEDS)) {
                addJob(list, clientPlayerEntity, null, stack -> {
                    return stack.isOf(Items.PUMPKIN_SEEDS);
                }, ON_FARMLAND);
            } else {
                addJob(list, clientPlayerEntity, null, stack -> {
                    return stack.isOf(Items.PUMPKIN);
                }, ON_SOIL);
            }
        }
        addJob(list, clientPlayerEntity, this.plantCarrot, stack -> {
            return stack.isOf(Items.CARROT);
        }, ON_FARMLAND);
        addJob(list, clientPlayerEntity, this.plantBeetroot, stack -> {
            return stack.isOf(Items.BEETROOT_SEEDS);
        }, ON_FARMLAND);
        addJob(list, clientPlayerEntity, this.plantNetherWart, stack -> {
            return stack.isOf(Items.NETHER_WART);
        }, ON_SOUL_SAND);
        addJob(list, clientPlayerEntity, this.plantChorus, stack -> {
            return stack.isOf(Items.CHORUS_FLOWER) || stack.isOf(Items.POPPED_CHORUS_FRUIT);
        }, ON_END_STONE, (int) this.chorusSpacing.currentValue());
        return list;
    }

    public void addJob(List<PlantJob> list, ClientPlayerEntity clientPlayerEntity, BooleanSetting booleanSetting, Predicate<ItemStack> itemFilter, Predicate<BlockState> blockFilter) {
        addJob(list, clientPlayerEntity, booleanSetting, itemFilter, blockFilter, 1);
    }

    public void addJob(List<PlantJob> list, ClientPlayerEntity clientPlayerEntity, BooleanSetting booleanSetting, Predicate<ItemStack> itemFilter, Predicate<BlockState> blockFilter, int spacing) {
        if (booleanSetting != null && !booleanSetting.isValue()) {
            return;
        }
        PlayerActionUtil.INSTANCE.hotbarSlotsStream(slot -> {
            return itemFilter.test(clientPlayerEntity.getInventory().getStack(slot));
        }).ifPresent(slot -> {
            list.add(new PlantJob(slot, blockFilter, spacing));
        });
    }

    /**
     * Finds the closest reachable spot where any of the jobs can plant: a valid support block
     * (farmland / soul sand / end stone / soil) with air directly above it.
     */
    public PlantTarget findTarget(ClientPlayerEntity clientPlayerEntity, List<PlantJob> list) {
        ClientWorld world = (ClientWorld) this.mc.getWorld();
        Vec3d eyePos = clientPlayerEntity.getEyePos();
        double range = clientPlayerEntity.getBlockInteractionRange();
        double bestDistance = range * range;
        boolean ignoreWalls = this.throughWalls.isValue();
        int radius = (int) this.radius.currentValue();
        PlantTarget best = null;
        for (BlockPos blockPos : BlockUtil.getCube(clientPlayerEntity.getBlockPos(), radius, radius, true)) {
            BlockState state = world.getBlockState(blockPos);
            if (state.isAir() || !world.getBlockState(blockPos.up()).isAir()) {
                continue;
            }
            Vec3d click = new Vec3d(blockPos.getX() + 0.5d, blockPos.getY() + 1.0d, blockPos.getZ() + 0.5d);
            double distance = eyePos.squaredDistanceTo(click);
            if (distance >= bestDistance) {
                continue;
            }
            if (!ignoreWalls && !RaycastUtil.isBlockVisible(blockPos, eyePos)) {
                continue;
            }
            for (PlantJob job : list) {
                if (job.blockFilter().test(state)) {
                    if (job.spacing() > 1 && isChorusSpacingBlocked(world, blockPos, job.spacing())) {
                        continue;
                    }
                    best = new PlantTarget(blockPos, click, job.slot());
                    bestDistance = distance;
                    break;
                }
            }
        }
        return best;
    }

    /**
     * Chorus bushes must be planted with a gap between them (default: every 2 blocks, so each
     * bush gets its own 2x2 cell) — otherwise the growing branches collide and flowers pop off.
     * Checks the whole horizontal neighbourhood around the spot across heights, since branches
     * reach sideways at different levels while the bushes grow taller.
     */
    public boolean isChorusSpacingBlocked(ClientWorld clientWorld, BlockPos blockPos, int spacing) {
        int reach = spacing - 1;
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                for (int dy = -8; dy <= 8; dy++) {
                    BlockState state = clientWorld.getBlockState(blockPos.add(dx, dy, dz));
                    if (state.isOf(Blocks.CHORUS_PLANT) || state.isOf(Blocks.CHORUS_FLOWER)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void plant(ClientPlayerEntity clientPlayerEntity, ClientPlayerInteractionManager interactionManager, PlantTarget plantTarget) {
        if (clientPlayerEntity.getInventory().selectedSlot != plantTarget.slot()) {
            if (this.savedSlot == -1) {
                this.savedSlot = clientPlayerEntity.getInventory().selectedSlot;
            }
            clientPlayerEntity.getInventory().selectedSlot = plantTarget.slot();
            clientPlayerEntity.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(plantTarget.slot()));
        }
        this.lastUsedSlot = plantTarget.slot();
        interactionManager.interactBlock(clientPlayerEntity, Hand.MAIN_HAND, BlockUtil.createHitResult(plantTarget.pos(), Direction.UP));
        clientPlayerEntity.swingHand(Hand.MAIN_HAND);
        this.timer.reset();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        RotationManager.INSTANCE.cancelTasksFor(this);
        ClientPlayerEntity player = this.mc.getPlayer();
        if (player != null && this.savedSlot != -1 && this.lastUsedSlot != -1 && player.getInventory().selectedSlot == this.lastUsedSlot) {
            player.getInventory().selectedSlot = this.savedSlot;
            player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.savedSlot));
        }
        this.savedSlot = -1;
        this.lastUsedSlot = -1;
        this.timer.reset();
    }

    public record PlantJob(int slot, Predicate<BlockState> blockFilter, int spacing) {
    }

    public record PlantTarget(BlockPos pos, Vec3d click, int slot) {
    }
}
