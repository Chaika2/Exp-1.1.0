package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum EspTargetType implements DisplayNamed {
    SELF(Lang.SELECTTARGETS_SELF),
    PLAYERS(Lang.SELECTTARGETS_PLAYERS),
    MOBS(Lang.SELECTTARGETS_MOBS),
    ANIMALS(Lang.SELECTTARGETS_ANIMALS),
    FRIENDS(Lang.SELECTTARGETS_FRIENDS),
    ITEMS(Lang.SELECTTARGETS_ITEMS);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    EspTargetType(Translation class254Var) {
        this.displayName = class254Var;
    }
}
