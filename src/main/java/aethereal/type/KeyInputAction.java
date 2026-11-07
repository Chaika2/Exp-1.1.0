package aethereal.type;


public final class KeyInputAction {
    public final int action;

    public KeyInputAction(int i) {
        this.action = i;
    }

    public boolean press() {
        return this.action == 1;
    }

    public boolean repeat() {
        return this.action == 2;
    }

    public boolean release() {
        return this.action == 0;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "action=" + this.action + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.action);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof KeyInputAction)) return false;
        KeyInputAction o = (KeyInputAction) obj;
        return java.util.Objects.equals(this.action, o.action);
    }
public int action() {
        return this.action;
    }
}
