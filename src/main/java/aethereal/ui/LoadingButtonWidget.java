package aethereal.ui;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.model.InputEventContext;
import aethereal.model.LayoutScaleContext;
import aethereal.render.MsdfFont;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.model.Translation;
import aethereal.util.WeightedEngine;

import java.util.function.BooleanSupplier;
import org.joml.Quaternionf;

public class LoadingButtonWidget extends AbstractWidget {
    public Translation text;
    public final float cornerRadius;
    public final float buttonWidth;
    public final float buttonHeight;
    public final boolean outlined;
    public final MsdfFont font;
    public final ClickableBehavior clickBehavior;
    public final ToggleAnimator disabledAnimation;
    public final ToggleAnimator loadingAnimation;
    public GlTexture icon;
    public int iconSize;
    public GlTexture loadingIcon;
    public Translation loadingText;
    public boolean loading;
    public float spinnerAngle;
    public int baseColor;
    public int textColor;
    public int outlineColor;
    public BooleanSupplier enabledSupplier;

    public LoadingButtonWidget(Translation class254Var, int i, int i2, Runnable runnable, float f, boolean z) {
        this(null, 0, class254Var, i, i2, runnable, f, z);
    }

    public LoadingButtonWidget(GlTexture class073Var, int i, Translation class254Var, int i2, int i3, Runnable runnable, float f, boolean z) {
        this.font = Fonts.INTER_SEMIBOLD.get();
        this.clickBehavior = new ClickableBehavior();
        this.disabledAnimation = new ToggleAnimator(220, Easings.EASE_IN_OUT_CUBIC);
        this.loadingAnimation = new ToggleAnimator(220, Easings.EASE_IN_OUT_CUBIC);
        this.loadingText = Translation.clearText("Загрузка...");
        this.loading = false;
        this.spinnerAngle = 0.0f;
        this.baseColor = 268435455;
        this.textColor = 268435455;
        this.outlineColor = 16777215;
        this.enabledSupplier = () -> {
            return true;
        };
        this.clickBehavior.clickCallback(runnable);
        this.text = class254Var;
        this.cornerRadius = f;
        this.outlined = z;
        this.buttonWidth = i2;
        this.buttonHeight = i3;
        this.icon = class073Var;
        this.iconSize = i;
    }

    public void primary(GlTexture class073Var, int i, Translation class254Var) {
        this.icon = class073Var;
        this.iconSize = i;
        this.text = class254Var;
        if (this.icon != null) {
            this.icon.setDimensions(this.iconSize, this.iconSize);
        }
    }

    public void loadingVisuals(GlTexture class073Var, Translation class254Var) {
        this.loadingIcon = class073Var;
        if (this.loadingIcon != null && this.iconSize > 0) {
            this.loadingIcon.setDimensions(this.iconSize, this.iconSize);
        }
        if (class254Var != null) {
            this.loadingText = class254Var;
        }
    }

    public void loading(boolean z) {
        this.loading = z;
        this.loadingAnimation.state(z);
    }

