package aethereal.event;
import aethereal.type.TickStage;

public class PlayerTickEvent extends CancellableEvent {
    public final TickStage stage;

    public boolean isPost() {
        return this.stage == TickStage.POST;
    }

    public boolean isPre() {
        return this.stage == TickStage.PRE;
    }

    public PlayerTickEvent(TickStage class131Var) {
        this.stage = class131Var;
    }

    public TickStage getStage() {
        return this.stage;
    }
}
