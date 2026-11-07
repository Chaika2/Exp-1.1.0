package aethereal.model;


public final class KeyModifiers {
    public final int mods;

    public KeyModifiers(int i) {
        this.mods = i;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "mods=" + this.mods + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.mods);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof KeyModifiers)) return false;
        KeyModifiers o = (KeyModifiers) obj;
        return java.util.Objects.equals(this.mods, o.mods);
    }
public int mods() {
        return this.mods;
    }
}
