package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum TotemActivationMode implements DisplayNamed {
    AUTO(Lang.COMBAT_AUTOTOTEM_ACTIVATION_MODE_AUTO),
    BUTTON(Lang.COMBAT_AUTOTOTEM_ACTIVATION_MODE_BUTTON);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }

    TotemActivationMode(Translation class254Var) {
        this.displayName = class254Var;
    }
}
