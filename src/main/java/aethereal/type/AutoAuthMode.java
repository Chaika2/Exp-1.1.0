package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum AutoAuthMode implements DisplayNamed {
    RANDOM(Lang.AUTOAUTH_MODE_RANDOM),
    CUSTOM(Lang.AUTOAUTH_MODE_CUSTOM);

    final Translation displayName;

    AutoAuthMode(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
