package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.type.RemovedEffectType;
import aethereal.event.StatusEffectEvent;
import aethereal.event.VisualEffectEvent;
import aethereal.type.VisualEffectType;

import net.minecraft.entity.effect.StatusEffects;

@Aliases(aliases = {"No Bad Effects", "No Effects", "Remove Effects", "Remove Bad Effects", "Anti Bad Effects"})
public class RemoveEffectsModule extends Module {
    public final MultiSelectSetting<RemovedEffectType> removedEffects;

    public RemoveEffectsModule() {
        super(ModuleTab.PLAYER, "Remove Effects");
        this.removedEffects = new MultiSelectSetting(Lang.REMOVEEFFECTS_POTIONS, Lang.REMOVEEFFECTS_POTIONS_DESC).values(RemovedEffectType.class);
        addSettings(this.removedEffects);
        register(VisualEffectEvent.class, class258Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded()) {
                VisualEffectType type = class258Var.getType();
                if ((type.isBlindness() && this.removedEffects.isSelected(RemovedEffectType.BLINDNESS)) || ((type.isDarkness() && this.removedEffects.isSelected(RemovedEffectType.DARKNESS)) || (type.isNausea() && this.removedEffects.isSelected(RemovedEffectType.NAUSEA)))) {
                    class258Var.cancel();
                }
            }
        });
        register(StatusEffectEvent.class, class046Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded() && class046Var.getEffect() == StatusEffects.JUMP_BOOST && this.removedEffects.isSelected(RemovedEffectType.JUMP_BOOST)) {
                class046Var.cancel();
            }
        });
    }
}
