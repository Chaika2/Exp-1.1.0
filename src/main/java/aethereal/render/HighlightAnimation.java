package aethereal.render;
import aethereal.math.EasingFunction;
import aethereal.util.WeightedEngine;

public class HighlightAnimation {
    public final AnimatedFloat animation;

    public long duration = 2000;
    public long triggerTime = -1;
    public boolean highlighting = false;

    public HighlightAnimation(int i, EasingFunction class356Var) {
        this.animation = new AnimatedFloat(i, class356Var);
        this.animation.set(0.0f);
        this.animation.destination(0.0f);
    }

    public void trigger() {
        this.highlighting = true;
        this.triggerTime = System.currentTimeMillis();
        this.animation.destination(1.0f);
    }

    public void animate(WeightedEngine class141Var) {
        if (this.highlighting && System.currentTimeMillis() - this.triggerTime >= this.duration) {
            this.animation.destination(0.0f);
            if (this.animation.animatedValue() < 0.01f) {
                this.highlighting = false;
                this.triggerTime = -1L;
            }
        }
        this.animation.animate(class141Var);
    }

    public void clearHighlight() {
        this.highlighting = false;
        this.triggerTime = -1L;
        this.animation.set(0.0f);
        this.animation.destination(0.0f);
    }

    public float value() {
        return this.animation.animatedValue();
    }

    public void reset() {
        this.highlighting = false;
        this.triggerTime = -1L;
        this.animation.set(0.0f);
        this.animation.destination(0.0f);
    }

    public AnimatedFloat animation() {
        return this.animation;
    }

    public HighlightAnimation highlightDuration(long j) {
        this.duration = j;
        return this;
    }

    public boolean highlighting() {
        return this.highlighting;
    }
}
