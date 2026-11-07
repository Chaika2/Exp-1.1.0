package aethereal.type;
import aethereal.Lang;
import aethereal.model.Translation;

public enum TpLootStage {
    APPROACH(Lang.CREEPERFARM_PHASE_APPROACH),
    LOADING_CHUNKS(Lang.CREEPERFARM_PHASE_LOADING_CHUNKS),
    LOOTING(Lang.CREEPERFARM_PHASE_LOOTING),
    UNLOADING(Lang.CREEPERFARM_PHASE_UNLOADING);

    public final Translation name;

    TpLootStage(Translation class254Var) {
        this.name = class254Var;
    }

    public Translation getName() {
        return this.name;
    }
}
