package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum CrosshairStyle implements DisplayNamed {
    DEFAULT(Lang.CROSSHAIR_TYPE_DEFAULT),
    CIRCLE(Lang.CROSSHAIR_TYPE_CIRCLE);

    final Translation displayName;

    CrosshairStyle(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