    @Override
    public void render(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        float fSmoothAnimation = this.disabledAnimation.smoothAnimation();
        float fSmoothAnimation2 = this.loadingAnimation.smoothAnimation();
        int iComputeColor = class115VarColorStack.computeColor(this.baseColor);
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.interpolate(iComputeColor, class115VarColorStack.autoBrightenDarken(iComputeColor, 0.05f), this.clickBehavior.hoverAnimation()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(600).argb(), 0.55f), fSmoothAnimation);
        int iInterpolate2 = class115VarColorStack.interpolate(class115VarColorStack.computeColor(this.outlineColor), class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(400).argb(), 0.55f), fSmoothAnimation);
        class699Var.fillOutlinedRoundedRect(x(), y(), this.buttonWidth, this.buttonHeight, this.cornerRadius, this.outlined ? 2.5f : 0.0f, class115VarColorStack.interpolate(iInterpolate2, class115VarColorStack.autoBrightenDarken(iInterpolate2, 0.05f), this.clickBehavior.hoverAnimation()), iInterpolate);
        float fX = x() + (this.buttonWidth / 2.0f);
        float fY = y() + (this.buttonHeight / 2.0f);
        int iComputeColor2 = class115VarColorStack.computeColor(this.textColor);
        int iComputeColor3 = class115VarColorStack.computeColor(class764VarPalette.text().tone(400).argb(), 0.55f);
        GlTexture class073Var = this.icon;
        GlTexture class073Var2 = this.loadingIcon != null ? this.loadingIcon : this.icon;
        float fX2 = x() + 10.0f;
        if (class073Var != null) {
            class115VarColorStack.push();
            class115VarColorStack.alpha(1.0f - fSmoothAnimation2);
            class699Var.textureVerticalC(class073Var, fX2, fY, class073Var.width(), class073Var.height(), class115VarColorStack.interpolate(iComputeColor2, class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb(), 0.55f), fSmoothAnimation));
            class115VarColorStack.pop();
        }
        if (class073Var2 != null) {
            class115VarColorStack.push();
            class115VarColorStack.alpha(fSmoothAnimation2);
            class699Var.matrixStack().push();
            float physical = class699Var.layoutContext().toPhysical(fX2 + (class073Var2.width() / 2.0f));
            float physical2 = class699Var.layoutContext().toPhysical(fY);
            class699Var.matrixStack().translate(physical, physical2, 0.0f);
            class699Var.matrixStack().multiply(new Quaternionf().rotateZ((float) Math.toRadians(this.spinnerAngle)));
            class699Var.matrixStack().translate(-physical, -physical2, 0.0f);
            class699Var.textureVerticalC(class073Var2, fX2, fY, class073Var2.width(), class073Var2.height(), class115VarColorStack.interpolate(iComputeColor2, class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb(), 0.55f), fSmoothAnimation));
            class699Var.matrixStack().pop();
            class115VarColorStack.pop();
        }
        String strEffective = this.text.effective();
        String strEffective2 = this.loadingText.effective();
        float fTextWidthPhysical = class699Var.textWidthPhysical(this.font, strEffective, 12);
        float fTextWidthPhysical2 = class699Var.textWidthPhysical(this.font, strEffective2, 12);
        float height = this.font.getHeight(12.0f);
        class115VarColorStack.push();
        class115VarColorStack.alpha(1.0f - fSmoothAnimation2);
        class699Var.text(this.font, strEffective, 12, fX - (fTextWidthPhysical / 2.0f), fY - (height / 2.0f), class115VarColorStack.interpolate(iComputeColor2, iComputeColor3, fSmoothAnimation));
        class115VarColorStack.pop();
        class115VarColorStack.push();
        class115VarColorStack.alpha(fSmoothAnimation2);
        class699Var.text(this.font, strEffective2, 12, fX - (fTextWidthPhysical2 / 2.0f), fY - (height / 2.0f), class115VarColorStack.interpolate(iComputeColor2, iComputeColor3, fSmoothAnimation));
        class115VarColorStack.pop();
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        if (!this.enabledSupplier.getAsBoolean() || this.loading) {
            return false;
        }
        return this.clickBehavior.handleInput(class688Var, z);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.disabledAnimation.state(!this.enabledSupplier.getAsBoolean());
        this.disabledAnimation.animate(class141Var);
        this.loadingAnimation.state(this.loading);
        this.loadingAnimation.animate(class141Var);
        this.clickBehavior.animate(class141Var);
        if (this.loadingAnimation.smoothAnimation() <= 0.001f) {
            this.spinnerAngle = 0.0f;
            return;
        }
        this.spinnerAngle += class141Var.weight() * 360.0f;
        if (this.spinnerAngle >= 360.0f) {
            this.spinnerAngle -= 360.0f;
        }
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        setSize(this.buttonWidth, this.buttonHeight);
        if (this.icon != null) {
            this.icon.setDimensions(this.iconSize, this.iconSize);
        }
        if (this.loadingIcon != null) {
            this.loadingIcon.setDimensions(this.iconSize, this.iconSize);
        }
        this.clickBehavior.setDimensions(x(), y(), width(), height());
    }

    public LoadingButtonWidget text(Translation class254Var) {
        this.text = class254Var;
        return this;
    }

    public void enabled(BooleanSupplier booleanSupplier) {
        this.enabledSupplier = booleanSupplier;
    }

    public Translation text() {
        return this.text;
    }

    public boolean loading() {
        return this.loading;
    }

    public int baseColor() {
        return this.baseColor;
    }

    public LoadingButtonWidget baseColor(int i) {
        this.baseColor = i;
        return this;
    }

    public int textColor() {
        return this.textColor;
    }

    public LoadingButtonWidget textColor(int i) {
        this.textColor = i;
        return this;
    }

    public int outlineColor() {
        return this.outlineColor;
    }

    public LoadingButtonWidget outlineColor(int i) {
        this.outlineColor = i;
        return this;
    }

    public BooleanSupplier enabledSupplier() {
        return this.enabledSupplier;
    }

    public LoadingButtonWidget enabledSupplier(BooleanSupplier booleanSupplier) {
        this.enabledSupplier = booleanSupplier;
        return this;
    }
}
