package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.event.ClipAtLedgeEvent;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;

@Aliases(aliases = {"Safe Walk", "Walk Assist", "Eagle", "Movement Walk"})
public class SafeWalkModule extends Module {
    public SafeWalkModule() {
        super(ModuleTab.MOVEMENT, "Safe Walk");
        register(ClipAtLedgeEvent.class, class250Var -> {
            Mc class815Var = Mc.INSTANCE;
            if (isState() && class815Var.isWorldLoaded()) {
                class250Var.setClip(true);
            }
        });
    }
}
