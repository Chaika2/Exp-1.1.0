package aethereal.math;

import net.minecraft.util.math.Vec3d;

public final class RotationVector {
    public final Rotation rotation;
    public final Vec3d vec;

    public RotationVector(Rotation class007Var, Vec3d vec3d) {
        this.rotation = class007Var;
        this.vec = vec3d;
    }

    public static RotationVector of(Rotation class007Var) {
        return new RotationVector(class007Var, class007Var.getDirectionVector());
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "rotation=" + this.rotation + ", " + "vec=" + this.vec + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.rotation, this.vec);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RotationVector)) return false;
        RotationVector o = (RotationVector) obj;
        return java.util.Objects.equals(this.rotation, o.rotation) && java.util.Objects.equals(this.vec, o.vec);
    }
public Rotation rotation() {
        return this.rotation;
    }

    public Vec3d vec() {
        return this.vec;
    }
}
