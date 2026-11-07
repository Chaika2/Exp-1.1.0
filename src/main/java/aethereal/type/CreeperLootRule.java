package aethereal.type;

public class CreeperLootRule implements PhaseRule {
    public final CreeperFarmLootPhase lootPhase;

    @Override
    public boolean shouldEnter() {
        return this.lootPhase.shouldEnter();
    }

    @Override
    public TpLootStage getPhase() {
        return TpLootStage.LOOTING;
    }

    public CreeperLootRule(CreeperFarmLootPhase class603Var) {
        this.lootPhase = class603Var;
    }
}
