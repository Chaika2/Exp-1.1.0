package aethereal.ui;
import aethereal.render.ToggleAnimator;
import aethereal.model.WidgetBounds;

public class OrderedListRow<E> {
    public final E value;
    public final ToggleSwitch toggleSwitch;
    public final ToggleAnimator toggleAnimation;
    public final WidgetBounds rowRect = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
    public final WidgetBounds grabRect = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);

    public E value() {
        return this.value;
    }

    public ToggleSwitch toggleSwitch() {
        return this.toggleSwitch;
    }

    public ToggleAnimator toggleAnimation() {
        return this.toggleAnimation;
    }

    public WidgetBounds rowRect() {
        return this.rowRect;
    }

    public WidgetBounds grabRect() {
        return this.grabRect;
    }

    public OrderedListRow(E e, ToggleSwitch class754Var, ToggleAnimator class323Var) {
        this.value = e;
        this.toggleSwitch = class754Var;
        this.toggleAnimation = class323Var;
    }
}
