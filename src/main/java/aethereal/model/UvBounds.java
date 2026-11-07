package aethereal.model;

public class UvBounds {
    public final float u0;
    public final float v0;
    public final float u1;
    public final float v1;

    public UvBounds() {
        this.u0 = 0.0f;
        this.v0 = 0.0f;
        this.u1 = 0.0f;
        this.v1 = 0.0f;
    }

    public UvBounds(float f, float f2, float f3, float f4) {
        this.u0 = f;
        this.v0 = f2;
        this.u1 = f3;
        this.v1 = f4;
    }

    public float u0() {
        return this.u0;
    }

    public float u1() {
        return this.u1;
    }

    public float v0() {
        return this.v0;
    }

    public float v1() {
        return this.v1;
    }
}
