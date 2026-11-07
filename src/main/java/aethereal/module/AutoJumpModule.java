package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.ui.setting.BooleanSetting;
import aethereal.type.EventPriority;
import aethereal.Expensive;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.MovementInputEvent;

@Aliases(aliases = {"Auto Jump", "Auto Hop", "Jump Assist", "Jump Bot", "Automatic Jump", "Movement Jump"})
public class AutoJumpModule extends Module {
    final BooleanSetting onlyWithAura;

    public AutoJumpModule() {
        super(ModuleTab.MOVEMENT, "Auto Jump");
        this.onlyWithAura = new BooleanSetting(Lang.ARM_TWEAKS_SWING_ANIMATION_ONLY_AURA);
        addSettings(this.onlyWithAura);
        register(MovementInputEvent.class, class040Var -> {
            Mc class815Var = Mc.INSTANCE;
            if (isState() && class815Var.isWorldLoaded()) {
                if (class815Var.getPlayer().isOnGround() || (class815Var.getPlayer().isTouchingWater() && !class815Var.getPlayer().isSubmergedInWater())) {
                    AttackAuraModule class878Var = (AttackAuraModule) Expensive.INSTANCE.moduleRepository().get(AttackAuraModule.class);
                    if (!this.onlyWithAura.isValue() || (class878Var.isState() && class878Var.target() != null)) {
                        class040Var.setJumping(true);
                    }
                }
            }
        }, EventPriority.LOW);
    }
}
