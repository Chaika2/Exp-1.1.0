package aethereal.model;
import aethereal.event.InputEvent;
import aethereal.type.InputType;


public final class CharInput implements InputEvent {
    public final int codePoint;
    public final int mods;

    public CharInput(int i, int i2) {
        this.codePoint = i;
        this.mods = i2;
    }

    @Override
    public InputType type() {
        return InputType.CHAR;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "codePoint=" + this.codePoint + ", " + "mods=" + this.mods + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.codePoint, this.mods);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CharInput)) return false;
        CharInput o = (CharInput) obj;
        return java.util.Objects.equals(this.codePoint, o.codePoint) && java.util.Objects.equals(this.mods, o.mods);
    }
public int codePoint() {
        return this.codePoint;
    }

    public int mods() {
        return this.mods;
    }
}
