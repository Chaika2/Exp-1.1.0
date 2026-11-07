package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum AutoSwapMode implements DisplayNamed {
    SMART(Translation.clearText("Умный")),
    BIND(Translation.clearText("Бинд"));

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }

    AutoSwapMode(Translation class254Var) {
        this.displayName = class254Var;
    }
}
