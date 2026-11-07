package aethereal.model;


public final class AttributeModifierSpec {
    public final CharSequence id;
    public final double amount;
    public final CharSequence slot;

    public AttributeModifierSpec(CharSequence charSequence, double d, CharSequence charSequence2) {
        this.id = charSequence;
        this.amount = d;
        this.slot = charSequence2;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "id=" + this.id + ", " + "amount=" + this.amount + ", " + "slot=" + this.slot + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.id, this.amount, this.slot);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AttributeModifierSpec)) return false;
        AttributeModifierSpec o = (AttributeModifierSpec) obj;
        return java.util.Objects.equals(this.id, o.id) && java.util.Objects.equals(this.amount, o.amount) && java.util.Objects.equals(this.slot, o.slot);
    }
public CharSequence id() {
        return this.id;
    }

    public double amount() {
        return this.amount;
    }

    public CharSequence slot() {
        return this.slot;
    }
}
