package aethereal.model;
import aethereal.event.InputEvent;
import aethereal.type.InputType;


public final class ScrollInput implements InputEvent {
    public final double deltaY;

    public ScrollInput(double d) {
        this.deltaY = d;
    }

    @Override
    public InputType type() {
        return InputType.SCROLL;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "deltaY=" + this.deltaY + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.deltaY);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ScrollInput)) return false;
        ScrollInput o = (ScrollInput) obj;
        return java.util.Objects.equals(this.deltaY, o.deltaY);
    }
public double deltaY() {
        return this.deltaY;
    }
}
