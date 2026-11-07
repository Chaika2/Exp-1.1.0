package aethereal.util;

import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.util.math.MathHelper;

/**
 * Естественное дрожание прицела.
 *
 * Живой игрок никогда не держит перекрестие ровно в точке наводки: рука
 * постоянно немного уходит. Серверные эвристики "aim automation" ловят как раз
 * идеальное удержание (aimErr ~ 0.0 градусов много тиков подряд) и постоянную
 * угловую скорость. Поэтому к углу цели добавляется медленно дрейфующее
 * смещение с небольшим шумом.
 *
 * Тикает ровно один раз за игровой тик (а не за каждый вызов process), чтобы
 * несколько целей/стратегий в одном тике не ускоряли дрожание.
 */
public final class RotationJitter {
    /** Максимальное смещение по yaw/pitch, градусы. */
    private static final float MAX_YAW = 1.35f;
    private static final float MAX_PITCH = 0.85f;

    private float driftYaw;
    private float driftPitch;
    private float yaw;
    private float pitch;
    private int lastAge = Integer.MIN_VALUE;

    public void tick(int age) {
        if (age == this.lastAge) {
            return;
        }
        this.lastAge = age;
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // Медленный дрейф (рука "уводит" прицел) + слабый высокочастотный шум.
        this.driftYaw = MathHelper.clamp(
                this.driftYaw * 0.985f + (float) random.nextGaussian() * 0.045f, -0.75f, 0.75f);
        this.driftPitch = MathHelper.clamp(
                this.driftPitch * 0.985f + (float) random.nextGaussian() * 0.03f, -0.45f, 0.45f);

        this.yaw = MathHelper.clamp(this.driftYaw + (float) random.nextGaussian() * 0.14f, -MAX_YAW, MAX_YAW);
        this.pitch = MathHelper.clamp(this.driftPitch + (float) random.nextGaussian() * 0.09f, -MAX_PITCH, MAX_PITCH);
    }

    public float yaw() {
        return this.yaw;
    }

    public float pitch() {
        return this.pitch;
    }

    public void reset() {
        this.driftYaw = 0.0f;
        this.driftPitch = 0.0f;
        this.yaw = 0.0f;
        this.pitch = 0.0f;
        this.lastAge = Integer.MIN_VALUE;
    }
}
