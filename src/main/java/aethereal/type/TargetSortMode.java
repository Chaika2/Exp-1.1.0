package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum TargetSortMode implements DisplayNamed {
    BY_FOV(Lang.ATTACKAURA_SORT_BY_FOV),
    BY_HEALTH(Lang.ATTACKAURA_SORT_BY_HEALTH),
    BY_DISTANCE(Lang.ATTACKAURA_SORT_BY_DISTANCE);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }

    TargetSortMode(Translation class254Var) {
        this.displayName = class254Var;
    }
}
