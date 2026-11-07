package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum NoSlowMode implements DisplayNamed {
    CANCEL(Lang.CANCEL),
    MATRIX(Translation.clearText("Matrix")),
    HOLYWORLD(Translation.clearText("Holy World")),
    FUNTIME_CROSSBOW(Translation.clearText("Funtime Crossbow")),
    GRIM_50(Translation.clearText("Grim 50%")),
    GRIM(Translation.clearText("Old Grim"));

    final Translation displayName;

    NoSlowMode(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
