package aethereal.ui;
import aethereal.model.ActionDialogData;
import aethereal.resource.ClasspathResource;
import aethereal.render.ColorStack;
import aethereal.model.CursorMoveInput;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.event.InputEvent;
import aethereal.model.InputEventContext;
import aethereal.model.KeyInput;
import aethereal.Lang;
import aethereal.model.LayoutScaleContext;
import aethereal.model.MouseButtonInput;
import aethereal.render.MsdfFont;
import aethereal.model.ScrollInput;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.model.Translation;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.util.Objects;

public class ActionConfirmDialog extends AbstractFrame {
    public final MsdfFont semiBoldFont;
    public final MsdfFont mediumFont;
    public final GlTexture errorIcon;
    public final GlTexture progressIcon;
    public static final Translation confirmLabelText = Lang.ACTION_CONFIRM;
    public static final String clickIconPath = "/icons/menu/new/click.png";
    public static final float defaultWidth = 320.0f;
    public static final float contentPadding = 12.0f;
    public static final float boxCornerRadius = 10.0f;
    public static final float elementSpacing = 8.0f;
    public static final float boxOutlineThickness = 2.5f;
    public static final float windowCornerRadius = 8.0f;
    public static final float sectionSpacing = 16.0f;
    public static final int animationDurationMs = 220;
    public static final float backgroundAlpha = 0.8f;
    public String errorMessage;
    public final ToggleAnimator errorAnimator;

    public final WidgetBounds errorBounds;
    public final WidgetBounds dialogBounds;
    public final ToggleAnimator openAnimator;
    public final LoadingButtonWidget confirmButton;
    public IconLabelBadge iconBadge;

    public ActionDialogData dialogData;
    int layoutFramesRemaining;
    public boolean opened;

    public ActionConfirmDialog() {
        super(new FrameElementColumn(296.0f, contentPadding));
        this.semiBoldFont = Fonts.INTER_SEMIBOLD.get();
        this.mediumFont = Fonts.INTER_MEDIUM.get();
        this.errorIcon = new GlTexture(new ClasspathResource("/icons/menu/new/error.png"));
        this.progressIcon = new GlTexture(new ClasspathResource("/icons/menu/new/progress.png"));
        this.errorMessage = null;
        this.errorAnimator = new ToggleAnimator(animationDurationMs, Easings.EASE_IN_OUT_CUBIC);
        this.errorBounds = new WidgetBounds(0.0f, 0.0f, defaultWidth, 0.0f);
        this.dialogBounds = new WidgetBounds(0.0f, 0.0f, defaultWidth, 0.0f);
        this.openAnimator = new ToggleAnimator(animationDurationMs, Easings.EASE_IN_OUT_CUBIC);
        this.layoutFramesRemaining = 2;
        setSize(MenuWindow.MENU_WIDTH, MenuWindow.MENU_HEIGHT);
        this.confirmButton = new LoadingButtonWidget(new GlTexture(new ClasspathResource(clickIconPath)), 12, confirmLabelText, 296, 25, this::confirm, 8.0f, true);
        this.confirmButton.loadingVisuals(this.progressIcon, Lang.ACTION_LOADING);
        addChild(this.confirmButton);
    }

    public void setLoading(boolean z, String str) {
        if (str != null && !str.isBlank()) {
            this.confirmButton.loadingVisuals(this.progressIcon, Translation.clearText(str));
        }
        this.confirmButton.loading(z);
    }

    public void showError(String str) {
        this.errorMessage = str;
        this.errorAnimator.state(true);
    }

    public void hideError() {
        this.errorAnimator.state(false);
        if (this.errorAnimator.isZero()) {
            this.errorMessage = null;
        }
    }

    public void open(ActionDialogData class778Var) {
        Objects.requireNonNull(class778Var, "ActionData cannot be null");
        this.dialogData = class778Var;
        this.iconBadge = new IconLabelBadge(class778Var.icon(), class778Var.placeholderText());
        if (class778Var.needsChangePlaceholderColor()) {
            this.iconBadge.setBaseColor(Integer.valueOf(class778Var.placeHolderColor()));
        }
        this.confirmButton.primary(new GlTexture(new ClasspathResource(clickIconPath)), 12, class778Var.confirmLabel() != null ? class778Var.confirmLabel() : confirmLabelText);
        this.confirmButton.loading(false);
        this.confirmButton.enabled(class778Var.enableConfirmButton() != null ? class778Var.enableConfirmButton() : () -> {
            return true;
        });
        this.frameElementsContainer.clearElements();
        if (class778Var.contentConfigurator() != null) {
            class778Var.contentConfigurator().accept(this.frameElementsContainer);
        }
        this.layoutFramesRemaining = 2;
        this.opened = !this.opened;
        this.openAnimator.state(this.opened);
    }

