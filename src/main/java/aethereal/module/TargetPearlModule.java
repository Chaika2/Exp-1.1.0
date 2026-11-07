package aethereal.module;

import aethereal.Expensive;
import aethereal.Lang;
import aethereal.annotation.Aliases;
import aethereal.event.ModuleStateEvent;
import aethereal.event.MovementInputEvent;
import aethereal.event.PlayerTickEvent;
import aethereal.math.DirectionalInput;
import aethereal.math.Rotation;
import aethereal.math.Stopwatch;
import aethereal.model.ScopedSlot;
import aethereal.model.SlotSearchResult2;
import aethereal.type.InventoryScope;
import aethereal.type.Mc;
import aethereal.type.SettingUnit;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.NumberSetting;
import aethereal.util.AuraCooldownGuard;
import aethereal.util.GrimDelayHandler;
import aethereal.util.PlayerActionUtil;
import aethereal.util.RotationManager;
import aethereal.util.SilentSlotManager;
import aethereal.util.SwapUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2f;

@Aliases(aliases = {"TargetPerl", "TargetPearl"})
public class TargetPearlModule extends Module {
    private static final long TARGET_MEMORY_DURATION = 10000L;
    private static final int PEARL_SCAN_INTERVAL = 70;
    private static final int THROW_COOLDOWN = 2000;
    private static final int MAX_SIM_TICKS = 160;
    private static final double MAX_DISTANCE_FROM_TARGET = 1.5;
    private static final double MAX_ACCEPTABLE_ERROR = 2.0;

    public final BooleanSetting onlyTarget;
    public final NumberSetting distance;
    public final BooleanSetting legit;
    public final BooleanSetting specialServer;
    public final BooleanSetting holdRotation;
    public final Stopwatch timerUtil;
    public final Mc mc;

    private EnderPearlEntity targetPearl;
    private Vec3d cachedLanding;
    private long lastPearlScan;
    private long lastThrowTime;
    private long delay;
    private long rotationHoldUntil;
    private boolean isThrowing;
    private boolean isChangingItem;
    private int originalSlot = -1;
    public Vector2f server;
    private Entity rememberedTarget;
    private long rememberedTargetTime;
    private final List<DelayedAction> delayedActions = new ArrayList<>();

    private static final class DelayedAction {
        final long executeAt;
        final Runnable action;

        DelayedAction(long executeAt, Runnable action) {
            this.executeAt = executeAt;
            this.action = action;
        }
    }

    public TargetPearlModule() {
        super(ModuleTab.COMBAT, "Target Pearl");
        this.mc = Mc.INSTANCE;
        this.onlyTarget = new BooleanSetting(Lang.TARGETPEARL_ONLY_TARGET).setValue(false);
        this.distance = new NumberSetting(Lang.TARGETPEARL_DISTANCE).currentValue(10.0f).range(8.0f, 20.0f).step(1.0f).unit(SettingUnit.BLOCKS);
        this.legit = new BooleanSetting(Lang.TARGETPEARL_LEGIT).setValue(false);
        this.specialServer = new BooleanSetting(Lang.TARGETPEARL_SPECIAL_SERVER).setValue(false);
        this.holdRotation = new BooleanSetting(Lang.TARGETPEARL_HOLD_ROTATION).setValue(true);
        this.timerUtil = new Stopwatch();
        addSettings(this.onlyTarget, this.distance, this.legit, this.specialServer, this.holdRotation);

        register(PlayerTickEvent.class, e -> {
            if (!isState() || this.mc.getPlayer() == null || this.mc.getWorld() == null) {
                return;
            }
            if (!e.isPre()) {
                return;
            }
            runDelayedActions();

            if (this.mc.getPlayer().isGliding()) {
                return;
            }

            tryThrow();

            if (this.rotationHoldUntil > 0L && System.currentTimeMillis() >= this.rotationHoldUntil) {
                this.isThrowing = false;
                this.server = null;
                this.rotationHoldUntil = 0L;
            }
        });

        register(MovementInputEvent.class, e -> {
            if (!isState() || this.mc.getPlayer() == null || !check()) {
                return;
            }
            fixMovement(e, this.server.x);
        });

        register(ModuleStateEvent.class, e -> {
            if (e.module() == this && !e.moduleState()) {
                onDisable();
            }
        });
    }

