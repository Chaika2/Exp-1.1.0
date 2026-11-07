package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.StopUsingItemEvent;

@Aliases(aliases = {"Multi Actions", "Multi Item Actions", "Multi-use Actions", "Multi Interactions", "Action Cancel", "Dual Actions", "Multiple Usages", "Concurrent Actions"})
public class MultiActionsModule extends Module {
    public MultiActionsModule() {
        super(ModuleTab.MISC, "Multi Actions");
        register(StopUsingItemEvent.class, class076Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded()) {
                class076Var.cancel();
            }
        });
    }
}
