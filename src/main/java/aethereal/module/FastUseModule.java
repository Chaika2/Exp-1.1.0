package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.PlayerTickEvent;

@Aliases(aliases = {"Fast Use", "No Right Click Delay", "Fast Place", "No Delay", "Anti Delay", "Instant Use", "Fast Exp"})
public class FastUseModule extends Module {
    public FastUseModule() {
        super(ModuleTab.PLAYER, "Fast Use");
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && class130Var.isPre()) {
                Mc.INSTANCE.getMinecraft().itemUseCooldown = 0;
            }
        });
    }
}
