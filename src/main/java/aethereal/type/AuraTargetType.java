package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum AuraTargetType implements DisplayNamed {
    PLAYERS(Lang.SELECTTARGETS_PLAYERS),
    MOBS(Lang.SELECTTARGETS_MOBS),
    ANIMALS(Lang.SELECTTARGETS_ANIMALS),
    FRIENDS(Lang.SELECTTARGETS_FRIENDS);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }

    AuraTargetType(Translation class254Var) {
        this.displayName = class254Var;
    }
}
