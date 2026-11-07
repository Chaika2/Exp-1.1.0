package aethereal.model;
import aethereal.ui.ClickableBehavior;
import aethereal.math.Easings;
import aethereal.type.MultiSelectOption;
import aethereal.render.ToggleAnimator;
import aethereal.ui.ToggleSwitch;

import java.util.function.Supplier;

public class MultiSelectOptionItem<T> {
    final MultiSelectOption<T> option;
    final ClickableBehavior clickableBehavior = new ClickableBehavior();
    public final ToggleAnimator currentOptionAnimation = new ToggleAnimator(250, Easings.EASE_IN_OUT_CUBIC);

    final ToggleSwitch toggleSwitch;

    public MultiSelectOptionItem(MultiSelectOption<T> class750Var, Supplier<Boolean> supplier) {
        this.option = class750Var;
        this.toggleSwitch = new ToggleSwitch(22.0f, 15.0f, supplier);
    }

    public MultiSelectOption<T> option() {
        return this.option;
    }

    public ClickableBehavior clickableBehavior() {
        return this.clickableBehavior;
    }

    public ToggleAnimator currentOptionAnimation() {
        return this.currentOptionAnimation;
    }

    public ToggleSwitch toggleSwitch() {
        return this.toggleSwitch;
    }
}
