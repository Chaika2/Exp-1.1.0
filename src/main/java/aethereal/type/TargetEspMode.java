package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum TargetEspMode implements DisplayNamed {
    CUBE(Lang.TARGETESP_MODE_CUBE),
    GHOSTS(Lang.TARGETESP_MODE_GHOSTS);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    TargetEspMode(Translation class254Var) {
        this.displayName = class254Var;
    }
}
