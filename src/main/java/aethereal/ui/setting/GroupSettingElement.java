package aethereal.ui.setting;
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
import aethereal.Lang;
import aethereal.event.LanguageChangeEvent;
import aethereal.model.LayoutScaleContext;
import aethereal.ui.ModuleFrame;
import aethereal.model.MouseButtonInput;
import aethereal.render.MsdfFont;
import aethereal.util.RenderCommandQueue;
import aethereal.render.ScreenResolution;
import aethereal.ui.SettingsGroupWindow;
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
import net.minecraft.client.util.math.MatrixStack;

public class GroupSettingElement extends ModuleFrame {
    static final int titleTextSize = 14;
    static final int descTextSize = 13;
    static final int bodyTextSize = 12;
    static final float d = 3.0f;
    static final float e = 10.0f;
    static final float f = 25.0f;
    static final float g = 8.0f;
    static final float i = 8.0f;
    static final float j = 5.0f;
    public final Translation name;
    public final Translation description;
    public Supplier<Boolean> toggleSupplier;
    public Supplier<Boolean> visibilitySupplier;
    public Runnable changeCallback;
    public String wrappedDescription;
    public String cachedDescriptionSource;
    public float descriptionHeight;
    public ScreenResolution resolution;

    public final SettingsGroupWindow groupWindow;
    public final MsdfFont semiBoldFont = Fonts.INTER_SEMIBOLD.get();
    public final MsdfFont mediumFont = Fonts.INTER_MEDIUM.get();
    public final GlTexture keyboardIcon = new GlTexture(new ClasspathResource("/icons/menu/new/keyboard.png"));
    public final GlTexture checkmarkIcon = new GlTexture(new ClasspathResource("/icons/menu/new/checkmark.png"));
    public final GlTexture groupIcon = new GlTexture(new ClasspathResource("/icons/menu/new/group.png"));
    public final EventCallback<LanguageChangeEvent> languageChangeCallback = class226Var -> {
        resetDescriptionCache();
    };
    public final HighlightAnimation highlightAnimation = new HighlightAnimation(300, Easings.EASE_IN_OUT_CUBIC);
    public final ClickableBehavior toggleClickable = new ClickableBehavior();
    public final ClickableBehavior keybindClickable = new ClickableBehavior();
    public final ClickableBehavior groupClickable = new ClickableBehavior();
    public final ToggleAnimator toggleAnimator = new ToggleAnimator(Easings.EASE_IN_OUT_CUBIC);
    public final KeybindCaptureState keybindCapture = new KeybindCaptureState();
    public final WidgetBounds bindingBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
    public final WidgetBounds groupButtonBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
    public boolean lastToggleState = false;
    public float lastDescriptionWidth = -1.0f;
    public boolean descriptionDirty = true;
    public float k = 0.0f;

    public GroupSettingElement(Translation class254Var, Translation class254Var2, List<Setting> list, List<Integer> list2) {
        this.name = class254Var;
        this.description = class254Var2;
        this.keybindCapture.addMutableKeys(list2);
        this.toggleClickable.clickCallback(this::invert);
        ClickableBehavior class766Var = this.keybindClickable;
        KeybindCaptureState class770Var = this.keybindCapture;
        Objects.requireNonNull(class770Var);
        class766Var.clickCallback(class770Var::toggleCapture);
        this.groupWindow = new SettingsGroupWindow(class254Var, list);
        this.groupClickable.clickCallback(this::toggleGroupWindow);
        Expensive.INSTANCE.eventDispatcher().register(LanguageChangeEvent.class, this.languageChangeCallback);
        Expensive.INSTANCE.menuWindow().priorityOverlayHandler().registerPopup(this.groupWindow);
    }

    public void onChange(Consumer<List<Integer>> consumer) {
        this.keybindCapture.onChange(consumer);
    }

