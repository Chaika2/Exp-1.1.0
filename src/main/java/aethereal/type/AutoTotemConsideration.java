package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum AutoTotemConsideration implements DisplayNamed {
    GOLDEN_HEARTS(Lang.COMBAT_AUTOTOTEM_CONSIDER_GOLDEN_HEARTS),
    SPHERES(Lang.COMBAT_AUTOTOTEM_CONSIDER_SPHERES),
    ELYTRA(Lang.COMBAT_AUTOTOTEM_CONSIDER_ELYTRA);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }

    AutoTotemConsideration(Translation class254Var) {
        this.displayName = class254Var;
    }
}
