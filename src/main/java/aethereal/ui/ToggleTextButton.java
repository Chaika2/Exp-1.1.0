package aethereal.ui;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.render.GlTexture;
import aethereal.model.InputEventContext;
import aethereal.model.LayoutScaleContext;
import aethereal.render.MsdfFont;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.model.Translation;
import aethereal.util.WeightedEngine;

public class ToggleTextButton extends AbstractWidget {
    public final MsdfFont font;
    public final GlTexture icon;
    public final float cornerRadius;
    public final float paddingX;
    public final float paddingY;
    public final int iconSize;
    public boolean active;
    public final ClickableBehavior clickable = new ClickableBehavior();
    public Translation labelText = Translation.clearText("");
    public int textSize = 12;
    public final ToggleAnimator activeAnimator = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);

    public ToggleTextButton(Runnable runnable, MsdfFont class161Var, GlTexture class073Var, float f, float f2, float f3, int i) {
        this.font = class161Var;
        this.icon = class073Var;
        this.cornerRadius = f;
        this.paddingX = f2;
        this.paddingY = f3;
        this.iconSize = i;
        this.clickable.clickCallback(() -> {
            toggle();
            if (runnable != null) {
                runnable.run();
            }
        });
    }

    public ToggleTextButton label(Translation class254Var, int i) {
        this.labelText = class254Var;
        this.textSize = i;
        return this;
    }

    public void setActive(boolean z) {
        if (this.active == z) {
            return;
        }
        this.active = z;
    }

    public void toggle() {
        setActive(!this.active);
    }

    @Override
    public void render(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        float fWidth = width();
        float fHeight = height();
        ToggleAnimator class323VarHoverAnimation = this.clickable.hoverAnimation();
        ToggleAnimator class323Var = this.activeAnimator;
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(700).argb(), 0.0f), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(700).argb(), 140), class323VarHoverAnimation), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(700).argb()), class323Var, class323Var);
        int iInterpolate2 = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(400).argb()), class323Var, class323Var);
        class699Var.fillOutlinedRoundedRect(x(), y(), fWidth, fHeight, this.cornerRadius, 2.5f, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb()), iInterpolate);
        class699Var.texture(this.icon, x() + this.paddingX, (y() + (fHeight / 2.0f)) - (this.iconSize / 2), this.iconSize, this.iconSize, class115VarColorStack.computeColor(iInterpolate2));
        class699Var.text(this.font, this.labelText.effective(), this.textSize, x() + this.paddingX + this.iconSize + 5.0f, (y() + (fHeight / 2.0f)) - (this.font.getHeight(this.textSize) / 2.0f), iInterpolate2);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        return this.clickable.handleInput(class688Var, z);
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        setSize(this.iconSize + 5.0f + class698Var.textWidthPhysical(this.font, this.labelText.effective(), this.textSize) + (this.paddingX * 2.0f), this.iconSize + this.font.metrics().lineHeight() + (this.paddingY * 2.0f));
        this.clickable.setDimensions(x(), y(), width(), height());
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.clickable.animate(class141Var);
        this.activeAnimator.state(this.active).animate(class141Var);
    }

    public boolean isActive() {
        return this.active;
    }
}
