package aethereal.type;
import aethereal.Lang;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum PopChamsColorMode implements DisplayNamed {
    CUSTOM(Lang.POPCHAMS_COLOR_MODE_CUSTOM),
    WHITE(Lang.POPCHAMS_COLOR_MODE_WHITE),
    RAINBOW(Lang.POPCHAMS_COLOR_MODE_RAINBOW),
    CLIENT(Lang.POPCHAMS_COLOR_MODE_CLIENT),
    FLAT(Lang.POPCHAMS_COLOR_MODE_FLAT);

    public final Translation displayName;

    PopChamsColorMode(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}