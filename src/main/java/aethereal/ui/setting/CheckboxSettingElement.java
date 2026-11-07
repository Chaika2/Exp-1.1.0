package aethereal.ui.setting;
import aethereal.type.BindMode;
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
import aethereal.ui.IconButtonWidget;
import aethereal.model.InputEventContext;
import aethereal.ui.KeybindConfigWindow;
import aethereal.Lang;
import aethereal.event.LanguageChangeEvent;
import aethereal.model.LayoutScaleContext;
import aethereal.ui.ModuleFrame;
import aethereal.render.MsdfFont;
import aethereal.util.RenderCommandQueue;
import aethereal.render.ScreenResolution;
import aethereal.util.StringUtil;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.model.Translation;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CheckboxSettingElement extends ModuleFrame {
    public static final float descriptionGap = 3.0f;
    public static final int nameTextSize = 14;
    public static final int descriptionTextSize = 13;
    public final MsdfFont titleFont;
    public final MsdfFont descriptionFont;
    public final GlTexture checkmarkIcon;
    public final GlTexture keybindIcon;
    public final Translation name;
    public final Translation description;
    public final ToggleAnimator checkAnimator;
    public final ClickableBehavior clickBehavior;
    public final HighlightAnimation highlightAnimation;
    public Supplier<Boolean> toggledStateSupplier;
    public Supplier<Boolean> visibilitySupplier;
    public Runnable changeAction;

    public final IconButtonWidget keybindButton;
    public boolean showKeybindButton;
    public boolean lastToggledState;
    public String wrappedDescription;
    public boolean descriptionDirty;
    public float cachedWidth;
    public String cachedDescriptionText;
    public float descriptionHeight;
    public String wrappedName;
    public boolean nameDirty;
    public String cachedNameText;
    public float nameHeight;
    public final WidgetBounds keybindButtonBounds;
    public final EventCallback<LanguageChangeEvent> languageChangeCallback;
    public final KeybindConfigWindow keybindWindow;
    public ScreenResolution resolution;
    public static final float windowGap = 8.0f;
    public static final float margin = 5.0f;

    public CheckboxSettingElement(Translation class254Var, Translation class254Var2, Supplier<List<Integer>> supplier) {
        this(class254Var, class254Var2, supplier, true);
    }

    public CheckboxSettingElement(Translation class254Var, Translation class254Var2, Supplier<List<Integer>> supplier, boolean z) {
        this.titleFont = Fonts.INTER_SEMIBOLD.get();
        this.descriptionFont = Fonts.INTER_MEDIUM.get();
        this.checkmarkIcon = new GlTexture(new ClasspathResource("/icons/menu/new/checkmark.png"));
        this.keybindIcon = new GlTexture(new ClasspathResource("/icons/menu/new/keyboard.png"));
        this.checkAnimator = new ToggleAnimator(Easings.EASE_IN_OUT_CUBIC);
        this.clickBehavior = new ClickableBehavior();
        this.highlightAnimation = new HighlightAnimation(300, Easings.EASE_IN_OUT_CUBIC);
        this.lastToggledState = false;
        this.wrappedDescription = null;
        this.descriptionDirty = true;
        this.cachedWidth = -1.0f;
        this.cachedDescriptionText = null;
        this.descriptionHeight = 0.0f;
        this.wrappedName = null;
        this.nameDirty = true;
        this.cachedNameText = null;
        this.nameHeight = 0.0f;
        this.keybindButtonBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
        this.languageChangeCallback = class226Var -> {
            this.descriptionDirty = true;
            this.cachedWidth = -1.0f;
            this.cachedDescriptionText = null;
            this.nameDirty = true;
            this.cachedNameText = null;
        };
        this.name = class254Var;
        this.description = class254Var2;
        this.showKeybindButton = z;
        this.keybindWindow = new KeybindConfigWindow(Lang.BINDING_MODULE, class254Var.effective(), supplier);
        Expensive.INSTANCE.menuWindow().priorityOverlayHandler().registerPopup(this.keybindWindow);
        this.clickBehavior.clickCallback(this::invert);
        this.keybindButton = new IconButtonWidget(this.keybindIcon, this::toggleKeybindWindow, 16.0f, 16.0f);
        Expensive.INSTANCE.eventDispatcher().register(LanguageChangeEvent.class, this.languageChangeCallback);
    }

    public void setupBindType(Supplier<BindMode> supplier, Consumer<BindMode> consumer) {
        if (this.keybindWindow != null) {
            this.keybindWindow.bindType(supplier, consumer);
        }
    }

    public void onChange(Consumer<List<Integer>> consumer) {
        this.keybindWindow.onChange(consumer);
    }

    public void toggleKeybindWindow() {
        float fDpiScaleFactor = Expensive.INSTANCE.windowController().dpiScaleFactor();
        if (this.keybindWindow.isOpen() || this.resolution == null) {
            this.keybindWindow.closeWindow();
            return;
        }
        float fScreenWidth = this.resolution.screenWidth() / fDpiScaleFactor;
        float fScreenHeight = this.resolution.screenHeight() / fDpiScaleFactor;
        float fWidth = this.keybindWindow.width();
        float f = this.keybindWindow.totalHeight();
        float fX = this.keybindButtonBounds.x();
        float fY = this.keybindButtonBounds.y() - this.viewportTop;
        float fWidth2 = this.keybindButtonBounds.width();
        float fHeight = this.keybindButtonBounds.height();
        float f2 = fX + fWidth2 + windowGap;
        float f3 = (fX - windowGap) - fWidth;
        if (f2 + fWidth > fScreenWidth - margin && f3 >= margin) {
            f2 = f3;
        }
        float fMax = Math.max(margin, Math.min(f2, (fScreenWidth - fWidth) - margin));
        float f4 = (fY + (fHeight / 2.0f)) - (f / 2.0f);
        if (f4 + f > fScreenHeight - margin) {
            f4 = (fScreenHeight - f) - margin;
        }
        if (f4 < margin) {
            f4 = 5.0f;
        }
        this.keybindWindow.openWindow();
        this.keybindWindow.setPosition(fMax, f4);
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
        this.resolution = class699Var.resolution();
        float fWidth = (((f - 18.0f) - (this.showKeybindButton ? this.keybindButton.width() : 0.0f)) - 10.0f) - margin;
        updateWrappedName();
        updateWrappedDescription(fWidth);
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        float fX = (x() + f) - 18.0f;
        float f3 = 18.0f / 2.0f;
        float fY = (y() + (height() / 2.0f)) - f3;
        float fY2 = y();
        float fValue = this.highlightAnimation.value();
        class699Var.text(this.titleFont, this.wrappedName != null ? this.wrappedName : this.name.effective(), nameTextSize, x(), fY2, class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(200).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(50).argb()), fValue));
        if (this.wrappedDescription != null) {
            class699Var.text(this.descriptionFont, this.wrappedDescription, descriptionTextSize, x(), fY2 + (this.nameHeight > 0.0f ? this.nameHeight : this.titleFont.getHeight(14.0f)) + descriptionGap, class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb()), fValue));
        }
        if (this.showKeybindButton) {
            float fX2 = (((x() + f) - 18.0f) - 10.0f) - this.keybindButton.width();
            float fHeight = (fY + f3) - (this.keybindButton.height() / 2.0f);
            this.keybindButton.setPosition(fX2, fHeight);
            this.keybindButton.setColor(class764VarPalette.text().tone(800).argb());
            this.keybindButton.render(class699Var);
            this.keybindButtonBounds.withPosition(fX2, fHeight).withSize(this.keybindButton.width(), this.keybindButton.height());
        } else {
            this.keybindButtonBounds.withSize(0.0f, 0.0f);
        }
        int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(500).argb());
        int iComputeColor2 = class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(400).argb());
        int iComputeColor3 = class115VarColorStack.computeColor(class764VarPalette.accentBright().argb());
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.interpolate(iComputeColor, iComputeColor2, this.clickBehavior.hoverAnimation()), class115VarColorStack.interpolate(iComputeColor3, class115VarColorStack.autoBrightenDarken(iComputeColor3, 0.1f), this.clickBehavior.hoverAnimation()), this.checkAnimator);
        int iComputeColor4 = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(700).argb());
        int iComputeColor5 = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(600).argb());
        int iComputeColor6 = class115VarColorStack.computeColor(class764VarPalette.accent().argb());
        class699Var.fillOutlinedRoundedRect(fX, fY, 18.0f, 18.0f, margin, 2.5f, iInterpolate, class115VarColorStack.interpolate(class115VarColorStack.interpolate(iComputeColor4, iComputeColor5, this.clickBehavior.hoverAnimation()), class115VarColorStack.interpolate(iComputeColor6, class115VarColorStack.autoBrightenDarken(iComputeColor6, 0.1f), this.clickBehavior.hoverAnimation()), this.checkAnimator));
        class699Var.texture(this.checkmarkIcon, (fX + (18.0f / 2.0f)) - Math.round(this.checkmarkIcon.width() / 2.0f), (fY + (18.0f / 2.0f)) - Math.round(this.checkmarkIcon.height() / 2.0f), this.checkmarkIcon.width(), this.checkmarkIcon.height(), class115VarColorStack.computeColor(16777215, class115VarColorStack.alphaMultiplier() * this.checkAnimator.smoothAnimation()));
        this.clickBehavior.setDimensions(x(), y(), f, height());
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        this.keybindWindow.layout(class698Var);
        if (this.showKeybindButton) {
            this.keybindButton.layout(class698Var);
        }
        this.checkmarkIcon.setDimensions(nameTextSize, nameTextSize);
    }

    public float textHeight(String str, int i, int i2) {
        float height = this.titleFont.getHeight(i);
        if (this.description != null) {
            height = height + descriptionGap + this.descriptionFont.getHeightWithLineBreaks(str, i2);
        }
        return height;
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        if (this.showKeybindButton && this.keybindButton.handleInput(class688Var, z)) {
            return true;
        }
        return this.clickBehavior.handleInput(class688Var, z);
    }

    @Override
    public float height() {
        if (this.parent != null) {
            float fWidth = (((this.parent.width() - 18.0f) - (this.showKeybindButton ? this.keybindButton.width() : 0.0f)) - 10.0f) - margin;
            updateWrappedName();
            if (this.description != null) {
                updateWrappedDescription(fWidth);
            }
        }
        float height = this.nameHeight > 0.0f ? this.nameHeight : this.titleFont.getHeight(14.0f);
        if (this.description != null && this.wrappedDescription != null) {
            height += descriptionGap + this.descriptionHeight;
        }
        return Math.max(height, 18.0f);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        boolean zBooleanValue;
        if (this.toggledStateSupplier != null && (zBooleanValue = this.toggledStateSupplier.get().booleanValue()) != this.lastToggledState) {
            this.checkAnimator.state(zBooleanValue);
            this.lastToggledState = zBooleanValue;
        }
        if (this.showKeybindButton) {
            this.keybindButton.animation(class141Var);
        }
        this.keybindWindow.animation(class141Var);
        if (this.visibilitySupplier != null) {
            visible(this.visibilitySupplier.get().booleanValue());
        }
        this.highlightAnimation.animate(class141Var);
        this.checkAnimator.animate(class141Var);
        this.clickBehavior.animate(class141Var);
        super.animation(class141Var);
    }

    @Override
    public void handleClose() {
        this.keybindWindow.closeWindow();
        Expensive.INSTANCE.eventDispatcher().unregister(LanguageChangeEvent.class, this.languageChangeCallback);
        super.handleClose();
    }

    @Override
    public void collectBloomElements(RenderCommandQueue class676Var) {
        this.keybindWindow.collectBloomElements(class676Var);
        super.collectBloomElements(class676Var);
    }

    public void updateWrappedName() {
        String strEffective = this.name == null ? null : this.name.effective();
        if (strEffective == null || strEffective.equals("null")) {
            this.wrappedName = strEffective;
            this.nameHeight = this.titleFont.getHeight(nameTextSize);
            this.nameDirty = false;
            this.cachedNameText = strEffective;
            return;
        }
        if (this.nameDirty || !strEffective.equals(this.cachedNameText)) {
            this.wrappedName = strEffective;
            this.nameHeight = this.titleFont.getHeightWithLineBreaks(strEffective, nameTextSize);
            this.cachedNameText = strEffective;
            this.nameDirty = false;
        }
    }

    public void updateWrappedDescription(float f) {
        if (this.description == null || this.description.effective() == null || this.description.effective().equals("null")) {
            this.wrappedDescription = null;
            this.descriptionHeight = 0.0f;
            this.descriptionDirty = false;
            this.cachedWidth = f;
            this.cachedDescriptionText = null;
            return;
        }
        String strEffective = this.description.effective();
        if (this.descriptionDirty || f != this.cachedWidth || strEffective == null || !strEffective.equals(this.cachedDescriptionText)) {
            if (strEffective == null) {
                strEffective = "";
            }
            this.wrappedDescription = StringUtil.formatTextToFitWidth(strEffective, f, this.descriptionFont, descriptionTextSize);
            this.descriptionHeight = this.descriptionFont.getHeightWithLineBreaks(this.wrappedDescription, descriptionTextSize);
            this.cachedWidth = f;
            this.cachedDescriptionText = strEffective;
            this.descriptionDirty = false;
        }
    }

    public void invert() {
        if (this.changeAction != null) {
            this.changeAction.run();
        }
    }

    public Translation name() {
        return this.name;
    }

    public CheckboxSettingElement toggledSupplier(Supplier<Boolean> supplier) {
        this.toggledStateSupplier = supplier;
        return this;
    }

    public CheckboxSettingElement visibleSupplier(Supplier<Boolean> supplier) {
        this.visibilitySupplier = supplier;
        return this;
    }

    public CheckboxSettingElement changeRunnable(Runnable runnable) {
        this.changeAction = runnable;
        return this;
    }
}
