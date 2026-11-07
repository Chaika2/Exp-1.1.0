package aethereal.util;
import aethereal.type.Mc;
import aethereal.math.Rotation;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

public final class MathUtil {
    public static int clamp(int i, int i2, int i3) {
        return Math.min(Math.max(i, i2), i3);
    }

    public static double clamp(double d, double d2, double d3) {
        return Math.min(Math.max(d, d2), d3);
    }

    public static int interpolate(int i, int i2, float f) {
        return Math.round(i + ((i2 - i) * f));
    }

    public static Rotation interpolate(Rotation class007Var, Rotation class007Var2) {
        return new Rotation(interpolate(class007Var.getYaw(), class007Var2.getYaw()), interpolate(class007Var.getPitch(), class007Var2.getPitch()));
    }

    public static int interpolateSmooth(double d, int i, int i2) {
        return (int) MathHelper.lerp(((double) Mc.INSTANCE.getMinecraft().getRenderTickCounter().getLastDuration()) / d, i, i2);
    }

    public static Vec3d cosSin(int i, int i2, double d) {
        int iMin = Math.min(i, i2);
        return new Vec3d((float) (Math.cos(((((double) iMin) * 3.141592653589793d) * 2.0d) / ((double) i2)) * d), 0.0d, (float) ((-Math.sin(((((double) iMin) * 3.141592653589793d) * 2.0d) / ((double) i2))) * d));
    }

    public static Vector3d interpolate(Vector3d vector3d, Vector3d vector3d2) {
        return new Vector3d(interpolate(vector3d.x, vector3d2.x), interpolate(vector3d.y, vector3d2.y), interpolate(vector3d.z, vector3d2.z));
    }

    public static Vec3d interpolate(Vec3d vec3d, Vec3d vec3d2) {
        return new Vec3d(interpolate(vec3d.x, vec3d2.x), interpolate(vec3d.y, vec3d2.y), interpolate(vec3d.z, vec3d2.z));
    }

    public static Vec3d getPrevPositionVec(Entity entity) {
        return new Vec3d(entity.prevX, entity.prevY, entity.prevZ);
    }

    public static void sizeAnimation(MatrixStack matrixStack, float f, float f2, float f3) {
        matrixStack.translate(f / 2.0f, f2 / 2.0f, 0.0f);
        matrixStack.scale(f3, f3, 0.0f);
        matrixStack.translate((-f) / 2.0f, (-f2) / 2.0f, 0.0f);
    }

    public static Vec3d getInterpolatedPositionVec(Entity entity, boolean z) {
        Vec3d prevPositionVec = getPrevPositionVec(entity);
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        return prevPositionVec.add(0.0d, player.getEyeHeight(player.getPose()), 0.0d).add(entity.getPos().subtract(prevPositionVec).multiply(Mc.INSTANCE.getTickDelta()));
    }

    public static Vec3d interpolate(Entity entity) {
        return entity == null ? Vec3d.ZERO : new Vec3d(interpolate(entity.prevX, entity.getX()), interpolate(entity.prevY, entity.getY()), interpolate(entity.prevZ, entity.getZ()));
    }

    public static float interpolate(float f, float f2) {
        return MathHelper.lerp(Mc.INSTANCE.getRenderTickCounter().getTickDelta(true), f, f2);
    }

    public static double interpolate(double d, double d2) {
        return MathHelper.lerp(Mc.INSTANCE.getRenderTickCounter().getTickDelta(true), d, d2);
    }

    public static float interpolateSmooth(double d, float f, float f2) {
        return (float) MathHelper.lerp(((double) Mc.INSTANCE.getMinecraft().getRenderTickCounter().getLastDuration()) / d, f, f2);
    }

    public static int getRandom(int i, int i2) {
        return (int) getRandom(i, i2 + 1.0f);
    }

    public static float getRandom(float f, float f2) {
        return (float) getRandom((double) f, (double) f2);
    }

    public static double getRandom(double d, double d2) {
        if (d == d2) {
            return d;
        }
        if (d > d2) {
            d = d2;
            d2 = d;
        }
        return ThreadLocalRandom.current().nextDouble(d, d2);
    }

    public static float oppositeYaw(ClientPlayerEntity clientPlayerEntity) {
        Vec3d velocity = clientPlayerEntity.getVelocity();
        return velocity.horizontalLengthSquared() > 1.0E-4d ? ((float) (Math.toDegrees(Math.atan2(velocity.z, velocity.x)) - 90.0d)) + 180.0f : clientPlayerEntity.getYaw() + 180.0f;
    }

    public static double interpolateSmooth(double d, double d2, double d3) {
        return MathHelper.lerp(((double) Mc.INSTANCE.getMinecraft().getRenderTickCounter().getLastDuration()) / d, d2, d3);
    }

