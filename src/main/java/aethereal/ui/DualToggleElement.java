package aethereal.ui;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.render.Fonts;
import aethereal.render.HighlightAnimation;
import aethereal.model.InputEventContext;
import aethereal.model.LayoutScaleContext;
import aethereal.render.MsdfFont;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.model.Translation;
import aethereal.util.WeightedEngine;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DualToggleElement extends ModuleFrame {
    static final int LABEL_SIZE = 14;
    static final int BUTTON_TEXT_SIZE = 9;
    static final float TOGGLE_HEIGHT = 22.0f;
    public final Translation labelText;
    public final Translation leftText;
    public final Translation rightText;
    public Consumer<Boolean> onToggleCallback;
    public Supplier<Boolean> visibleSupplier;
    public final MsdfFont labelFont = Fonts.INTER_SEMIBOLD.get();
    public final MsdfFont buttonFont = Fonts.INTER_EXTRA_BOLD.get();
    public final HighlightAnimation highlightAnimation = new HighlightAnimation(300, Easings.EASE_IN_OUT_CUBIC);
    public final ClickableBehavior clickBehavior = new ClickableBehavior();
    public final ToggleAnimator toggleAnimation = new ToggleAnimator(220, Easings.EASE_IN_OUT_CUBIC);
    public boolean leftActive = false;

    public DualToggleElement(Translation class254Var, Translation class254Var2, Translation class254Var3) {
        this.labelText = class254Var;
        this.leftText = class254Var2;
        this.rightText = class254Var3;
        this.clickBehavior.clickCallback(this::toggle);
    }

    @Override
    public void highlight() {
        this.highlightAnimation.trigger();
    }

    @Override
    public void clearHighlight() {
        this.highlightAnimation.clearHighlight();
    }

    @Override
    public void draw(DrawCtx class699Var, float f, float f2) {
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        float fX = (x() + f) - 110.0f;
        float fHeight = height() / 2.0f;
        float fY = y();
        class699Var.text(this.labelFont, this.labelText.effective(), LABEL_SIZE, x(), (y() + fHeight) - (this.labelFont.getHeight(14.0f) / 2.0f), class115VarColorStack.computeColor(class764VarPalette.text().tone(200).argb()));
        float f3 = 110.0f / 2.0f;
        class699Var.fillRoundedRect(fX + (f3 * (1.0f - this.toggleAnimation.smoothAnimation())), fY, f3, TOGGLE_HEIGHT, 5.0f, class115VarColorStack.computeColor(class764VarPalette.accent().argb()));
        class699Var.fillOutlinedRoundedRect(fX, fY, 110.0f, TOGGLE_HEIGHT, 6.0f, 2.5f, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(400).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb(), 0));
        float fTextWidthPhysical = class699Var.textWidthPhysical(this.buttonFont, this.leftText.effective(), BUTTON_TEXT_SIZE);
        float fTextWidthPhysical2 = class699Var.textWidthPhysical(this.buttonFont, this.rightText.effective(), BUTTON_TEXT_SIZE);
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()), class115VarColorStack.white(), this.toggleAnimation.smoothAnimation());
        int iInterpolate2 = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()), class115VarColorStack.white(), 1.0f - this.toggleAnimation.smoothAnimation());
        class699Var.text(this.buttonFont, this.leftText.effective(), BUTTON_TEXT_SIZE, (fX + (f3 / 2.0f)) - (fTextWidthPhysical / 2.0f), (fY + 11.0f) - (this.buttonFont.getHeight(9.0f) / 2.0f), iInterpolate);
        class699Var.text(this.buttonFont, this.rightText.effective(), BUTTON_TEXT_SIZE, ((fX + f3) + (f3 / 2.0f)) - (fTextWidthPhysical2 / 2.0f), (fY + 11.0f) - (this.buttonFont.getHeight(9.0f) / 2.0f), iInterpolate2);
        this.clickBehavior.setDimensions(fX, fY, 110.0f, TOGGLE_HEIGHT);
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        return this.clickBehavior.handleInput(class688Var, z);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.clickBehavior.animate(class141Var);
        this.toggleAnimation.animate(class141Var);
        super.animation(class141Var);
    }

    @Override
    public float height() {
        return Math.max(this.labelFont.getHeight(14.0f), TOGGLE_HEIGHT);
    }

    public void toggle() {
        this.leftActive = !this.leftActive;
        this.toggleAnimation.state(this.leftActive);
        if (this.onToggleCallback != null) {
            this.onToggleCallback.accept(Boolean.valueOf(this.leftActive));
        }
    }

    public void setState(boolean z) {
        this.leftActive = z;
        this.toggleAnimation.state(z);
    }

    public boolean isLeftActive() {
        return this.leftActive;
    }

    public void setOnToggleCallback(Consumer<Boolean> consumer) {
        this.onToggleCallback = consumer;
    }

    public void setVisibleSupplier(Supplier<Boolean> supplier) {
        this.visibleSupplier = supplier;
    }
}
