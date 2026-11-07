package aethereal.ui.setting;
import aethereal.resource.ClasspathResource;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.util.DrawEngine;
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
import aethereal.ui.TextInputField;
import aethereal.render.ThemePalette;
import aethereal.model.Translation;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class TextFieldSettingElement extends ModuleFrame {
    public static final int nameFontSize = 14;
    public static final int descriptionFontSize = 13;
    public static final int inputFontSize = 13;
    public static final float T = 3.0f;
    public static final float U = 10.0f;
    public static final float V = 25.0f;
    public static final float W = 110.0f;
    public final Translation name;
    public final Translation placeholder;
    public final Translation description;
    public final boolean masked;

    public Consumer<String> changeCallback;
    public Supplier<Boolean> visibilitySupplier;
    public final MsdfFont nameFont = Fonts.INTER_SEMIBOLD.get();
    public final MsdfFont textFont = Fonts.INTER_MEDIUM.get();
    public final GlTexture pencilTexture = new GlTexture(new ClasspathResource("/icons/menu/new/pencil.png"));
    public final TextInputField inputField = new TextInputField(this.textFont, 13);
    public final HighlightAnimation highlightAnimation = new HighlightAnimation(300, Easings.EASE_IN_OUT_CUBIC);
    public final WidgetBounds inputBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
    public float scaleFactor = 1.0f;

    public String wrappedDescription = null;
    public boolean descriptionDirty = true;
    public float lastWrapWidth = -1.0f;
    public String lastDescription = null;
    public float descriptionHeight = 0.0f;
    public final EventCallback<LanguageChangeEvent> languageChangeCallback = class226Var -> {
        this.descriptionDirty = true;
        this.lastWrapWidth = -1.0f;
        this.lastDescription = null;
    };

    public TextFieldSettingElement(Translation class254Var, Translation class254Var2, Translation class254Var3, boolean z, int i, boolean z2, Supplier<String> supplier) {
        this.name = class254Var;
        this.placeholder = class254Var3;
        this.description = class254Var2;
        this.masked = z2;
        if (supplier != null) {
            this.inputField.append(supplier.get());
        }
        this.inputField.numbersOnly(z);
        if (i != -1) {
            this.inputField.maxLength(i);
        }
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
        String strEffective;
        updateDescription(f);
        ThemePalette class764VarPalette = class699Var.theme().palette();
        DrawEngine class154VarDrawEngine = class699Var.drawEngine();
        ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
        float fY = y();
        float fValue = this.highlightAnimation.value();
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(200).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(50).argb()), fValue);
        int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(600).argb());
        int iComputeColor2 = class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(400).argb());
        int iComputeColor3 = class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb());
        int iComputeColor4 = class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb());
        int iInterpolate2 = class115VarColorStack.interpolate(iComputeColor, class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(500).argb()), this.inputField.hoverAnimation());
        int iInterpolate3 = class115VarColorStack.interpolate(iComputeColor2, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(300).argb()), this.inputField.hoverAnimation());
        String strText = (this.inputField.focused() || !this.masked) ? this.inputField.text() : "*".repeat(this.inputField.text().length());
        if (this.placeholder != null && strText.isEmpty() && !this.inputField.focused()) {
            iComputeColor3 = class115VarColorStack.computeColor(class764VarPalette.text().tone(600).argb());
        }
        float fX = this.inputBounds.x() + 8.0f;
        if (!strText.isEmpty() || this.inputField.focused()) {
            strEffective = strText;
        } else {
            strEffective = this.placeholder != null ? this.placeholder.effective() : "";
        }
        String str = strEffective;
        if (this.inputField.focused()) {
            class699Var.window().interceptKeyboard(true);
        }
        float fTextWidthPhysical = class699Var.textWidthPhysical(this.nameFont, this.name.effective(), nameFontSize);
        float f3 = (f - W) - U;
        if (this.wrappedDescription != null || fTextWidthPhysical >= f3) {
            class699Var.text(this.nameFont, this.name.effective(), nameFontSize, x(), fY, iInterpolate);
            if (this.wrappedDescription != null) {
                fY += this.nameFont.getHeight(14.0f) + T;
                class699Var.text(this.textFont, this.wrappedDescription, 13, x(), fY, class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb()), fValue));
            }
            this.inputBounds.withPosition(x(), fY + (this.description != null ? this.descriptionHeight : this.nameFont.getHeight(14.0f)) + U).withSize(f, V);
        } else {
            this.inputBounds.withPosition((x() + f) - W, y()).withSize(W, V);
            class699Var.text(this.nameFont, this.name.effective(), nameFontSize, x(), (this.inputBounds.y() + (this.inputBounds.height() / 2.0f)) - (this.nameFont.getHeight(14.0f) / 2.0f), iInterpolate);
        }
        float fY2 = this.inputBounds.y() + (this.inputBounds.height() / 2.0f);
        class699Var.fillOutlinedRoundedRect(this.inputBounds.x(), this.inputBounds.y(), this.inputBounds.width(), this.inputBounds.height(), 6.0f, 2.5f, iInterpolate3, iInterpolate2);
        class699Var.textureVerticalC(this.pencilTexture, fX, fY2, this.pencilTexture.width(), this.pencilTexture.height(), iComputeColor4);
        float fWidth = (fX + (this.pencilTexture.width() + 6.0f)) - this.inputField.viewportOffset();
        class154VarDrawEngine.beginStencil();
        class699Var.fillOutlinedRoundedRect(this.inputBounds.x() + 8.0f + this.pencilTexture.width() + 6.0f, this.inputBounds.y(), this.inputBounds.width() - 40.0f, this.inputBounds.height(), 6.0f, 2.5f, iInterpolate3, iInterpolate2);
        class154VarDrawEngine.prepareStencil(1);
        float height = this.textFont.getHeight(13.0f);
        if (this.inputField.hasSelection()) {
            int iSelMin = this.inputField.selMin();
            int iSelMax = this.inputField.selMax();
            float fTextWidthPhysical2 = fWidth + class699Var.textWidthPhysical(this.textFont, str.substring(0, iSelMin), 13);
            class699Var.fillRoundedRect(fTextWidthPhysical2, (fY2 - (height / 2.0f)) - 1.0f, ((fWidth + class699Var.textWidthPhysical(this.textFont, str.substring(0, iSelMax), 13)) + 1.0f) - fTextWidthPhysical2, height + 2.0f, 4.0f, class115VarColorStack.computeColor(class764VarPalette.accent().argb(), 128));
        }
        class699Var.text(this.textFont, str, 13, fWidth, fY2 - (height / 2.0f), iComputeColor3);
        if (this.inputField.isCursorVisible() && !this.inputField.hasSelection()) {
            class699Var.fillRect(fWidth + class699Var.textWidthPhysical(this.textFont, str.substring(0, this.inputField.cursorIndex()), 13), fY2 - (height / 2.0f), 1.0f, height, class115VarColorStack.computeColor(16777215));
        }
        class154VarDrawEngine.endStencil();
        this.inputField.changeText(str2 -> {
            if (this.changeCallback == null) {
                return;
            }
            this.changeCallback.accept(str2);
        });
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        this.pencilTexture.setDimensions(12, 12);
        this.scaleFactor = class698Var.scaleFactor();
        this.inputField.listen(this.inputBounds.x(), this.inputBounds.y(), this.inputBounds.width(), this.inputBounds.height(), this.inputBounds.width() - 47.0f, this.inputBounds.x() + 8.0f + this.pencilTexture.width() + 6.0f, this.scaleFactor);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        return this.inputField.handleInput(class688Var, z);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        if (this.visibilitySupplier != null) {
            visible(this.visibilitySupplier.get().booleanValue());
        }
        this.inputField.animate(class141Var);
        this.highlightAnimation.animate(class141Var);
        super.animation(class141Var);
    }

    @Override
    public float height() {
        float fMax;
        if (this.description != null && this.descriptionDirty && this.parent != null) {
            updateDescription(this.parent.width() - 18.0f);
        }
        float fMethod003 = measureWidth(this.name.effective(), nameFontSize);
        float fWidth = (this.parent.width() - W) - U;
        if ((this.description == null || this.wrappedDescription == null) && fMethod003 < fWidth) {
            fMax = Math.max(this.nameFont.getHeight(14.0f), V);
        } else {
            float height = 0.0f + this.nameFont.getHeight(14.0f);
            if (this.description != null && this.wrappedDescription != null) {
                height += T + this.descriptionHeight;
            }
            fMax = height + 35.0f;
        }
        return fMax;
    }

    public float measureWidth(String str, int i) {
        if (this.scaleFactor <= 0.0f || this.scaleFactor == 1.0f) {
            return this.nameFont.getWidth(str, i);
        }
        return this.nameFont.getWidth(str, Math.max(1, Math.round(i * this.scaleFactor))) / this.scaleFactor;
    }

    @Override
    public void handleClose() {
        this.inputField.focused(false);
        Expensive.INSTANCE.eventDispatcher().unregister(LanguageChangeEvent.class, this.languageChangeCallback);
        super.handleClose();
    }

    public void updateDescription(float f) {
        if (this.description == null || this.description.effective() == null || this.description.effective().equals("null")) {
            this.wrappedDescription = null;
            this.descriptionHeight = 0.0f;
            this.descriptionDirty = false;
            this.lastWrapWidth = f;
            this.lastDescription = null;
            return;
        }
        String strEffective = this.description.effective();
        if (this.descriptionDirty || f != this.lastWrapWidth || strEffective == null || !strEffective.equals(this.lastDescription)) {
            if (strEffective == null) {
                strEffective = "";
            }
            this.wrappedDescription = StringUtil.formatTextToFitWidth(strEffective, f, this.textFont, 13);
            this.descriptionHeight = this.textFont.getHeightWithLineBreaks(this.wrappedDescription, 13);
            this.lastWrapWidth = f;
            this.lastDescription = strEffective;
            this.descriptionDirty = false;
        }
    }

    public void focusAtEnd() {
        this.inputField.focusAtEnd();
    }

    public Translation name() {
        return this.name;
    }

    public TextFieldSettingElement onChange(Consumer<String> consumer) {
        this.changeCallback = consumer;
        return this;
    }

    public TextFieldSettingElement visibleSupplier(Supplier<Boolean> supplier) {
        this.visibilitySupplier = supplier;
        return this;
    }
}
