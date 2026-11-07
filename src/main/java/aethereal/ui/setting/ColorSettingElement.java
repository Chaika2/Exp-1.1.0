package aethereal.ui.setting;
import aethereal.resource.ClasspathResource;
import aethereal.ui.ClickableBehavior;
import aethereal.ui.ColorPickerWindow;
import aethereal.render.ColorStack;
import aethereal.render.ColorSwatch2;
import aethereal.util.ColorUtil;
import aethereal.render.ColorValue;
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
import aethereal.util.StencilBufferUtil;
import aethereal.util.StringUtil;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.model.Translation;
import aethereal.util.WeightedEngine;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorSettingElement extends ModuleFrame {
    static final int nameTextSize = 14;
    static final int descriptionTextSize = 13;
    static final int hexTextSize = 10;
    static final float descriptionGap = 3.0f;
    static final float horizontalPadding = 10.0f;
    static final float barHeight = 25.0f;
    static final float verticalPadding = 10.0f;
    static final float swatchRadius = 15.0f;
    static final float swatchHoverScale = 1.5f;

    static final long copiedDurationMs = 1000;
    public final List<ColorSwatch2> swatches;
    public final Translation name;
    public final Translation description;
    public String wrappedDescription;
    public String cachedDescriptionText;
    public float descriptionHeight;
    public Supplier<Boolean> visibilitySupplier;

    public Consumer<Integer> colorCallback;
    public Consumer<Float> alphaCallback;
    public final Supplier<Integer> colorSupplier;
    public final Supplier<Float> alphaSupplier;
    public Runnable commitAction;
    public final MsdfFont titleFont = Fonts.INTER_SEMIBOLD.get();
    public final MsdfFont descriptionFont = Fonts.INTER_MEDIUM.get();
    public final MsdfFont hexFont = Fonts.INTER_EXTRA_BOLD.get();
    public final GlTexture brushIcon = new GlTexture(new ClasspathResource("/icons/menu/new/brush.png"));
    public final GlTexture checkmarkIcon = new GlTexture(new ClasspathResource("/icons/menu/new/checkmark.png"));
    public final GlTexture previewTexture = new GlTexture(new ClasspathResource("/icons/menu/new/container.png"));
    public final HighlightAnimation highlightAnimation = new HighlightAnimation(300, Easings.EASE_IN_OUT_CUBIC);
    public final EventCallback<LanguageChangeEvent> languageChangeCallback = class226Var -> {
        markDescriptionDirty();
    };
    public final ClickableBehavior barClickable = new ClickableBehavior();
    public final ClickableBehavior hexClickable = new ClickableBehavior();
    public final ToggleAnimator copiedAnimator = new ToggleAnimator(250, Easings.EASE_IN_OUT_CUBIC);
    public final ToggleAnimator hexAnimator = new ToggleAnimator(250, Easings.EASE_IN_OUT_CUBIC);
    public final ToggleAnimator copyAnimator = new ToggleAnimator(250, Easings.EASE_IN_OUT_CUBIC);
    public boolean copied = false;

    public long copiedTime = 0;
    public float hue = 0.0f;
    public float saturation = 1.0f;
    public float brightness = 1.0f;
    public float alpha = 1.0f;
    public ColorSwatch2 selectedSwatch = null;
    public float cachedWidth = -1.0f;
    public boolean descriptionDirty = true;
    public boolean pickerOpen = false;

    public ColorPickerWindow pickerWindow = null;

    public ColorSettingElement(Translation class254Var, Translation class254Var2, Supplier<Integer> supplier, Supplier<Float> supplier2) {
        this.name = class254Var;
        this.description = class254Var2;
        this.colorSupplier = supplier;
        this.alphaSupplier = supplier2;
        initFromSuppliers(supplier, supplier2);
        this.swatches = createSwatches();
        bindSwatchCallbacks();
        bindCopyCallback();
        this.barClickable.clickCallback(() -> {
            ColorPickerWindow class773Var = (ColorPickerWindow) Expensive.INSTANCE.windowController().getWindow(ColorPickerWindow.class);
            if (class773Var == null) {
                ColorPickerWindow class773Var2 = new ColorPickerWindow(this.hue, this.saturation, this.brightness, this.alpha);
                Expensive.INSTANCE.windowController().newWindow(class773Var2);
                class773Var2.colorConsumer(num -> {
                    if (this.colorCallback != null) {
                        this.colorCallback.accept(num);
                    }
                    float[] fArrRGBtoHSB = Color.RGBtoHSB(ColorUtil.red(num.intValue()), ColorUtil.green(num.intValue()), ColorUtil.blue(num.intValue()), (float[]) null);
                    this.hue = fArrRGBtoHSB[0];
                    this.saturation = fArrRGBtoHSB[1];
                    this.brightness = fArrRGBtoHSB[2];
                });
                class773Var2.applyCallback(() -> {
                    if (this.commitAction != null) {
                        this.commitAction.run();
                    }
                });
                class773Var2.alphaConsumer(f -> {
                    this.alpha = f.floatValue();
                    if (this.alphaCallback != null) {
                        this.alphaCallback.accept(Float.valueOf(this.alpha));
                    }
                });
                this.pickerWindow = class773Var2;
                this.pickerOpen = true;
                class773Var2.openPicker();
                return;
            }
            class773Var.updateColor(this.hue, this.saturation, this.brightness, this.alpha);
            class773Var.colorConsumer(num2 -> {
                if (this.colorCallback != null) {
                    this.colorCallback.accept(num2);
                }
                float[] fArrRGBtoHSB = Color.RGBtoHSB(ColorUtil.red(num2.intValue()), ColorUtil.green(num2.intValue()), ColorUtil.blue(num2.intValue()), (float[]) null);
                this.hue = fArrRGBtoHSB[0];
                this.saturation = fArrRGBtoHSB[1];
                this.brightness = fArrRGBtoHSB[2];
            });
            class773Var.applyCallback(() -> {
                if (this.commitAction != null) {
                    this.commitAction.run();
                }
            });
            class773Var.alphaConsumer(f2 -> {
                this.alpha = f2.floatValue();
                if (this.alphaCallback != null) {
                    this.alphaCallback.accept(Float.valueOf(this.alpha));
                }
            });
            if (class773Var.opened()) {
                class773Var.closePicker();
            } else {
                class773Var.openPicker();
            }
            this.pickerWindow = class773Var;
            this.pickerOpen = true;
        });
        Expensive.INSTANCE.eventDispatcher().register(LanguageChangeEvent.class, this.languageChangeCallback);
    }

    public void initFromSuppliers(Supplier<Integer> supplier, Supplier<Float> supplier2) {
        int iIntValue = supplier.get().intValue();
        float[] fArrRGBtoHSB = Color.RGBtoHSB(ColorUtil.red(iIntValue), ColorUtil.green(iIntValue), ColorUtil.blue(iIntValue), (float[]) null);
        this.hue = fArrRGBtoHSB[0];
        this.saturation = fArrRGBtoHSB[1];
        this.brightness = fArrRGBtoHSB[2];
        this.alpha = supplier2.get().floatValue();
    }

    public void syncFromSupplier() {
        if (this.pickerOpen) {
            return;
        }
        int iIntValue = this.colorSupplier.get().intValue();
        float fFloatValue = this.alphaSupplier.get().floatValue();
        int iHSBtoRGB = Color.HSBtoRGB(this.hue, this.saturation, this.brightness) & 16777215;
        int i = iIntValue & 16777215;
        int iMethod001 = (int) (clamp01(this.alpha) * 255.0f);
        int iMethod002 = (int) (clamp01(fFloatValue) * 255.0f);
        if (iHSBtoRGB != i || Math.abs(iMethod001 - iMethod002) > 1) {
            float[] fArrRGBtoHSB = Color.RGBtoHSB(ColorUtil.red(iIntValue), ColorUtil.green(iIntValue), ColorUtil.blue(iIntValue), (float[]) null);
            this.hue = fArrRGBtoHSB[0];
            this.saturation = fArrRGBtoHSB[1];
            this.brightness = fArrRGBtoHSB[2];
            this.alpha = fFloatValue;
            updateSelectedSwatch();
        }
    }

    public List<ColorSwatch2> createSwatches() {
        return List.of(new ColorSwatch2(ThemePalette.mistyBlue.argb()), new ColorSwatch2(ThemePalette.golden.argb()), new ColorSwatch2(ThemePalette.violet.argb()), new ColorSwatch2(ColorValue.fromHex("C5C6C8").argb()), new ColorSwatch2(ThemePalette.red.argb()), new ColorSwatch2(ThemePalette.mint.argb()), new ColorSwatch2(Expensive.INSTANCE.theme().palette().accent().argb()));
    }

    public void bindSwatchCallbacks() {
        this.swatches.forEach(class824Var -> {
            class824Var.clickable.clickCallback(() -> {
                selectSwatch(class824Var);
            });
        });
    }

    public void selectSwatch(ColorSwatch2 class824Var) {
        this.selectedSwatch = class824Var;
        if (this.colorCallback != null) {
            this.colorCallback.accept(Integer.valueOf(class824Var.color()));
        }
        applySwatchColor(class824Var);
        ColorPickerWindow class773Var = (ColorPickerWindow) Expensive.INSTANCE.windowController().getWindow(ColorPickerWindow.class);
        if (class773Var != null) {
            class773Var.updateColor(this.hue, this.saturation, this.brightness, this.alpha);
        }
        if (this.commitAction != null) {
            this.commitAction.run();
        }
    }

    public void applySwatchColor(ColorSwatch2 class824Var) {
        float[] fArrRGBtoHSB = Color.RGBtoHSB(ColorUtil.red(class824Var.color()), ColorUtil.green(class824Var.color()), ColorUtil.blue(class824Var.color()), (float[]) null);
        this.hue = fArrRGBtoHSB[0];
        this.saturation = fArrRGBtoHSB[1];
        this.brightness = fArrRGBtoHSB[2];
        int iAlpha = ColorUtil.alpha(class824Var.color());
        this.alpha = (iAlpha == 0 && (class824Var.color() >>> 24) == 0) ? 1.0f : iAlpha / 255.0f;
        if (this.alphaCallback != null) {
            this.alphaCallback.accept(Float.valueOf(this.alpha));
        }
    }

    public void bindCopyCallback() {
        this.hexClickable.clickCallback(() -> {
            StringUtil.copyToClipboard(String.format("#%06X", Integer.valueOf(Color.HSBtoRGB(this.hue, this.saturation, this.brightness) & 16777215)));
            this.copied = true;
            this.copiedTime = System.currentTimeMillis();
        });
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
        drawBar(class699Var, f, drawDescription(class699Var, drawName(class699Var, y())) + 10.0f);
        this.barClickable.setDimensions(x(), y(), f, height());
    }

    public void drawBar(DrawCtx class699Var, float f, float f2) {
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        class699Var.fillOutlinedRoundedRect(x(), f2, f, barHeight, 6.0f, 2.5f, class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(400).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(300).argb()), this.barClickable.hoverAnimation()), class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(600).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(500).argb()), this.barClickable.hoverAnimation()));
        float fMethod025 = drawSwatches(class699Var, class115VarColorStack, drawDivider(class699Var, class115VarColorStack, drawPreview(class699Var, class115VarColorStack, f2), f2) + 10.0f + 9.0f, f2);
        drawDivider(class699Var, class115VarColorStack, fMethod025, f2);
        drawHexLabel(class699Var, class764VarPalette, class115VarColorStack, fMethod025, f2, f);
    }

    public float drawPreview(DrawCtx class699Var, ColorStack class115Var, float f) {
        float fX = x() + 14.0f;
        int iComputeColor = class115Var.computeColor(Color.HSBtoRGB(this.hue, this.saturation, this.brightness), this.alpha);
        class699Var.texture(this.previewTexture, x(), f, 40.0f, barHeight, class115Var.interpolate(iComputeColor, class115Var.autoBrightenDarken(iComputeColor, 0.2f), this.barClickable.hoverAnimation().smoothAnimation()));
        return fX + this.brushIcon.width() + 12.0f;
    }

    public float drawDivider(DrawCtx class699Var, ColorStack class115Var, float f, float f2) {
        class699Var.fillRect(f, f2, 1.0f, barHeight, class115Var.computeColor(class699Var.theme().palette().surfaceOutline().tone(400).argb()));
        return f;
    }

    public float drawSwatches(DrawCtx class699Var, ColorStack class115Var, float f, float f2) {
        int i = 0;
        while (i < this.swatches.size()) {
            drawSwatch(class699Var, class115Var, this.swatches.get(i), f, f2);
            f += i < this.swatches.size() - 1 ? 19.0f : 18.0f;
            i++;
        }
        return f;
    }

    public void drawSwatch(DrawCtx class699Var, ColorStack class115Var, ColorSwatch2 class824Var, float f, float f2) {
        float f3 = f2 + 12.5f;
        class699Var.circle(f, f3, (10.0f + Math.max(class824Var.clickable.hoverAnimation().smoothAnimation() * swatchHoverScale, class824Var.selectAnim.smoothAnimation() * 2.0f)) / 2.0f, class115Var.computeColor(class824Var.color()));
        class824Var.clickable.setDimensions(f - (19.0f / 2.0f), f2, 19.0f, barHeight);
        if (class824Var == this.selectedSwatch) {
            class699Var.textureVerticalC(this.checkmarkIcon, f - 5.0f, f3, this.checkmarkIcon.width(), this.checkmarkIcon.height(), class115Var.computeColor(class699Var.theme().palette().surfaceBackground().tone(900).argb(), class824Var.selectAnim.smoothAnimation()));
        }
    }

    public void drawHexLabel(DrawCtx class699Var, ThemePalette class764Var, ColorStack class115Var, float f, float f2, float f3) {
        float fX = (x() + f3) - f;
        this.hexClickable.setDimensions(f, f2, fX, barHeight);
        String str = String.format("#%06X", Integer.valueOf(Color.HSBtoRGB(this.hue, this.saturation, this.brightness) & 16777215));
        float height = (f2 + 12.5f) - (this.hexFont.getHeight(10.0f) / 2.0f);
        float f4 = f + (fX / 2.0f);
        int iComputeColor = class115Var.computeColor(class764Var.text().tone(300).argb());
        if (this.copied) {
            drawCenteredText(class699Var, "Copied", f4, height, class115Var.computeColor(iComputeColor, this.copiedAnimator.smoothAnimation()));
        } else if (this.hexClickable.hoverAnimation().smoothAnimation() <= 0.01f) {
            drawCenteredText(class699Var, str, f4, height, class115Var.computeColor(iComputeColor, this.hexAnimator.smoothAnimation()));
        } else {
            drawCenteredText(class699Var, str, f4, height, class115Var.computeColor(iComputeColor, this.hexAnimator.smoothAnimation()));
            drawCenteredText(class699Var, "Copy", f4, height, class115Var.computeColor(iComputeColor, this.copyAnimator.smoothAnimation()));
        }
    }

    public void drawCenteredText(DrawCtx class699Var, String str, float f, float f2, int i) {
        class699Var.text(this.hexFont, str, hexTextSize, (f - (class699Var.textWidthPhysical(this.hexFont, str, hexTextSize) / 2.0f)) - 2.0f, f2, i);
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        this.brushIcon.setDimensions(12, 12);
        this.checkmarkIcon.setDimensions(hexTextSize, hexTextSize);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        boolean zHandleInput = this.hexClickable.handleInput(class688Var, z);
        Iterator<ColorSwatch2> it = this.swatches.iterator();
        while (it.hasNext()) {
            zHandleInput |= it.next().clickable.handleInput(class688Var, zHandleInput);
        }
        boolean zHandleInput2 = zHandleInput | this.barClickable.handleInput(class688Var, zHandleInput);
        return zHandleInput2 | super.handleInput(class688Var, zHandleInput2);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        if (this.visibilitySupplier != null) {
            visible(this.visibilitySupplier.get().booleanValue());
        }
        if (this.pickerWindow != null && !this.pickerWindow.opened()) {
            this.pickerWindow = null;
            this.pickerOpen = false;
        }
        syncFromSupplier();
        updateSelectedSwatch();
        this.barClickable.animate(class141Var);
        updateCopiedState();
        animateSwatches(class141Var);
        this.highlightAnimation.animate(class141Var);
        super.animation(class141Var);
    }

    public void updateCopiedState() {
        if (!this.copied || System.currentTimeMillis() - this.copiedTime <= copiedDurationMs) {
            return;
        }
        this.copied = false;
    }

    public void animateSwatches(WeightedEngine class141Var) {
        boolean z = this.hexClickable.hoverAnimation().smoothAnimation() > 0.01f;
        this.hexAnimator.state((z || this.copied) ? false : true);
        this.copyAnimator.state(z && !this.copied);
        this.copiedAnimator.state(this.copied);
        this.hexClickable.animate(class141Var);
        this.hexAnimator.animate(class141Var);
        this.copyAnimator.animate(class141Var);
        this.copiedAnimator.animate(class141Var);
        this.swatches.forEach(class824Var -> {
            class824Var.selectAnim.state(this.selectedSwatch == class824Var);
            class824Var.clickable.animate(class141Var);
            class824Var.selectAnim.animate(class141Var);
        });
    }

    @Override
    public void handleClose() {
        Expensive.INSTANCE.eventDispatcher().unregister(LanguageChangeEvent.class, this.languageChangeCallback);
        super.handleClose();
    }

    @Override
    public float height() {
        if (this.description != null && this.descriptionDirty && this.selectedSwatch != null) {
            updateWrappedDescription(width());
        }
        float height = this.titleFont.getHeight(14.0f);
        if (this.description != null && this.wrappedDescription != null) {
            height += descriptionGap + this.descriptionHeight;
        }
        return height + 10.0f + barHeight;
    }

    public float drawName(DrawCtx class699Var, float f) {
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        class699Var.text(this.titleFont, this.name.effective(), nameTextSize, x(), f, class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(200).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(50).argb()), this.highlightAnimation.value()));
        return f + this.titleFont.getHeight(14.0f);
    }

    public float drawDescription(DrawCtx class699Var, float f) {
        if (this.wrappedDescription == null) {
            return f;
        }
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb()), this.highlightAnimation.value());
        float f2 = f + descriptionGap;
        class699Var.text(this.descriptionFont, this.wrappedDescription, descriptionTextSize, x(), f2, iInterpolate);
        return f2 + this.descriptionHeight;
    }

    public void markDescriptionDirty() {
        this.descriptionDirty = true;
        this.cachedWidth = -1.0f;
        this.cachedDescriptionText = null;
    }

    public void updateWrappedDescription(float f) {
        if (this.description == null || this.description.effective() == null || this.description.effective().equals("null")) {
            clearWrappedDescription(f);
            return;
        }
        String strEffective = this.description.effective();
        if (isDescriptionCacheValid(f, strEffective)) {
            return;
        }
        computeWrappedDescription(f, strEffective == null ? "" : strEffective);
    }

    public void updateSelectedSwatch() {
        int iMethod033 = currentColor();
        ColorSwatch2 class824Var = null;
        for (ColorSwatch2 class824Var2 : this.swatches) {
            if (class824Var2.color() == iMethod033) {
                class824Var = class824Var2;
                break;
            }
        }
        if (this.selectedSwatch != class824Var) {
            this.selectedSwatch = class824Var;
            Iterator<ColorSwatch2> it = this.swatches.iterator();
            while (it.hasNext()) {
                ColorSwatch2 next = it.next();
                next.selectAnim.state(next == this.selectedSwatch);
            }
        }
    }

    public void clearWrappedDescription(float f) {
        this.wrappedDescription = null;
        this.descriptionHeight = 0.0f;
        this.descriptionDirty = false;
        this.cachedWidth = f;
        this.cachedDescriptionText = null;
    }

    public boolean isDescriptionCacheValid(float f, String str) {
        return !this.descriptionDirty && f == this.cachedWidth && str != null && str.equals(this.cachedDescriptionText);
    }

    public int currentColor() {
        return ((((int) (clamp01(this.alpha) * 255.0f)) & StencilBufferUtil.STENCIL_MASK) << 24) | (Color.HSBtoRGB(this.hue, this.saturation, this.brightness) & 16777215);
    }

    public static float clamp01(float f) {
        if (f < 0.0f) {
            return 0.0f;
        }
        return Math.min(f, 1.0f);
    }

    public void computeWrappedDescription(float f, String str) {
        this.wrappedDescription = StringUtil.formatTextToFitWidth(str, f, this.descriptionFont, descriptionTextSize);
        this.descriptionHeight = this.descriptionFont.getHeightWithLineBreaks(this.wrappedDescription, descriptionTextSize);
        this.cachedWidth = f;
        this.cachedDescriptionText = str;
        this.descriptionDirty = false;
    }

    public Translation name() {
        return this.name;
    }

    public ColorSettingElement visibleSupplier(Supplier<Boolean> supplier) {
        this.visibilitySupplier = supplier;
        return this;
    }

    public ColorSettingElement colorConsumer(Consumer<Integer> consumer) {
        this.colorCallback = consumer;
        return this;
    }

    public ColorSettingElement alphaConsumer(Consumer<Float> consumer) {
        this.alphaCallback = consumer;
        return this;
    }

    public ColorSettingElement commitRunnable(Runnable runnable) {
        this.commitAction = runnable;
        return this;
    }
}
