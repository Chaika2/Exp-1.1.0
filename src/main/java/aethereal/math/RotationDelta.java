package aethereal.math;

import net.minecraft.util.math.Vec2f;

public final class RotationDelta {
    public final float deltaYaw;
    public final float deltaPitch;

    public RotationDelta(float f, float f2) {
        this.deltaYaw = f;
        this.deltaPitch = f2;
    }

    public float length() {
        return (float) Math.hypot(this.deltaYaw, this.deltaPitch);
    }

    public Vec2f toVec2f() {
        return new Vec2f(this.deltaYaw, this.deltaPitch);
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "deltaYaw=" + this.deltaYaw + ", " + "deltaPitch=" + this.deltaPitch + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.deltaYaw, this.deltaPitch);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RotationDelta)) return false;
        RotationDelta o = (RotationDelta) obj;
        return java.util.Objects.equals(this.deltaYaw, o.deltaYaw) && java.util.Objects.equals(this.deltaPitch, o.deltaPitch);
    }
public float deltaYaw() {
        return this.deltaYaw;
    }

    public float deltaPitch() {
        return this.deltaPitch;
    }
}
