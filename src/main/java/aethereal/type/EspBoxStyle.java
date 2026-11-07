package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum EspBoxStyle implements DisplayNamed {
    BOX(Lang.ESP_BOX_MODE_BOX),
    CORNER(Lang.ESP_BOX_MODE_CORNER),
    ROUNDED(Lang.ESP_BOX_MODE_ROUNDED),
    CELESTIAL(Lang.ESP_BOX_MODE_CELESTIAL);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    EspBoxStyle(Translation class254Var) {
        this.displayName = class254Var;
    }
}
