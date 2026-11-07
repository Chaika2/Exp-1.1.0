package aethereal.ui;
import aethereal.resource.ClasspathResource;
import aethereal.render.ColorStack;
import aethereal.render.ColorValue;
import aethereal.model.CursorMoveInput;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.event.InputEvent;
import aethereal.model.InputEventContext;
import aethereal.Lang;
import aethereal.model.LayoutScaleContext;
import aethereal.model.MouseButtonInput;
import aethereal.render.MsdfFont;
import aethereal.model.PixelPoint;
import aethereal.util.StencilBufferUtil;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import org.joml.Vector4f;

public class ColorPickerWindow extends AbstractWindow {
    public final GlTexture clickIcon;
    public final GlTexture plusIcon;
    public final GlTexture checkmarkIcon;
    public final MsdfFont font;
    static final int animationDuration = 350;
    static final float cornerRadius = 8.0f;
    static final float outlineWidth = 2.5f;
    static final float padding = 12.0f;
    static final float svBoxWidth = 192.0f;
    static final float svBoxHeight = 140.0f;
    static final float barWidth = 6.0f;
    static final float rgbaRowHeight = 26.0f;
    static final float separatorWidth = 1.5f;
    static final float headerHeight = 12.0f;
    static final float labelColumnWidth = 60.0f;
    static final float valueColumnWidth = 40.0f;
    static final float margin = 10.0f;
    static final float swatchRadius = 4.0f;
    static final float half = 2.0f;
    static final float baseHeight = 288.0f;
    static final float rowHeightIncrement = 23.0f;
    static final int maxSwatches = 29;
    static final float snapThreshold = 50.0f;
    static final float pointerSize = 4.0f;
    public final WidgetBounds svBounds;
    public final WidgetBounds hueBounds;
    public final WidgetBounds alphaBounds;

    public final WidgetBounds addSwatchBounds;
    public final WidgetBounds dragBarBounds;
    public final ToggleAnimator openAnimator;
    public List<ColorSwatch> swatches;
    public float hue;
    public float saturation;
    public float brightness;
    public float alpha;
    public boolean draggingSV;

    public boolean draggingHue;

    public boolean draggingAlpha;

    public boolean draggingSide;
    public boolean onRightSide;
    public final ClickableBehavior addSwatchClickable;

    public final LoadingButtonWidget closeButton;
    public final MenuWindow menuWindow;

    public ColorSwatch selectedSwatch;
    public Consumer<Integer> colorCallback;
    public Consumer<Float> alphaCallback;

    public Runnable applyAction;

    public ColorPickerWindow(float f, float f2, float f3, float f4) {
        super(248.0f, computeHeight(20));
        this.clickIcon = new GlTexture(new ClasspathResource("/icons/menu/new/click.png"));
        this.plusIcon = new GlTexture(new ClasspathResource("/icons/menu/new/plus.png"));
        this.checkmarkIcon = new GlTexture(new ClasspathResource("/icons/menu/new/checkmark.png"));
        this.font = Fonts.INTER_EXTRA_BOLD.get();
        this.svBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
        this.hueBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
        this.alphaBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
        this.addSwatchBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
        this.dragBarBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
        this.openAnimator = new ToggleAnimator(animationDuration, Easings.EASE_IN_OUT_CUBIC);
        this.swatches = null;
        this.onRightSide = true;
        this.addSwatchClickable = new ClickableBehavior();
        this.selectedSwatch = null;
        int iArgb = Expensive.INSTANCE.theme().palette().accent().argb();
        if (this.swatches == null) {
            this.swatches = new ArrayList(Arrays.asList(new ColorSwatch(iArgb, true), new ColorSwatch(ColorValue.fromHex("E7BC37").argb(), true), new ColorSwatch(ColorValue.fromHex("8673FA").argb(), true), new ColorSwatch(ColorValue.fromHex("B8FF8C").argb(), true), new ColorSwatch(ColorValue.fromHex("C34040").argb(), true), new ColorSwatch(ColorValue.fromHex("65677A").argb(), true), new ColorSwatch(ColorValue.fromHex("D6D6E6").argb(), true), new ColorSwatch(ColorValue.fromHex("59B35C").argb(), true), new ColorSwatch(ColorValue.fromHex("EDCA5C").argb(), true), new ColorSwatch(ColorValue.fromHex("568CDD").argb(), true), new ColorSwatch(ColorValue.fromHex("FF6158").argb(), true), new ColorSwatch(ColorValue.fromHex("af52de").argb(), true), new ColorSwatch(ThemePalette.mint.argb(), true), new ColorSwatch(ThemePalette.lightOrange.argb(), true), new ColorSwatch(ThemePalette.golden.argb(), true), new ColorSwatch(ColorValue.fromHex("575AC6").argb(), true), new ColorSwatch(ColorValue.fromHex("7AD7C1").argb(), true), new ColorSwatch(ColorValue.fromHex("F2A65A").argb(), true), new ColorSwatch(ColorValue.fromHex("6A9FB5").argb(), true)));
        }
        this.closeButton = new LoadingButtonWidget(Lang.CLOSE, Math.round(width() - 24.0f), 25, this::onCloseClicked, 7.0f, true);
        this.hue = f;
        this.saturation = f2;
        this.brightness = f3;
        this.alpha = f4;
        this.menuWindow = Expensive.INSTANCE.menuWindow();
        this.clickIcon.setDimensions(12, 12);
        this.addSwatchClickable.clickCallback(() -> {
            if (this.swatches.size() < maxSwatches) {
                addSwatch();
            }
        });
        for (ColorSwatch class774Var : this.swatches) {
            class774Var.clickable().clickCallback(() -> {
                selectSwatch(class774Var);
            });
            if (!class774Var.system()) {
                class774Var.clickable().rightClickCallback(() -> {
                    removeSwatch(class774Var);
                });
            }
        }
        addChild(this.closeButton);
    }

