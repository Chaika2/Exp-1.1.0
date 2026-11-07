package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum NukerWorkMode implements DisplayNamed {
    EVERYWHERE(Lang.NUKER_WORKMODE_EVERYWHERE),
    ONLY_MINE(Lang.NUKER_WORKMODE_ONLY_MINE);

    public final Translation displayName;

    NukerWorkMode(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
