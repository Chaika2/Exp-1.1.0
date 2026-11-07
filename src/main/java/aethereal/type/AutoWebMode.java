package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum AutoWebMode implements DisplayNamed {
    GRIM(Lang.AUTOWEB_MODE_GRIM),
    UNIVERSAL(Lang.AUTOWEB_MODE_UNIVERSAL);

    final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    AutoWebMode(Translation class254Var) {
        this.displayName = class254Var;
    }
}
