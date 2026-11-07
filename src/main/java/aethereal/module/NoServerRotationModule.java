package aethereal.module;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.math.Rotation;
import aethereal.event.ServerRotationEvent;

public class NoServerRotationModule extends Module {
    public NoServerRotationModule() {
        super(ModuleTab.PLAYER, "No Server Rotation");
        register(ServerRotationEvent.class, class079Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded()) {
                class079Var.cancel();
            }
        });
    }
}
