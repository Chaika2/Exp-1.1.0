package aethereal.util;
import aethereal.math.DirectionalInput;
import aethereal.type.Mc;
import aethereal.math.SimulatedPlayer;

import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class MovementInputHelper {
    public static KeyBinding[] getMovementKeys(boolean z, boolean z2) {
        GameOptions gameOptions = Mc.INSTANCE.getGameOptions();
        KeyBinding[] keyBindingArr = new KeyBinding[7];
        keyBindingArr[0] = gameOptions.forwardKey;
        keyBindingArr[1] = gameOptions.backKey;
        keyBindingArr[2] = gameOptions.leftKey;
        keyBindingArr[3] = gameOptions.rightKey;
        keyBindingArr[4] = gameOptions.jumpKey;
        keyBindingArr[5] = z2 ? gameOptions.sprintKey : null;
        keyBindingArr[6] = z ? gameOptions.sneakKey : null;
        return (KeyBinding[]) Stream.of(keyBindingArr).filter((v0) -> {
            return Objects.nonNull(v0);
        }).toArray(i -> {
            return new KeyBinding[i];
        });
    }

    public static boolean hasCollisionWith(Entity entity) {
        return hasCollisionWith(entity, 0.0f);
    }

    public static boolean hasCollisionWith(Entity entity, float f) {
        Box boundingBox = Mc.INSTANCE.getPlayer().getBoundingBox();
        Box boxExpand = entity.getBoundingBox().expand(f, 0.0d, f);
        return boundingBox.maxX > boxExpand.minX && boundingBox.maxY > boxExpand.minY && boundingBox.maxZ > boxExpand.minZ && boundingBox.minX < boxExpand.maxX && boundingBox.minY < boxExpand.maxY && boundingBox.minZ < boxExpand.maxZ;
    }

    public static DirectionalInput getDirectionalInputForDegrees(DirectionalInput class041Var, double d, float f) {
        boolean zForward = class041Var.forward();
        boolean zBackward = class041Var.backward();
        boolean zLeft = class041Var.left();
        boolean zRight = class041Var.right();
        if (d >= (-90.0f) + f && d <= 90.0f - f) {
            zForward = true;
        } else if (d < (-90.0f) - f || d > 90.0f + f) {
            zBackward = true;
        }
        if (d >= 0.0f + f && d <= 180.0f - f) {
            zRight = true;
        } else if (d >= (-180.0f) + f && d <= 0.0f - f) {
            zLeft = true;
        }
        return new DirectionalInput(zForward, zBackward, zLeft, zRight);
    }

    public static DirectionalInput getDirectionalInputForDegrees(DirectionalInput class041Var, double d) {
        return getDirectionalInputForDegrees(class041Var, d, 20.0f);
    }

    public static double getDegreesRelativeToView(Vec3d vec3d, float f) {
        return MathHelper.wrapDegrees((float) (Math.toDegrees(Math.atan2(-vec3d.x, vec3d.z)) - ((double) f)));
    }

    public static Vec3d withStrafe(Vec3d vec3d, double d, double d2, DirectionalInput class041Var, float f) {
        if (class041Var != null && !class041Var.isMoving()) {
            return new Vec3d(0.0d, vec3d.y, 0.0d);
        }
        double d3 = vec3d.x * (1.0d - d2);
        double d4 = vec3d.z * (1.0d - d2);
        double d5 = d * d2;
        double radians = Math.toRadians(f);
        return new Vec3d(((-Math.sin(radians)) * d5) + d3, vec3d.y, (Math.cos(radians) * d5) + d4);
    }

    public static Vec3d withStrafe(Vec3d vec3d, DirectionalInput class041Var, float f, float f2) {
        return withStrafe(vec3d, f, 1.0d, class041Var, getMovementDirectionOfInput(f2, class041Var));
    }

    public static float getMovementDirectionOfInput(float f, DirectionalInput class041Var) {
        float f2 = f;
        float f3 = 1.0f;
        if (class041Var.backward()) {
            f2 += 180.0f;
            f3 = -0.5f;
        } else if (class041Var.forward()) {
            f3 = 0.5f;
        }
        if (class041Var.left()) {
            f2 -= 90.0f * f3;
        }
        if (class041Var.right()) {
            f2 += 90.0f * f3;
        }
        return f2;
    }

    public static boolean isBlockUnder(float f) {
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        return player.getY() >= 0.0d && !Mc.INSTANCE.getWorld().getCollisions(player, player.getBoundingBox().offset(0.0d, (double) (-f), 0.0d)).iterator().hasNext();
    }

    public static double[] direction(double d) {
        return direction(RotationManager.INSTANCE.getMoveRotation().getYaw(), d);
    }

    public static double[] direction(float f, double d) {
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        return direction(player.input.movementForward, player.input.movementSideways, f, d);
    }

    public static double[] direction(float f, float f2, float f3, double d) {
        if (f != 0.0f) {
            if (f2 > 0.0f) {
                f3 += f > 0.0f ? -45.0f : 45.0f;
            } else if (f2 < 0.0f) {
                f3 += f > 0.0f ? 45.0f : -45.0f;
            }
            f2 = 0.0f;
            f = f > 0.0f ? 1.0f : -1.0f;
        }
        double dSin = Math.sin(Math.toRadians(f3 + 90.0f));
        double dCos = Math.cos(Math.toRadians(f3 + 90.0f));
        return new double[]{(((double) f) * d * dCos) + (((double) f2) * d * dSin), ((((double) f) * d) * dSin) - ((((double) f2) * d) * dCos)};
    }

    public static double getSpeed() {
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        if (player == null) {
            return 0.0d;
        }
        Vec3d velocity = player.getVelocity();
        return Math.hypot(velocity.getX(), velocity.getZ());
    }

    public static boolean isMoving(LivingEntity livingEntity) {
        if (!(livingEntity instanceof PlayerEntity)) {
            return false;
        }
        SimulatedPlayer class136VarSimulateOtherPlayer = SimulatedPlayer.simulateOtherPlayer((PlayerEntity) livingEntity, 1);
        return (class136VarSimulateOtherPlayer.input.movementForward == 0.0f && class136VarSimulateOtherPlayer.input.movementSideways == 0.0f) ? false : true;
    }

    public static boolean hasPlayerMovement() {
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        return (player.input.movementForward == 0.0f && player.input.movementSideways == 0.0f) ? false : true;
    }

    public static void setSpeed(double d) {
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        if (player == null) {
            return;
        }
        DirectionalInput class041VarFromPlayerInput = DirectionalInput.fromPlayerInput(player);
        if (!class041VarFromPlayerInput.isMoving()) {
            player.setVelocity(0.0d, player.getVelocity().getY(), 0.0d);
            return;
        }
        double radians = Math.toRadians(getMovementDirectionOfInput(player.getYaw(), class041VarFromPlayerInput));
        player.addVelocity((-Math.sin(radians)) * d, 0.0d, Math.cos(radians) * d);
    }

    public MovementInputHelper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
