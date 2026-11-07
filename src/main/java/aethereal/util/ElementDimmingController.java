package aethereal.util;
import aethereal.render.AnimatedFloat;
import aethereal.math.Easings;
import aethereal.ui.ModuleFrame;

public class ElementDimmingController {
    public final AnimatedFloat dimAnimation = new AnimatedFloat(400, Easings.EASE_IN_OUT_CUBIC);
    public ModuleFrame focusedElement = null;
    public long focusStartTime = -1;
    public long focusDuration = 2000;

    public ElementDimmingController() {
        this.dimAnimation.set(0.0f);
        this.dimAnimation.destination(0.0f);
    }

    public void focusElement(ModuleFrame class758Var) {
        if (class758Var == null) {
            return;
        }
        this.focusedElement = class758Var;
        this.focusStartTime = System.currentTimeMillis();
        this.dimAnimation.destination(1.0f);
    }

    public void clearFocus() {
        this.focusedElement = null;
        this.focusStartTime = -1L;
        this.dimAnimation.destination(0.0f);
    }

    public void animate(WeightedEngine class141Var) {
        if (this.focusedElement != null && this.focusStartTime > 0 && System.currentTimeMillis() - this.focusStartTime >= this.focusDuration) {
            clearFocus();
        }
        this.dimAnimation.animate(class141Var);
    }

    public float getDimmingForElement(ModuleFrame class758Var) {
        if (this.focusedElement == null || class758Var == this.focusedElement) {
            return 0.0f;
        }
        return this.dimAnimation.animatedValue();
    }

    public boolean isDimming() {
        return this.dimAnimation.animatedValue() > 0.01f;
    }

    public boolean hasFocus() {
        return this.focusedElement != null;
    }

    public ModuleFrame focusedElement() {
        return this.focusedElement;
    }

    public ElementDimmingController dimmingDuration(long j) {
        this.focusDuration = j;
        return this;
    }
}
