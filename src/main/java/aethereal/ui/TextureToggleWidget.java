package aethereal.ui;
import aethereal.render.ColorStack;
import aethereal.render.ColorValue;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.render.GlTexture;
import aethereal.model.InputEventContext;
import aethereal.model.LayoutScaleContext;
import aethereal.util.StencilBufferUtil;
import aethereal.render.ToggleAnimator;
import aethereal.util.WeightedEngine;

import java.util.function.Supplier;

public class TextureToggleWidget extends AbstractWidget {
    public final GlTexture inactiveTexture;
    public final GlTexture activeTexture;
    public final float textureWidth;
    public final float textureHeight;
    public final Supplier<Boolean> stateSupplier;
    public ColorValue inactiveColorValue;

    public ColorValue activeColorValue;
    public Runnable clickRunnable;
    public final ClickableBehavior clickable = new ClickableBehavior();
    public boolean lastState = false;
    public float hitboxPaddingValue = 0.0f;
    public final ToggleAnimator toggleAnimator = new ToggleAnimator(Easings.EASE_IN_OUT_CUBIC);

    public TextureToggleWidget(GlTexture class073Var, GlTexture class073Var2, Supplier<Boolean> supplier, float f, float f2) {
        this.inactiveTexture = class073Var;
        this.activeTexture = class073Var2;
        this.textureWidth = f;
        this.textureHeight = f2;
        this.stateSupplier = supplier;
        this.clickable.clickCallback(() -> {
            if (this.clickRunnable != null) {
                this.clickRunnable.run();
            }
        });
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        setSize(this.textureWidth, this.textureHeight);
        float f = this.hitboxPaddingValue;
        float f2 = this.hitboxPaddingValue;
        this.clickable.setDimensions(x() - f, y() - f2, width() + (f * 2.0f), height() + (f2 * 2.0f));
    }

    @Override
    public void render(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        boolean zBooleanValue = this.stateSupplier.get().booleanValue();
        if (zBooleanValue != this.lastState) {
            this.toggleAnimator.state(zBooleanValue);
            this.lastState = zBooleanValue;
        }
        if (this.inactiveColorValue == null) {
            this.inactiveColorValue = class699Var.theme().palette().text().tone(500);
        }
        if (this.activeColorValue == null) {
            this.activeColorValue = class699Var.theme().palette().text().tone(100);
        }
        int iComputeColor = class115VarColorStack.computeColor(this.inactiveColorValue.argb());
        int iComputeColor2 = class115VarColorStack.computeColor(this.activeColorValue.argb());
        int iBrightenedInterpolatedColor = class115VarColorStack.brightenedInterpolatedColor(iComputeColor, 0.1f, this.clickable.hoverAnimation());
        int iDarkenedInterpolatedColor = class115VarColorStack.darkenedInterpolatedColor(iComputeColor2, 0.1f, this.clickable.hoverAnimation());
        float fSmoothAnimation = this.toggleAnimator.smoothAnimation();
        int iMethod001 = applyAlpha(iBrightenedInterpolatedColor, 1.0f - fSmoothAnimation);
        int iMethod002 = applyAlpha(iDarkenedInterpolatedColor, fSmoothAnimation);
        class699Var.texture(this.inactiveTexture, x(), y(), this.textureWidth, this.textureHeight, iMethod001);
        class699Var.texture(this.activeTexture, x(), y(), this.textureWidth, this.textureHeight, iMethod002);
    }

    public static int applyAlpha(int i, float f) {
        return (i & 16777215) | (((int) (((i >>> 24) & StencilBufferUtil.STENCIL_MASK) * Math.max(0.0f, Math.min(1.0f, f)))) << 24);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        return this.clickable.handleInput(class688Var, z);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.toggleAnimator.animate(class141Var);
        this.clickable.animate(class141Var);
        super.animation(class141Var);
    }

    public TextureToggleWidget inactiveColor(ColorValue class761Var) {
        this.inactiveColorValue = class761Var;
        return this;
    }

    public TextureToggleWidget activeColor(ColorValue class761Var) {
        this.activeColorValue = class761Var;
        return this;
    }

    public TextureToggleWidget hitboxPadding(float f) {
        this.hitboxPaddingValue = f;
        return this;
    }

    public TextureToggleWidget runnable(Runnable runnable) {
        this.clickRunnable = runnable;
        return this;
    }
}
