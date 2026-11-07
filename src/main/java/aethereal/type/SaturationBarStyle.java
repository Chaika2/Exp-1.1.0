package aethereal.type;
import aethereal.Lang;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum SaturationBarStyle implements DisplayNamed {
    SECOND_ROW(Lang.SATURATIONBAR_STYLE_SECOND_ROW),
    OUTLINE(Lang.SATURATIONBAR_STYLE_OUTLINE),
    OUTLINE_PULSE(Lang.SATURATIONBAR_STYLE_OUTLINE_PULSE),
    GRADIENT(Lang.SATURATIONBAR_STYLE_GRADIENT);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    SaturationBarStyle(Translation class254Var) {
        this.displayName = class254Var;
    }
}