    public void close() {
        this.opened = false;
        this.openAnimator.state(false);
        hideError();
        setLoading(false, "");
    }

    public boolean isOpen() {
        return this.opened || !this.openAnimator.isZero();
    }

    public float computeContentHeight() {
        float fHeight = 16.0f;
        if (this.iconBadge != null) {
            fHeight = sectionSpacing + this.iconBadge.height() + 8.0f;
        }
        float height = fHeight + this.semiBoldFont.getHeight(sectionSpacing);
        if (this.dialogData.description() != null) {
            height += 8.0f + this.mediumFont.getHeightWithLineBreaks(this.dialogData.description().effective(), 12);
        }
        float fHeight2 = height + sectionSpacing;
        if (this.frameElementsContainer.height() > 0.0f) {
            fHeight2 = fHeight2 + this.frameElementsContainer.height() + sectionSpacing;
        }
        return fHeight2 + this.confirmButton.height() + sectionSpacing;
    }

    public float computeErrorHeight() {
        float heightWithLineBreaks = 24.0f;
        if (this.errorMessage != null) {
            heightWithLineBreaks = 24.0f + this.semiBoldFont.getHeightWithLineBreaks(this.errorMessage, 12);
        }
        return heightWithLineBreaks;
    }

    @Override
    public void render(DrawCtx class699Var) {
        if (this.openAnimator.isZero() || this.dialogData == null) {
            return;
        }
        if (this.layoutFramesRemaining > 0) {
            this.layoutFramesRemaining--;
            this.dialogBounds.withSize(this.dialogData.width() > 0.0f ? this.dialogData.width() : defaultWidth, computeContentHeight());
        }
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        class115VarColorStack.push();
        try {
            class115VarColorStack.alpha(this.openAnimator.smoothAnimation());
            class699Var.roundedBlur(Expensive.INSTANCE.windowController().searchBlur().getBlurFramebuffer().getColorAttachment(), x(), y(), width(), height(), 8.0f, class115VarColorStack.white());
            class699Var.fillRoundedRect(x() - 1.0f, y(), width() + 2.0f, height() + 1.0f, 8.0f, class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb(), backgroundAlpha));
            class115VarColorStack.push();
            class115VarColorStack.alpha(this.errorAnimator.smoothAnimation());
            class699Var.fillOutlinedRoundedRect(this.errorBounds.x(), this.errorBounds.y(), this.errorBounds.width(), this.errorBounds.height(), boxCornerRadius, boxOutlineThickness, class115VarColorStack.computeColor(15620209, 0.3f), class115VarColorStack.computeColor(15620209, 0.07f));
            class699Var.textureVerticalC(this.errorIcon, this.errorBounds.x() + contentPadding, this.errorBounds.y() + (this.errorBounds.height() / 2.0f), this.errorIcon.width(), this.errorIcon.height(), class115VarColorStack.computeColor(class764VarPalette.error().tone(500).argb()));
            if (this.errorMessage != null) {
                class699Var.text(this.semiBoldFont, this.errorMessage, 12, this.errorBounds.x() + contentPadding + this.errorIcon.width() + 8.0f, (this.errorBounds.y() + (this.errorBounds.height() / 2.0f)) - (this.semiBoldFont.getHeightWithLineBreaks(this.errorMessage, 12) / 2.0f), class115VarColorStack.computeColor(class764VarPalette.error().tone(500).argb()));
            }
            class115VarColorStack.pop();
            class699Var.fillOutlinedRoundedRect(this.dialogBounds.x(), this.dialogBounds.y(), this.dialogBounds.width(), this.dialogBounds.height(), boxCornerRadius, boxOutlineThickness, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(500).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(800).argb()));
            float fY = this.dialogBounds.y() + sectionSpacing;
            if (this.iconBadge != null) {
                this.iconBadge.render(class699Var);
                fY = this.iconBadge.y() + this.iconBadge.height() + 8.0f;
            }
            class699Var.text(this.semiBoldFont, this.dialogData.title().effective(), 16, this.dialogBounds.x() + contentPadding, fY, class115VarColorStack.computeColor(class764VarPalette.text().tone(100).argb()));
            float height = fY + this.semiBoldFont.getHeight(sectionSpacing) + 8.0f;
            if (this.dialogData.description() != null) {
                class699Var.text(this.mediumFont, this.dialogData.description().effective(), 12, this.dialogBounds.x() + contentPadding, height, class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()));
            }
            if (this.frameElementsContainer.height() > 0.0f) {
                renderFrameElements(class699Var);
            }
            this.confirmButton.baseColor(class115VarColorStack.computeColor(class764VarPalette.accent().argb()));
            this.confirmButton.textColor(class115VarColorStack.computeColor(ThemePalette.white.argb()));
            this.confirmButton.outlineColor(class115VarColorStack.computeColor(class764VarPalette.accentBright().argb()));
            this.confirmButton.render(class699Var);
        } finally {
            class115VarColorStack.pop();
        }
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        if (this.dialogData == null) {
            return;
        }
        this.errorIcon.setDimensions(15, 15);
        this.errorBounds.withSize(this.dialogData.width() > 0.0f ? this.dialogData.width() : defaultWidth, computeErrorHeight());
        float fX = x() + (width() / 2.0f);
        float fY = y() + (height() / 2.0f);
        float fHeight = this.errorBounds.height() * this.errorAnimator.smoothAnimation();
        float fSmoothAnimation = 6.0f * this.errorAnimator.smoothAnimation();
        float fHeight2 = fY - (((fHeight + fSmoothAnimation) + this.dialogBounds.height()) / 2.0f);
        if (this.errorMessage != null && !this.errorAnimator.isZero()) {
            this.errorBounds.withPosition(fX - (this.errorBounds.width() / 2.0f), fHeight2);
        }
        this.dialogBounds.withPosition(fX - (this.dialogBounds.width() / 2.0f), fHeight2 + fHeight + fSmoothAnimation);
        float fY2 = this.dialogBounds.y() + sectionSpacing;
        if (this.iconBadge != null) {
            this.iconBadge.setPosition(this.dialogBounds.x() + contentPadding, fY2);
            this.iconBadge.layout(class698Var);
            fY2 = this.iconBadge.y() + this.iconBadge.height() + 8.0f;
        }
        float height = fY2 + this.semiBoldFont.getHeight(sectionSpacing);
        if (this.dialogData.description() != null) {
            height += 8.0f + this.mediumFont.getHeightWithLineBreaks(this.dialogData.description().effective(), 12);
        }
        float fHeight3 = height + sectionSpacing;
        if (this.frameElementsContainer.height() > 0.0f) {
            this.frameElementsContainer.setPosition(this.dialogBounds.x() + contentPadding, fHeight3);
            fHeight3 += this.frameElementsContainer.height() + sectionSpacing;
        }
        this.confirmButton.setPosition(this.dialogBounds.x() + contentPadding, fHeight3);
        this.confirmButton.layout(class698Var);
        super.layout(class698Var);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.openAnimator.animate(class141Var);
        this.confirmButton.animation(class141Var);
        this.errorAnimator.animate(class141Var);
        super.animation(class141Var);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        if (this.openAnimator.isZero() || this.dialogData == null) {
            return false;
        }
        if (!z) {
            InputEvent class691VarInputEvent = class688Var.inputEvent();
            if (class691VarInputEvent instanceof KeyInput) {
                KeyInput class696Var = (KeyInput) class691VarInputEvent;
                if (class696Var.keyAction().press() && (class696Var.keyCode() == 257 || class696Var.keyCode() == 335)) {
                    if ((this.dialogData.enableConfirmButton() == null || this.dialogData.enableConfirmButton().getAsBoolean()) && !this.confirmButton.loading() && this.dialogData.onConfirm() != null) {
                        confirm();
                        return true;
                    }
                }
            }
        }
        if (((class688Var.inputEvent() instanceof ScrollInput) && !z) || this.confirmButton.handleInput(class688Var, z) || super.handleInput(class688Var, z)) {
            return true;
        }
        if (z) {
            return false;
        }
        InputEvent class691VarInputEvent2 = class688Var.inputEvent();
        if (class691VarInputEvent2 instanceof KeyInput) {
            KeyInput class696Var2 = (KeyInput) class691VarInputEvent2;
            if ((class696Var2.keyAction().press() || class696Var2.keyAction().repeat()) && class696Var2.keyCode() == 256) {
                close();
                return true;
            }
        }
        InputEvent class691VarInputEvent3 = class688Var.inputEvent();
        if (class691VarInputEvent3 instanceof MouseButtonInput) {
            MouseButtonInput class693Var = (MouseButtonInput) class691VarInputEvent3;
            if (class693Var.action().press() && class693Var.button() == 0) {
                if (class688Var.inArea(this.dialogBounds.x(), this.dialogBounds.y(), this.dialogBounds.width(), this.dialogBounds.height())) {
                    return true;
                }
                close();
                return true;
            }
        }
        return class688Var.inputEvent() instanceof CursorMoveInput;
    }

    public void confirm() {
        if (this.dialogData == null || this.dialogData.onConfirm() == null) {
            return;
        }
        this.dialogData.onConfirm().run();
    }

    @Override
    public float contentHeight() {
        return this.dialogBounds.height();
    }

    public boolean isOpened() {
        return this.opened;
    }
}
