package aethereal.model;

import net.minecraft.entity.ItemEntity;
import org.joml.Vector4f;

public final class EspItemProjection {
    public final ItemEntity item;

    public final Vector4f proj;

    public EspItemProjection(ItemEntity itemEntity, Vector4f vector4f) {
        this.item = itemEntity;
        this.proj = vector4f;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "item=" + this.item + ", " + "proj=" + this.proj + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.item, this.proj);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof EspItemProjection)) return false;
        EspItemProjection o = (EspItemProjection) obj;
        return java.util.Objects.equals(this.item, o.item) && java.util.Objects.equals(this.proj, o.proj);
    }
public ItemEntity item() {
        return this.item;
    }

    public Vector4f proj() {
        return this.proj;
    }
}
