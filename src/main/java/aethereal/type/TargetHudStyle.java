package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum TargetHudStyle implements DisplayNamed {
    LARGE(Lang.WIDGET_TARGETHUD_STYLE_LARGE),
    ZENITH(Lang.WIDGET_TARGETHUD_STYLE_ZENITH),
    NURS(Lang.WIDGET_TARGETHUD_STYLE_NURS),
    CELESTIAL(Lang.WIDGET_TARGETHUD_STYLE_CELESTIAL);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    TargetHudStyle(Translation class254Var) {
        this.displayName = class254Var;
    }
}