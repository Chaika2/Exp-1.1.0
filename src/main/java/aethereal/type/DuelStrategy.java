package aethereal.type;
import aethereal.model.DuelContext;
import aethereal.event.HandledScreenRenderEvent;

public interface DuelStrategy {
    void doDuelLogic(DuelContext class445Var);

    void onScreen(HandledScreenRenderEvent class015Var, DuelContext class445Var);

    void onChat(String str, DuelContext class445Var);

    void deactivate();
}
