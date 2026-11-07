package aethereal.ui;


public final class WatermarkStar {
    public final float xOff;
    public final float yOff;
    public final float w;
    public final float h;
    public final float alpha;

    WatermarkStar(float f, float f2, float f3, float f4, float f5) {
        this.xOff = f;
        this.yOff = f2;
        this.w = f3;
        this.h = f4;
        this.alpha = f5;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "xOff=" + this.xOff + ", " + "yOff=" + this.yOff + ", " + "w=" + this.w + ", " + "h=" + this.h + ", " + "alpha=" + this.alpha + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.xOff, this.yOff, this.w, this.h, this.alpha);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WatermarkStar)) return false;
        WatermarkStar o = (WatermarkStar) obj;
        return java.util.Objects.equals(this.xOff, o.xOff) && java.util.Objects.equals(this.yOff, o.yOff) && java.util.Objects.equals(this.w, o.w) && java.util.Objects.equals(this.h, o.h) && java.util.Objects.equals(this.alpha, o.alpha);
    }
public float xOff() {
        return this.xOff;
    }

    public float yOff() {
        return this.yOff;
    }

    public float w() {
        return this.w;
    }

    public float h() {
        return this.h;
    }

    public float alpha() {
        return this.alpha;
    }
}
