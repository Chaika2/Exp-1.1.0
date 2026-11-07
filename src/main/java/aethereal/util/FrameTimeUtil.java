package aethereal.util;

import net.minecraft.util.Util;

public final class FrameTimeUtil {
    public static long lastFrameNano = -1;

    public static float frameDt() {
        long measuringTimeNano = Util.getMeasuringTimeNano();
        if (lastFrameNano < 0) {
            lastFrameNano = measuringTimeNano;
            return 0.0f;
        }
        long j = measuringTimeNano - lastFrameNano;
        lastFrameNano = measuringTimeNano;
        return MathUtil.clamp(j / 1.0E9f, 0.0f, 0.1f);
    }
}
