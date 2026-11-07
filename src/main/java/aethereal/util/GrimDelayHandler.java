package aethereal.util;
import aethereal.math.DirectionalInput;
import aethereal.Expensive;
import aethereal.module.FreeCameraModule;
import aethereal.type.Mc;
import aethereal.module.Module;
import aethereal.ui.ModuleTab;
import aethereal.event.MovementInputEvent;
import aethereal.math.Rotation;
import aethereal.config.RotationConfig;
import aethereal.math.RotationVector;
import aethereal.math.SimulatedPlayer;

import java.util.Objects;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;

public final class GrimDelayHandler {
    public static final ActionScheduler script = new ActionScheduler();
    public static boolean canMove = true;
    public static final Mc mc = Mc.INSTANCE;
    static final Module rotationModule = new Module(ModuleTab.MISC, "GrimDelayHandler").setStateSilent(true);

    public static void onWorldChange() {
        canMove = true;
        script.cleanup();
        refreshPressedKeys();
    }

    public static void tick() {
        script.update().cleanupIfFinished();
    }

    public static void input(MovementInputEvent class040Var) {
        if (canMove) {
            return;
        }
        class040Var.setStopProgression(true);
        class040Var.setJumping(false);
        class040Var.setSprinting(false);
        class040Var.setSneaking(false);
        class040Var.setInput(DirectionalInput.NONE);
    }

    public static void addTask(Runnable runnable) {
        addTask(RotationManager.INSTANCE.getCurrentRotation(), runnable);
    }

    public static void addTask(boolean z, Runnable runnable) {
        addTask(RotationManager.INSTANCE.getCurrentRotation(), z, runnable);
    }

    public static int getTaskRunnableTime() {
        switch (ServerUtil.getServer()) {
            case "FunTime":
                return (ServerUtil.getAnarchy() >= 100 || ServerUtil.getProtocolVersion() <= 767) ? 1 : 2;
            case "SpookyTime":
                if (!MovementInputHelper.hasPlayerMovement()) {
                    return 1;
                }
                SimulatedPlayer class136VarSimulateLocalPlayer = SimulatedPlayer.simulateLocalPlayer(2);
                return (class136VarSimulateLocalPlayer.onGround || class136VarSimulateLocalPlayer.touchingWater) ? 1 : 2;
            default:
                return 1;
        }
    }

    public static void addTask(Rotation class007Var, Runnable runnable) {
        addTask(class007Var, true, runnable);
    }

    public static void addTask(Rotation class007Var, boolean z, Runnable runnable) {
        boolean z2 = RotationManager.computeRotationDifference(class007Var, RotationManager.INSTANCE.getCurrentRotation()) > 1.0d;
        if (!z || (((FreeCameraModule) Expensive.INSTANCE.moduleRepository().get(FreeCameraModule.class)).isState() && !MovementInputHelper.hasPlayerMovement())) {
            ActionScheduler class265VarAddTickStep = script.addTickStep(0, () -> {
                rotateToAngle(z2, class007Var);
            });
            Objects.requireNonNull(runnable);
            class265VarAddTickStep.addTickStep(1, runnable::run);
            return;
        }
        if (ServerUtil.isConnectedToServer("spookytime")) {
            SimulatedPlayer class136VarSimulateLocalPlayer = SimulatedPlayer.simulateLocalPlayer(2);
            if (!class136VarSimulateLocalPlayer.onGround && !class136VarSimulateLocalPlayer.touchingWater) {
                rotateToAngle(z2, class007Var);
                ActionScheduler class265VarAddTickStep2 = script.addTickStep(0, () -> {
                    rotateToAngle(z2, class007Var);
                    disableMoveKeys();
                });
                Objects.requireNonNull(runnable);
                class265VarAddTickStep2.addTickStep(1, runnable::run).addTickStep(2, GrimDelayHandler::enableMoveKeys);
                return;
            }
        }
        rotateToAngle(z2, class007Var);
        script.addTickStep(0, () -> {
            rotateToAngle(z2, class007Var);
            disableMoveKeys();
        }).addTickStep(1, () -> {
            runnable.run();
            enableMoveKeys();
        });
    }

    public static void rotateToAngle(boolean z, Rotation class007Var) {
        if (z) {
            rotateToAngle(class007Var);
        }
    }

    public static void rotateToAngle(Rotation class007Var) {
        RotationManager.INSTANCE.clear();
        RotationManager.INSTANCE.scheduleRotation(new RotationVector(class007Var, class007Var.getDirectionVector()), (LivingEntity) mc.getPlayer(), RotationConfig.BLOCK_WITH_CORRECTION, 3, rotationModule, 5);
    }

    public static void disableMoveKeys() {
        canMove = false;
        releaseKeys();
    }

    public static void enableMoveKeys() {
        canMove = true;
        refreshPressedKeys();
    }

    public static void releaseKeys() {
        for (KeyBinding keyBinding : MovementInputHelper.getMovementKeys(false, true)) {
            keyBinding.setPressed(false);
        }
    }

    public static void refreshPressedKeys() {
        long handle = Mc.INSTANCE.getWindow().getHandle();
        for (KeyBinding keyBinding : MovementInputHelper.getMovementKeys(false, true)) {
            keyBinding.setPressed(InputUtil.isKeyPressed(handle, keyBinding.getDefaultKey().getCode()));
        }
    }

    public GrimDelayHandler() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
