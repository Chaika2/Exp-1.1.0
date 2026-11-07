package aethereal.type;

public enum ShapeType {
    COLOR(0),
    TEXTURE(1),
    ROUNDED_RECTANGLE(2),
    ROUNDED_TEXTURE(3),
    BLUR(4),
    CHECKER(5),
    CIRCLE(6),
    OUTER_MASK(7),
    ALPHA_MASK(8),
    MSDF_FONT(9),
    RADIAL_ROUNDED_RECTANGLE(10);

    public final int mode;

    public int mode() {
        return this.mode;
    }

    ShapeType(int i) {
        this.mode = i;
    }
}
