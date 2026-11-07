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
import aethereal.model.InputEventContext;
import aethereal.event.LanguageChangeEvent;
import aethereal.model.LayoutScaleContext;
import aethereal.ui.ModuleFrame;
import aethereal.render.MsdfFont;
import aethereal.util.StringUtil;
import aethereal.render.ThemePalette;
import aethereal.model.Translation;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.util.function.Supplier;

public class ButtonSettingElement extends ModuleFrame {
    public static final int nameTextSize = 14;
    public static final int descriptionTextSize = 13;
    public static final int buttonTextSize = 12;
    public static final float descriptionGap = 3.0f;
    public static final float horizontalPadding = 10.0f;
    public static final float buttonHeight = 25.0f;
    public static final float buttonWidth = 101.0f;
    public final Translation name;
    public final Translation description;
    public final Translation buttonLabel;
    public Supplier<Boolean> visibilitySupplier;
    public final MsdfFont titleFont = Fonts.INTER_SEMIBOLD.get();
    public final MsdfFont descriptionFont = Fonts.INTER_MEDIUM.get();
    public final GlTexture clickIcon = new GlTexture(new ClasspathResource("/icons/menu/new/click.png"));
    public final HighlightAnimation highlightAnimation = new HighlightAnimation(300, Easings.EASE_IN_OUT_CUBIC);
    public final WidgetBounds buttonBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
    public float scaleFactor = 1.0f;
    public final ClickableBehavior clickBehavior = new ClickableBehavior();
    public String wrappedDescription = null;
    public boolean descriptionDirty = true;
    public float cachedWidth = -1.0f;
    public String cachedDescriptionText = null;
    public float descriptionHeight = 0.0f;
    public final EventCallback<LanguageChangeEvent> languageChangeCallback = class226Var -> {
        this.descriptionDirty = true;
        this.cachedWidth = -1.0f;
        this.cachedDescriptionText = null;
    };

    public ButtonSettingElement(Translation class254Var, Translation class254Var2, Translation class254Var3, Runnable runnable) {
        this.name = class254Var;
        this.description = class254Var2;
        this.buttonLabel = class254Var3;
        this.clickBehavior.clickCallback(runnable);
        Expensive.INSTANCE.eventDispatcher().register(LanguageChangeEvent.class, this.languageChangeCallback);
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
        updateWrappedDescription(f);
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        float fY = y();
        float fValue = this.highlightAnimation.value();
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(200).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(50).argb()), fValue);
        int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(600).argb());
        int iComputeColor2 = class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(400).argb());
        int iComputeColor3 = class115VarColorStack.computeColor(class764VarPalette.text().tone(400).argb());
        int iComputeColor4 = class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb());
        int iInterpolate2 = class115VarColorStack.interpolate(iComputeColor, class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(500).argb()), this.clickBehavior.hoverAnimation());
        int iInterpolate3 = class115VarColorStack.interpolate(iComputeColor2, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(300).argb()), this.clickBehavior.hoverAnimation());
        float fTextWidthPhysical = class699Var.textWidthPhysical(this.titleFont, this.name.effective(), nameTextSize);
        float f3 = (f - buttonWidth) - horizontalPadding;
        float fX = x();
        float height = this.titleFont.getHeight(12.0f);
        float fTextWidthPhysical2 = class699Var.textWidthPhysical(this.titleFont, this.buttonLabel.effective(), buttonTextSize);
        if (this.wrappedDescription != null || fTextWidthPhysical >= f3) {
            class699Var.text(this.titleFont, this.name.effective(), nameTextSize, x(), fY, iInterpolate);
            if (this.wrappedDescription != null) {
                fY += this.titleFont.getHeight(14.0f) + descriptionGap;
                class699Var.text(this.descriptionFont, this.wrappedDescription, descriptionTextSize, x(), fY, class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb()), fValue));
            }
            this.buttonBounds.withPosition(x(), fY + (this.description != null ? this.descriptionHeight : this.titleFont.getHeight(14.0f)) + horizontalPadding).withSize(f, buttonHeight);
            float fY2 = this.buttonBounds.y() + (this.buttonBounds.height() / 2.0f);
            class699Var.fillOutlinedRoundedRect(this.buttonBounds.x(), this.buttonBounds.y(), this.buttonBounds.width(), this.buttonBounds.height(), 6.0f, 2.5f, iInterpolate3, iInterpolate2);
            class699Var.textureVerticalC(this.clickIcon, fX + horizontalPadding, fY2, this.clickIcon.width(), this.clickIcon.height(), iComputeColor4);
            class699Var.text(this.titleFont, this.buttonLabel.effective(), buttonTextSize, (this.buttonBounds.x() + (this.buttonBounds.width() / 2.0f)) - (fTextWidthPhysical2 / 2.0f), fY2 - (height / 2.0f), iComputeColor3);
        } else {
            this.buttonBounds.withPosition((x() + f) - buttonWidth, y()).withSize(buttonWidth, buttonHeight);
            float fY3 = this.buttonBounds.y() + (this.buttonBounds.height() / 2.0f);
            class699Var.text(this.titleFont, this.name.effective(), nameTextSize, x(), fY3 - (this.titleFont.getHeight(14.0f) / 2.0f), iInterpolate);
            class699Var.fillOutlinedRoundedRect(this.buttonBounds.x(), this.buttonBounds.y(), this.buttonBounds.width(), this.buttonBounds.height(), 6.0f, 2.5f, iInterpolate3, iInterpolate2);
            float f4 = fX + horizontalPadding;
            class699Var.textureVerticalC(this.clickIcon, f4, fY3, this.clickIcon.width(), this.clickIcon.height(), iComputeColor4);
            class699Var.text(this.titleFont, this.buttonLabel.effective(), buttonTextSize, f4 + this.clickIcon.width() + 6.0f, fY3 - (height / 2.0f), iComputeColor3);
        }
        this.clickBehavior.setDimensions(x(), y(), f, height());
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        this.clickIcon.setDimensions(buttonTextSize, buttonTextSize);
        this.scaleFactor = class698Var.scaleFactor();
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        return this.clickBehavior.handleInput(class688Var, z);
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
            updateWrappedDescription(this.parent.width() - 18.0f);
        }
        float fMethod002 = measureTextWidth(this.name.effective(), nameTextSize);
        float fWidth = (this.parent.width() - buttonWidth) - horizontalPadding;
        if ((this.description == null || this.wrappedDescription == null) && fMethod002 < fWidth) {
            fMax = Math.max(this.titleFont.getHeight(14.0f), buttonHeight);
        } else {
            float height = 0.0f + this.titleFont.getHeight(14.0f);
            if (this.description != null && this.wrappedDescription != null) {
                height += descriptionGap + this.descriptionHeight;
            }
            fMax = height + 35.0f;
        }
        return fMax;
    }

    public float measureTextWidth(String str, int i) {
        if (this.scaleFactor <= 0.0f || this.scaleFactor == 1.0f) {
            return this.titleFont.getWidth(str, i);
        }
        return this.titleFont.getWidth(str, Math.max(1, Math.round(i * this.scaleFactor))) / this.scaleFactor;
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        if (this.visibilitySupplier != null) {
            visible(this.visibilitySupplier.get().booleanValue());
        }
        this.highlightAnimation.animate(class141Var);
        this.clickBehavior.animate(class141Var);
        super.animation(class141Var);
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

    public Translation name() {
        return this.name;
    }

    public ButtonSettingElement visibleSupplier(Supplier<Boolean> supplier) {
        this.visibilitySupplier = supplier;
        return this;
    }
}
