package aethereal.ui;
import aethereal.render.AnimatedFloat;
import aethereal.resource.ClasspathResource;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.model.InputEventContext;
import aethereal.model.LayoutScaleContext;
import aethereal.model.MouseButtonInput;
import aethereal.render.MsdfFont;
import aethereal.type.MultiSelectOption;
import aethereal.model.MultiSelectOptionItem;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.joml.Quaternionf;

public class MultiSelectDropdown<T> extends AbstractWidget {
    public boolean initialized;
    public final float dropdownWidth;
    public final MsdfFont font;
    public final GlTexture icon;
    public final float cornerRadius;
    public final float paddingX;
    public final float paddingY;
    public final int iconSize;
    public Consumer<Set<T>> selectCallback;
    public boolean open;
    public static final float optionHeight = 23.0f;
    public static final float optionPadding = 6.0f;
    public static final float rowSpacing = 2.0f;
    public final AnimatedFloat widthAnimation = new AnimatedFloat(220, Easings.LINEAR);
    public final AnimatedFloat heightAnimation = new AnimatedFloat(220, Easings.LINEAR);
    public final ClickableBehavior clickBehavior = new ClickableBehavior();
    public final List<MultiSelectOptionItem<T>> optionItems = new ArrayList();
    public final MsdfFont optionFont = Fonts.INTER_BOLD.get();
    public final GlTexture arrowIcon = new GlTexture(new ClasspathResource("/icons/menu/new/arrow.png")).setDimensions(8, 5);

