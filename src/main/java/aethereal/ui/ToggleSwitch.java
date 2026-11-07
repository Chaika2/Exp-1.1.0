package aethereal.ui;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.model.InputEventContext;
import aethereal.model.LayoutScaleContext;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.util.WeightedEngine;

import java.util.function.Supplier;

public class ToggleSwitch extends AbstractWidget {
    public final float switchWidth;
    public final float switchHeight;
    public final Supplier<Boolean> stateSupplier;
    public Runnable clickRunnable;
    public final ClickableBehavior clickable = new ClickableBehavior();
    public final ToggleAnimator toggleSwitchAnimation = new ToggleAnimator(220, Easings.EASE_IN_OUT_CUBIC);
    public boolean lastState = false;

    public ToggleSwitch(float f, float f2, Supplier<Boolean> supplier) {
        this.switchWidth = f;
        this.switchHeight = f2;
        this.stateSupplier = supplier;
        this.clickable.clickCallback(this::invert);
    }

    @Override
    public void render(DrawCtx class699Var) {
        boolean zBooleanValue = this.stateSupplier.get().booleanValue();
        if (zBooleanValue != this.lastState) {
            this.toggleSwitchAnimation.state(zBooleanValue);
            this.lastState = zBooleanValue;
        }
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        int i = class115VarColorStack.toggleDarkenedInterpolateColor(class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(500).argb()), class115VarColorStack.computeColor(class764VarPalette.accent().argb()), 0.05f, this.clickable.hoverAnimation(), this.toggleSwitchAnimation);
        class699Var.fillOutlinedRoundedRect(x(), y(), this.switchWidth, this.switchHeight, 9.0f, 2.5f, class115VarColorStack.toggleDarkenedInterpolateColor(class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(300).argb()), class115VarColorStack.computeColor(class764VarPalette.accentBright().argb()), 0.05f, this.clickable.hoverAnimation(), this.toggleSwitchAnimation), i);
        float f = (this.switchHeight - (2.0f * 3.0f)) / 2.0f;
        class699Var.circle(x() + 3.0f + f + (((this.switchWidth - (2.0f * 3.0f)) - (2.0f * f)) * this.toggleSwitchAnimation.smoothAnimation()), y() + (this.switchHeight / 2.0f), f, class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(200).argb()), class115VarColorStack.white(), this.toggleSwitchAnimation));
    }

    public void invert() {
        if (this.clickRunnable != null) {
            this.clickRunnable.run();
        }
    }

    public void setClickableArea(float f, float f2, float f3, float f4) {
        this.clickable.setDimensions(f, f2, f3, f4);
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        setSize(this.switchWidth, this.switchHeight);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.toggleSwitchAnimation.animate(class141Var);
        this.clickable.animate(class141Var);
        super.animation(class141Var);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        boolean zHandleInput = super.handleInput(class688Var, z);
        if (!zHandleInput) {
            zHandleInput = this.clickable.handleInput(class688Var, z);
        }
        return zHandleInput;
    }

    public ToggleAnimator toggleSwitchAnimation() {
        return this.toggleSwitchAnimation;
    }

    public ToggleSwitch runnable(Runnable runnable) {
        this.clickRunnable = runnable;
        return this;
    }
}
