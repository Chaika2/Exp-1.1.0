package aethereal.model;
import aethereal.ui.ClickableBehavior;
import aethereal.type.DropdownOption;
import aethereal.math.Easings;
import aethereal.render.ToggleAnimator;

public class DropdownOptionItem<T> {
    final DropdownOption<T> option;
    final ClickableBehavior clickableBehavior = new ClickableBehavior();
    public final ToggleAnimator currentOptionAnimation = new ToggleAnimator(250, Easings.EASE_IN_OUT_CUBIC);

    public DropdownOption<T> option() {
        return this.option;
    }

    public ClickableBehavior clickableBehavior() {
        return this.clickableBehavior;
    }

    public ToggleAnimator currentOptionAnimation() {
        return this.currentOptionAnimation;
    }

    public DropdownOptionItem(DropdownOption<T> class739Var) {
        this.option = class739Var;
    }
}
