package aethereal.module;
import aethereal.Lang;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;
public enum WaterSpeedMode implements DisplayNamed {
    MATRIX(Lang.WATERSPEED_MODE_MATRIX),
    GRIM(Lang.WATERSPEED_MODE_GRIM),
    METAHVH(Lang.WATERSPEED_MODE_METAHVH);
    public final Translation displayName;
    WaterSpeedMode(Translation translation) {
        this.displayName = translation;
    }
    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
