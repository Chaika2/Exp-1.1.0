package aethereal.type;

public class CreeperUnloadRule implements PhaseRule {
    public final CreeperFarmUnloadPhase unloadPhase;

    @Override
    public boolean shouldEnter() {
        if (!this.unloadPhase.tryStartUnload()) {
            return false;
        }
        if (this.unloadPhase.getUnloadTarget() != CreeperFarmUnloadTarget.CHEST) {
        }
        return true;
    }

    @Override
    public TpLootStage getPhase() {
        return TpLootStage.UNLOADING;
    }

    public CreeperUnloadRule(CreeperFarmUnloadPhase class600Var) {
        this.unloadPhase = class600Var;
    }
}
