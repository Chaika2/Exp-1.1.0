package aethereal.util;
import aethereal.render.AnimationStack2;


public final class WeightedEngine {
    public final float weight;

    public final AnimationStack2 engine;

    public WeightedEngine(float f, AnimationStack2 class245Var) {
        this.weight = f;
        this.engine = class245Var;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "weight=" + this.weight + ", " + "engine=" + this.engine + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.weight, this.engine);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WeightedEngine)) return false;
        WeightedEngine o = (WeightedEngine) obj;
        return java.util.Objects.equals(this.weight, o.weight) && java.util.Objects.equals(this.engine, o.engine);
    }
public float weight() {
        return this.weight;
    }

    public AnimationStack2 engine() {
        return this.engine;
    }
}