    public void selectSwatch(ColorSwatch class774Var) {
        this.selectedSwatch = class774Var;
        int iColor = class774Var.color();
        int i = (iColor >> 16) & StencilBufferUtil.STENCIL_MASK;
        int i2 = (iColor >> 8) & StencilBufferUtil.STENCIL_MASK;
        int i3 = iColor & StencilBufferUtil.STENCIL_MASK;
        int i4 = (iColor >> 24) & StencilBufferUtil.STENCIL_MASK;
        float[] fArrRGBtoHSB = Color.RGBtoHSB(i, i2, i3, (float[]) null);
        this.hue = fArrRGBtoHSB[0];
        this.saturation = fArrRGBtoHSB[1];
        this.brightness = fArrRGBtoHSB[2];
        this.alpha = i4 / 255.0f;
        class774Var.selectAnim().state(true);
        emitColorAndAlpha();
    }

    public void removeSwatch(ColorSwatch class774Var) {
        if (class774Var.system()) {
            return;
        }
        if (this.selectedSwatch == class774Var) {
            clearSelection();
        }
        this.swatches.remove(class774Var);
        setSize(width(), computeHeight(this.swatches.size() + 1));
    }

    public void onCloseClicked() {
        closePicker();
    }

    public void updateColor(float f, float f2, float f3, float f4) {
        this.hue = f;
        this.saturation = f2;
        this.brightness = f3;
        this.alpha = f4;
    }

    public static float computeHeight(int i) {
        int iCeil = (int) Math.ceil(((double) i) / ((double) 10));
        return iCeil <= 2 ? baseHeight : baseHeight + ((iCeil - 2) * rowHeightIncrement);
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        updatePosition();
        updateBounds();
        super.layout(class698Var);
    }

    void updatePosition() {
        if (this.onRightSide) {
            setPosition(this.menuWindow.x() + MenuWindow.MENU_WIDTH + this.menuWindow.chat().width() + margin, this.menuWindow.y());
        } else {
            setPosition((this.menuWindow.x() - width()) - margin, this.menuWindow.y());
        }
    }

    void updateBounds() {
        this.svBounds.withPosition(x() + 12.0f, y() + 12.0f).withSize(svBoxWidth, svBoxHeight);
        this.hueBounds.withSize(barWidth, svBoxHeight).withPosition(((x() + width()) - 28.0f) - barWidth, this.svBounds.y());
        this.alphaBounds.withSize(barWidth, svBoxHeight).withPosition(((x() + width()) - 12.0f) - barWidth, this.svBounds.y());
        this.closeButton.setPosition(x() + 12.0f, ((y() + height()) - 12.0f) - this.closeButton.height());
        this.dragBarBounds.withPosition(x(), y()).withSize(width(), 12.0f);
        this.checkmarkIcon.setDimensions(12, 12);
        this.clickIcon.setDimensions(12, 12);
    }

