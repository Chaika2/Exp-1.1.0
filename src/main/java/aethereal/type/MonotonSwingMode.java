package aethereal.type;
import aethereal.Lang;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum MonotonSwingMode implements DisplayNamed {
    OFF(Lang.ARMTWEAKS_MONOTON_SWING_OFF),
    NORMAL(Lang.ARMTWEAKS_MONOTON_SWING_NORMAL),
    CUSTOM_1(Lang.ARMTWEAKS_MONOTON_SWING_CUSTOM_1),
    CUSTOM_2(Lang.ARMTWEAKS_MONOTON_SWING_CUSTOM_2),
    CUSTOM_3(Lang.ARMTWEAKS_MONOTON_SWING_CUSTOM_3),
    CUSTOM_4(Lang.ARMTWEAKS_MONOTON_SWING_CUSTOM_4),
    CUSTOM_5(Lang.ARMTWEAKS_MONOTON_SWING_CUSTOM_5),
    CUSTOM_6(Lang.ARMTWEAKS_MONOTON_SWING_CUSTOM_6);

    public final Translation displayName;

    MonotonSwingMode(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
