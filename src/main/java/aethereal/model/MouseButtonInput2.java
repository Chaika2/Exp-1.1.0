package aethereal.model;
import aethereal.type.ButtonAction;
import aethereal.render.ScreenResolution;

import net.minecraft.client.Mouse;

public final class MouseButtonInput2 {
    public final boolean intercepted;
    public final Mouse mouse;
    public final ButtonAction action;
    public final ScreenResolution resolution;
    public final int button;
    public final float aD;

    public MouseButtonInput2(boolean z, Mouse mouse, ButtonAction class108Var, ScreenResolution class710Var, int i, float f) {
        this.intercepted = z;
        this.mouse = mouse;
        this.action = class108Var;
        this.resolution = class710Var;
        this.button = i;
        this.aD = f;
    }

    public boolean press() {
        return this.action == ButtonAction.PRESS;
    }

    public boolean release() {
        return this.action == ButtonAction.RELEASE;
    }

    public boolean isLeftButtonPressed() {
        return this.button == 0;
    }

    public boolean isRightButtonPressed() {
        return this.button == 1;
    }

    public boolean isWithinBounds(float f, float f2, float f3, float f4) {
        double x = this.mouse.getX() / ((double) this.aD);
        double y = this.mouse.getY() / ((double) this.aD);
        return x >= ((double) f) && x <= ((double) (f + f3)) && y >= ((double) f2) && y <= ((double) (f2 + f4));
    }

    public float mouseX() {
        return (float) (this.mouse.getX() / ((double) this.aD));
    }

    public float mouseY() {
        return (float) (this.mouse.getY() / ((double) this.aD));
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "intercepted=" + this.intercepted + ", " + "mouse=" + this.mouse + ", " + "action=" + this.action + ", " + "resolution=" + this.resolution + ", " + "button=" + this.button + ", " + "aD=" + this.aD + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.intercepted, this.mouse, this.action, this.resolution, this.button, this.aD);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MouseButtonInput2)) return false;
        MouseButtonInput2 o = (MouseButtonInput2) obj;
        return java.util.Objects.equals(this.intercepted, o.intercepted) && java.util.Objects.equals(this.mouse, o.mouse) && java.util.Objects.equals(this.action, o.action) && java.util.Objects.equals(this.resolution, o.resolution) && java.util.Objects.equals(this.button, o.button) && java.util.Objects.equals(this.aD, o.aD);
    }
public boolean intercepted() {
        return this.intercepted;
    }

    public Mouse mouse() {
        return this.mouse;
    }

    public ButtonAction action() {
        return this.action;
    }

    public ScreenResolution resolution() {
        return this.resolution;
    }

    public int button() {
        return this.button;
    }

    public float scaleFactor() {
        return this.aD;
    }
}
