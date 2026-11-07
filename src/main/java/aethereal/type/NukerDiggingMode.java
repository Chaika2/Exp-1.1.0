package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum NukerDiggingMode implements DisplayNamed {
    EVERYONE(Lang.NUKER_DIGGINGMODE_EVERYONE),
    ORE_PRIORITY(Lang.NUKER_DIGGINGMODE_ORE_PRIORITY),
    ONLY_ORE(Lang.NUKER_DIGGINGMODE_ONLY_ORE);

    public final Translation displayName;

    NukerDiggingMode(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
