package aethereal.ui;
import aethereal.render.AnimatedFloat;
import aethereal.resource.ClasspathResource;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.render.GlTexture;
import aethereal.event.InputEvent;
import aethereal.model.InputEventContext;
import aethereal.model.KeyInput;
import aethereal.type.KeybindCaptureState;
import aethereal.render.KeybindColors;
import aethereal.model.LayoutScaleContext;
import aethereal.model.MouseButtonInput;
import aethereal.render.MsdfFont;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.util.WeightedEngine;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class KeybindField extends AbstractWidget {
    public final MsdfFont font;
    public final float paddingX;
    public final float paddingY;
    public final float cornerRadius;
    public final int fontSize;
    public boolean initialized;
    public final ClickableBehavior clickBehavior = new ClickableBehavior();
    public final AnimatedFloat widthAnimation = new AnimatedFloat(200, Easings.EASE_IN_OUT_CUBIC);
    public final GlTexture keyboardIcon = new GlTexture(new ClasspathResource("/icons/menu/new/keyboard.png"));
    public final KeybindCaptureState captureState = new KeybindCaptureState();
    public Supplier<Float> rawXSupplier;

    public KeybindColors colorsOverride;

    public KeybindField(MsdfFont class161Var, Runnable runnable, float f, float f2, float f3, int i) {
        this.font = class161Var;
        this.paddingX = f;
        this.paddingY = f2;
        this.cornerRadius = f3;
        this.fontSize = i;
        if (runnable != null) {
            this.clickBehavior.rightClickCallback(runnable);
        }
        ClickableBehavior class766Var = this.clickBehavior;
        KeybindCaptureState class770Var = this.captureState;
        Objects.requireNonNull(class770Var);
        class766Var.clickCallback(class770Var::toggleCapture);
    }

    public void onChange(Consumer<List<Integer>> consumer) {
        this.captureState.onChange(consumer);
    }

    public KeybindField addMutableKeys(List<Integer> list) {
        this.captureState.addMutableKeys(list);
        return this;
    }

    public void syncKeys(List<Integer> list) {
        this.captureState.syncKeys(list);
    }

    public void addImmutableKeys(List<Integer> list) {
        this.captureState.addImmutableKeys(list);
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        float fComputeDesiredHeight = computeDesiredHeight();
        float fComputeDesiredWidth = computeDesiredWidth(class698Var);
        if (!this.initialized) {
            this.widthAnimation.set(fComputeDesiredWidth);
            this.initialized = true;
        }
        this.widthAnimation.destination(fComputeDesiredWidth);
        setSize(this.widthAnimation.animatedValue(), fComputeDesiredHeight);
        this.clickBehavior.setDimensions(x(), y(), width(), height());
        this.keyboardIcon.setDimensions(12, 12);
    }

    @Override
    public void render(DrawCtx class699Var) {
        int iTextEmpty;
        float fWidth;
        float fX;
        if (this.captureState.captureKey()) {
            class699Var.window().interceptKeyboard(true);
        }
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        String bindingText = this.captureState.formatBindingText();
        float fTextWidthPhysical = class699Var.textWidthPhysical(this.font, bindingText, this.fontSize);
        float height = this.font.getHeight(this.fontSize);
        KeybindColors class744VarFromPalette = this.colorsOverride != null ? this.colorsOverride : KeybindColors.fromPalette(class764VarPalette, class115VarColorStack);
        ToggleAnimator class323VarHoverAnimation = this.clickBehavior.hoverAnimation();
        int iBrightenedInterpolatedColor = class115VarColorStack.brightenedInterpolatedColor(class744VarFromPalette.outline(), 0.05f, class323VarHoverAnimation);
        int iBrightenedInterpolatedColor2 = class115VarColorStack.brightenedInterpolatedColor(class744VarFromPalette.background(), 0.05f, class323VarHoverAnimation);
        class699Var.fillOutlinedRoundedRect(x(), y(), width(), height(), this.cornerRadius, 2.5f, iBrightenedInterpolatedColor, iBrightenedInterpolatedColor2);
        float fY = (y() + (height() / 2.0f)) - (height / 2.0f);
        float fX2 = x() + this.widthAnimation.animatedValue();
        if (this.captureState.hasAnyKeys()) {
            float fComputeDesiredWidth = computeDesiredWidth(class699Var.layoutContext());
            iTextEmpty = class744VarFromPalette.text();
            fWidth = ((this.rawXSupplier != null ? this.rawXSupplier.get().floatValue() : x()) + (fComputeDesiredWidth / 2.0f)) - (fTextWidthPhysical / 2.0f);
            fX = (x() + (width() / 2.0f)) - (fTextWidthPhysical / 2.0f);
        } else {
            iTextEmpty = class744VarFromPalette.textEmpty();
            float fX3 = x() + this.paddingX;
            class699Var.texture(this.keyboardIcon, fX3, (y() + (height() / 2.0f)) - (this.keyboardIcon.height() / 2), this.keyboardIcon.width(), this.keyboardIcon.height(), iTextEmpty);
            fWidth = fX3 + this.keyboardIcon.width() + 6.0f;
            fX = fWidth;
        }
        if (fX + fTextWidthPhysical <= fX2 - 5.0f) {
            class699Var.text(this.font, bindingText, this.fontSize, fWidth, fY, iTextEmpty);
            return;
        }
        class699Var.drawEngine().beginStencil();
        class699Var.fillOutlinedRoundedRect(x(), y(), width(), height(), this.cornerRadius, 2.5f, iBrightenedInterpolatedColor, iBrightenedInterpolatedColor2);
        class699Var.drawEngine().prepareStencil(1);
        class699Var.text(this.font, bindingText, this.fontSize, fWidth, fY, iTextEmpty);
        class699Var.drawEngine().endStencil();
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.widthAnimation.animate(class141Var);
        this.clickBehavior.animate(class141Var);
        super.animation(class141Var);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        boolean zHandleInput = super.handleInput(class688Var, z);
        if (!z) {
            if (this.captureState.captureKey()) {
                InputEvent class691VarInputEvent = class688Var.inputEvent();
                if (class691VarInputEvent instanceof MouseButtonInput) {
                    MouseButtonInput class693Var = (MouseButtonInput) class691VarInputEvent;
                    if (class693Var.action().press() && class693Var.button() == 0 && !class688Var.inArea(x(), y(), width(), height())) {
                        this.captureState.toggleCapture();
                        zHandleInput = true;
                    }
                }
            }
            boolean zHandleInput2 = zHandleInput | this.clickBehavior.handleInput(class688Var, zHandleInput);
            if (!zHandleInput2) {
                InputEvent class691VarInputEvent2 = class688Var.inputEvent();
                if (class691VarInputEvent2 instanceof KeyInput) {
                    zHandleInput2 = this.captureState.handleKeyInput((KeyInput) class691VarInputEvent2);
                }
                if (class691VarInputEvent2 instanceof MouseButtonInput) {
                    zHandleInput2 = this.captureState.handleMouseInput((MouseButtonInput) class691VarInputEvent2);
                }
            }
            zHandleInput = zHandleInput2 | (this.captureState.captureKey() && !zHandleInput2);
        }
        return zHandleInput;
    }

    public float computeDesiredWidth(LayoutScaleContext class698Var) {
        float fTextWidthPhysical = class698Var.textWidthPhysical(this.font, this.captureState.formatBindingText(), Math.max(1, Math.round(this.fontSize * class698Var.scaleFactor()))) / class698Var.scaleFactor();
        if (this.captureState.hasAnyKeys()) {
            return fTextWidthPhysical + (this.paddingX * 2.0f);
        }
        return (this.paddingX * 2.0f) + this.keyboardIcon.width() + 6.0f + fTextWidthPhysical;
    }

    public float computeDesiredHeight() {
        float height = this.font.getHeight(this.fontSize);
        return (this.captureState.hasAnyKeys() ? height : Math.max(this.keyboardIcon.height(), height)) + (this.paddingY * 2.0f);
    }

    public KeybindField rawX(Supplier<Float> supplier) {
        this.rawXSupplier = supplier;
        return this;
    }

    public KeybindField colors(KeybindColors class744Var) {
        this.colorsOverride = class744Var;
        return this;
    }
}
