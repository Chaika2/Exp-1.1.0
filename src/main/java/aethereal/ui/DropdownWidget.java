package aethereal.ui;
import aethereal.render.AnimatedFloat;
import aethereal.resource.ClasspathResource;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.type.DropdownOption;
import aethereal.model.DropdownOptionItem;
import aethereal.math.Easings;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.model.InputEventContext;
import aethereal.model.LayoutScaleContext;
import aethereal.model.MouseButtonInput;
import aethereal.render.MsdfFont;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.model.Translation;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public class DropdownWidget<T> extends AbstractWidget {
    public boolean initialized;
    public final MsdfFont labelFont;
    public final GlTexture iconTexture;
    public final float cornerRadius;
    public final float padding;
    public final float verticalPadding;
    public final int iconSize;
    public float panelWidth;
    public Consumer<T> onSelectCallback;
    public T selectedValue;
    public boolean open;
    public static final float OPTION_HEIGHT = 23.0f;
    public static final float OPTION_PADDING = 6.0f;
    public static final float TWO = 2.0f;
    public final AnimatedFloat widthAnimation = new AnimatedFloat(220, Easings.LINEAR);
    public final AnimatedFloat heightAnimation = new AnimatedFloat(220, Easings.LINEAR);
    public final ClickableBehavior clickBehavior = new ClickableBehavior();
    public final List<DropdownOptionItem<T>> optionItems = new ArrayList();
    public final MsdfFont optionFont = Fonts.INTER_BOLD.get();
    public final GlTexture arrowTexture = new GlTexture(new ClasspathResource("/icons/menu/new/arrow.png")).setDimensions(8, 5);

    public final WidgetBounds headerBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
    public final WidgetBounds panelBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
    public Translation currentLabel = Translation.clearText("");
    public int labelSize = 12;
    public final ToggleAnimator openAnimation = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);
    public final GlTexture checkmarkTexture = new GlTexture(new ClasspathResource("/icons/menu/new/checkmark.png"));

    public DropdownWidget(MsdfFont class161Var, GlTexture class073Var, float f, float f2, float f3, int i) {
        this.labelFont = class161Var;
        this.iconTexture = class073Var;
        this.cornerRadius = f;
        this.padding = f2;
        this.verticalPadding = f3;
        this.iconSize = i;
        this.clickBehavior.clickCallback(this::toggle);
    }

    public void onSelect(Consumer<T> consumer) {
        this.onSelectCallback = consumer;
    }

    public void setOptions(List<DropdownOption<T>> list, T t) {
        this.optionItems.clear();
        for (DropdownOption<T> class739Var : list) {
            DropdownOptionItem<T> class740Var = new DropdownOptionItem<>(class739Var);
            class740Var.clickableBehavior().clickCallback(() -> {
                applySelection(class739Var.value(), true);
            });
            this.optionItems.add(class740Var);
        }
        applySelection(t, false);
    }

    public void select(T t) {
        applySelection(t, false);
    }

    public void label(Translation class254Var, int i) {
        this.currentLabel = class254Var;
        this.labelSize = i;
    }

    public void toggle() {
        this.open = !this.open;
    }

    public void applySelection(T t, boolean z) {
        if (this.optionItems.isEmpty()) {
            return;
        }
        DropdownOptionItem<T> class740VarOrElse = this.optionItems.stream().filter(class740Var -> {
            return Objects.equals(class740Var.option().value(), t);
        }).findFirst().orElse((DropdownOptionItem) this.optionItems.getFirst());
        this.selectedValue = class740VarOrElse.option().value();
        this.currentLabel = class740VarOrElse.option().label();
        if (!z || this.onSelectCallback == null) {
            return;
        }
        this.onSelectCallback.accept(class740VarOrElse.option().value());
        this.open = false;
    }

    @Override
    public void render(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        float fWidth = width();
        float fHeight = height();
        float fSmoothAnimation = this.openAnimation.smoothAnimation();
        float size = 12.0f + (OPTION_HEIGHT * this.optionItems.size()) + (TWO * (this.optionItems.size() - 1));
        ToggleAnimator class323VarHoverAnimation = this.clickBehavior.hoverAnimation();
        ToggleAnimator class323Var = this.openAnimation;
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb(), 0.8f), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(800).argb(), 70), class323VarHoverAnimation);
        int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb());
        int iComputeColor2 = class115VarColorStack.computeColor(class764VarPalette.text().tone(400).argb());
        int iInterpolate2 = class115VarColorStack.interpolate(class115VarColorStack.interpolate(iComputeColor, iComputeColor2, class323Var, class323Var), iComputeColor2, class323VarHoverAnimation, class323VarHoverAnimation);
        class699Var.fillOutlinedRoundedRect(x(), y(), fWidth, fHeight, this.cornerRadius, 2.5f, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb()), iInterpolate);
        float fX = x() + this.padding;
        float fY = y() + (fHeight / TWO);
        class699Var.texture(this.iconTexture, fX, fY - (this.iconSize / 2), this.iconSize, this.iconSize, class115VarColorStack.computeColor(iInterpolate2));
        float fX2 = x() + this.padding + this.iconSize + 5.0f;
        class699Var.text(this.labelFont, this.currentLabel.effective(), this.labelSize, fX2, (y() + (fHeight / TWO)) - (this.labelFont.getHeight(this.labelSize) / TWO), iInterpolate2);
        float fTextWidthPhysical = fX2 + class699Var.textWidthPhysical(this.labelFont, this.currentLabel.effective(), this.labelSize) + OPTION_PADDING;
        float f = fY - (10.0f / TWO);
        float f2 = fTextWidthPhysical + (10.0f / TWO);
        float f3 = f + (10.0f / TWO);
        float fSmoothAnimation2 = (float) (3.141592653589793d * ((double) this.openAnimation.smoothAnimation()));
        float physical = class699Var.layoutContext().toPhysical(f2);
        float physical2 = class699Var.layoutContext().toPhysical(f3);
        class699Var.matrixStack().push();
        class699Var.matrixStack().translate(physical, physical2, 0.0f);
        class699Var.matrixStack().multiply(new Quaternionf().rotateZ(fSmoothAnimation2));
        class699Var.matrixStack().translate(-physical, -physical2, 0.0f);
        class699Var.texture(this.arrowTexture, fTextWidthPhysical, f, 10.0f, 10.0f, class115VarColorStack.computeColor(class764VarPalette.text().tone(600).argb()));
        class699Var.matrixStack().pop();
        if (this.optionItems.isEmpty() || (!this.open && this.openAnimation.isZero())) {
            this.panelBounds.withSize(0.0f, 0.0f);
            return;
        }
        float fY2 = y() + fHeight + 3.0f + fSmoothAnimation;
        this.panelBounds.withPosition(x(), fY2).withSize(this.panelWidth, size);
        class115VarColorStack.push();
        class115VarColorStack.alpha(fSmoothAnimation);
        class699Var.fillOutlinedRoundedRect(x(), fY2, this.panelWidth, size, 8.0f, 2.5f, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb()));
        for (int i = 0; i < this.optionItems.size(); i++) {
            DropdownOptionItem<T> class740Var = this.optionItems.get(i);
            float f4 = fY2 + OPTION_PADDING + (i * 25.0f);
            int iInterpolate3 = class115VarColorStack.interpolate(class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(600).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()), class740Var.clickableBehavior().hoverAnimation()), class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb()), class740Var.currentOptionAnimation());
            float fX3 = x() + 5.0f;
            float f5 = this.panelWidth - 10.0f;
            class699Var.fillRoundedRect(fX3, f4, f5, OPTION_HEIGHT, new Vector4f(OPTION_PADDING, OPTION_PADDING, OPTION_PADDING, OPTION_PADDING), class115VarColorStack.interpolate(class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(600).argb(), 0), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(600).argb(), 100), class740Var.clickableBehavior().hoverAnimation()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(600).argb()), class740Var.currentOptionAnimation()));
            if (Objects.equals(class740Var.option().value(), this.selectedValue)) {
                class699Var.textureVerticalC(this.checkmarkTexture, ((fX3 + f5) - 5.0f) - this.checkmarkTexture.width(), f4 + 11.5f, this.checkmarkTexture.width(), this.checkmarkTexture.height(), class115VarColorStack.computeColor(class764VarPalette.text().tone(100).argb()));
            }
            class699Var.text(this.optionFont, class740Var.option().label().effective(), this.labelSize, fX3 + 5.0f, (f4 + 11.5f) - (this.optionFont.getHeight(this.labelSize) / TWO), iInterpolate3);
        }
        class115VarColorStack.pop();
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        boolean z2 = false;
        if (this.open && !z && (class688Var.inputEvent() instanceof MouseButtonInput)) {
            boolean zInArea = class688Var.inArea(this.headerBounds.x(), this.headerBounds.y(), this.headerBounds.width(), this.headerBounds.height());
            boolean zInArea2 = class688Var.inArea(this.panelBounds.x(), this.panelBounds.y(), this.panelBounds.width(), this.panelBounds.height());
            if (!zInArea && !zInArea2) {
                close();
                return true;
            }
        }
        if (this.open) {
            for (int size = this.optionItems.size() - 1; size >= 0; size--) {
                if (this.optionItems.get(size).clickableBehavior().handleInput(class688Var, z || z2)) {
                    z2 = true;
                }
            }
        }
        if (this.clickBehavior.handleInput(class688Var, z || z2)) {
            z2 = true;
        }
        return z2;
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        float fTextWidthPhysical = class698Var.textWidthPhysical(this.labelFont, this.currentLabel.effective(), this.labelSize);
        float fMax = Math.max(this.iconSize, Math.max(this.labelFont.metrics().lineHeight(), 10.0f)) + (this.verticalPadding * TWO);
        float f = this.iconSize + 5.0f + fTextWidthPhysical + OPTION_PADDING + 10.0f + (this.padding * TWO);
        if (!this.initialized) {
            this.widthAnimation.set(f);
            this.heightAnimation.set(fMax);
            this.initialized = true;
        }
        this.widthAnimation.destination(f);
        this.heightAnimation.destination(fMax);
        setSize(this.widthAnimation.animatedValue(), this.heightAnimation.animatedValue());
        this.panelWidth = computePanelWidth(class698Var);
        this.headerBounds.withPosition(x(), y()).withSize(width(), height());
        this.clickBehavior.setDimensions(x(), y(), width(), height());
        this.checkmarkTexture.setDimensions(14, 14);
        if (this.optionItems.isEmpty()) {
            return;
        }
        float fY = y() + height() + 3.0f;
        for (int i = 0; i < this.optionItems.size(); i++) {
            this.optionItems.get(i).clickableBehavior().setDimensions(x(), fY + OPTION_PADDING + (i * 25.0f), this.panelWidth, OPTION_HEIGHT);
        }
    }

    @Override
    public void handleClose() {
        close();
        super.handleClose();
    }

    public void close() {
        if (this.open || !this.openAnimation.isZero()) {
            this.open = false;
            this.openAnimation.state(false);
        }
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.clickBehavior.animate(class141Var);
        this.openAnimation.state(this.open).animate(class141Var);
        this.widthAnimation.animate(class141Var);
        this.heightAnimation.animate(class141Var);
        for (DropdownOptionItem<T> class740Var : this.optionItems) {
            class740Var.clickableBehavior().animate(class141Var);
            if (this.selectedValue != null) {
                class740Var.currentOptionAnimation.state(Objects.equals(class740Var.option().value(), this.selectedValue));
                class740Var.currentOptionAnimation.animate(class141Var);
            }
        }
    }

    public float computePanelWidth(LayoutScaleContext class698Var) {
        float fMax = 0.0f;
        Iterator<DropdownOptionItem<T>> it = this.optionItems.iterator();
        while (it.hasNext()) {
            fMax = Math.max(fMax, class698Var.textWidthPhysical(this.optionFont, it.next().option().label().effective(), this.labelSize));
        }
        return Math.max(width(), fMax + this.checkmarkTexture.width() + 25.0f);
    }

    public List<DropdownOption<T>> options() {
        return this.optionItems.stream().map((v0) -> {
            return v0.option();
        }).toList();
    }

    public boolean isOpen() {
        return this.open;
    }
}
