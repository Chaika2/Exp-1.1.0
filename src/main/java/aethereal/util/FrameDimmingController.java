package aethereal.util;
import aethereal.ui.AbstractFrame;
import aethereal.render.AnimatedFloat;
import aethereal.math.Easings;

public class FrameDimmingController {
    public final AnimatedFloat dimAnimation = new AnimatedFloat(400, Easings.EASE_IN_OUT_CUBIC);
    public AbstractFrame focusedFrame = null;
    public long focusStartTime = -1;
    public static final long FOCUS_DURATION = 2000;

    public void focusFrame(AbstractFrame class757Var) {
        if (class757Var == null) {
            return;
        }
        this.focusedFrame = class757Var;
        this.focusStartTime = System.currentTimeMillis();
        this.dimAnimation.destination(1.0f);
    }

    public void clearFocus() {
        this.focusedFrame = null;
        this.focusStartTime = -1L;
        this.dimAnimation.destination(0.0f);
    }

    public void animate(WeightedEngine class141Var) {
        if (this.focusedFrame != null && this.focusStartTime > 0 && System.currentTimeMillis() - this.focusStartTime >= FOCUS_DURATION) {
            clearFocus();
        }
        this.dimAnimation.animate(class141Var);
    }

    public float getDimmingForFrame(AbstractFrame class757Var) {
        if (this.focusedFrame == null || class757Var == this.focusedFrame) {
            return 0.0f;
        }
        return this.dimAnimation.animatedValue();
    }

    public boolean isDimming() {
        return this.dimAnimation.animatedValue() > 0.01f;
    }

    public AbstractFrame focusedFrame() {
        return this.focusedFrame;
    }
}
