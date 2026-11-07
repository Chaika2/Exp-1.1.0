package aethereal.event;

public class RotatedMovementInputEvent implements Event {
    public float forward;
    public float sideways;

    public RotatedMovementInputEvent(float f, float f2) {
        this.forward = f;
        this.sideways = f2;
    }

    public float getForward() {
        return this.forward;
    }

    public float getSideways() {
        return this.sideways;
    }

    public void setForward(float f) {
        this.forward = f;
    }

    public void setSideways(float f) {
        this.sideways = f;
    }

    public String toString() {
        return "RotatedMovementInputEvent(forward=" + getForward() + ", sideways=" + getSideways() + ")";
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RotatedMovementInputEvent)) {
            return false;
        }
        RotatedMovementInputEvent class230Var = (RotatedMovementInputEvent) obj;
        return class230Var.canEqual(this) && Float.compare(getForward(), class230Var.getForward()) == 0 && Float.compare(getSideways(), class230Var.getSideways()) == 0;
    }

    public boolean canEqual(Object obj) {
        return obj instanceof RotatedMovementInputEvent;
    }

    public int hashCode() {
        return (((1 * 59) + Float.floatToIntBits(getForward())) * 59) + Float.floatToIntBits(getSideways());
    }
}
