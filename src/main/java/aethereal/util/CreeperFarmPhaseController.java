package aethereal.util;
import aethereal.module.CreeperFarmModule;
import aethereal.type.PhaseRule;
import aethereal.type.TpLootStage;

import java.util.ArrayList;
import java.util.List;

public class CreeperFarmPhaseController {
    TpLootStage currentPhase = TpLootStage.APPROACH;
    public final List<PhaseRule> rules = new ArrayList();
    public final CreeperFarmModule module;

    public void tickPhaseRules() {
        for (PhaseRule class530Var : this.rules) {
            if (class530Var.shouldEnter()) {
                setPhase(class530Var.getPhase());
                return;
            }
        }
    }

    public void addRule(PhaseRule class530Var) {
        this.rules.add(class530Var);
    }

    public void setPhase(TpLootStage class529Var) {
        if (this.currentPhase != class529Var) {
            this.currentPhase = class529Var;
            if (this.module.getStatsHandler().getStats() != null) {
                this.module.getStatsHandler().getStats().currentPhase = class529Var;
            }
        }
    }

    public boolean isPhase(TpLootStage class529Var) {
        return this.currentPhase == class529Var;
    }

    public TpLootStage getCurrentPhase() {
        return this.currentPhase;
    }

    public CreeperFarmPhaseController(CreeperFarmModule class597Var) {
        this.module = class597Var;
    }
}
