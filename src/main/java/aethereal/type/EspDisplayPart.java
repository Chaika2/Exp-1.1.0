package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum EspDisplayPart implements DisplayNamed {
    BOX(Lang.ESP_DISPLAY_BOX),
    TAGS(Lang.ESP_DISPLAY_TAGS),
    HEALTH(Lang.ESP_DISPLAY_HEALTH),
    OFFHAND(Lang.ESP_DISPLAY_OFFHAND),
    SHULKER(Lang.ESP_DISPLAY_SHULKER),
    ENCHANTS(Lang.ESP_DISPLAY_ENCHANTS),
    POTIONS(Lang.ESP_DISPLAY_POTIONS);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    EspDisplayPart(Translation class254Var) {
        this.displayName = class254Var;
    }
}
