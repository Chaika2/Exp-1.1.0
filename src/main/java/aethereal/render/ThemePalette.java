package aethereal.render;


public final class ThemePalette {
    public final ColorValue accent;
    public final ColorValue accentBright;
    public final ColorValue favorite;
    public final ColorValue frameBackground;

    public final ColorToneScale text;

    public final ColorToneScale error;

    public final ColorToneScale surfaceOutline;

    public final ColorToneScale surfaceBackground;
    public static ColorValue white = ColorValue.fromHex("FFFFFF");
    public static ColorValue golden = ColorValue.fromHex("FDC95A");
    public static ColorValue darkGolden = ColorValue.fromHex("E9B444");
    public static ColorValue darkOrange = ColorValue.fromHex("DA944F");
    public static ColorValue deepGreen = ColorValue.fromHex("4CA765");
    public static ColorValue darkGreen = ColorValue.fromHex("49A153");
    public static ColorValue red = ColorValue.fromHex("FF4C4F");
    public static ColorValue darkRed = ColorValue.fromHex("C83D3F");
    public static ColorValue violet = ColorValue.fromHex("7267FF");
    public static ColorValue lightOrange = ColorValue.fromHex("E87421");
    public static ColorValue blue = ColorValue.fromHex("5E84FF");
    public static ColorValue mistyBlue = ColorValue.fromHex("7986CB");
    public static ColorValue mint = ColorValue.fromHex("81C784");
    public static ColorValue grey = ColorValue.fromHex("868791");

    public ThemePalette(ColorValue class761Var, ColorValue class761Var2, ColorValue class761Var3, ColorValue class761Var4, ColorToneScale class759Var, ColorToneScale class759Var2, ColorToneScale class759Var3, ColorToneScale class759Var4) {
        this.accent = class761Var;
        this.accentBright = class761Var2;
        this.favorite = class761Var3;
        this.frameBackground = class761Var4;
        this.text = class759Var;
        this.error = class759Var2;
        this.surfaceOutline = class759Var3;
        this.surfaceBackground = class759Var4;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "accent=" + this.accent + ", " + "accentBright=" + this.accentBright + ", " + "favorite=" + this.favorite + ", " + "frameBackground=" + this.frameBackground + ", " + "text=" + this.text + ", " + "error=" + this.error + ", " + "surfaceOutline=" + this.surfaceOutline + ", " + "surfaceBackground=" + this.surfaceBackground + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.accent, this.accentBright, this.favorite, this.frameBackground, this.text, this.error, this.surfaceOutline, this.surfaceBackground);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ThemePalette)) return false;
        ThemePalette o = (ThemePalette) obj;
        return java.util.Objects.equals(this.accent, o.accent) && java.util.Objects.equals(this.accentBright, o.accentBright) && java.util.Objects.equals(this.favorite, o.favorite) && java.util.Objects.equals(this.frameBackground, o.frameBackground) && java.util.Objects.equals(this.text, o.text) && java.util.Objects.equals(this.error, o.error) && java.util.Objects.equals(this.surfaceOutline, o.surfaceOutline) && java.util.Objects.equals(this.surfaceBackground, o.surfaceBackground);
    }
public ColorValue accent() {
        return this.accent;
    }

    public ColorValue accentBright() {
        return this.accentBright;
    }

    public ColorValue favorite() {
        return this.favorite;
    }

    public ColorValue frameBackground() {
        return this.frameBackground;
    }

    public ColorToneScale text() {
        return this.text;
    }

    public ColorToneScale error() {
        return this.error;
    }

    public ColorToneScale surfaceOutline() {
        return this.surfaceOutline;
    }

    public ColorToneScale surfaceBackground() {
        return this.surfaceBackground;
    }
}
