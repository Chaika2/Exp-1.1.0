package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.event.EntityTraceEvent;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;

@Aliases(aliases = {"No Player Trace", "No Entity Trace", "Anti Trace", "No Trace"})
public class NoPlayerTraceModule extends Module {
    public NoPlayerTraceModule() {
        super(ModuleTab.COMBAT, "No Player Trace");
        register(EntityTraceEvent.class, class342Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded()) {
                class342Var.cancel();
            }
        });
    }
}
