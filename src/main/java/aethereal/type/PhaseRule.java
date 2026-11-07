package aethereal.type;

public interface PhaseRule {
    boolean shouldEnter();

    TpLootStage getPhase();
}
