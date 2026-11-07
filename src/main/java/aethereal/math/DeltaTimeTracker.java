package aethereal.math;

import java.util.concurrent.TimeUnit;

public class DeltaTimeTracker {
    public static final double nanosPerSecond = TimeUnit.SECONDS.toNanos(1);
    public long lastTime = System.nanoTime();

    public void clear() {
        this.lastTime = System.nanoTime();
    }

    public float elapsedUnit() {
        long jNanoTime = System.nanoTime();
        double d = (jNanoTime - this.lastTime) / nanosPerSecond;
        this.lastTime = jNanoTime;
        return (float) d;
    }
}
