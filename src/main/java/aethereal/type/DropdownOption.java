package aethereal.type;
import aethereal.model.Translation;


public final class DropdownOption<T> {
    public final T value;
    public final Translation label;

    public DropdownOption(T t, Translation class254Var) {
        this.value = t;
        this.label = class254Var;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "value=" + this.value + ", " + "label=" + this.label + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.value, this.label);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DropdownOption)) return false;
        DropdownOption o = (DropdownOption) obj;
        return java.util.Objects.equals(this.value, o.value) && java.util.Objects.equals(this.label, o.label);
    }
public T value() {
        return this.value;
    }

    public Translation label() {
        return this.label;
    }
}
