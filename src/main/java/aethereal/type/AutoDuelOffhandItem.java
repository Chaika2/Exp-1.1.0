package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum AutoDuelOffhandItem implements DisplayNamed {
    ANY(Lang.AUTODUEL_OFFHAND_ITEM_ANY),
    SPHERE(Lang.AUTODUEL_OFFHAND_ITEM_SPHERE),
    TOTEM(Lang.AUTODUEL_OFFHAND_ITEM_TOTEM);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }

    AutoDuelOffhandItem(Translation class254Var) {
        this.displayName = class254Var;
    }
}
