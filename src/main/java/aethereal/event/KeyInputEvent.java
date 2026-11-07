package aethereal.event;
import aethereal.type.KeyPressState;


public final class KeyInputEvent implements Event {
    public final KeyPressState action;
    public final int key;

    public KeyInputEvent(KeyPressState class050Var, int i) {
        this.action = class050Var;
        this.key = i;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "action=" + this.action + ", " + "key=" + this.key + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.action, this.key);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof KeyInputEvent)) return false;
        KeyInputEvent o = (KeyInputEvent) obj;
        return java.util.Objects.equals(this.action, o.action) && java.util.Objects.equals(this.key, o.key);
    }
public KeyPressState action() {
        return this.action;
    }

    public int key() {
        return this.key;
    }
}
