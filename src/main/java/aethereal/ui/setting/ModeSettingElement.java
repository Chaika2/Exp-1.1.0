package aethereal.ui.setting;
import aethereal.resource.ClasspathResource;
import aethereal.ui.ClickableBehavior;
import aethereal.render.ColorStack;
import aethereal.model.CursorMoveInput;
import aethereal.model.DisplayNamed;
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
import aethereal.type.ModeOption;
import aethereal.ui.ModuleFrame;
import aethereal.model.MouseButtonInput;
import aethereal.render.MsdfFont;
import aethereal.model.ScrollInput;
import aethereal.util.StringUtil;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.model.Translation;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.lang.Enum;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public class ModeSettingElement<T extends Enum<T>> extends ModuleFrame {
    static final int titleTextSize = 14;
    static final int descTextSize = 13;
    static final float descGap = 3.0f;
    static final float blockSpacing = 12.0f;
    static final float rowHeight = 25.0f;
    static final float a = 23.0f;
    static final float b = 6.0f;
    static final float c = 2.0f;
    public final Translation name;
    public final Translation description;
    public boolean expanded;
    public String wrappedDescription;
    public String cachedDescription;
    public float descriptionHeight;
    public Supplier<T> valueSupplier;
    public Consumer<T> changeCallback;
    public Supplier<Boolean> visibleCondition;
    public final MsdfFont titleFont = Fonts.INTER_SEMIBOLD.get();
    public final MsdfFont valueFont = Fonts.INTER_BOLD.get();
    public final MsdfFont descriptionFont = Fonts.INTER_MEDIUM.get();
    public final GlTexture enumIcon = new GlTexture(new ClasspathResource("/icons/menu/new/enum.png"));
    public final GlTexture arrowIcon = new GlTexture(new ClasspathResource("/icons/menu/new/arrow.png"));
    public final GlTexture checkmarkIcon = new GlTexture(new ClasspathResource("/icons/menu/new/checkmark.png"));
    public final ToggleAnimator expandAnimation = new ToggleAnimator(220, Easings.EASE_IN_OUT_CUBIC);
    public final HighlightAnimation highlightAnimation = new HighlightAnimation(300, Easings.EASE_IN_OUT_CUBIC);
    public final ClickableBehavior clickBehavior = new ClickableBehavior();
    public final List<ModeOption<T>> options = new ArrayList();
    public final WidgetBounds dropdownBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
    public final EventCallback<LanguageChangeEvent> languageChangeCallback = class226Var -> {
        invalidateLayout();
    };
    public final WidgetBounds boxBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
    public float cachedWidth = -1.0f;
    public boolean descriptionDirty = true;

    public ModeSettingElement(Translation class254Var, Translation class254Var2, T[] tArr) {
        this.name = class254Var;
        this.description = class254Var2;
        for (T t : tArr) {
            this.options.add(new ModeOption<>(t));
        }
        setupOptionCallbacks();
        this.clickBehavior.clickCallback(this::toggleExpanded);
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
        updateDescription(f);
        float fMethod011 = drawDescription(class699Var, drawTitle(class699Var, y())) + blockSpacing;
        this.boxBounds.withPosition(x(), fMethod011).withSize(f, rowHeight);
        drawBox(class699Var, fMethod011, f);
        drawBoxContent(class699Var, fMethod011, f);
        updateBounds(fMethod011, f);
    }

    @Override
    public void drawOverlay(DrawCtx class699Var) {
        if (this.options.isEmpty()) {
            return;
        }
        if (!this.expandAnimation.isZero() || this.expanded) {
            ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
            class115VarColorStack.push();
            class115VarColorStack.alpha(this.expandAnimation.smoothAnimation());
            drawDropdownBackground(class699Var);
            drawDropdownOptions(class699Var);
            class115VarColorStack.pop();
        }
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        this.enumIcon.setDimensions(12, 12);
        this.arrowIcon.setDimensions(12, 12);
        this.checkmarkIcon.setDimensions(titleTextSize, titleTextSize);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        if (this.visibleCondition != null) {
            visible(this.visibleCondition.get().booleanValue());
        }
        this.expandAnimation.animate(class141Var);
        this.clickBehavior.animate(class141Var);
        this.highlightAnimation.animate(class141Var);
        for (ModeOption<T> class826Var : this.options) {
            class826Var.clickableBehavior().animate(class141Var);
            if (this.valueSupplier != null) {
                class826Var.currentOptionAnimation.state(class826Var.option == this.valueSupplier.get());
                class826Var.currentOptionAnimation.animate(class141Var);
            }
        }
        super.animation(class141Var);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        boolean zMethod004 = false;
        if (this.expanded) {
            zMethod004 = handleDropdownInput(class688Var, z);
        }
        if (this.clickBehavior.handleInput(class688Var, z || zMethod004)) {
            zMethod004 = true;
        }
        return zMethod004;
    }

    @Override
    public void handleClose() {
        closeDropdown();
        Expensive.INSTANCE.eventDispatcher().unregister(LanguageChangeEvent.class, this.languageChangeCallback);
        super.handleClose();
    }

    @Override
    public void handleViewportVisibility(float f, float f2, float f3) {
        if (this.expanded || !this.expandAnimation.isZero()) {
            float fY = y() - f3;
            if (fY + height() < f || fY > f2) {
                closeDropdown();
            }
        }
    }

    @Override
    public float height() {
        if (this.description != null && this.descriptionDirty && this.parent != null) {
            updateDescription(this.parent.width());
        }
        float height = this.titleFont.getHeight(14.0f);
        if (this.description != null && this.wrappedDescription != null) {
            height += descGap + this.descriptionHeight;
        }
        return height + blockSpacing + rowHeight;
    }

    public void setupOptionCallbacks() {
        for (ModeOption<T> class826Var : this.options) {
            class826Var.clickableBehavior().clickCallback(() -> {
                if (this.valueSupplier != null && class826Var.option != this.valueSupplier.get()) {
                    toggleExpanded();
                }
                if (this.changeCallback != null) {
                    this.changeCallback.accept((T) ((Enum) class826Var.option));
                }
            });
        }
    }

    public float drawTitle(DrawCtx class699Var, float f) {
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        class699Var.text(this.titleFont, this.name.effective(), titleTextSize, x(), f, class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(200).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(50).argb()), this.highlightAnimation.value()));
        return f + this.titleFont.getHeight(14.0f);
    }

    public float drawDescription(DrawCtx class699Var, float f) {
        if (this.wrappedDescription == null) {
            return f;
        }
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb()), this.highlightAnimation.value());
        float f2 = f + descGap;
        class699Var.text(this.descriptionFont, this.wrappedDescription, descTextSize, x(), f2, iInterpolate);
        return f2 + this.descriptionHeight;
    }

    public void drawBox(DrawCtx class699Var, float f, float f2) {
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        class699Var.fillOutlinedRoundedRect(x(), f, f2, rowHeight, b, 2.5f, class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(400).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(300).argb()), this.clickBehavior.hoverAnimation()), class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(600).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(500).argb()), this.clickBehavior.hoverAnimation()));
    }

    public void drawBoxContent(DrawCtx class699Var, float f, float f2) {
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        float fX = x() + 10.0f;
        class699Var.texture(this.enumIcon, fX, (f + 12.5f) - Math.round(this.enumIcon.height() / c), this.enumIcon.width(), this.enumIcon.height(), class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()));
        drawArrow(class699Var, f, f2);
        drawValueText(class699Var, f, f2, fX + this.enumIcon.width() + b);
    }

    public void drawArrow(DrawCtx class699Var, float f, float f2) {
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        float fX = ((x() + f2) - 10.0f) - this.arrowIcon.width();
        float fRound = (f + 12.5f) - Math.round(this.arrowIcon.height() / c);
        float fWidth = fX + (this.arrowIcon.width() / c);
        float fHeight = fRound + (this.arrowIcon.height() / c);
        float fSmoothAnimation = (float) (3.141592653589793d * ((double) this.expandAnimation.smoothAnimation()));
        float physical = class699Var.layoutContext().toPhysical(fWidth);
        float physical2 = class699Var.layoutContext().toPhysical(fHeight);
        class699Var.matrixStack().push();
        class699Var.matrixStack().translate(physical, physical2, 0.0f);
        class699Var.matrixStack().multiply(new Quaternionf().rotateZ(fSmoothAnimation));
        class699Var.matrixStack().translate(-physical, -physical2, 0.0f);
        class699Var.texture(this.arrowIcon, fX, fRound, this.arrowIcon.width(), this.arrowIcon.height(), class115VarColorStack.computeColor(class764VarPalette.text().tone(600).argb()));
        class699Var.matrixStack().pop();
    }

    public void drawValueText(DrawCtx class699Var, float f, float f2, float f3) {
        if (this.valueSupplier == null) {
            return;
        }
        T t = this.valueSupplier.get();
        if (t instanceof DisplayNamed) {
            DisplayNamed class668Var = (DisplayNamed) t;
            int iComputeColor = class699Var.drawEngine().colorStack().computeColor(class699Var.theme().palette().text().tone(300).argb());
            String strEffective = class668Var.getDisplayName().effective();
            float fX = ((((x() + f2) - 10.0f) - this.arrowIcon.width()) - 10.0f) - f3;
            float fRound = (f + 12.5f) - Math.round(this.valueFont.getHeight(blockSpacing) / c);
            if (class699Var.textWidthPhysical(this.valueFont, strEffective, 12) > fX) {
                drawTruncatedText(class699Var, strEffective, f3, fRound, fX, iComputeColor);
            } else {
                class699Var.text(this.valueFont, strEffective, 12, f3, fRound, iComputeColor);
            }
        }
    }

    public void drawTruncatedText(DrawCtx class699Var, String str, float f, float f2, float f3, int i) {
        int i2 = i & 16777215;
        String str2 = "";
        for (int length = str.length(); length > 0; length--) {
            String strSubstring = str.substring(0, length);
            if (class699Var.textWidthPhysical(this.valueFont, strSubstring, 12) <= f3) {
                str2 = strSubstring;
                break;
            }
        }
        class699Var.textWithHorizontalGradient(this.valueFont, str2, 12, f, f2, i, i2);
    }

    public void drawDropdownBackground(DrawCtx class699Var) {
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        class699Var.fillOutlinedRoundedRect(this.dropdownBounds.x(), this.dropdownBounds.y(), this.dropdownBounds.width(), this.dropdownBounds.height(), 7.0f, 2.5f, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(400).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(600).argb()));
    }

    public void drawDropdownOptions(DrawCtx class699Var) {
        T t = this.valueSupplier == null ? null : this.valueSupplier.get();
        for (int i = 0; i < this.options.size(); i++) {
            ModeOption<T> class826Var = this.options.get(i);
            float fY = this.dropdownBounds.y() + b + (i * rowHeight);
            if (fY + a > this.dropdownBounds.y() + this.dropdownBounds.height()) {
                class826Var.clickableBehavior.setDimensions(0.0f, 0.0f, 0.0f, 0.0f);
                return;
            } else {
                class826Var.clickableBehavior.setDimensions(this.dropdownBounds.x(), fY, this.dropdownBounds.width(), a);
                drawOption(class699Var, class826Var, fY, t != null && class826Var.option == t);
            }
        }
    }

    public void drawOption(DrawCtx class699Var, ModeOption<T> class826Var, float f, boolean z) {
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(400).argb(), 0), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(400).argb(), 100), class826Var.clickableBehavior().hoverAnimation()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(400).argb()), class826Var.currentOptionAnimation());
        float fX = this.dropdownBounds.x() + 5.0f;
        float fWidth = this.dropdownBounds.width() - 10.0f;
        class699Var.fillRoundedRect(fX, f, fWidth, a, new Vector4f(b, b, b, b), iInterpolate);
        if (z) {
            class699Var.textureVerticalC(this.checkmarkIcon, ((fX + fWidth) - 5.0f) - this.checkmarkIcon.width(), f + 11.5f, this.checkmarkIcon.width(), this.checkmarkIcon.height(), class115VarColorStack.computeColor(class764VarPalette.text().tone(100).argb()));
        }
        T t = class826Var.option;
        if (t instanceof DisplayNamed) {
            class699Var.text(this.valueFont, ((DisplayNamed) t).getDisplayName().effective(), 12, fX + 5.0f, (f + 11.5f) - Math.round(this.valueFont.getHeight(blockSpacing) / c), class115VarColorStack.computeColor(class764VarPalette.text().tone(z ? 100 : 500).argb()));
        }
    }

    public void updateBounds(float f, float f2) {
        float f3 = f + rowHeight + descGap;
        float fMethod018 = computeDropdownHeight();
        float fX = x();
        if (this.options.isEmpty() || (!this.expanded && this.expandAnimation.isZero())) {
            this.dropdownBounds.withSize(0.0f, 0.0f);
        } else {
            this.dropdownBounds.withPosition(fX, f3).withSize(f2, fMethod018);
        }
        this.clickBehavior.setDimensions(x(), y(), f2, height());
    }

    public float computeDropdownHeight() {
        if (this.expandAnimation.isZero() && !this.expanded) {
            return 0.0f;
        }
        int size = this.options.size();
        return blockSpacing + (size * a) + (Math.max(0, size - 1) * c);
    }

    public boolean handleDropdownInput(InputEventContext class688Var, boolean z) {
        boolean z2 = false;
        for (int size = this.options.size() - 1; size >= 0; size--) {
            if (this.options.get(size).clickableBehavior().handleInput(class688Var, z || z2)) {
                z2 = true;
            }
        }
        if (!z && !z2 && (((class688Var.inputEvent() instanceof MouseButtonInput) || (class688Var.inputEvent() instanceof CursorMoveInput)) && class688Var.inArea(this.dropdownBounds.x(), this.dropdownBounds.y(), this.dropdownBounds.width(), this.dropdownBounds.height()))) {
            return true;
        }
        if (class688Var.inputEvent() instanceof ScrollInput) {
            closeDropdown();
            return true;
        }
        if (!z && (class688Var.inputEvent() instanceof MouseButtonInput)) {
            boolean zInArea = class688Var.inArea(this.clickBehavior.ownerX(), this.clickBehavior.ownerY(), this.clickBehavior.ownerW(), this.clickBehavior.ownerH());
            boolean zInArea2 = class688Var.inArea(this.dropdownBounds.x(), this.dropdownBounds.y(), this.dropdownBounds.width(), this.dropdownBounds.height());
            if (!zInArea && !zInArea2) {
                closeDropdown();
                return true;
            }
        }
        return z2;
    }

    public void toggleExpanded() {
        this.expanded = !this.expanded;
        this.expandAnimation.state(this.expanded);
    }

    public void closeDropdown() {
        if (this.expanded || !this.expandAnimation.isZero()) {
            this.expanded = false;
            this.expandAnimation.state(false);
        }
    }

    public void invalidateLayout() {
        this.descriptionDirty = true;
        this.cachedWidth = -1.0f;
        this.cachedDescription = null;
    }

    public void updateDescription(float f) {
        if (this.description == null || this.description.effective() == null || this.description.effective().equals("null")) {
            clearDescription(f);
            return;
        }
        String strEffective = this.description.effective();
        if (isDescriptionCached(f, strEffective)) {
            return;
        }
        wrapDescription(f, strEffective == null ? "" : strEffective);
    }

    public void clearDescription(float f) {
        this.wrappedDescription = null;
        this.descriptionHeight = 0.0f;
        this.descriptionDirty = false;
        this.cachedWidth = f;
        this.cachedDescription = null;
    }

    public boolean isDescriptionCached(float f, String str) {
        return !this.descriptionDirty && f == this.cachedWidth && str != null && str.equals(this.cachedDescription);
    }

    public void wrapDescription(float f, String str) {
        this.wrappedDescription = StringUtil.formatTextToFitWidth(str, f, this.descriptionFont, descTextSize);
        this.descriptionHeight = this.descriptionFont.getHeightWithLineBreaks(this.wrappedDescription, descTextSize);
        this.cachedWidth = f;
        this.cachedDescription = str;
        this.descriptionDirty = false;
    }

    public Translation name() {
        return this.name;
    }

    public ModeSettingElement<T> currentValueSupplier(Supplier<T> supplier) {
        this.valueSupplier = supplier;
        return this;
    }

    public ModeSettingElement<T> onChangeCallback(Consumer<T> consumer) {
        this.changeCallback = consumer;
        return this;
    }

    public ModeSettingElement<T> visibleSupplier(Supplier<Boolean> supplier) {
        this.visibleCondition = supplier;
        return this;
    }
}
