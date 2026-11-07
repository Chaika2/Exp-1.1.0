package aethereal.render;
import aethereal.math.EasingFunction;
import aethereal.math.Easings;
import aethereal.util.MathUtil;
import aethereal.util.WeightedEngine;

public class ToggleAnimator {
    public static final int defaultDuration = 220;
    public final EasingFunction easeIn;
    public final EasingFunction easeOut;
    public final float speed;

    public boolean state;
    public float animation;
    public float smoothAnimation;
    public float maxValueScale;

    public ToggleAnimator(EasingFunction class356Var) {
        this(defaultDuration, class356Var, class356Var);
    }

    public ToggleAnimator(int i, EasingFunction class356Var) {
        this(i, class356Var, class356Var);
    }

    public ToggleAnimator(int i, EasingFunction class356Var, EasingFunction class356Var2) {
        this.maxValueScale = 1.0f;
        this.easeIn = class356Var;
        this.easeOut = class356Var2;
        this.speed = 1000.0f / i;
    }

    public void animate(WeightedEngine class141Var) {
        float fWeight = class141Var.weight() * this.speed;
        float fAnimationMultiplier = class141Var.engine().animationMultiplier();
        this.animation = MathUtil.clamp(this.animation + (this.state ? fWeight : -fWeight), 0.0f, 1.0f);
        float fEase = (this.state ? this.easeIn : this.easeOut).ease(this.state ? this.animation : 1.0f - this.animation);
        this.smoothAnimation = (this.state ? fEase : 1.0f - fEase) * fAnimationMultiplier * this.maxValueScale;
    }

    public void fill() {
        this.animation = 1.0f;
    }

    public void force(boolean z) {
        this.state = z;
        this.animation = z ? 1.0f : 0.0f;
        this.smoothAnimation = (z ? 1.0f : 0.0f) * this.maxValueScale;
    }

    public void invert() {
        this.state = !this.state;
    }

    public float interpolate(float f, float f2) {
        return f + ((f2 - f) * this.smoothAnimation);
    }

    public boolean finished() {
        return isZero() || isOne();
    }

    public boolean isZero() {
        return this.animation == 0.0f;
    }

    public boolean isOne() {
        return this.animation == 1.0f;
    }

    public static ToggleAnimator times(int i) {
        return times(i, Easings.EASE_IN_OUT_CUBIC);
    }

    public static ToggleAnimator times(int i, int i2) {
        return times(i, i2, Easings.EASE_IN_OUT_CUBIC);
    }

    public static ToggleAnimator times(int i, EasingFunction class356Var) {
        return new ToggleAnimator((defaultDuration / i) * i, class356Var).maxValue(i);
    }

    public static ToggleAnimator times(int i, int i2, EasingFunction class356Var) {
        return new ToggleAnimator(i2 * i, class356Var).maxValue(i);
    }

    public static ToggleAnimator times(int i, int i2, EasingFunction class356Var, EasingFunction class356Var2) {
        return new ToggleAnimator(i2 * i, class356Var, class356Var2).maxValue(i);
    }

    public ToggleAnimator state(boolean z) {
        this.state = z;
        return this;
    }

    public boolean state() {
        return this.state;
    }

    public ToggleAnimator animation(float f) {
        this.animation = f;
        return this;
    }

    public float animation() {
        return this.animation;
    }

    public float smoothAnimation() {
        return this.smoothAnimation;
    }

    public ToggleAnimator maxValue(float f) {
        this.maxValueScale = f;
        return this;
    }
}
