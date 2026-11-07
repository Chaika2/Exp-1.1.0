package aethereal.type;
import aethereal.module.CreeperFarmModule;

public class CreeperApproachRule implements PhaseRule {
    public final CreeperFarmModule module;

    @Override
    public boolean shouldEnter() {
        return !this.module.getCreepersSortedByDistance().isEmpty();
    }

    @Override
    public TpLootStage getPhase() {
        return TpLootStage.APPROACH;
    }

    public CreeperApproachRule(CreeperFarmModule class597Var) {
        this.module = class597Var;
    }
}
