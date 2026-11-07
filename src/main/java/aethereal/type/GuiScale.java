package aethereal.type;
import aethereal.model.Translation;

import java.util.Arrays;
import java.util.List;

public enum GuiScale {
    AUTO("Auto", 1.0f, true),
    SCALE_75("75%", 0.75f, false),
    SCALE_100("100%", 1.0f, false),
    SCALE_125("125%", 1.25f, false),
    SCALE_150("150%", 1.5f, false),
    SCALE_175("175%", 1.75f, false),
    SCALE_200("200%", 2.0f, false),
    SCALE_300("300%", 3.0f, false);

    public final Translation label;
    public final float scaleFactor;
    public final boolean auto;

    GuiScale(String str, float f, boolean z) {
        this.label = Translation.clearText(str);
        this.scaleFactor = f;
        this.auto = z;
    }

    public Translation label() {
        return this.label;
    }

    public float scaleFactor() {
        return this.scaleFactor;
    }

    public boolean auto() {
        return this.auto;
    }

    public static GuiScale nearestScale(float f) {
        GuiScale class785Var = SCALE_100;
        float f2 = Float.MAX_VALUE;
        for (GuiScale class785Var2 : values()) {
            if (!class785Var2.auto) {
                float fAbs = Math.abs(class785Var2.scaleFactor - f);
                if (fAbs < f2) {
                    f2 = fAbs;
                    class785Var = class785Var2;
                }
            }
        }
        return class785Var;
    }

    public static List<DropdownOption<GuiScale>> options() {
        return Arrays.stream(values()).map(class785Var -> {
            return new DropdownOption<GuiScale>(class785Var, class785Var.label());
        }).toList();
    }
}
