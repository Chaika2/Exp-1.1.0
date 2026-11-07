package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum TargetPearlMode implements DisplayNamed {
    AURA(Lang.TARGETPEARL_TARGET_AURA),
    ALL(Lang.TARGETPEARL_TARGET_ALL);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }

    TargetPearlMode(Translation class254Var) {
        this.displayName = class254Var;
    }
}
