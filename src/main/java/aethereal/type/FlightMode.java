package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum FlightMode implements DisplayNamed {
    VANILLA(Lang.FLIGHT_MODE_VANILLA),
    CREATIVE(Lang.FLIGHT_MODE_CREATIVE),
    GRIM(Translation.clearText("Grim Blocks"));

    final Translation displayName;

    FlightMode(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