    public static int roundToInt(double d) {
        return (int) Math.round(d);
    }

    public static double round(double d, double d2) {
        return Math.round((Math.round(d / d2) * d2) * 100.0d) / 100.0d;
    }

    public static float textScrolling(float f) {
        int i = (int) (f * 75.0f);
        return ((float) clamp(((System.currentTimeMillis() % ((long) i)) * 3.141592653589793d) / ((double) i), 0.0d, 1.0d)) * f;
    }

    public static float clamp(float f, float f2, float f3) {
        return Math.min(Math.max(f, f2), f3);
    }

    public static float fastPow(float f, float f2) {
        return (float) Math.pow(f, f2);
    }

    public static boolean isHovered(double d, double d2, double d3, double d4, double d5, double d6) {
        return d >= d3 && d <= d3 + d5 && d2 >= d4 && d2 <= d4 + d6;
    }

    public static Rotation quadraticBezier(Rotation class007Var, Rotation class007Var2, float f, float f2, float f3) {
        float fClamp = MathHelper.clamp(f, 0.0f, 1.0f);
        float f4 = 1.0f - fClamp;
        float fWrapDegrees = MathHelper.wrapDegrees(class007Var2.getYaw() - class007Var.getYaw());
        Rotation class007Var3 = new Rotation(class007Var.getYaw() + (fWrapDegrees * 0.5f) + f2, class007Var.getPitch() + ((class007Var2.getPitch() - class007Var.getPitch()) * 0.5f) + f3);
        return new Rotation(class007Var.getYaw() + (f4 * f4 * 0.0f) + (2.0f * f4 * fClamp * (class007Var3.getYaw() - class007Var.getYaw())) + (fClamp * fClamp * fWrapDegrees), (f4 * f4 * class007Var.getPitch()) + (2.0f * f4 * fClamp * class007Var3.getPitch()) + (fClamp * fClamp * class007Var2.getPitch()));
    }

    public static float interpolate(float f, float f2, float f3) {
        return f + ((f2 - f) * f3);
    }

    public static double interpolate(double d, double d2, double d3) {
        return d + ((d2 - d) * d3);
    }

    public static float lerp(float f, float f2, float f3) {
        return (float) (((double) f) + (((double) (f2 - f)) * clamp(deltaTime() * ((double) f3), 0.0d, 1.0d)));
    }

    public static Vec3d lerp(Vec3d vec3d, Vec3d vec3d2, double d) {
        return new Vec3d(vec3d.x + ((vec3d2.x - vec3d.x) * d), vec3d.y + ((vec3d2.y - vec3d.y) * d), vec3d.z + ((vec3d2.z - vec3d.z) * d));
    }

    public static double lerp(double d, double d2, double d3) {
        return d + ((d2 - d) * clamp(deltaTime() * d3, 0.0d, 1.0d));
    }

    public static double computeGcd() {
        double dDoubleValue = (((Double) Mc.INSTANCE.getGameOptions().getMouseSensitivity().getValue()).doubleValue() * 0.6000000238418579d) + 0.20000000298023224d;
        return dDoubleValue * dDoubleValue * dDoubleValue * 8.0d * 0.15d;
    }

    public static float rad(float f) {
        return (float) ((((double) f) * 3.141592653589793d) / 180.0d);
    }

    public static int floorNearestMulN(int i, int i2) {
        return i2 * ((int) Math.floor(((double) i) / ((double) i2)));
    }

    public static Vec3d getEntityRenderPosition(Entity entity, double d) {
        Vec3d pos = Mc.INSTANCE.getGameRenderer().getCamera().getPos();
        return new Vec3d((entity.prevX + ((entity.getX() - entity.prevX) * d)) - pos.x, (entity.prevY + ((entity.getY() - entity.prevY) * d)) - pos.y, (entity.prevZ + ((entity.getZ() - entity.prevZ) * d)) - pos.z);
    }

    public static double absSinAnimation(double d) {
        return Math.abs(1.0d + Math.sin(d)) / 2.0d;
    }

    public static String tickToElapsedTime(int i) {
        int i2 = i / 20;
        int i3 = i2 / 60;
        int i4 = i2 % 60;
        return i4 < 10 ? i3 + ":0" + i4 : i3 + ":" + i4;
    }

    public static String decimalFormat(Number number, int i) {
        DecimalFormat decimalFormat = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        decimalFormat.setMaximumFractionDigits(i);
        return decimalFormat.format(number);
    }

    public static double deltaTime() {
        int currentFps = MinecraftClient.getInstance().getCurrentFps();
        if (currentFps > 0) {
            return 1.0d / ((double) currentFps);
        }
        return 1.0d;
    }

    public MathUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