    public boolean check() {
        return isState() && this.isThrowing && this.server != null;
    }

    private void delay(long milliseconds, Runnable action) {
        this.delayedActions.add(new DelayedAction(System.currentTimeMillis() + milliseconds, action));
    }

    private void runDelayedActions() {
        if (this.delayedActions.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        Iterator<DelayedAction> iterator = this.delayedActions.iterator();
        while (iterator.hasNext()) {
            DelayedAction delayed = iterator.next();
            if (delayed.executeAt <= now) {
                iterator.remove();
                delayed.action.run();
            }
        }
    }

    private void tryThrow() {
        ClientPlayerEntity player = this.mc.getPlayer();
        if (System.currentTimeMillis() - this.lastThrowTime < THROW_COOLDOWN) {
            return;
        }
        if (!canThrow()) {
            return;
        }
        if (!GrimDelayHandler.script.isFinished()) {
            return;
        }

        updateTargetPearl();
        if (this.cachedLanding == null) {
            return;
        }

        float[] rot = calculateYawPitch(this.cachedLanding);
        if (rot == null) {
            rot = findAnyWorkingRotation(this.cachedLanding);
            if (rot == null) {
                return;
            }
        }

        Vec3d predicted = simulateTrajectory(rot[0], rot[1]);
        if (predicted != null) {
            double distToTarget = this.cachedLanding.distanceTo(predicted);
            Vec3d eye = player.getEyePos();
            double distToCached = eye.distanceTo(this.cachedLanding);
            double heightDiff = this.cachedLanding.y - eye.y;
            boolean isHighTarget = heightDiff > 5.0;

            double maxAllowedError = distToCached > 60 ? MAX_DISTANCE_FROM_TARGET * 2.0 : MAX_DISTANCE_FROM_TARGET * 1.5;
            if (isHighTarget) {
                maxAllowedError = MAX_DISTANCE_FROM_TARGET * 3.0;
            }
            if (hasObstacleBetween(eye, this.cachedLanding)) {
                maxAllowedError = isHighTarget ? MAX_DISTANCE_FROM_TARGET * 4.0 : MAX_DISTANCE_FROM_TARGET * 2.5;
            }

            if (distToTarget > maxAllowedError) {
                rot = findAnyWorkingRotation(this.cachedLanding);
                if (rot == null) {
                    return;
                }
            }
        }

        this.isThrowing = true;
        this.server = new Vector2f(rot[0], rot[1]);
        sendPlayerRotationPacket(player, rot[0], rot[1], player.isOnGround());

        if (!player.getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL.getDefaultStack()) && getItemSlot(player, Items.ENDER_PEARL) != -1) {
            if (this.legit.isValue()) {
                holySwapClick(player);
            } else if (this.specialServer.isValue()) {
                handleItemChange(player, System.currentTimeMillis() - this.delay > 200L);

                int hbSlot = findItem(player, Items.ENDER_PEARL, true);
                int invSlot = findItem(player, Items.ENDER_PEARL, false);

                int slot = findAndThrowItem(player, hbSlot, invSlot);
                if (slot > 8) {
                    pickItem(player, slot);
                }
                useItem(player, Hand.MAIN_HAND);
            } else if (!throwPearl(player)) {
                this.isThrowing = false;
                this.server = null;
                this.rotationHoldUntil = 0L;
                return;
            }
            this.timerUtil.reset();
            this.lastThrowTime = System.currentTimeMillis();
        }

        if (this.legit.isValue() || this.holdRotation.isValue()) {
            this.rotationHoldUntil = System.currentTimeMillis() + 50L;
        } else {
            this.isThrowing = false;
            this.server = null;
            this.rotationHoldUntil = 0L;
        }
    }

