package aethereal.type;

public class ChunkLoadingRule implements PhaseRule {
    @Override
    public boolean shouldEnter() {
        return true;
    }

    @Override
    public TpLootStage getPhase() {
        return TpLootStage.LOADING_CHUNKS;
    }
}
