package aethereal.ui.setting;
import aethereal.render.AnimatedFloat;
import aethereal.resource.ClasspathResource;
import aethereal.ui.ClickableBehavior;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.event.EventCallback;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.render.HighlightAnimation;
import aethereal.event.InputEvent;
import aethereal.model.InputEventContext;
import aethereal.model.KeyInput;
import aethereal.type.KeybindCaptureState;
import aethereal.event.LanguageChangeEvent;
import aethereal.model.LayoutScaleContext;
import aethereal.ui.ModuleFrame;
import aethereal.model.MouseButtonInput;
import aethereal.render.MsdfFont;
import aethereal.util.StringUtil;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.model.Translation;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class KeybindSettingElement extends ModuleFrame {
    static final int nameFontSize = 14;
    static final int descriptionFontSize = 13;
    static final int bindingFontSize = 12;
    static final float l = 3.0f;
    static final float m = 10.0f;
    static final float n = 25.0f;
    static final float o = 8.0f;
    final Translation name;
    final Translation description;
    Supplier<Boolean> visibilitySupplier;
    String wrappedDescription;
    String cachedDescriptionSource;
    float descriptionHeight;
    public final Supplier<List<Integer>> keysSupplier;
    final MsdfFont semiBoldFont = Fonts.INTER_SEMIBOLD.get();
    final MsdfFont mediumFont = Fonts.INTER_MEDIUM.get();
    final GlTexture keyboardIcon = new GlTexture(new ClasspathResource("/icons/menu/new/keyboard.png"));
    final EventCallback<LanguageChangeEvent> languageChangeCallback = class226Var -> {
        invalidateDescriptionCache();
    };
    final HighlightAnimation highlightAnimation = new HighlightAnimation(300, Easings.EASE_IN_OUT_CUBIC);
    final ClickableBehavior clickableBehavior = new ClickableBehavior();
    final WidgetBounds bindingBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
    final KeybindCaptureState captureState = new KeybindCaptureState();
    final ToggleAnimator activeToggleAnimator = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);
    final AnimatedFloat widthAnimator = new AnimatedFloat(200, Easings.EASE_IN_OUT_CUBIC);
    boolean widthInitialized = false;
    float scaleFactor = 1.0f;
    float cachedLayoutWidth = -1.0f;
    boolean descriptionDirty = true;

    public KeybindSettingElement(Translation class254Var, Translation class254Var2, Supplier<List<Integer>> supplier) {
        this.name = class254Var;
        this.description = class254Var2;
        this.keysSupplier = supplier;
        ClickableBehavior class766Var = this.clickableBehavior;
        KeybindCaptureState class770Var = this.captureState;
        Objects.requireNonNull(class770Var);
        class766Var.clickCallback(class770Var::toggleCapture);
        Expensive.INSTANCE.eventDispatcher().register(LanguageChangeEvent.class, this.languageChangeCallback);
    }

    public void onChange(Consumer<List<Integer>> consumer) {
        this.captureState.onChange(consumer);
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
        if (this.captureState.captureKey()) {
            class699Var.window().interceptKeyboard(true);
        }
        updateDescriptionLayout(f);
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        float fScaleFactor = class699Var.layoutContext().scaleFactor();
        float fMethod006 = measureTextWidth(this.name.effective(), nameFontSize, fScaleFactor);
        float f3 = f / 2.0f;
        boolean z = fMethod006 >= (f - f3) + m || this.wrappedDescription != null;
        float fY = y();
        if (z) {
            fY = drawDescriptionText(class699Var, drawNameLabel(class699Var, fY));
        }
        String bindingText = this.captureState.formatBindingText();
        float fMethod007 = measureTextWidth(bindingText, bindingFontSize, fScaleFactor);
        float height = this.semiBoldFont.getHeight(12.0f);
        boolean zHasAnyKeys = this.captureState.hasAnyKeys();
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(400).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(300).argb()), this.clickableBehavior.hoverAnimation());
        int iInterpolate2 = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(600).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(500).argb()), this.clickableBehavior.hoverAnimation());
        float f4 = !zHasAnyKeys ? 6.0f : 0.0f;
        float fWidth = !zHasAnyKeys ? this.keyboardIcon.width() : 0.0f;
        float fWidth2 = z ? (((this.bindingBounds.width() - 20.0f) - fWidth) - f4) - 60.0f : (f - f3) - 30.0f;
        String strMethod005 = bindingText;
        if (fMethod007 > fWidth2) {
            strMethod005 = truncateWithEllipsis(bindingText, fWidth2, fScaleFactor);
        }
        float fMethod008 = measureTextWidth(strMethod005, bindingFontSize, fScaleFactor);
        float f5 = fWidth + f4 + fMethod008;
        float fY2 = this.bindingBounds.y() + (this.bindingBounds.height() / 2.0f);
        int iInterpolate3 = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(600).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb()), this.activeToggleAnimator.smoothAnimation());
        int iInterpolate4 = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(600).argb()), class115VarColorStack.white(), this.activeToggleAnimator.smoothAnimation());
        if (z) {
            this.bindingBounds.withPosition(x(), fY + m).withSize(f, n);
            float fX = this.bindingBounds.x() + ((this.bindingBounds.width() - f5) / 2.0f);
            class699Var.fillOutlinedRoundedRect(this.bindingBounds.x(), this.bindingBounds.y(), this.bindingBounds.width(), this.bindingBounds.height(), 6.0f, 2.5f, iInterpolate, iInterpolate2);
            class699Var.textureVerticalC(this.keyboardIcon, fX - (((fX - this.bindingBounds.x()) - m) * this.activeToggleAnimator.smoothAnimation()), fY2, this.keyboardIcon.width(), this.keyboardIcon.height(), iInterpolate4);
            class699Var.text(this.semiBoldFont, strMethod005, bindingFontSize, fX + fWidth + f4, fY2 - (height / 2.0f), iInterpolate3);
        } else {
            float fAnimatedValue = this.widthAnimator.animatedValue();
            this.bindingBounds.withPosition((x() + f) - fAnimatedValue, y()).withSize(fAnimatedValue, n);
            float fX2 = this.bindingBounds.x() + (((f5 + 16.0f) - f5) / 2.0f);
            class699Var.fillOutlinedRoundedRect(this.bindingBounds.x(), this.bindingBounds.y(), this.bindingBounds.width(), this.bindingBounds.height(), 6.0f, 2.5f, iInterpolate, iInterpolate2);
            float f6 = fX2 + fWidth + f4;
            if (f6 + fMethod008 > (this.bindingBounds.x() + fAnimatedValue) - 5.0f) {
                class699Var.drawEngine().beginStencil();
                class699Var.fillOutlinedRoundedRect(this.bindingBounds.x(), this.bindingBounds.y(), this.bindingBounds.width(), this.bindingBounds.height(), 6.0f, 2.5f, iInterpolate, iInterpolate2);
                class699Var.drawEngine().prepareStencil(1);
                class699Var.textureVerticalC(this.keyboardIcon, fX2, fY2, this.keyboardIcon.width(), this.keyboardIcon.height(), iInterpolate3);
                class699Var.text(this.semiBoldFont, strMethod005, bindingFontSize, f6, fY2 - (height / 2.0f), iInterpolate3);
                class699Var.drawEngine().endStencil();
            } else {
                if (!zHasAnyKeys) {
                    class699Var.textureVerticalC(this.keyboardIcon, fX2, fY2, this.keyboardIcon.width(), this.keyboardIcon.height(), iInterpolate3);
                }
                class699Var.text(this.semiBoldFont, strMethod005, bindingFontSize, f6, fY2 - (height / 2.0f), iInterpolate3);
            }
            class699Var.text(this.semiBoldFont, this.name.effective(), nameFontSize, x(), fY2 - (this.semiBoldFont.getHeight(14.0f) / 2.0f), class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(200).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(50).argb()), this.highlightAnimation.value()));
        }
        this.clickableBehavior.setDimensions(x(), y(), f, height());
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        String bindingText = this.captureState.formatBindingText();
        this.scaleFactor = class698Var.scaleFactor();
        float fMethod006 = measureTextWidth(bindingText, bindingFontSize, this.scaleFactor);
        float fWidth = (this.parent.width() - (this.parent.width() / 2.0f)) - 30.0f;
        String strMethod005 = bindingText;
        if (fMethod006 > fWidth) {
            strMethod005 = truncateWithEllipsis(bindingText, fWidth, this.scaleFactor);
        }
        float fMethod007 = measureTextWidth(strMethod005, bindingFontSize, this.scaleFactor);
        boolean zHasAnyKeys = this.captureState.hasAnyKeys();
        float fWidth2 = (!zHasAnyKeys ? this.keyboardIcon.width() : 0.0f) + (!zHasAnyKeys ? 6.0f : 0.0f) + fMethod007 + 16.0f;
        if (!this.widthInitialized) {
            this.widthAnimator.set(fWidth2);
            this.widthInitialized = true;
        }
        this.widthAnimator.destination(fWidth2);
        this.keyboardIcon.setDimensions(bindingFontSize, bindingFontSize);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        boolean zHandleInput = super.handleInput(class688Var, z);
        boolean zHandleInput2 = zHandleInput | this.clickableBehavior.handleInput(class688Var, zHandleInput);
        if (!z) {
            if (this.captureState.captureKey()) {
                InputEvent class691VarInputEvent = class688Var.inputEvent();
                if (class691VarInputEvent instanceof MouseButtonInput) {
                    MouseButtonInput class693Var = (MouseButtonInput) class691VarInputEvent;
                    if (class693Var.action().press() && class693Var.button() == 0 && !class688Var.inArea(this.clickableBehavior.ownerX(), this.clickableBehavior.ownerY(), this.clickableBehavior.ownerW(), this.clickableBehavior.ownerH())) {
                        this.captureState.toggleCapture();
                        zHandleInput2 = true;
                    }
                }
            }
            if (!zHandleInput2) {
                InputEvent class691VarInputEvent2 = class688Var.inputEvent();
                if (class691VarInputEvent2 instanceof KeyInput) {
                    zHandleInput2 = this.captureState.handleKeyInput((KeyInput) class691VarInputEvent2);
                }
                if (class691VarInputEvent2 instanceof MouseButtonInput) {
                    zHandleInput2 = this.captureState.handleMouseInput((MouseButtonInput) class691VarInputEvent2);
                }
            }
            zHandleInput2 |= this.captureState.captureKey() && !zHandleInput2;
        }
        return zHandleInput2;
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        if (this.visibilitySupplier != null) {
            visible(this.visibilitySupplier.get().booleanValue());
        }
        this.captureState.syncKeys(this.keysSupplier.get());
        this.activeToggleAnimator.state(this.captureState.hasAnyKeys());
        this.activeToggleAnimator.animate(class141Var);
        this.widthAnimator.animate(class141Var);
        this.clickableBehavior.animate(class141Var);
        this.highlightAnimation.animate(class141Var);
        super.animation(class141Var);
    }

    @Override
    public void handleClose() {
        Expensive.INSTANCE.eventDispatcher().unregister(LanguageChangeEvent.class, this.languageChangeCallback);
        super.handleClose();
    }

    @Override
    public float height() {
        float fMax;
        if (this.description != null && this.descriptionDirty && this.parent != null) {
            updateDescriptionLayout(this.parent.width());
        }
        float fMethod006 = measureTextWidth(this.name.effective(), nameFontSize, this.scaleFactor);
        float fWidth = (this.parent.width() - (this.parent.width() / 2.0f)) + m;
        if (this.wrappedDescription != null || fMethod006 >= fWidth) {
            float height = 0.0f + this.semiBoldFont.getHeight(14.0f);
            if (this.description != null && this.wrappedDescription != null) {
                height += l + this.descriptionHeight;
            }
            fMax = height + 35.0f;
        } else {
            fMax = Math.max(this.semiBoldFont.getHeight(14.0f), n);
        }
        return fMax;
    }

    public String truncateWithEllipsis(String str, float f, float f2) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        float fMethod006 = f - measureTextWidth("...", bindingFontSize, f2);
        if (fMethod006 <= 0.0f) {
            return "...";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            if (measureTextWidth(sb.toString() + str.charAt(i), bindingFontSize, f2) > fMethod006) {
                return String.valueOf(sb) + "...";
            }
            sb.append(str.charAt(i));
        }
        return str;
    }

    public float measureTextWidth(String str, int i, float f) {
        return this.semiBoldFont.getWidth(str, Math.max(1, Math.round(i * f))) / f;
    }

    public float drawNameLabel(DrawCtx class699Var, float f) {
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        class699Var.text(this.semiBoldFont, this.name.effective(), nameFontSize, x(), f, class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(200).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(50).argb()), this.highlightAnimation.value()));
        return f + this.semiBoldFont.getHeight(14.0f);
    }

    public float drawDescriptionText(DrawCtx class699Var, float f) {
        if (this.wrappedDescription == null) {
            return f;
        }
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb()), this.highlightAnimation.value());
        float f2 = f + l;
        class699Var.text(this.mediumFont, this.wrappedDescription, descriptionFontSize, x(), f2, iInterpolate);
        return f2 + this.descriptionHeight;
    }

    public void invalidateDescriptionCache() {
        this.descriptionDirty = true;
        this.cachedLayoutWidth = -1.0f;
        this.cachedDescriptionSource = null;
    }

    public void updateDescriptionLayout(float f) {
        if (this.description == null || this.description.effective() == null || this.description.effective().equals("null")) {
            clearDescription(f);
            return;
        }
        String strEffective = this.description.effective();
        if (isDescriptionCacheValid(f, strEffective)) {
            return;
        }
        formatDescription(f, strEffective == null ? "" : strEffective);
    }

    public void clearDescription(float f) {
        this.wrappedDescription = null;
        this.descriptionHeight = 0.0f;
        this.descriptionDirty = false;
        this.cachedLayoutWidth = f;
        this.cachedDescriptionSource = null;
    }

    public boolean isDescriptionCacheValid(float f, String str) {
        return !this.descriptionDirty && f == this.cachedLayoutWidth && str != null && str.equals(this.cachedDescriptionSource);
    }

    public void formatDescription(float f, String str) {
        this.wrappedDescription = StringUtil.formatTextToFitWidth(str, f, this.mediumFont, descriptionFontSize);
        this.descriptionHeight = this.mediumFont.getHeightWithLineBreaks(this.wrappedDescription, descriptionFontSize);
        this.cachedLayoutWidth = f;
        this.cachedDescriptionSource = str;
        this.descriptionDirty = false;
    }

    public Translation name() {
        return this.name;
    }

    public KeybindSettingElement visibleSupplier(Supplier<Boolean> supplier) {
        this.visibilitySupplier = supplier;
        return this;
    }
}
