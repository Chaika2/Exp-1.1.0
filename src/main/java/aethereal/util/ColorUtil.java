package aethereal.util;

import net.minecraft.util.math.ColorHelper;

public final class ColorUtil {
    static float alphaMultiplier = 1.0f;

    public static int applyOpacity(int i, float f) {
        return ColorHelper.getArgb((int) ((alpha(i) * f) / 255.0f), red(i), green(i), blue(i));
    }

    public static int argb(int i, int i2, int i3, int i4) {
        return ColorHelper.getArgb(i, i2, i3, i4);
    }

    public static int argb(float f, int i, int i2, int i3) {
        return ColorHelper.getArgb(Math.round(MathUtil.clamp(f, 0.0f, 1.0f) * 255.0f), i, i2, i3);
    }

    public static int red(int i) {
        return (i >> 16) & StencilBufferUtil.STENCIL_MASK;
    }

    public static int green(int i) {
        return (i >> 8) & StencilBufferUtil.STENCIL_MASK;
    }

    public static int blue(int i) {
        return i & StencilBufferUtil.STENCIL_MASK;
    }

    public static int alpha(int i) {
        return (i >> 24) & StencilBufferUtil.STENCIL_MASK;
    }

    public static int interpolateColor(int i, int i2, float f) {
        float f2 = ((i >> 24) & StencilBufferUtil.STENCIL_MASK) / 255.0f;
        float f3 = ((i >> 16) & StencilBufferUtil.STENCIL_MASK) / 255.0f;
        float f4 = ((i >> 8) & StencilBufferUtil.STENCIL_MASK) / 255.0f;
        float f5 = (i & StencilBufferUtil.STENCIL_MASK) / 255.0f;
        return (((int) ((f2 + (f * ((((i2 >> 24) & StencilBufferUtil.STENCIL_MASK) / 255.0f) - f2))) * 255.0f)) << 24) | (((int) ((f3 + (f * ((((i2 >> 16) & StencilBufferUtil.STENCIL_MASK) / 255.0f) - f3))) * 255.0f)) << 16) | (((int) ((f4 + (f * ((((i2 >> 8) & StencilBufferUtil.STENCIL_MASK) / 255.0f) - f4))) * 255.0f)) << 8) | ((int) ((f5 + (f * (((i2 & StencilBufferUtil.STENCIL_MASK) / 255.0f) - f5))) * 255.0f));
    }

    public static int replAlpha(int i, int i2) {
        return argb(i2, red(i), green(i), blue(i));
    }

    public static int replAlpha(int i, float f) {
        return argb(f, red(i), green(i), blue(i));
    }

    public static int multBright(int i, float f) {
        return argb(alpha(i), Math.min(StencilBufferUtil.STENCIL_MASK, Math.round(red(i) / f)), Math.min(StencilBufferUtil.STENCIL_MASK, Math.round(green(i) / f)), Math.min(StencilBufferUtil.STENCIL_MASK, Math.round(blue(i) / f)));
    }

    public ColorUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static float getAlphaMultiplier() {
        return alphaMultiplier;
    }
}
