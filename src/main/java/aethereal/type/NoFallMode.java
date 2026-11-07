package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum NoFallMode implements DisplayNamed {
    SPOOF_GROUND(Lang.NOFALL_MODE_SPOOF_GROUND),
    NO_GROUND(Lang.NOFALL_MODE_NO_GROUND),
    PACKET(Lang.NOFALL_MODE_PACKET);

    final Translation displayName;

    NoFallMode(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
