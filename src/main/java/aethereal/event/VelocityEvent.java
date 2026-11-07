package aethereal.event;

public class VelocityEvent extends CancellableEvent {
    public double horizontal;
    public double vertical;

    public double getHorizontal() {
        return this.horizontal;
    }

    public double getVertical() {
        return this.vertical;
    }

    public void setHorizontal(double d) {
        this.horizontal = d;
    }

    public void setVertical(double d) {
        this.vertical = d;
    }

    public VelocityEvent(double d, double d2) {
        this.horizontal = d;
        this.vertical = d2;
    }
}
