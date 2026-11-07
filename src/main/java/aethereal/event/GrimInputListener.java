package aethereal.event;
import aethereal.type.EventPriority;
import aethereal.Expensive;
import aethereal.util.GrimDelayHandler;
import aethereal.type.Mc;

public class GrimInputListener implements ClientListener {
    public GrimInputListener() {
        Expensive.INSTANCE.eventDispatcher().register(MovementInputEvent.class, class040Var -> {
            if (Mc.INSTANCE.isWorldLoaded()) {
                GrimDelayHandler.input(class040Var);
            }
        }, EventPriority.HIGHEST);
    }
}