    public void toggleGroupWindow() {
        float fDpiScaleFactor = Expensive.INSTANCE.windowController().dpiScaleFactor();
        ScreenResolution class710VarResolution = this.resolution != null ? this.resolution : ScreenResolution.resolution();
        float fScreenWidth = class710VarResolution.screenWidth() / fDpiScaleFactor;
        float fScreenHeight = class710VarResolution.screenHeight() / fDpiScaleFactor;
        float fWidth = this.groupWindow.width();
        float f2 = this.groupWindow.totalHeight();
        float fX = this.groupButtonBounds.x() + this.groupButtonBounds.width() + 8.0f;
        float fX2 = (this.groupButtonBounds.x() - 8.0f) - fWidth;
        if (fX + fWidth > fScreenWidth - j && fX2 >= j) {
            fX = fX2;
        }
        float fMax = Math.max(j, Math.min(fX, (fScreenWidth - fWidth) - j));
        float fY = ((this.groupButtonBounds.y() - this.viewportTop) + (this.groupButtonBounds.height() / 2.0f)) - (f2 / 2.0f);
        if (fY + f2 > fScreenHeight - j) {
            fY = (fScreenHeight - f2) - j;
        }
        if (fY < j) {
            fY = 5.0f;
        }
        if (this.groupWindow.isOpen()) {
            this.groupWindow.closeWindow();
        } else {
            this.groupWindow.openWindow();
            this.groupWindow.setPosition(fMax, fY);
        }
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
    public void draw(DrawCtx class699Var, float f2, float f3) {
        if (this.keybindCapture.captureKey()) {
            class699Var.window().interceptKeyboard(true);
        }
        this.resolution = class699Var.resolution();
        updateDescriptionLayout(f2);
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        float fMethod007 = drawDescription(class699Var, drawTitle(class699Var, y())) + e;
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(400).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(300).argb()), this.keybindClickable.hoverAnimation());
        int iInterpolate2 = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(600).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(500).argb()), this.keybindClickable.hoverAnimation());
        this.bindingBounds.withPosition(x(), fMethod007).withSize(111.0f, f);
        class699Var.fillOutlinedRoundedRect(this.bindingBounds.x(), this.bindingBounds.y(), this.bindingBounds.width(), this.bindingBounds.height(), 6.0f, 2.5f, iInterpolate, iInterpolate2);
        String bindingText = this.keybindCapture.formatBindingText();
        float fTextWidthPhysical = class699Var.textWidthPhysical(this.semiBoldFont, bindingText, bodyTextSize);
        float height = this.semiBoldFont.getHeight(12.0f);
        boolean zHasAnyKeys = this.keybindCapture.hasAnyKeys();
        float f4 = !zHasAnyKeys ? 6.0f : 0.0f;
        float fWidth = !zHasAnyKeys ? this.keyboardIcon.width() : 0.0f;
        float fWidth2 = ((this.bindingBounds.width() - 16.0f) - fWidth) - f4;
        String strMethod006 = bindingText;
        if (fTextWidthPhysical > fWidth2) {
            strMethod006 = truncateText(class699Var, bindingText, fWidth2);
        }
        float fX = this.bindingBounds.x() + ((this.bindingBounds.width() - ((fWidth + f4) + class699Var.textWidthPhysical(this.semiBoldFont, strMethod006, bodyTextSize))) / 2.0f);
        float fY = this.bindingBounds.y() + (this.bindingBounds.height() / 2.0f);
        int iComputeColor = zHasAnyKeys ? class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb()) : class115VarColorStack.computeColor(class764VarPalette.text().tone(600).argb());
        if (!zHasAnyKeys) {
            class699Var.textureVerticalC(this.keyboardIcon, fX, fY, this.keyboardIcon.width(), this.keyboardIcon.height(), iComputeColor);
        }
        class699Var.text(this.semiBoldFont, strMethod006, bodyTextSize, fX + fWidth + f4, fY - (height / 2.0f), iComputeColor);
        this.groupButtonBounds.withPosition(x() + this.bindingBounds.width() + 6.0f, fMethod007).withSize(111.0f, f);
        drawGroupButton(class699Var);
        drawToggle(class699Var, fMethod007, f2);
    }

    @Override
    public void collectBloomElements(RenderCommandQueue class676Var) {
        this.groupWindow.collectBloomElements(class676Var);
        super.collectBloomElements(class676Var);
    }

    @Override
    public void drawOverlay(DrawCtx class699Var) {
        if (this.groupWindow.isOpen()) {
            this.groupWindow.render(new DrawCtx(class699Var.window(), class699Var.resolution(), new MatrixStack(), class699Var.mousePosition(), class699Var.drawEngine(), class699Var.drawEngine().colorStack(), class699Var.lastKnownMousePosition(), class699Var.theme(), class699Var.layoutContext()));
        }
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        this.groupClickable.setDimensions(this.groupButtonBounds.x(), this.groupButtonBounds.y(), this.groupButtonBounds.width(), this.groupButtonBounds.height());
        this.keybindClickable.setDimensions(this.bindingBounds.x(), this.bindingBounds.y(), this.bindingBounds.width(), this.bindingBounds.height());
        this.groupIcon.setDimensions(bodyTextSize, bodyTextSize);
        this.groupWindow.layout(class698Var);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        boolean zHandleInput = this.keybindClickable.handleInput(class688Var, z);
        if (this.keybindCapture.captureKey()) {
            InputEvent class691VarInputEvent = class688Var.inputEvent();
            if (class691VarInputEvent instanceof MouseButtonInput) {
                MouseButtonInput class693Var = (MouseButtonInput) class691VarInputEvent;
                if (class693Var.action().press() && class693Var.button() == 0 && !class688Var.inArea(this.bindingBounds.x(), this.bindingBounds.y(), this.bindingBounds.width(), this.bindingBounds.height())) {
                    this.keybindCapture.toggleCapture();
                    zHandleInput = true;
                }
            }
        }
        boolean zHandleInput2 = zHandleInput | this.groupClickable.handleInput(class688Var, zHandleInput);
        InputEvent class691VarInputEvent2 = class688Var.inputEvent();
        if (class691VarInputEvent2 instanceof KeyInput) {
            zHandleInput2 |= this.keybindCapture.handleKeyInput((KeyInput) class691VarInputEvent2) && !zHandleInput2;
        }
        InputEvent class691VarInputEvent3 = class688Var.inputEvent();
        if (class691VarInputEvent3 instanceof MouseButtonInput) {
            zHandleInput2 |= this.keybindCapture.handleMouseInput((MouseButtonInput) class691VarInputEvent3) && !zHandleInput2;
        }
        boolean zHandleInput3 = zHandleInput2 | this.toggleClickable.handleInput(class688Var, zHandleInput2);
        return zHandleInput3 | super.handleInput(class688Var, zHandleInput3);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        boolean zBooleanValue;
        if (this.toggleSupplier != null && (zBooleanValue = this.toggleSupplier.get().booleanValue()) != this.lastToggleState) {
            this.toggleAnimator.state(zBooleanValue);
            this.lastToggleState = zBooleanValue;
        }
        if (this.visibilitySupplier != null) {
            visible(this.visibilitySupplier.get().booleanValue());
        }
        this.groupWindow.animation(class141Var);
        this.groupClickable.animate(class141Var);
        this.keybindClickable.animate(class141Var);
        this.highlightAnimation.animate(class141Var);
        this.toggleClickable.animate(class141Var);
        this.toggleAnimator.animate(class141Var);
        this.checkmarkIcon.setDimensions(titleTextSize, titleTextSize);
        super.animation(class141Var);
    }

    @Override
    public void handleClose() {
        Expensive.INSTANCE.eventDispatcher().unregister(LanguageChangeEvent.class, this.languageChangeCallback);
        this.groupWindow.closeWindow();
        super.handleClose();
    }

    @Override
    public float height() {
        if (this.description != null && this.descriptionDirty && this.parent != null) {
            updateDescriptionLayout(this.parent.width());
        }
        float height = this.semiBoldFont.getHeight(14.0f);
        if (this.description != null && this.wrappedDescription != null) {
            height += d + this.descriptionHeight;
        }
        return height + e + f;
    }

    public void invert() {
        if (this.changeCallback != null) {
            this.changeCallback.run();
        }
    }

    public String truncateText(DrawCtx class699Var, String str, float f2) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        float fTextWidthPhysical = f2 - class699Var.textWidthPhysical(this.semiBoldFont, "...", bodyTextSize);
        if (fTextWidthPhysical <= 0.0f) {
            return "...";
        }
        StringBuilder sb = new StringBuilder();
        for (int i2 = 0; i2 < str.length(); i2++) {
            if (class699Var.textWidthPhysical(this.semiBoldFont, sb.toString() + str.charAt(i2), bodyTextSize) > fTextWidthPhysical) {
                return String.valueOf(sb) + "...";
            }
            sb.append(str.charAt(i2));
        }
        return str;
    }

    public float drawTitle(DrawCtx class699Var, float f2) {
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        class699Var.text(this.semiBoldFont, this.name.effective(), titleTextSize, x(), f2, class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(200).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(50).argb()), this.highlightAnimation.value()));
        return f2 + this.semiBoldFont.getHeight(14.0f);
    }

    public float drawDescription(DrawCtx class699Var, float f2) {
        if (this.wrappedDescription == null) {
            return f2;
        }
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb()), this.highlightAnimation.value());
        float f3 = f2 + d;
        class699Var.text(this.mediumFont, this.wrappedDescription, descTextSize, x(), f3, iInterpolate);
        return f3 + this.descriptionHeight;
    }

    public void drawGroupButton(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        class699Var.fillOutlinedRoundedRect(this.groupButtonBounds.x(), this.groupButtonBounds.y(), this.groupButtonBounds.width(), this.groupButtonBounds.height(), 6.0f, 2.5f, class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(400).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(300).argb()), this.groupClickable.hoverAnimation()), class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(600).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(500).argb()), this.groupClickable.hoverAnimation()));
        String strEffective = Lang.SHOW_GROUP.effective();
        float fX = this.groupButtonBounds.x() + ((this.groupButtonBounds.width() - ((this.groupIcon.width() + 6.0f) + class699Var.textWidthPhysical(this.semiBoldFont, strEffective, bodyTextSize))) / 2.0f);
        float fY = this.groupButtonBounds.y() + (this.groupButtonBounds.height() / 2.0f);
        int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb());
        class699Var.textureVerticalC(this.groupIcon, fX, fY, this.groupIcon.width(), this.groupIcon.height(), iComputeColor);
        class699Var.text(this.semiBoldFont, strEffective, bodyTextSize, fX + this.groupIcon.width() + 6.0f, fY - (this.semiBoldFont.getHeight(12.0f) / 2.0f), iComputeColor);
    }

    public void drawToggle(DrawCtx class699Var, float f2, float f3) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(500).argb());
        int iComputeColor2 = class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(400).argb());
        int iComputeColor3 = class115VarColorStack.computeColor(class764VarPalette.accentBright().argb());
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.interpolate(iComputeColor, iComputeColor2, this.toggleClickable.hoverAnimation()), class115VarColorStack.interpolate(iComputeColor3, class115VarColorStack.autoBrightenDarken(iComputeColor3, 0.1f), this.toggleClickable.hoverAnimation()), this.toggleAnimator);
        int iComputeColor4 = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(700).argb());
        int iComputeColor5 = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(600).argb());
        int iComputeColor6 = class115VarColorStack.computeColor(class764VarPalette.accent().argb());
        int iInterpolate2 = class115VarColorStack.interpolate(class115VarColorStack.interpolate(iComputeColor4, iComputeColor5, this.toggleClickable.hoverAnimation()), class115VarColorStack.interpolate(iComputeColor6, class115VarColorStack.autoBrightenDarken(iComputeColor6, 0.1f), this.toggleClickable.hoverAnimation()), this.toggleAnimator);
        float fX = (x() + f3) - 18.0f;
        float f4 = (f2 + 12.5f) - (18.0f / 2.0f);
        class699Var.fillOutlinedRoundedRect(fX, f4, 18.0f, 18.0f, j, 2.5f, iInterpolate, iInterpolate2);
        class699Var.texture(this.checkmarkIcon, (fX + (18.0f / 2.0f)) - Math.round(this.checkmarkIcon.width() / 2.0f), (f4 + (18.0f / 2.0f)) - Math.round(this.checkmarkIcon.height() / 2.0f), this.checkmarkIcon.width(), this.checkmarkIcon.height(), class115VarColorStack.computeColor(16777215, class115VarColorStack.alphaMultiplier() * this.toggleAnimator.smoothAnimation()));
        this.toggleClickable.setDimensions(x(), y(), f3, height());
    }

    public void resetDescriptionCache() {
        this.descriptionDirty = true;
        this.lastDescriptionWidth = -1.0f;
        this.cachedDescriptionSource = null;
    }

    public void updateDescriptionLayout(float f2) {
        if (this.description == null || this.description.effective() == null || this.description.effective().equals("null")) {
            clearDescriptionLayout(f2);
            return;
        }
        String strEffective = this.description.effective();
        if (isDescriptionUpToDate(f2, strEffective)) {
            return;
        }
        computeDescriptionLayout(f2, strEffective == null ? "" : strEffective);
    }

    public void clearDescriptionLayout(float f2) {
        this.wrappedDescription = null;
        this.descriptionHeight = 0.0f;
        this.descriptionDirty = false;
        this.lastDescriptionWidth = f2;
        this.cachedDescriptionSource = null;
    }

    public boolean isDescriptionUpToDate(float f2, String str) {
        return !this.descriptionDirty && f2 == this.lastDescriptionWidth && str != null && str.equals(this.cachedDescriptionSource);
    }

    public void computeDescriptionLayout(float f2, String str) {
        this.wrappedDescription = StringUtil.formatTextToFitWidth(str, f2, this.mediumFont, descTextSize);
        this.descriptionHeight = this.mediumFont.getHeightWithLineBreaks(this.wrappedDescription, descTextSize);
        this.lastDescriptionWidth = f2;
        this.cachedDescriptionSource = str;
        this.descriptionDirty = false;
    }

    public Translation name() {
        return this.name;
    }

    public GroupSettingElement toggledSupplier(Supplier<Boolean> supplier) {
        this.toggleSupplier = supplier;
        return this;
    }

    public GroupSettingElement visibleSupplier(Supplier<Boolean> supplier) {
        this.visibilitySupplier = supplier;
        return this;
    }

    public GroupSettingElement changeRunnable(Runnable runnable) {
        this.changeCallback = runnable;
        return this;
    }
}
