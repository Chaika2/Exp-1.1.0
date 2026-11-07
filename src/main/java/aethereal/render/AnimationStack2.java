package aethereal.render;

public class AnimationStack2 {
    public final AnimationStack stack = new AnimationStack();

    public void begin() {
        this.stack.begin();
    }

    public float animationMultiplier() {
        return this.stack.animation();
    }

    public void push() {
        this.stack.push();
    }

    public void animation(ToggleAnimator class323Var) {
        this.stack.animation(class323Var.smoothAnimation());
    }

    public void pop() {
        this.stack.pop();
    }

    public void end() {
        this.stack.end();
    }
}
