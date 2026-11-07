package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum AspectRatioPreset implements DisplayNamed {
    RESOLUTION_16_9(Lang.ASPECTRATIO_RESOLUTION_16_9),
    RESOLUTION_16_10(Lang.ASPECTRATIO_RESOLUTION_16_10),
    RESOLUTION_21_9(Lang.ASPECTRATIO_RESOLUTION_21_9),
    RESOLUTION_4_3(Lang.ASPECTRATIO_RESOLUTION_4_3),
    CUSTOM(Lang.ASPECTRATIO_RESOLUTION_CUSTOM);

    final Translation displayName;

    AspectRatioPreset(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