    public final WidgetBounds headerBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);

    public final WidgetBounds dropdownBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
    public String text = "";
    public int textSize = 12;

    public final Set<T> selectedValues = new HashSet();
    public final ToggleAnimator openAnimation = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);

    public MultiSelectDropdown(MsdfFont class161Var, GlTexture class073Var, float f, float f2, float f3, int i, float f4) {
        this.font = class161Var;
        this.icon = class073Var;
        this.cornerRadius = f;
        this.paddingX = f2;
        this.paddingY = f3;
        this.iconSize = i;
        this.dropdownWidth = f4;
        this.clickBehavior.clickCallback(this::toggle);
    }

    public void onSelect(Consumer<Set<T>> consumer) {
        this.selectCallback = consumer;
    }

    public void setOptions(List<MultiSelectOption<T>> list, Set<T> set) {
        this.optionItems.clear();
        this.selectedValues.clear();
        for (MultiSelectOption<T> class750Var : list) {
            MultiSelectOptionItem<T> class751Var = new MultiSelectOptionItem<>(class750Var, () -> {
                return Boolean.valueOf(this.selectedValues.contains(class750Var.value()));
            });
            class751Var.clickableBehavior().clickCallback(() -> {
                toggleSelection(class751Var.option().value(), true);
            });
            class751Var.toggleSwitch().runnable(() -> {
                toggleSelection(class751Var.option().value(), true);
            });
            this.optionItems.add(class751Var);
        }
        if (set == null || set.isEmpty()) {
            this.selectedValues.addAll(list.stream().limit(1L).map((v0) -> {
                return v0.value();
            }).toList());
        } else {
            this.selectedValues.addAll(set);
        }
        ensureSelection();
        updateText();
    }

    public void label(String str, int i) {
        this.text = str;
        this.textSize = i;
    }

    public void toggle() {
        this.open = !this.open;
    }

    public void toggleSelection(T t, boolean z) {
        if (this.optionItems.isEmpty()) {
            return;
        }
        if (!this.selectedValues.contains(t)) {
            this.selectedValues.add(t);
        } else if (this.selectedValues.size() > 1) {
            this.selectedValues.remove(t);
        }
        ensureSelection();
        updateText();
        if (!z || this.selectCallback == null) {
            return;
        }
        this.selectCallback.accept(Set.copyOf(this.selectedValues));
    }

    public void ensureSelection() {
        if (!this.selectedValues.isEmpty() || this.optionItems.isEmpty()) {
            return;
        }
        this.selectedValues.add(((MultiSelectOptionItem<T>) this.optionItems.getFirst()).option().value());
    }

    public void updateText() {
        this.text = (String) this.optionItems.stream().filter(class751Var -> {
            return this.selectedValues.contains(class751Var.option().value());
        }).map(class751Var2 -> {
            return class751Var2.option().label();
        }).collect(Collectors.joining(", "));
        if (!this.text.isBlank() || this.optionItems.isEmpty()) {
            return;
        }
        this.text = ((MultiSelectOptionItem) this.optionItems.getFirst()).option().label();
    }

    @Override
    public void render(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        float fWidth = width();
        float fHeight = height();
        float fSmoothAnimation = this.openAnimation.smoothAnimation();
        float size = 12.0f + (optionHeight * this.optionItems.size()) + (rowSpacing * (this.optionItems.size() - 1));
        ToggleAnimator class323VarHoverAnimation = this.clickBehavior.hoverAnimation();
        ToggleAnimator class323Var = this.openAnimation;
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb(), 0.8f), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(800).argb(), 70), class323VarHoverAnimation);
        int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb());
        int iComputeColor2 = class115VarColorStack.computeColor(class764VarPalette.text().tone(400).argb());
        int iInterpolate2 = class115VarColorStack.interpolate(class115VarColorStack.interpolate(iComputeColor, iComputeColor2, class323Var, class323Var), iComputeColor2, class323VarHoverAnimation, class323VarHoverAnimation);
        class699Var.fillOutlinedRoundedRect(x(), y(), fWidth, fHeight, this.cornerRadius, 2.5f, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb()), iInterpolate);
        float fX = x() + this.paddingX;
        float fY = y() + (fHeight / rowSpacing);
        class699Var.texture(this.icon, fX, fY - (this.iconSize / 2), this.iconSize, this.iconSize, class115VarColorStack.computeColor(iInterpolate2));
        float fX2 = x() + this.paddingX + this.iconSize + 5.0f;
        float fY2 = (y() + (fHeight / rowSpacing)) - (this.font.getHeight(this.textSize) / rowSpacing);
        float fX3 = ((x() + fWidth) - this.paddingX) - 10.0f;
        float f = fY - (10.0f / rowSpacing);
        class699Var.text(this.font, truncateText(class699Var, this.font, this.text, this.textSize, Math.max(0.0f, (fX3 - optionPadding) - fX2)), this.textSize, fX2, fY2, iInterpolate2);
        float f2 = fX3 + (10.0f / rowSpacing);
        float f3 = f + (10.0f / rowSpacing);
        float fSmoothAnimation2 = (float) (3.141592653589793d * ((double) this.openAnimation.smoothAnimation()));
        class699Var.matrixStack().push();
        class699Var.matrixStack().translate(f2, f3, 0.0f);
        class699Var.matrixStack().multiply(new Quaternionf().rotateZ(fSmoothAnimation2));
        class699Var.matrixStack().translate(-f2, -f3, 0.0f);
        class699Var.texture(this.arrowIcon, fX3, f, 10.0f, 10.0f, class115VarColorStack.computeColor(class764VarPalette.text().tone(600).argb()));
        class699Var.matrixStack().pop();
        if (this.optionItems.isEmpty() || (!this.open && this.openAnimation.isZero())) {
            this.dropdownBounds.withSize(0.0f, 0.0f);
            return;
        }
        float fY3 = y() + fHeight + 3.0f + fSmoothAnimation;
        this.dropdownBounds.withPosition(x(), fY3).withSize(width(), size);
        class115VarColorStack.push();
        class115VarColorStack.alpha(fSmoothAnimation);
        class699Var.fillOutlinedRoundedRect(x(), fY3, fWidth, size, 8.0f, 2.5f, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb()));
        for (int i = 0; i < this.optionItems.size(); i++) {
            MultiSelectOptionItem<T> class751Var = this.optionItems.get(i);
            float f4 = fY3 + optionPadding + (i * 25.0f);
            int iInterpolate3 = class115VarColorStack.interpolate(class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(600).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()), class751Var.clickableBehavior().hoverAnimation()), class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb()), class751Var.currentOptionAnimation());
            float fX4 = x() + 5.0f;
            class751Var.toggleSwitch().render(class699Var);
            class699Var.text(this.optionFont, class751Var.option().label(), this.textSize, fX4 + 5.0f, (f4 + 11.5f) - (this.optionFont.getHeight(this.textSize) / rowSpacing), iInterpolate3);
        }
        class115VarColorStack.pop();
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        boolean z2 = false;
        if (this.open && !z && (class688Var.inputEvent() instanceof MouseButtonInput)) {
            boolean zInArea = class688Var.inArea(this.headerBounds.x(), this.headerBounds.y(), this.headerBounds.width(), this.headerBounds.height());
            boolean zInArea2 = class688Var.inArea(this.dropdownBounds.x(), this.dropdownBounds.y(), this.dropdownBounds.width(), this.dropdownBounds.height());
            if (!zInArea && !zInArea2) {
                close();
                return true;
            }
        }
        if (this.open) {
            for (int size = this.optionItems.size() - 1; size >= 0; size--) {
                MultiSelectOptionItem<T> class751Var = this.optionItems.get(size);
                if (class751Var.toggleSwitch().handleInput(class688Var, z || z2)) {
                    z2 = true;
                }
                if (class751Var.clickableBehavior().handleInput(class688Var, z || z2)) {
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
        float fMax = Math.max(this.iconSize, Math.max(this.font.metrics().lineHeight(), 10.0f)) + (this.paddingY * rowSpacing);
        float f = this.dropdownWidth;
        if (!this.initialized) {
            this.widthAnimation.set(f);
            this.heightAnimation.set(fMax);
            this.initialized = true;
        }
        this.widthAnimation.destination(f);
        this.heightAnimation.destination(fMax);
        setSize(this.widthAnimation.animatedValue(), this.heightAnimation.animatedValue());
        this.headerBounds.withPosition(x(), y()).withSize(width(), height());
        this.clickBehavior.setDimensions(x(), y(), width(), height());
        if (this.optionItems.isEmpty()) {
            return;
        }
        float fY = y() + height() + 3.0f;
        for (int i = 0; i < this.optionItems.size(); i++) {
            MultiSelectOptionItem<T> class751Var = this.optionItems.get(i);
            float f2 = fY + optionPadding + (i * 25.0f);
            class751Var.clickableBehavior().setDimensions(x(), f2, width(), optionHeight);
            class751Var.toggleSwitch().layout(class698Var);
            class751Var.toggleSwitch().setPosition(((x() + width()) - 9.0f) - class751Var.toggleSwitch().width(), ((f2 + 11.5f) - (class751Var.toggleSwitch().height() / rowSpacing)) + 1.0f);
            class751Var.toggleSwitch().layout(class698Var);
        }
    }

    public String truncateText(DrawCtx class699Var, MsdfFont class161Var, String str, int i, float f) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        if (class699Var.textWidthPhysical(class161Var, str, i) <= f) {
            return str;
        }
        if (class699Var.textWidthPhysical(class161Var, ".", i) > f) {
            return "";
        }
        int i2 = 0;
        int length = str.length();
        while (i2 < length) {
            int i3 = ((i2 + length) + 1) >>> 1;
            if (class699Var.textWidthPhysical(class161Var, str.substring(0, i3) + ".", i) <= f) {
                i2 = i3;
            } else {
                length = i3 - 1;
            }
        }
        return str.substring(0, i2) + ".";
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
        for (MultiSelectOptionItem<T> class751Var : this.optionItems) {
            class751Var.clickableBehavior().animate(class141Var);
            class751Var.toggleSwitch().animation(class141Var);
            class751Var.currentOptionAnimation.state(this.selectedValues.contains(class751Var.option().value()));
            class751Var.currentOptionAnimation.animate(class141Var);
        }
    }

    public List<MultiSelectOption<T>> options() {
        return this.optionItems.stream().map((v0) -> {
            return v0.option();
        }).toList();
    }

    public boolean isOpen() {
        return this.open;
    }
}
