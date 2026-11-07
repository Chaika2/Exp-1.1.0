package aethereal.type;


public final class MultiSelectOption<T> {
    public final T value;
    public final String label;

    public MultiSelectOption(T t, String str) {
        this.value = t;
        this.label = str;
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
        if (!(obj instanceof MultiSelectOption)) return false;
        MultiSelectOption o = (MultiSelectOption) obj;
        return java.util.Objects.equals(this.value, o.value) && java.util.Objects.equals(this.label, o.label);
    }
public T value() {
        return this.value;
    }

    public String label() {
        return this.label;
    }
}
