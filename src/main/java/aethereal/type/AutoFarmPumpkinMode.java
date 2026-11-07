package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum AutoFarmPumpkinMode implements DisplayNamed {
    SEEDS(Lang.AUTOFARM_PUMPKIN_MODE_SEEDS),
    BLOCK(Lang.AUTOFARM_PUMPKIN_MODE_BLOCK);

    public final Translation displayName;

    AutoFarmPumpkinMode(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
