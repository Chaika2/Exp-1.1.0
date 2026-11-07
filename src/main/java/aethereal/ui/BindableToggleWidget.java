package aethereal.ui;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.event.InputEvent;
import aethereal.model.InputEventContext;
import aethereal.model.KeyInput;
import aethereal.render.KeybindColors;
import aethereal.model.LayoutScaleContext;
import aethereal.render.MsdfFont;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.util.WeightedEngine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BindableToggleWidget extends AbstractWidget {
    public final MsdfFont font;
    public final GlTexture icon;
    public final float cornerRadius;
    public final float horizontalPadding;
    public final float verticalPadding;
    public final int iconSize;
    public final Runnable onToggleCallback;
    public boolean active;
    public final KeybindField keybindField;
    public final ClickableBehavior clickable = new ClickableBehavior();
    public String text = "";
    public int textSize = 12;
    public final ToggleAnimator toggleAnimator = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);
    public boolean bindingEditable = true;
    public List<Integer> mutableKeys = new ArrayList();
    public List<Integer> immutableKeys = new ArrayList();

    public final Set<Integer> pressedKeys = new HashSet();

    public BindableToggleWidget(Runnable runnable, MsdfFont class161Var, GlTexture class073Var, float f, float f2, float f3, int i) {
        this.font = class161Var;
        this.icon = class073Var;
        this.cornerRadius = f;
        this.horizontalPadding = f2;
        this.verticalPadding = f3;
        this.iconSize = i;
        this.onToggleCallback = runnable;
        this.clickable.clickCallback(this::onClick);
        this.keybindField = new KeybindField(Fonts.INTER_EXTRA_BOLD.get(), null, 6.0f, 2.0f, 5.0f, 9);
        this.keybindField.onChange(list -> {
            this.mutableKeys = new ArrayList(list);
        });
    }

    public BindableToggleWidget setBindingEditable(boolean z) {
        this.bindingEditable = z;
        return this;
    }

    public BindableToggleWidget setEditableBinding(List<Integer> list) {
        this.mutableKeys = new ArrayList(list);
        this.keybindField.addMutableKeys(this.mutableKeys);
        return this;
    }

    public List<Integer> editableBinding() {
        return new ArrayList(this.mutableKeys);
    }

    public BindableToggleWidget setImmutableBinding(List<Integer> list) {
        this.immutableKeys = new ArrayList(list);
        this.keybindField.addImmutableKeys(this.immutableKeys);
        return this;
    }

    public void label(String str, int i) {
        this.text = str;
        this.textSize = i;
    }

    public void setActive(boolean z) {
        if (this.active == z) {
            return;
        }
        this.active = z;
        this.toggleAnimator.state(z);
    }

    public void toggle() {
        setActive(!this.active);
    }

    @Override
    public void render(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        this.keybindField.colors(KeybindColors.builder().outline(class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(300).argb())).background(class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(700).argb(), 0.0f)).text(class115VarColorStack.computeColor(class764VarPalette.text().tone(600).argb())).textEmpty(class115VarColorStack.computeColor(class764VarPalette.text().tone(600).argb())).build());
        float fWidth = width();
        float fHeight = height();
        ToggleAnimator class323VarHoverAnimation = this.clickable.hoverAnimation();
        ToggleAnimator class323Var = this.toggleAnimator;
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(700).argb(), 0.0f), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(700).argb(), 100), class323VarHoverAnimation), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(700).argb()), class323Var, class323Var);
        int iInterpolate2 = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()), class323Var, class323Var);
        class699Var.fillOutlinedRoundedRect(x(), y(), fWidth, fHeight, this.cornerRadius, 2.5f, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb()), iInterpolate);
        class699Var.texture(this.icon, x() + this.horizontalPadding, (y() + (fHeight / 2.0f)) - (this.iconSize / 2), this.iconSize, this.iconSize, class115VarColorStack.computeColor(iInterpolate2));
        class699Var.text(this.font, this.text, this.textSize, x() + this.horizontalPadding + this.iconSize + 5.0f, (y() + (fHeight / 2.0f)) - (this.font.getHeight(this.textSize) / 2.0f), iInterpolate2);
        this.keybindField.render(class699Var);
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        float fTextWidthPhysical = class698Var.textWidthPhysical(this.font, this.text, this.textSize);
        float fLineHeight = this.font.metrics().lineHeight();
        this.keybindField.addMutableKeys(this.mutableKeys);
        this.keybindField.addImmutableKeys(this.immutableKeys);
        float fMax = Math.max(this.iconSize, fLineHeight) + (this.verticalPadding * 2.0f);
        this.keybindField.layout(class698Var);
        setSize(this.horizontalPadding + this.iconSize + 5.0f + fTextWidthPhysical + 5.0f + this.keybindField.width() + this.horizontalPadding, fMax);
        this.keybindField.setPosition(x() + this.horizontalPadding + this.iconSize + 5.0f + fTextWidthPhysical + 5.0f, (y() + (fMax / 2.0f)) - (this.keybindField.height() / 2.0f));
        this.clickable.setDimensions(x(), y(), width(), height());
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.clickable.animate(class141Var);
        this.toggleAnimator.state(this.active);
        this.toggleAnimator.animate(class141Var);
        this.keybindField.syncKeys(this.mutableKeys);
        this.keybindField.animation(class141Var);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        boolean zHandleInput = z;
        if (this.bindingEditable) {
            zHandleInput |= this.keybindField.handleInput(class688Var, zHandleInput);
        }
        boolean zHandleInput2 = zHandleInput | this.clickable.handleInput(class688Var, zHandleInput);
        InputEvent class691VarInputEvent = class688Var.inputEvent();
        if (class691VarInputEvent instanceof KeyInput) {
            KeyInput class696Var = (KeyInput) class691VarInputEvent;
            int iKeyCode = class696Var.keyCode();
            if (class696Var.keyAction().press() && !zHandleInput2) {
                this.pressedKeys.add(Integer.valueOf(iKeyCode));
                if (!(!this.mutableKeys.isEmpty() && this.pressedKeys.containsAll(this.mutableKeys))) {
                    return false;
                }
                onClick();
                return true;
            }
            if (class696Var.keyAction().release()) {
                this.pressedKeys.remove(Integer.valueOf(iKeyCode));
                return false;
            }
        }
        return zHandleInput2;
    }

    public void onClick() {
        toggle();
        this.onToggleCallback.run();
    }

    public void resetPressedKeys() {
        this.pressedKeys.clear();
    }

    public boolean isActive() {
        return this.active;
    }
}
