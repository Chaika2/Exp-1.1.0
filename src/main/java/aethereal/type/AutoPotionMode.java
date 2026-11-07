package aethereal.type;

import aethereal.Lang;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum AutoPotionMode implements DisplayNamed {
    LEGIT(Lang.AUTOPOTION_MODE_LEGIT),
    PACKET(Lang.AUTOPOTION_MODE_PACKET);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    AutoPotionMode(Translation class254Var) {
        this.displayName = class254Var;
    }
}