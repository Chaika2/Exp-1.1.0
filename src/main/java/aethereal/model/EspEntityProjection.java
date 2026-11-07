package aethereal.model;

import net.minecraft.entity.LivingEntity;
import org.joml.Vector4f;

public final class EspEntityProjection {
    public final LivingEntity living;
    public final Vector4f proj;
    public final boolean friend;

    public EspEntityProjection(LivingEntity livingEntity, Vector4f vector4f, boolean z) {
        this.living = livingEntity;
        this.proj = vector4f;
        this.friend = z;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "living=" + this.living + ", " + "proj=" + this.proj + ", " + "friend=" + this.friend + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.living, this.proj, this.friend);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof EspEntityProjection)) return false;
        EspEntityProjection o = (EspEntityProjection) obj;
        return java.util.Objects.equals(this.living, o.living) && java.util.Objects.equals(this.proj, o.proj) && java.util.Objects.equals(this.friend, o.friend);
    }
public LivingEntity living() {
        return this.living;
    }

    public Vector4f proj() {
        return this.proj;
    }

    public boolean friend() {
        return this.friend;
    }
}
