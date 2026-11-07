package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum VelocityMode implements DisplayNamed {
    CANCEL(Translation.clearText("Cancel")),
    JUMP_RESET(Translation.clearText("Jump Reset")),
    GRIM(Translation.clearText("Grim 2344"));

    final Translation displayName;

    VelocityMode(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
