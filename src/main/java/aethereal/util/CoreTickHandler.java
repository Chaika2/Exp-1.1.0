package aethereal.util;
import aethereal.event.ClientListener;
import aethereal.type.CombatPauseManager;
import aethereal.type.EventPriority;
import aethereal.Expensive;
import aethereal.type.Mc;
import aethereal.event.PlayerTickEvent;

public class CoreTickHandler implements ClientListener {
    public CoreTickHandler() {
        Expensive.INSTANCE.eventDispatcher().register(PlayerTickEvent.class, class130Var -> {
            if (Mc.INSTANCE.isWorldLoaded() && class130Var.isPre()) {
                StaffDetector.update();
                GrimDelayHandler.tick();
                PvPModeDetector.tick();
                ServerUtil.tick();
                CombatPauseManager.INSTANCE.update();
            }
        }, EventPriority.LOW);
    }
}
