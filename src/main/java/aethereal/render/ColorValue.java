package aethereal.render;
import aethereal.util.StencilBufferUtil;


public final class ColorValue {
    public final int argb;

    public ColorValue(int i) {
        this.argb = i;
    }

    public static ColorValue fromRGBA(int i, int i2, int i3, int i4) {
        return new ColorValue(((i4 & StencilBufferUtil.STENCIL_MASK) << 24) | ((i & StencilBufferUtil.STENCIL_MASK) << 16) | ((i2 & StencilBufferUtil.STENCIL_MASK) << 8) | (i3 & StencilBufferUtil.STENCIL_MASK));
    }

    public static ColorValue fromHex(String str) {
        if (str == null) {
            throw new IllegalArgumentException("Hex string cannot be null");
        }
        if (str.length() == 6) {
            str = "FF" + str;
        } else if (str.length() != 8) {
            throw new IllegalArgumentException("Hex string must be 6 or 8 hex characters");
        }
        return new ColorValue((int) Long.parseLong(str, 16));
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "argb=" + this.argb + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.argb);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ColorValue)) return false;
        ColorValue o = (ColorValue) obj;
        return java.util.Objects.equals(this.argb, o.argb);
    }
public int argb() {
        return this.argb;
    }
}
