package aethereal.math;
import aethereal.util.MathUtil;

public final class Easings {
    public static final EasingFunction LINEAR = f -> {
        return f;
    };
    public static final EasingFunction EASE_IN_QUAD = f -> {
        return f * f;
    };
    public static final EasingFunction EASE_OUT_QUAD = f -> {
        return 1.0f - ((1.0f - f) * (1.0f - f));
    };
    public static final EasingFunction EASE_IN_OUT_QUAD = f -> {
        return f < 0.5f ? 2.0f * f * f : 1.0f - (MathUtil.fastPow(((-2.0f) * f) + 2.0f, 2.0f) / 2.0f);
    };
    public static final EasingFunction EASE_IN_CUBIC = f -> {
        return f * f * f;
    };
    public static final EasingFunction EASE_OUT_CUBIC = f -> {
        return 1.0f - MathUtil.fastPow(1.0f - f, 3.0f);
    };
    public static final EasingFunction EASE_IN_OUT_CUBIC = f -> {
        return f < 0.5f ? 4.0f * f * f * f : 1.0f - (MathUtil.fastPow(((-2.0f) * f) + 2.0f, 3.0f) / 2.0f);
    };
    public static final EasingFunction EASE_IN_QUART = f -> {
        return f * f * f * f;
    };
    public static final EasingFunction EASE_OUT_QUART = f -> {
        return 1.0f - MathUtil.fastPow(1.0f - f, 4.0f);
    };
    public static final EasingFunction EASE_IN_OUT_QUART = f -> {
        return f < 0.5f ? 8.0f * MathUtil.fastPow(f, 4.0f) : 1.0f - (MathUtil.fastPow(((-2.0f) * f) + 2.0f, 4.0f) / 2.0f);
    };
    public static final EasingFunction EASE_IN_QUINT = f -> {
        return MathUtil.fastPow(f, 5.0f);
    };
    public static final EasingFunction EASE_OUT_QUINT = f -> {
        return 1.0f - MathUtil.fastPow(1.0f - f, 5.0f);
    };
    public static final EasingFunction EASE_IN_OUT_QUINT = f -> {
        return f < 0.5f ? 16.0f * MathUtil.fastPow(f, 5.0f) : 1.0f - (MathUtil.fastPow(((-2.0f) * f) + 2.0f, 5.0f) / 2.0f);
    };

    public Easings() {
    }
}
