package aethereal.model;
import aethereal.event.InputEvent;
import aethereal.type.InputType;


public final class CursorMoveInput implements InputEvent {
    public final PixelPoint mousePosition;

    public CursorMoveInput(PixelPoint class708Var) {
        this.mousePosition = class708Var;
    }

    @Override
    public InputType type() {
        return InputType.CURSOR;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "mousePosition=" + this.mousePosition + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.mousePosition);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CursorMoveInput)) return false;
        CursorMoveInput o = (CursorMoveInput) obj;
        return java.util.Objects.equals(this.mousePosition, o.mousePosition);
    }
public PixelPoint mousePosition() {
        return this.mousePosition;
    }
}
