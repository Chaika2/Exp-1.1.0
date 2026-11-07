package aethereal.model;

import net.minecraft.util.Identifier;

public final class PotionKey {
    public final Identifier id;
    public final int amplifier;

    public PotionKey(Identifier identifier, int i) {
        this.id = identifier;
        this.amplifier = i;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "id=" + this.id + ", " + "amplifier=" + this.amplifier + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.id, this.amplifier);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PotionKey)) return false;
        PotionKey o = (PotionKey) obj;
        return java.util.Objects.equals(this.id, o.id) && java.util.Objects.equals(this.amplifier, o.amplifier);
    }
public Identifier id() {
        return this.id;
    }

    public int amplifier() {
        return this.amplifier;
    }
}
