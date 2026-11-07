package aethereal.model;


public final class PotionEffectSpec {
    public final CharSequence id;
    public final int amplifier;
    public final int duration;

    public PotionEffectSpec(CharSequence charSequence, int i, int i2) {
        this.id = charSequence;
        this.amplifier = i;
        this.duration = i2;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "id=" + this.id + ", " + "amplifier=" + this.amplifier + ", " + "duration=" + this.duration + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.id, this.amplifier, this.duration);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PotionEffectSpec)) return false;
        PotionEffectSpec o = (PotionEffectSpec) obj;
        return java.util.Objects.equals(this.id, o.id) && java.util.Objects.equals(this.amplifier, o.amplifier) && java.util.Objects.equals(this.duration, o.duration);
    }
public CharSequence id() {
        return this.id;
    }

    public int amplifier() {
        return this.amplifier;
    }

    public int duration() {
        return this.duration;
    }
}
