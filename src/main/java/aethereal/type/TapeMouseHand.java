package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum TapeMouseHand implements DisplayNamed {
    RIGHT(Lang.TAPEMOUSE_HANDMODE_RIGHT),
    LEFT(Lang.TAPEMOUSE_HANDMODE_LEFT);

    final Translation displayName;

    TapeMouseHand(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
