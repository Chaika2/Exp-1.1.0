package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum GrimAdvancedMode implements DisplayNamed {
    GRIM_ADVANCED(Translation.clearText("Grim Advanced"));

    final Translation displayName;

    GrimAdvancedMode(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
