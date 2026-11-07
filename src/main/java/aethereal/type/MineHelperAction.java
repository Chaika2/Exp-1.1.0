package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum MineHelperAction implements DisplayNamed {
    NEXT_MINE(Lang.MINE_HELPER_HELP_TYPE_NEXT_MINE),
    CLEAN_INVENTORY(Lang.MINE_HELPER_HELP_TYPE_CLEAN_INVENTORY),
    SAVE_PICKAXE(Lang.MINE_HELPER_HELP_TYPE_SAVE_PICKAXE);

    public final Translation displayName;

    MineHelperAction(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
