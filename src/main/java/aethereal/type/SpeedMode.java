package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum SpeedMode implements DisplayNamed {
    MATRIX(Lang.SPEED_MODE_MATRIX),
    COLLISION(Lang.SPEED_MODE_COLLISION),
    HOLYWORLD(Translation.clearText("HolyWorld"));

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }

    SpeedMode(Translation class254Var) {
        this.displayName = class254Var;
    }
}
