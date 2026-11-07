package aethereal.render;
import aethereal.model.ScissorRegion;

import java.util.ArrayDeque;
import java.util.Deque;

public class ScissorStack {
    public static final int maxSize = 16;
    public final Deque<ScissorRegion> stack = new ArrayDeque(maxSize);

    public void push(int i, int i2, int i3, int i4, float f, float f2, float f3, float f4) {
        if (this.stack.size() >= maxSize) {
            throw new IllegalStateException("Stack overflow");
        }
        ScissorRegion class048Var = new ScissorRegion(i, i2, i3, i4, f, f2, f3, f4);
        this.stack.push(class048Var);
        class048Var.apply();
    }

    public void pop() {
        if (this.stack.isEmpty()) {
            throw new IllegalStateException("Stack underflow");
        }
        this.stack.pop();
        currentScissor().apply();
    }

    public void end() {
        this.stack.pop();
        if (!this.stack.isEmpty()) {
            throw new IllegalStateException("Stack overflow");
        }
    }

    public ScissorRegion currentScissor() {
        ScissorRegion class048VarPeek = this.stack.peek();
        if (class048VarPeek == null) {
            throw new IllegalStateException("Stack underflow");
        }
        return class048VarPeek;
    }
}