    @Override
    public void render(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        this.closeButton.baseColor(class764VarPalette.surfaceBackground().tone(600).argb());
        this.closeButton.textColor(class764VarPalette.text().tone(600).argb());
        this.closeButton.outlineColor(class764VarPalette.surfaceOutline().tone(400).argb());
        if (this.openAnimator.isZero()) {
            return;
        }
        class115VarColorStack.push();
        if (!this.openAnimator.isOne()) {
            class115VarColorStack.alpha(this.openAnimator.smoothAnimation());
        }
        class699Var.fillOutlinedRoundedRect(x(), y(), width(), height(), cornerRadius, outlineWidth, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb()));
        if (this.draggingSide) {
            class699Var.fillRoundedRect(this.onRightSide ? (this.menuWindow.x() - 4.0f) - margin : this.menuWindow.x() + MenuWindow.MENU_WIDTH + this.menuWindow.chat().width() + margin, this.menuWindow.y(), 4.0f, height(), half, class115VarColorStack.computeColor(class764VarPalette.accent().argb()));
        }
        int iMethod009 = renderSaturationBrightness(class699Var, class115VarColorStack);
        renderHueBar(class699Var, class115VarColorStack);
        renderAlphaBar(class699Var, iMethod009, class115VarColorStack);
        float fMethod011 = renderRgbaRow(class699Var, class115VarColorStack, class764VarPalette) + 12.0f;
        if (this.swatches != null) {
            for (int i = 0; i < this.swatches.size(); i++) {
                ColorSwatch class774Var = this.swatches.get(i);
                float fX = this.svBounds.x() + ((i % 10) * (14.0f + 9.0f));
                float f = fMethod011 + ((i / 10) * (14.0f + 9.0f));
                class699Var.circle(fX + (14.0f / half), f + (14.0f / half), (14.0f + (class774Var.clickable.hoverAnimation().smoothAnimation() * 1.3f)) / half, class115VarColorStack.computeColor(class774Var.color()));
                class774Var.clickable().determineFromLastDrawEngineOperation(class699Var);
                if (this.selectedSwatch == class774Var && class774Var.selectAnim().smoothAnimation() > 0.0f) {
                    class115VarColorStack.push();
                    class115VarColorStack.alpha(class774Var.selectAnim().smoothAnimation());
                    class699Var.texture(this.checkmarkIcon, (fX + (14.0f / half)) - (this.checkmarkIcon.width() / 2), (f + (14.0f / half)) - (this.checkmarkIcon.height() / 2), this.checkmarkIcon.width(), this.checkmarkIcon.height(), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb()));
                    class115VarColorStack.pop();
                }
            }
            if (this.swatches.size() < maxSwatches) {
                int size = this.swatches.size();
                float fX2 = this.svBounds.x() + ((size % 10) * (14.0f + 9.0f));
                float f2 = fMethod011 + ((size / 10) * (14.0f + 9.0f));
                float fSmoothAnimation = this.addSwatchClickable.hoverAnimation().smoothAnimation() * 1.3f;
                this.addSwatchBounds.withPosition(fX2, f2).withSize(14.0f, 14.0f);
                class699Var.circle(fX2 + (14.0f / half), f2 + (14.0f / half), (14.0f + fSmoothAnimation) / half, class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()));
                this.addSwatchClickable.determineFromLastDrawEngineOperation(class699Var);
                class699Var.texture(this.plusIcon, (fX2 + (14.0f / half)) - (this.plusIcon.width() / 2), ((f2 + (14.0f / half)) - (this.plusIcon.height() / 2)) - 0.1f, this.plusIcon.width(), this.plusIcon.height(), -1);
            }
        }
        this.closeButton.render(class699Var);
        class115VarColorStack.pop();
    }

    float renderRgbaRow(DrawCtx class699Var, ColorStack class115Var, ThemePalette class764Var) {
        float fY = this.hueBounds.y() + this.hueBounds.height() + 12.0f;
        class699Var.fillOutlinedRoundedRect(this.svBounds.x(), fY, width() - 24.0f, rgbaRowHeight, barWidth, outlineWidth, class115Var.computeColor(class764Var.surfaceOutline().tone(300).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(900).argb(), 0.0f));
        float fX = this.svBounds.x() + 1.0f;
        class699Var.fillRoundedRect(fX, fY + 1.0f, 58.0f, 24.0f, new Vector4f(0.0f, 0.0f, barWidth, barWidth), class115Var.computeColor(class764Var.surfaceBackground().tone(600).argb()));
        class699Var.textureVerticalC(this.clickIcon, this.svBounds.x() + cornerRadius, fY + 13.0f, this.clickIcon.width(), this.clickIcon.height(), class115Var.computeColor(class764Var.text().tone(400).argb()));
        class699Var.text(this.font, "RGBA", 10, this.svBounds.x() + cornerRadius + this.clickIcon.width() + 4.0f, (fY + 13.0f) - (this.font.getHeight(margin) / half), class115Var.computeColor(class764Var.text().tone(400).argb()));
        float f = (fX + labelColumnWidth) - 3.0f;
        drawSeparator(class699Var, class115Var, class764Var, f, fY);
        float f2 = f + 1.0f;
        int iHSBtoRGB = Color.HSBtoRGB(this.hue, this.saturation, this.brightness);
        int[] iArr = {(iHSBtoRGB >> 16) & StencilBufferUtil.STENCIL_MASK, (iHSBtoRGB >> 8) & StencilBufferUtil.STENCIL_MASK, iHSBtoRGB & StencilBufferUtil.STENCIL_MASK, (int) (clamp01(this.alpha) * 255.0f)};
        for (int i = 0; i < iArr.length; i++) {
            String strValueOf = String.valueOf(iArr[i]);
            class699Var.text(this.font, strValueOf, 10, f2 + ((valueColumnWidth - class699Var.textWidthPhysical(this.font, strValueOf, 10)) / half), (fY + 13.0f) - (this.font.getHeight(margin) / half), class115Var.computeColor(class764Var.text().tone(400).argb()));
            f2 += valueColumnWidth;
            if (i != iArr.length - 1) {
                drawSeparator(class699Var, class115Var, class764Var, f2, fY);
                f2 += 1.0f;
            }
        }
        return fY + rgbaRowHeight;
    }

    void drawSeparator(DrawCtx class699Var, ColorStack class115Var, ThemePalette class764Var, float f, float f2) {
        class699Var.fillRect(f, f2 + 1.0f, separatorWidth, 24.0f, class115Var.computeColor(class764Var.surfaceOutline().tone(500).argb()));
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        if (!opened() || this.openAnimator.isZero()) {
            return false;
        }
        InputEvent class691VarInputEvent = class688Var.inputEvent();
        if (class691VarInputEvent instanceof CursorMoveInput) {
            PixelPoint class708VarLogicalMousePosition = class688Var.logicalMousePosition();
            if (handleDrag(class708VarLogicalMousePosition) || handleSideDrag(class708VarLogicalMousePosition)) {
                return true;
            }
        }
        if (class691VarInputEvent instanceof MouseButtonInput) {
            MouseButtonInput class693Var = (MouseButtonInput) class691VarInputEvent;
            if (class693Var.button() == 0) {
                if (class693Var.action().release()) {
                    boolean z2 = this.draggingSV || this.draggingHue || this.draggingAlpha || this.draggingSide;
                    if (this.draggingSide) {
                        this.draggingSide = false;
                    }
                    this.draggingAlpha = false;
                    this.draggingHue = false;
                    this.draggingSV = false;
                    return z2;
                }
                if (class688Var.inArea(this.dragBarBounds.x(), this.dragBarBounds.y(), this.dragBarBounds.width(), this.dragBarBounds.height())) {
                    this.draggingSide = true;
                    return true;
                }
                if (class688Var.inArea(this.svBounds.x(), this.svBounds.y(), this.svBounds.width(), this.svBounds.height())) {
                    this.draggingSV = true;
                    return true;
                }
                if (class688Var.inArea(this.hueBounds.x(), this.hueBounds.y(), this.hueBounds.width(), this.hueBounds.height())) {
                    this.draggingHue = true;
                    return true;
                }
                if (class688Var.inArea(this.alphaBounds.x(), this.alphaBounds.y(), this.alphaBounds.width(), this.alphaBounds.height())) {
                    this.draggingAlpha = true;
                    return true;
                }
            }
        }
        if (!this.draggingAlpha && !this.draggingSV && !this.draggingHue && !this.draggingSide) {
            Iterator<ColorSwatch> it = this.swatches.iterator();
            while (it.hasNext()) {
                if (it.next().clickable().handleInput(class688Var, z)) {
                    return true;
                }
            }
            if (this.addSwatchClickable.handleInput(class688Var, z)) {
                return true;
            }
        }
        return super.handleInput(class688Var, z);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.openAnimator.animate(class141Var);
        this.addSwatchClickable.animate(class141Var);
        for (ColorSwatch class774Var : this.swatches) {
            class774Var.clickable().animate(class141Var);
            class774Var.selectAnim().animate(class141Var);
        }
        super.animation(class141Var);
    }

    boolean handleDrag(PixelPoint class708Var) {
        if (this.draggingSV) {
            updateSaturationBrightness(class708Var.x(), class708Var.y());
            clearSelection();
            emitColorAndAlpha();
            return true;
        }
        if (this.draggingHue) {
            updateHue(class708Var.y());
            clearSelection();
            emitColorAndAlpha();
            return true;
        }
        if (!this.draggingAlpha) {
            return false;
        }
        updateAlpha(class708Var.y());
        emitColorAndAlpha();
        return true;
    }

    boolean handleSideDrag(PixelPoint class708Var) {
        if (!this.draggingSide) {
            return false;
        }
        float fX = this.menuWindow.x() + ((MenuWindow.MENU_WIDTH + this.menuWindow.chat().width()) / half);
        float fX2 = class708Var.x();
        if (this.onRightSide) {
            if (fX2 >= fX - snapThreshold) {
                return true;
            }
            this.onRightSide = false;
            updatePosition();
            updateBounds();
            return true;
        }
        if (fX2 <= fX + snapThreshold) {
            return true;
        }
        this.onRightSide = true;
        updatePosition();
        updateBounds();
        return true;
    }

    public void clearSelection() {
        if (this.selectedSwatch != null) {
            this.selectedSwatch.selectAnim().state(false);
            this.selectedSwatch = null;
        }
    }

    int renderSaturationBrightness(DrawCtx class699Var, ColorStack class115Var) {
        int iComputeColor = class115Var.computeColor(Color.HSBtoRGB(this.hue, 1.0f, 1.0f));
        LayoutScaleContext class698VarLayoutContext = class699Var.layoutContext();
        class699Var.fillGradientRoundedRect(this.svBounds.x(), this.svBounds.y(), this.svBounds.width(), this.svBounds.height(), 4, class115Var.white(), iComputeColor, iComputeColor, class115Var.white());
        class699Var.fillGradientRoundedRect(this.svBounds.x(), this.svBounds.y(), this.svBounds.width(), this.svBounds.height(), 4, class115Var.computeColor(0, 1.0f), class115Var.computeColor(0, 1.0f), class115Var.transparent(), class115Var.transparent());
        class699Var.drawEngine().hollowCircle(class699Var.matrixStack().peek().getPositionMatrix(), class698VarLayoutContext.toPhysical(this.svBounds.x() + clamp(this.saturation * this.svBounds.width(), barWidth, this.svBounds.width() - barWidth)), class698VarLayoutContext.toPhysical(this.svBounds.y() + clamp((1.0f - this.brightness) * this.svBounds.height(), barWidth, this.svBounds.height() - barWidth)), class698VarLayoutContext.toPhysical(barWidth), class698VarLayoutContext.toPhysical(outlineWidth), class115Var.white());
        return iComputeColor;
    }

    void renderHueBar(DrawCtx class699Var, ColorStack class115Var) {
        float fHeight = this.hueBounds.height() - (3.0f * half);
        for (int i = 0; i < fHeight; i++) {
            class699Var.circle(this.hueBounds.x() + 3.0f, this.hueBounds.y() + i + 3.0f, 3.0f, class115Var.HSBtoRGB(i / fHeight, 1.0f, 1.0f));
        }
        class699Var.fillRoundedRect((this.hueBounds.x() + (this.hueBounds.width() / half)) - 5.0f, this.hueBounds.y() + (this.hue * (this.hueBounds.height() - 4.0f)), margin, 4.0f, half, class115Var.white());
    }

    void renderAlphaBar(DrawCtx class699Var, int i, ColorStack class115Var) {
        int iTransparent = i & class115Var.transparent();
        int iComputeColor = i | class115Var.computeColor(0);
        LayoutScaleContext class698VarLayoutContext = class699Var.layoutContext();
        class699Var.drawEngine().roundedChecker(class699Var.matrixStack().peek().getPositionMatrix(), class698VarLayoutContext.toPhysical(this.alphaBounds.x()), class698VarLayoutContext.toPhysical(this.alphaBounds.y()), class698VarLayoutContext.toPhysical(this.alphaBounds.width()), class698VarLayoutContext.toPhysical(this.alphaBounds.height()), 3.0f, half, class115Var.computeColor(0.7f, 39, 39, 40), class115Var.computeColor(0.7f, 14, 14, 15));
        class699Var.fillGradientRoundedRect(this.alphaBounds.x(), this.alphaBounds.y(), this.alphaBounds.width(), this.alphaBounds.height(), 3, iTransparent, iTransparent, iComputeColor, iComputeColor);
        class699Var.fillRoundedRect((this.alphaBounds.x() + (this.alphaBounds.width() / half)) - 5.0f, this.alphaBounds.y() + (this.alphaBounds.height() * (1.0f - clamp01(this.alpha))), margin, 4.0f, half, class115Var.white());
    }

    void updateSaturationBrightness(float f, float f2) {
        this.saturation = clamp01((f - this.svBounds.x()) / this.svBounds.width());
        this.brightness = 1.0f - clamp01((f2 - this.svBounds.y()) / this.svBounds.height());
    }

    void updateHue(float f) {
        this.hue = clamp01((f - this.hueBounds.y()) / this.hueBounds.height());
    }

    void updateAlpha(float f) {
        this.alpha = 1.0f - clamp01((f - this.alphaBounds.y()) / this.alphaBounds.height());
    }

    void emitColor() {
        if (this.colorCallback == null) {
            return;
        }
        this.colorCallback.accept(Integer.valueOf(Color.HSBtoRGB(this.hue, this.saturation, this.brightness)));
    }

    void emitAlpha() {
        if (this.alphaCallback == null) {
            return;
        }
        this.alphaCallback.accept(Float.valueOf(this.alpha));
    }

    void addSwatch() {
        if (this.swatches == null || this.swatches.size() >= maxSwatches) {
            return;
        }
        ColorSwatch class774Var = new ColorSwatch((Color.HSBtoRGB(this.hue, this.saturation, this.brightness) & 16777215) | (((int) (clamp01(this.alpha) * 255.0f)) << 24), false);
        class774Var.clickable().clickCallback(() -> {
            selectSwatch(class774Var);
        });
        class774Var.clickable().rightClickCallback(() -> {
            removeSwatch(class774Var);
        });
        this.swatches.add(class774Var);
        setSize(width(), computeHeight(this.swatches.size() + 1));
    }

    public void emitColorAndAlpha() {
        emitColor();
        emitAlpha();
    }

    static float clamp01(float f) {
        if (f < 0.0f) {
            return 0.0f;
        }
        return Math.min(f, 1.0f);
    }

    static float clamp(float f, float f2, float f3) {
        return Math.max(f2, Math.min(f3, f));
    }

    public void togglePicker() {
        if (this.openAnimator.isOne() || this.openAnimator.state()) {
            closePicker();
        } else {
            openPicker();
        }
    }

    public void openPicker() {
        this.openAnimator.state(true);
    }

    public void closePicker() {
        if (this.applyAction != null) {
            this.applyAction.run();
        }
        this.draggingHue = false;
        this.draggingSV = false;
        this.draggingAlpha = false;
        this.openAnimator.state(false);
    }

    public boolean opened() {
        return this.openAnimator.isOne() || this.openAnimator.state();
    }

    public ColorPickerWindow colorConsumer(Consumer<Integer> consumer) {
        this.colorCallback = consumer;
        return this;
    }

    public ColorPickerWindow alphaConsumer(Consumer<Float> consumer) {
        this.alphaCallback = consumer;
        return this;
    }

    public ColorPickerWindow applyCallback(Runnable runnable) {
        this.applyAction = runnable;
        return this;
    }
}
