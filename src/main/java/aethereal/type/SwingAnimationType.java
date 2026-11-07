package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum SwingAnimationType implements DisplayNamed {
    TYPE_1(Translation.clearText("Type 1")),
    TYPE_2(Translation.clearText("Type 2")),
    TYPE_3(Translation.clearText("Type 3")),
    TYPE_4(Translation.clearText("Type 4")),
    TYPE_5(Translation.clearText("Type 5"));

    final Translation displayName;

    SwingAnimationType(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
