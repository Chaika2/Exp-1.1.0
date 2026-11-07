package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum AutoTotemMode implements DisplayNamed {
    FUNTIME(Translation.clearText("Funtime")),
    ADVANCED(Translation.clearText("Advanced"));

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }

    AutoTotemMode(Translation class254Var) {
        this.displayName = class254Var;
    }
}
