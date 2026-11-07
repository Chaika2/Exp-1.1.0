package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum AutoTotemThreat implements DisplayNamed {
    EXPLOSION(Lang.COMBAT_AUTOTOTEM_OPTION_EXPLOSIONS),
    BROKEN_ARMOR(Lang.COMBAT_AUTOTOTEM_OPTION_BROKEN_ARMOR),
    PROJECTILES(Lang.COMBAT_AUTOTOTEM_OPTION_PROJECTILES),
    FALL(Lang.COMBAT_AUTOTOTEM_OPTION_FALL),
    TRIDENT(Lang.COMBAT_AUTOTOTEM_OPTION_TRIDENT),
    MACE(Lang.COMBAT_AUTOTOTEM_OPTION_MACE);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }

    AutoTotemThreat(Translation class254Var) {
        this.displayName = class254Var;
    }
}
