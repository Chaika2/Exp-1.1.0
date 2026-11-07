package aethereal.model;


public final class PixelPoint {
    public final int x;
    public final int y;

    public PixelPoint(double d, double d2) {
        this(round(d), round(d2));
    }

    public PixelPoint(int i, int i2) {
        this.x = i;
        this.y = i2;
    }

    public static int round(double d) {
        return (int) Math.round(d);
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "x=" + this.x + ", " + "y=" + this.y + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.x, this.y);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PixelPoint)) return false;
        PixelPoint o = (PixelPoint) obj;
        return java.util.Objects.equals(this.x, o.x) && java.util.Objects.equals(this.y, o.y);
    }
public int x() {
        return this.x;
    }

    public int y() {
        return this.y;
    }
}
