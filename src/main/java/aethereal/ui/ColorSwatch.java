package aethereal.ui;
import aethereal.math.Easings;
import aethereal.render.ToggleAnimator;

public class ColorSwatch {
    final int color;
    final boolean system;
    final ClickableBehavior clickable = new ClickableBehavior();
    final ToggleAnimator selectAnim = new ToggleAnimator(250, Easings.EASE_IN_OUT_CUBIC);

    public int color() {
        return this.color;
    }

    public boolean system() {
        return this.system;
    }

    public ClickableBehavior clickable() {
        return this.clickable;
    }

    public ToggleAnimator selectAnim() {
        return this.selectAnim;
    }

    public ColorSwatch(int i, boolean z) {
        this.color = i;
        this.system = z;
    }
}
