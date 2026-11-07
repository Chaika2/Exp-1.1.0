package aethereal.ui;
import aethereal.render.AnimatedFloat;
import aethereal.resource.ClasspathResource;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.util.DrawEngine;
import aethereal.math.Easings;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.event.InputEvent;
import aethereal.model.InputEventContext;
import aethereal.model.KeyInput;
import aethereal.type.KeyInputAction;
import aethereal.util.KeyboardUtil;
import aethereal.Lang;
import aethereal.model.LayoutScaleContext;
import aethereal.model.MouseButtonInput;
import aethereal.render.MsdfFont;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.util.WeightedEngine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class HeaderBindingEditor extends AbstractWidget {
    public final Supplier<List<Integer>> bindingSupplier;
    public final Consumer<List<Integer>> bindingConsumer;
    public boolean capturing;
    public boolean widthInitialized;
    public float aj;
    public float ak;
    public float al;
    public float am;
    public final ClickableBehavior clickable = new ClickableBehavior();
    public final MsdfFont labelFont = Fonts.INTER_SEMIBOLD.get();
    public final MsdfFont bindingFont = Fonts.INTER_BOLD.get();
    public final float ag = 6.0f;
    public final int labelTextSize = 12;
    public final GlTexture keyboardIcon = new GlTexture(new ClasspathResource("/icons/menu/new/keyboard.png")).setDimensions(12, 12);
    public final float ah = 6.0f;
    public final float ai = 10.0f;
    public final ToggleAnimator captureAnimator = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);
    public final AnimatedFloat widthAnimation = new AnimatedFloat(220, Easings.LINEAR);
    public final Set<Integer> pressedKeys = new HashSet();
    public final Set<Integer> capturedKeys = new LinkedHashSet();
    public int maxKeys = 2;
    public final int bindingTextSize = 12;

    public HeaderBindingEditor(Supplier<List<Integer>> supplier, Consumer<List<Integer>> consumer) {
        this.bindingSupplier = supplier;
        this.bindingConsumer = consumer;
        this.clickable.clickCallback(this::toggleCapture);
    }

    public HeaderBindingEditor maxBindingKeys(int i) {
        this.maxKeys = Math.max(1, i);
        return this;
    }

    public float preferredWidth(LayoutScaleContext class698Var) {
        return class698Var.textWidthPhysical(this.labelFont, Lang.HEADER_BINDING_LABEL.effective(), 12) + 10.0f + this.widthAnimation.animatedValue();
    }

    public float preferredHeight() {
        return Math.max(this.labelFont.metrics().lineHeight(), Math.max(this.bindingFont.getHeight(12.0f), 12.0f) + 12.0f);
    }

    public void toggleCapture() {
        this.capturing = !this.capturing;
        this.captureAnimator.state(this.capturing);
        this.capturedKeys.clear();
        this.pressedKeys.clear();
    }

    public void stopCapture() {
        this.capturing = false;
        this.captureAnimator.state(false);
        this.capturedKeys.clear();
        this.pressedKeys.clear();
    }

    @Override
    public void render(DrawCtx class699Var) {
        if (this.capturing) {
            class699Var.window().interceptKeyboard(true);
        }
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        float fTextWidthPhysical = class699Var.textWidthPhysical(this.labelFont, Lang.HEADER_BINDING_LABEL.effective(), 12);
        class699Var.text(this.labelFont, Lang.HEADER_BINDING_LABEL.effective(), 12, x(), this.ak + ((this.am - this.labelFont.getHeight(12.0f)) / 2.0f), class115VarColorStack.computeColor(class764VarPalette.text().tone(600).argb()));
        drawBindingBox(class699Var, fTextWidthPhysical + 10.0f);
    }

    public void drawBindingBox(DrawCtx class699Var, float f) {
        DrawEngine class154VarDrawEngine = class699Var.drawEngine();
        ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        String strMethod001 = formatBinding();
        float fTextWidthPhysical = class699Var.textWidthPhysical(this.bindingFont, strMethod001, 12);
        ToggleAnimator class323VarHoverAnimation = this.clickable.hoverAnimation();
        int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(400).argb());
        int iInterpolate = class115VarColorStack.interpolate(iComputeColor, class115VarColorStack.brighten(iComputeColor, 0.2f), class323VarHoverAnimation);
        int iInterpolate2 = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(700).argb(), 0.05f), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(700).argb(), 90), this.captureAnimator, this.captureAnimator);
        float f2 = this.aj;
        float f3 = this.ak;
        float f4 = this.al;
        float f5 = this.am;
        float fSmoothAnimation = 1.0f - (0.4f * this.captureAnimator.smoothAnimation());
        class115VarColorStack.push();
        class115VarColorStack.alpha(fSmoothAnimation);
        class699Var.fillOutlinedRoundedRect(f2, f3, f4, f5, 6.0f, 2.5f, iInterpolate, iInterpolate2);
        float f6 = f2 + ((f4 - (18.0f + fTextWidthPhysical)) / 2.0f);
        float f7 = f6 + 12.0f + 6.0f;
        float height = f3 + ((f5 - this.bindingFont.getHeight(12.0f)) / 2.0f);
        class154VarDrawEngine.beginStencil();
        class699Var.fillOutlinedRoundedRect(f2, f3, f4, f5, 6.0f, 2.5f, iInterpolate, iInterpolate2);
        class154VarDrawEngine.prepareStencil(1);
        class699Var.texture(this.keyboardIcon, f6, f3 + ((f5 - 12.0f) / 2.0f), 12.0f, 12.0f, class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb()));
        class699Var.text(this.bindingFont, strMethod001, 12, f7, height, class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb()));
        class154VarDrawEngine.endStencil();
        class115VarColorStack.pop();
    }

    public String formatBinding() {
        List<Integer> listMethod008 = getBinding();
        if (this.capturing) {
            return "...";
        }
        return listMethod008.isEmpty() ? "NONE" : (String) listMethod008.stream().map((v0) -> {
            return KeyboardUtil.keyToString(v0);
        }).collect(Collectors.joining(" + "));
    }

    public List<Integer> getBinding() {
        List<Integer> list = this.bindingSupplier.get();
        return list == null ? List.of() : list;
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        InputEvent class691VarInputEvent = class688Var.inputEvent();
        if (class691VarInputEvent instanceof MouseButtonInput) {
            MouseButtonInput class693Var = (MouseButtonInput) class691VarInputEvent;
            if (class693Var.button() == 0 && this.capturing) {
                boolean zInArea = class688Var.inArea(this.aj, this.ak, this.al, this.am);
                if (class693Var.action().press() && !zInArea) {
                    stopCapture();
                    return false;
                }
            }
        }
        boolean zHandleInput = this.clickable.handleInput(class688Var, z);
        InputEvent class691VarInputEvent2 = class688Var.inputEvent();
        if (class691VarInputEvent2 instanceof KeyInput) {
            KeyInput class696Var = (KeyInput) class691VarInputEvent2;
            if (!z) {
                zHandleInput = handleKeyInput(class696Var) || zHandleInput;
            }
        }
        return zHandleInput;
    }

    public boolean handleKeyInput(KeyInput class696Var) {
        int iKeyCode = class696Var.keyCode();
        KeyInputAction class704VarKeyAction = class696Var.keyAction();
        if (class704VarKeyAction.press()) {
            this.pressedKeys.add(Integer.valueOf(iKeyCode));
            if (!this.capturing) {
                return false;
            }
            if (iKeyCode == 256) {
                stopCapture();
                return true;
            }
            captureKey(iKeyCode);
            return true;
        }
        if (!class704VarKeyAction.release()) {
            return false;
        }
        if (!this.capturing) {
            this.pressedKeys.remove(Integer.valueOf(iKeyCode));
            return false;
        }
        if (iKeyCode != 256) {
            captureKey(iKeyCode);
        }
        this.pressedKeys.remove(Integer.valueOf(iKeyCode));
        if (!this.pressedKeys.isEmpty() || this.capturedKeys.isEmpty()) {
            return true;
        }
        this.bindingConsumer.accept(deduplicateKeys(this.capturedKeys.stream().toList()));
        stopCapture();
        return true;
    }

    public void captureKey(int i) {
        if (this.capturedKeys.contains(Integer.valueOf(i))) {
            return;
        }
        if (this.capturedKeys.size() < this.maxKeys) {
            this.capturedKeys.add(Integer.valueOf(i));
        }
        if (this.capturedKeys.size() == this.maxKeys) {
            this.bindingConsumer.accept(deduplicateKeys(this.capturedKeys.stream().toList()));
            stopCapture();
        }
    }

    public List<Integer> deduplicateKeys(List<Integer> list) {
        ArrayList arrayList = new ArrayList(this.maxKeys);
        for (Integer num : list) {
            if (!arrayList.contains(num)) {
                arrayList.add(num);
                if (arrayList.size() == this.maxKeys) {
                    break;
                }
            }
        }
        return arrayList;
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        float fTextWidthPhysical = class698Var.textWidthPhysical(this.labelFont, Lang.HEADER_BINDING_LABEL.effective(), 12);
        float fLineHeight = this.labelFont.metrics().lineHeight();
        float fTextWidthPhysical2 = class698Var.textWidthPhysical(this.bindingFont, formatBinding(), 12);
        float f = 18.0f + fTextWidthPhysical2;
        float fMax = Math.max(this.bindingFont.getHeight(12.0f), 12.0f);
        float f2 = f + (12.0f * 2.0f);
        float f3 = fMax + 12.0f;
        if (!this.widthInitialized) {
            this.widthAnimation.set(f2);
            this.widthInitialized = true;
        }
        this.widthAnimation.destination(f2);
        float fAnimatedValue = this.widthAnimation.animatedValue();
        setSize(fTextWidthPhysical + 10.0f + fAnimatedValue, Math.max(fLineHeight, f3));
        this.al = fAnimatedValue;
        this.am = f3;
        this.aj = x() + fTextWidthPhysical + 10.0f;
        this.ak = y();
        this.clickable.setDimensions(this.aj, this.ak, this.al, this.am);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.clickable.animate(class141Var);
        this.captureAnimator.animate(class141Var);
        this.widthAnimation.animate(class141Var);
        super.animation(class141Var);
    }
}
