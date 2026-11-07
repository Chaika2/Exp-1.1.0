package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum AuraRotationMode implements DisplayNamed {
    FUNTIME(Lang.ATTACKAURA_ROTATION_MODE_FUNTIME),
    SMOOTH(Translation.clearText("Smooth")),
    GRIM(Lang.ATTACKAURA_ROTATION_MODE_GRIM),
    SLOTHAC(Translation.clearText("SlothAC")),
    HOLYWORLD(Lang.ATTACKAURA_ROTATION_MODE_HOLYWORLD),
    BEZIER(Translation.clearText("Bezier")),
    MATRIX(Lang.ATTACKAURA_ROTATION_MODE_MATRIX);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }

    AuraRotationMode(Translation class254Var) {
        this.displayName = class254Var;
    }
}
