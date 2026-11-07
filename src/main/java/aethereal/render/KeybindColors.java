package aethereal.render;
import aethereal.util.KeybindColorsBuilder;

public final class KeybindColors {
    public final int outline;
    public final int background;
    public final int text;
    public final int textEmpty;

    public static KeybindColors fromPalette(ThemePalette class764Var, ColorStack class115Var) {
        return builder().outline(class115Var.computeColor(class764Var.surfaceOutline().tone(400).argb())).background(class115Var.computeColor(class764Var.surfaceBackground().tone(600).argb())).text(class115Var.computeColor(class764Var.text().tone(400).argb())).textEmpty(class115Var.computeColor(class764Var.text().tone(600).argb())).build();
    }

    public KeybindColors(int i, int i2, int i3, int i4) {
        this.outline = i;
        this.background = i2;
        this.text = i3;
        this.textEmpty = i4;
    }

    public static KeybindColorsBuilder builder() {
        return new KeybindColorsBuilder();
    }

    public int outline() {
        return this.outline;
    }

    public int background() {
        return this.background;
    }

    public int text() {
        return this.text;
    }

    public int textEmpty() {
        return this.textEmpty;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof KeybindColors)) {
            return false;
        }
        KeybindColors class744Var = (KeybindColors) obj;
        return outline() == class744Var.outline() && background() == class744Var.background() && text() == class744Var.text() && textEmpty() == class744Var.textEmpty();
    }

    public int hashCode() {
        return (((((((1 * 59) + outline()) * 59) + background()) * 59) + text()) * 59) + textEmpty();
    }

    public String toString() {
        return "KeyBindableElement.KeyBindableColors(outline=" + outline() + ", background=" + background() + ", text=" + text() + ", textEmpty=" + textEmpty() + ")";
    }
}