    private boolean canThrow() {
        return !this.mc.getPlayer().getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL.getDefaultStack());
    }

    private void updateTargetPearl() {
        long now = System.currentTimeMillis();
        if (now - this.lastPearlScan < PEARL_SCAN_INTERVAL) {
            return;
        }

        this.lastPearlScan = now;
        this.cachedLanding = null;
        this.targetPearl = findBestPearl();

        if (this.targetPearl != null && this.targetPearl.isAlive()) {
            this.cachedLanding = predictLanding(this.targetPearl);
            if (this.cachedLanding != null) {
                BlockPos pos = BlockPos.ofFloored(this.cachedLanding);
                BlockState below = this.mc.getWorld().getBlockState(pos.down());
                if (below.getCollisionShape(this.mc.getWorld(), pos.down()).isEmpty()) {
                    BlockPos belowTwo = pos.down(2);
                    BlockState belowTwoState = this.mc.getWorld().getBlockState(belowTwo);
                    if (belowTwoState.getCollisionShape(this.mc.getWorld(), belowTwo).isEmpty()) {
                        this.cachedLanding = null;
                    }
                }
            }
        }
    }

    private EnderPearlEntity findBestPearl() {
        ClientPlayerEntity player = this.mc.getPlayer();
        List<Entity> entities = this.mc.getWorld().getOtherEntities(player, player.getBoundingBox().expand(130.0), e -> true);

        EnderPearlEntity best = null;
        double bestDist = Double.MAX_VALUE;
        double minDist = this.distance.currentValue();

        Entity targetToUse = null;
        if (this.onlyTarget.isValue()) {
            Entity currentAuraTarget = ((AttackAuraModule) Expensive.INSTANCE.moduleRepository().get(AttackAuraModule.class)).lastTarget();
            long currentTime = System.currentTimeMillis();

            if (currentAuraTarget != null && currentAuraTarget.isAlive()) {
                this.rememberedTarget = currentAuraTarget;
                this.rememberedTargetTime = currentTime;
                targetToUse = currentAuraTarget;
            } else if (this.rememberedTarget != null) {
                if (!this.rememberedTarget.isAlive() || this.rememberedTarget.isRemoved()) {
                    this.rememberedTarget = null;
                    this.rememberedTargetTime = 0L;
                } else {
                    long timeSinceRemembered = currentTime - this.rememberedTargetTime;
                    if (timeSinceRemembered < TARGET_MEMORY_DURATION) {
                        targetToUse = this.rememberedTarget;
                    } else {
                        this.rememberedTarget = null;
                        this.rememberedTargetTime = 0L;
                    }
                }
            }
        }

        for (Entity e : entities) {
            if (!(e instanceof EnderPearlEntity pearl) || !pearl.isAlive()) {
                continue;
            }
            if (this.onlyTarget.isValue()) {
                if (targetToUse == null) {
                    continue;
                }
                if (!pearl.getOwner().equals(targetToUse)) {
                    continue;
                }
            }

            Vec3d landing = predictLanding(pearl);
            if (landing == null) {
                continue;
            }

            double dist = player.getPos().distanceTo(landing);
            if (dist >= minDist && dist <= 120.0 && dist < bestDist) {
                best = pearl;
                bestDist = dist;
            }
        }
        return best;
    }

    private Vec3d predictLanding(EnderPearlEntity pearl) {
        Vec3d pos = pearl.getPos();
        Vec3d vel = pearl.getVelocity();

        for (int i = 0; i < MAX_SIM_TICKS; i++) {
            Vec3d next = pos.add(vel);
            vel = vel.multiply(0.99).subtract(0.0, 0.03, 0.0);

            if (next.y <= 0.0) {
                return snapToBlockCenter(next);
            }

            BlockPos bp = BlockPos.ofFloored(next);
            BlockState state = this.mc.getWorld().getBlockState(bp);
            if (!state.getCollisionShape(this.mc.getWorld(), bp).isEmpty()) {
                return snapToBlockCenter(next);
            }
            pos = next;
        }
        return null;
    }

    private Vec3d snapToBlockCenter(Vec3d vec) {
        return new Vec3d(MathHelper.floor(vec.x) + 0.5, MathHelper.floor(vec.y), MathHelper.floor(vec.z) + 0.5);
    }

    private boolean hasObstacleBetween(Vec3d start, Vec3d end) {
        Vec3d direction = end.subtract(start);
        double distance = direction.length();
        Vec3d normalized = direction.normalize();

        int steps = (int) (distance / 0.5) + 1;
        for (int i = 1; i < steps; i++) {
            Vec3d checkPos = start.add(normalized.multiply(i * 0.5));
            BlockPos bp = BlockPos.ofFloored(checkPos);
            BlockState state = this.mc.getWorld().getBlockState(bp);
            if (!state.getCollisionShape(this.mc.getWorld(), bp).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private float[] calculateYawPitch(Vec3d target) {
        Vec3d eye = this.mc.getPlayer().getEyePos();
        double dx = target.x - eye.x;
        double dz = target.z - eye.z;
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;

        double dist = eye.distanceTo(target);
        double heightDiff = target.y - eye.y;
        boolean isHighTarget = heightDiff > 5.0;

        float maxPitch = isHighTarget ? 89.0f : 85.0f;
        float minPitch = dist > 60.0 ? -50.0f : -30.0f;
        if (isHighTarget) {
            minPitch = -80.0f;
        }
        float step = dist > 60.0 ? 0.5f : 0.42f;
        if (isHighTarget) {
            step = 0.3f;
        }

        int bestPitch = 0;
        int bestTicks = Integer.MAX_VALUE;
        double bestError = Double.MAX_VALUE;
        double maxError = isHighTarget ? MAX_ACCEPTABLE_ERROR * 2.0 : MAX_ACCEPTABLE_ERROR;

        for (float pitch = maxPitch; pitch >= minPitch; pitch -= step) {
            SimulationResult res = simulateWithTicks(yaw, pitch, target);
            if (res != null && res.error <= maxError) {
                if (res.ticks < bestTicks || (res.ticks == bestTicks && res.error < bestError)) {
                    bestTicks = res.ticks;
                    bestPitch = Math.round(pitch);
                    bestError = res.error;
                }
            }
        }

        if (bestTicks != Integer.MAX_VALUE) {
            return new float[]{yaw, MathHelper.clamp((float) bestPitch, -90.0f, 90.0f)};
        }
        return null;
    }

    private float[] findAnyWorkingRotation(Vec3d target) {
        Vec3d eye = this.mc.getPlayer().getEyePos();
        double dx = target.x - eye.x;
        double dz = target.z - eye.z;
        float baseYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;

        double dist = eye.distanceTo(target);
        double heightDiff = target.y - eye.y;
        boolean isHighTarget = heightDiff > 5.0;

        float maxPitch = isHighTarget ? 89.0f : 85.0f;
        float minPitch = dist > 60.0 ? -70.0f : -50.0f;
        if (isHighTarget) {
            minPitch = -85.0f;
        }
        float step = dist > 60.0 ? 1.2f : 1.5f;
        if (isHighTarget) {
            step = 1.0f;
        }
        float yawRange = dist > 60.0 ? 30.0f : 20.0f;
        if (isHighTarget) {
            yawRange = 40.0f;
        }
        float yawStep = dist > 60.0 ? 3.0f : 2.5f;
        if (isHighTarget) {
            yawStep = 2.0f;
        }

        double baseMaxError = dist > 60.0 ? MAX_DISTANCE_FROM_TARGET * 2.0 : MAX_DISTANCE_FROM_TARGET * 1.5;
        if (isHighTarget) {
            baseMaxError = MAX_DISTANCE_FROM_TARGET * 3.0;
        }

        for (float pitch = maxPitch; pitch >= minPitch; pitch -= step) {
            Vec3d landing = simulateTrajectory(baseYaw, pitch);
            if (landing != null) {
                double error = target.distanceTo(landing);
                if (error <= baseMaxError) {
                    return new float[]{baseYaw, MathHelper.clamp(pitch, -90.0f, 90.0f)};
                }
            }
        }

        for (float yawOffset = -yawRange; yawOffset <= yawRange; yawOffset += yawStep) {
            if (yawOffset == 0.0f) {
                continue;
            }
            float yaw = baseYaw + yawOffset;
            for (float pitch = maxPitch; pitch >= minPitch; pitch -= step) {
                Vec3d landing = simulateTrajectory(yaw, pitch);
                if (landing != null) {
                    double error = target.distanceTo(landing);
                    if (error <= baseMaxError) {
                        return new float[]{yaw, MathHelper.clamp(pitch, -90.0f, 90.0f)};
                    }
                }
            }
        }

        if (isHighTarget) {
            for (float pitch = 89.0f; pitch >= 75.0f; pitch -= 1.5f) {
                for (float yawOffset = -yawRange; yawOffset <= yawRange; yawOffset += yawStep) {
                    float yaw = baseYaw + yawOffset;
                    Vec3d landing = simulateTrajectory(yaw, pitch);
                    if (landing != null) {
                        double error = target.distanceTo(landing);
                        if (error <= baseMaxError * 1.5) {
                            return new float[]{yaw, MathHelper.clamp(pitch, -90.0f, 90.0f)};
                        }
                    }
                }
            }
        }

        for (float pitch = maxPitch; pitch >= 70.0f; pitch -= 2.0f) {
            for (float yawOffset = -yawRange; yawOffset <= yawRange; yawOffset += yawStep * 2.0f) {
                float yaw = baseYaw + yawOffset;
                Vec3d landing = simulateTrajectory(yaw, pitch);
                if (landing != null) {
                    double error = target.distanceTo(landing);
                    double maxError = isHighTarget ? MAX_DISTANCE_FROM_TARGET * 4.0 : MAX_DISTANCE_FROM_TARGET * 2.5;
                    if (error <= maxError) {
                        return new float[]{yaw, MathHelper.clamp(pitch, -90.0f, 90.0f)};
                    }
                }
            }
        }

        return null;
    }

    private static final class SimulationResult {
        final double error;
        final int ticks;

        SimulationResult(double error, int ticks) {
            this.error = error;
            this.ticks = ticks;
        }
    }

    private SimulationResult simulateWithTicks(float yaw, float pitch, Vec3d target) {
        Vec3d pos = getThrowPos(yaw, pitch);
        Vec3d motion = getThrowMotion(yaw, pitch);

        for (int tick = 0; tick < MAX_SIM_TICKS; tick++) {
            pos = pos.add(motion);
            motion = motion.multiply(0.99).subtract(0.0, 0.03, 0.0);

            if (pos.y <= 0.0) {
                return new SimulationResult(snapToBlockCenter(pos).distanceTo(target), tick + 1);
            }

            BlockPos bp = BlockPos.ofFloored(pos);
            BlockState state = this.mc.getWorld().getBlockState(bp);
            if (!state.getCollisionShape(this.mc.getWorld(), bp).isEmpty()) {
                return new SimulationResult(snapToBlockCenter(pos).distanceTo(target), tick + 1);
            }
        }
        return null;
    }

    private Vec3d simulateTrajectory(float yaw, float pitch) {
        Vec3d pos = getThrowPos(yaw, pitch);
        Vec3d motion = getThrowMotion(yaw, pitch);

        for (int i = 0; i < MAX_SIM_TICKS; i++) {
            pos = pos.add(motion);
            motion = motion.multiply(0.99).subtract(0.0, 0.03, 0.0);

            if (pos.y <= 0.0) {
                return snapToBlockCenter(pos);
            }

            BlockPos bp = BlockPos.ofFloored(pos);
            BlockState state = this.mc.getWorld().getBlockState(bp);
            if (!state.getCollisionShape(this.mc.getWorld(), bp).isEmpty()) {
                return snapToBlockCenter(pos);
            }
        }
        return null;
    }

    private Vec3d getThrowPos(float yaw, float pitch) {
        ClientPlayerEntity player = this.mc.getPlayer();
        float yr = (float) Math.toRadians(yaw);
        double x = player.getX() - Math.cos(yr) * 0.16;
        double y = player.getY() + player.getEyeHeight(player.getPose()) - 0.1;
        double z = player.getZ() - Math.sin(yr) * 0.16;
        return new Vec3d(x, y, z);
    }

    private Vec3d getThrowMotion(float yaw, float pitch) {
        ClientPlayerEntity player = this.mc.getPlayer();
        double v = 1.5;
        float yr = (float) Math.toRadians(yaw);
        float pr = (float) Math.toRadians(pitch);
        double vx = -Math.sin(yr) * Math.cos(pr) * v;
        double vy = -Math.sin(pr) * v;
        double vz = Math.cos(yr) * Math.cos(pr) * v;

        vy += player.getVelocity().y;

        return new Vec3d(vx, vy, vz);
    }

    private void sendPlayerRotationPacket(ClientPlayerEntity player, float yaw, float pitch, boolean onGround) {
        if (player.networkHandler == null) {
            return;
        }
        player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, onGround, false));
    }

    private int findItem(ClientPlayerEntity player, Item item, boolean hotbarOnly) {
        for (int i = hotbarOnly ? 0 : 9; i < (hotbarOnly ? 9 : 36); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    private boolean doesHotbarHaveItem(ClientPlayerEntity player, Item item) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).getItem() == item) {
                return true;
            }
        }
        return false;
    }

    private int getItemSlot(ClientPlayerEntity player, Item input) {
        for (ItemStack stack : player.getInventory().armor) {
            if (stack.getItem() == input) {
                return -2;
            }
        }

        ItemStack cursorStack = player.currentScreenHandler.getCursorStack();
        if (!cursorStack.isEmpty() && cursorStack.getItem() == input) {
            return -3;
        }

        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == input) {
                slot = i;
                break;
            }
        }

        if (slot < 9 && slot != -1) {
            slot += 36;
        }

        return slot;
    }

    private int findAndThrowItem(ClientPlayerEntity player, int hbSlot, int invSlot) {
        // UpdateSelectedSlot + свинг от useItem сбрасывали кулдаун ауры (PacketSendEvent) -
        // после броска перла аура стояла 10-13 тиков. Подавляем сброс внутри броска.
        return AuraCooldownGuard.INSTANCE.call(() -> {
            if (hbSlot != -1) {
                setOriginalSlot(player);
                player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(hbSlot));
                useItem(player, Hand.MAIN_HAND);
                this.delay = System.currentTimeMillis();
                return hbSlot;
            }
            if (invSlot != -1) {
                setOriginalSlot(player);
                pickItem(player, invSlot);
                useItem(player, Hand.MAIN_HAND);
                this.delay = System.currentTimeMillis();
                return invSlot;
            }
            return -1;
        });
    }

    private void pickItem(ClientPlayerEntity player, int slot) {
        this.mc.getInteractionManager().clickSlot(slot, 0, 0, SlotActionType.PICKUP, player);
    }

    private boolean throwPearl(ClientPlayerEntity player) {
        if (this.mc.getCurrentScreen() != null || this.server == null) {
            return false;
        }

        Predicate<ItemStack> predicate = stack -> !stack.isEmpty() && stack.getItem() instanceof EnderPearlItem;
        Optional<SlotSearchResult2> found = Expensive.INSTANCE.inventoryService().searcher().findItem(predicate, InventoryScope.ALL);
        if (found.isEmpty() || !found.get().found()) {
            return false;
        }

        Rotation rotation = new Rotation(this.server.x, this.server.y);
        sendPlayerRotationPacket(player, rotation.yaw, rotation.pitch, player.isOnGround());

        ScopedSlot pearlSlot = found.get().slotReference();
        if (pearlSlot.slot() < 9) {
            // Перл в хотбаре: тихий свап слота, пакет использования несёт бросок-ротацию, затем возврат слота.
            // UpdateSelectedSlot/свинг внутри броска сбрасывают кулдаун ауры (PacketSendEvent) -
            // подавляем этот сброс, пока идёт бросок (см. AuraCooldownGuard).
            AuraCooldownGuard.INSTANCE.run(() -> {
                int previousSlot = player.getInventory().selectedSlot;
                SilentSlotManager swapper = Expensive.INSTANCE.inventoryService().hotbarSlotSwapper();
                swapper.swapTo(this, pearlSlot, 1);
                interactItemWithThrowRotation(rotation);
                swapper.pendingSwap = null;
                player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
            });
        } else {
            // Перл в инвентаре: общий пайплайн swap -> use -> restore, выполняемый по тикам через
            // GrimDelayHandler (как в Click Pearl). Три SWAP-клика в одном тике сервер отклоняет,
            // из-за чего бросок перла из инвентаря не срабатывал.
            // Окно подавления ставим вокруг самого interactItem - он выполняется позже, по тикам.
            SwapUtil.swapAndExecute(pearlSlot.increasedSlot(), rotation, SwapUtil.needsStop(), () ->
                    AuraCooldownGuard.INSTANCE.run(() -> interactItemWithThrowRotation(rotation)));
        }
        return true;
    }

    private void interactItemWithThrowRotation(Rotation rotation) {
        Rotation previous = RotationManager.INSTANCE.currentRotationOriginal;
        RotationManager.INSTANCE.currentRotationOriginal = rotation;
        try {
            PlayerActionUtil.INSTANCE.interactItem(Hand.MAIN_HAND, rotation, false);
        } finally {
            RotationManager.INSTANCE.currentRotationOriginal = previous;
        }
    }

    private int findPearlSlot(ClientPlayerEntity player) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof EnderPearlItem) {
                return i;
            }
        }
        return -1;
    }

    private void useItem(ClientPlayerEntity player, Hand hand) {
        if (player.networkHandler == null) {
            return;
        }
        Rotation rotation = this.server == null ? Rotation.playerRotation() : new Rotation(this.server.x, this.server.y);
        // interactItem + swingHand шлют HandSwing/UpdateSelectedSlot - по ним аура
        // сбрасывает кулдаун атаки. Во время броска перла сброс подавляем.
        AuraCooldownGuard.INSTANCE.run(() -> {
            Expensive.INSTANCE.inventoryService().itemInteractor().interactItem(player, hand, rotation);
            player.swingHand(hand);
        });
    }

    private void setOriginalSlot(ClientPlayerEntity player) {
        this.originalSlot = player.getInventory().selectedSlot;
    }

    private void handleItemChange(ClientPlayerEntity player, boolean resetItem) {
        if (this.isChangingItem && this.originalSlot != -1) {
            player.getInventory().selectedSlot = this.originalSlot;
            if (resetItem) {
                this.isChangingItem = false;
                this.originalSlot = -1;
            }
        }
    }

    private void holySwapClick(ClientPlayerEntity player) {
        if (getItemSlot(player, Items.ENDER_PEARL) == -1) {
            return;
        }

        if (doesHotbarHaveItem(player, Items.ENDER_PEARL)) {
            int i = findItem(player, Items.ENDER_PEARL, true);
            int originalSlot = player.getInventory().selectedSlot;
            player.getInventory().selectedSlot = i;
            delay(40L, () -> useItem(player, Hand.MAIN_HAND));
            delay(60L, () -> player.getInventory().selectedSlot = originalSlot);
            return;
        }

        int slot = findItem(player, Items.ENDER_PEARL, false);
        if (slot == -1) {
            return;
        }
        int button = player.getInventory().selectedSlot % 8;

        // SWAP-клик + UpdateSelectedSlot - свинг/слот-пакеты сбрасывают кулдаун ауры,
        // во время броска перла подавляем (см. AuraCooldownGuard).
        AuraCooldownGuard.INSTANCE.run(() -> {
            this.mc.getInteractionManager().clickSlot(slot, button, 0, SlotActionType.SWAP, player);
            player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(button));
        });

        delay(40L, () -> useItem(player, Hand.MAIN_HAND));
        delay(60L, () -> AuraCooldownGuard.INSTANCE.run(() -> {
            player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(player.getInventory().selectedSlot));
            this.mc.getInteractionManager().clickSlot(slot, player.getInventory().selectedSlot % 8, 0, SlotActionType.SWAP, player);
        }));
    }

    private void fixMovement(MovementInputEvent e, float yaw) {
        ClientPlayerEntity player = this.mc.getPlayer();
        DirectionalInput input = e.getInput();
        float forward = (input.forward() ? 1.0f : 0.0f) - (input.backward() ? 1.0f : 0.0f);
        float strafe = (input.left() ? 1.0f : 0.0f) - (input.right() ? 1.0f : 0.0f);

        float angle = MathHelper.wrapDegrees((float) Math.toDegrees(direction(player.isGliding() ? player.getYaw() : yaw, forward, strafe)));

        if (forward == 0.0f && strafe == 0.0f) {
            return;
        }

        float closestForward = 0.0f;
        float closestStrafe = 0.0f;
        float closestDifference = Float.MAX_VALUE;
        for (float predictedForward = -1.0f; predictedForward <= 1.0f; predictedForward += 1.0f) {
            for (float predictedStrafe = -1.0f; predictedStrafe <= 1.0f; predictedStrafe += 1.0f) {
                if (predictedStrafe == 0.0f && predictedForward == 0.0f) {
                    continue;
                }
                float predictedAngle = MathHelper.wrapDegrees((float) Math.toDegrees(direction(player.getYaw(), predictedForward, predictedStrafe)));
                float difference = Math.abs(angle - predictedAngle);

                if (difference < closestDifference) {
                    closestDifference = difference;
                    closestForward = predictedForward;
                    closestStrafe = predictedStrafe;
                }
            }
        }

        e.setInput(DirectionalInput.fromMovementForwardAndSideways(closestForward, closestStrafe));
    }

    private static double direction(float rotationYaw, double moveForward, double moveStrafing) {
        float yaw = rotationYaw;
        if (moveForward < 0.0f) {
            yaw += 180.0f;
        }

        float forward = 1.0f;
        if (moveForward < 0.0f) {
            forward = -0.5f;
        } else if (moveForward > 0.0f) {
            forward = 0.5f;
        }

        if (moveStrafing > 0.0f) {
            yaw -= 90.0f * forward;
        }
        if (moveStrafing < 0.0f) {
            yaw += 90.0f * forward;
        }

        return Math.toRadians(yaw);
    }

    private void onDisable() {
        this.isThrowing = false;
        this.targetPearl = null;
        this.cachedLanding = null;
        this.server = null;
        this.rotationHoldUntil = 0L;
        this.isChangingItem = false;
        this.originalSlot = -1;
        this.timerUtil.reset();
        this.rememberedTarget = null;
        this.rememberedTargetTime = 0L;
        this.delayedActions.clear();
    }
}