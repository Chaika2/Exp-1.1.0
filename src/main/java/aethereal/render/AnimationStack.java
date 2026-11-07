package aethereal.render;

public class AnimationStack {
    public static final int capacity = 128;

    public final float[] stack = new float[capacity];
    public int pointer;

    public void begin() {
        float[] fArr = this.stack;
        this.pointer = 0;
        fArr[0] = 1.0f;
    }

    public void push() {
        int i = this.pointer + 1;
        if (i >= capacity) {
            overflow();
        }
        this.stack[i] = this.stack[this.pointer];
        this.pointer = i;
    }

    public void animation(float f) {
        this.stack[this.pointer] = f;
    }

    public float animation() {
        return this.stack[this.pointer];
    }

    public void pop() {
        int i = this.pointer - 1;
        this.pointer = i;
        if (i < 0) {
            underflow();
        }
    }

    public void end() {
        if (this.pointer > 0) {
            overflow();
        }
    }

    public void overflow() {
        throw new IllegalStateException("Stack overflow");
    }

    public void underflow() {
        throw new IllegalStateException("Stack underflow");
    }
}
