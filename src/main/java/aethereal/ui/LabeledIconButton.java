package aethereal.ui;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.render.GlTexture;
import aethereal.model.InputEventContext;
import aethereal.model.LayoutScaleContext;
import aethereal.render.MsdfFont;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.model.Translation;
import aethereal.util.WeightedEngine;

public class LabeledIconButton extends AbstractWidget {
    public final MsdfFont font;
    public final GlTexture icon;
    public final float cornerRadius;
    public final float paddingX;
    public final float paddingY;
    public final int iconSize;
    public final Translation label;
    public final ClickableBehavior clickBehavior = new ClickableBehavior();
    public final int textSize = 12;

    public LabeledIconButton(Translation class254Var, Runnable runnable, MsdfFont class161Var, GlTexture class073Var, float f, float f2, float f3, int i) {
        this.label = class254Var;
        this.font = class161Var;
        this.icon = class073Var;
        this.cornerRadius = f;
        this.paddingX = f2;
        this.paddingY = f3;
        this.iconSize = i;
        this.clickBehavior.clickCallback(() -> {
            if (runnable != null) {
                runnable.run();
            }
        });
    }

    @Override
    public void render(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        float fWidth = width();
        float fHeight = height();
        ToggleAnimator class323VarHoverAnimation = this.clickBehavior.hoverAnimation();
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(700).argb(), 0.0f), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(700).argb(), 140), class323VarHoverAnimation);
        int iInterpolate2 = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(400).argb()), class323VarHoverAnimation);
        class699Var.fillOutlinedRoundedRect(x(), y(), fWidth, fHeight, this.cornerRadius, 2.5f, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb()), iInterpolate);
        class699Var.texture(this.icon, x() + this.paddingX, (y() + (fHeight / 2.0f)) - (this.iconSize / 2), this.iconSize, this.iconSize, class115VarColorStack.computeColor(iInterpolate2));
        class699Var.text(this.font, this.label.effective(), 12, x() + this.paddingX + this.iconSize + 5.0f, (y() + (fHeight / 2.0f)) - (this.font.getHeight(12.0f) / 2.0f), iInterpolate2);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        return this.clickBehavior.handleInput(class688Var, z);
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        setSize(this.iconSize + 5.0f + class698Var.textWidthPhysical(this.font, this.label.effective(), 12) + (this.paddingX * 2.0f), Math.max(this.iconSize, this.font.metrics().lineHeight()) + (this.paddingY * 2.0f));
        this.clickBehavior.setDimensions(x(), y(), width(), height());
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.clickBehavior.animate(class141Var);
    }
}
