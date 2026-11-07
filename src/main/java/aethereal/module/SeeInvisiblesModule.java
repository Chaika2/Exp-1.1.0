package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.type.EntityCategory;
import aethereal.model.EntityFilter;
import aethereal.event.EntityInvisibilityEvent;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.type.TargetSelectionType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;

@Aliases(aliases = {"Reveal Invisibles", "Invisible Detection", "See Hidden", "Invisible Vision", "Detect Invisibles", "Anti-Invisibility", "See Stealth", "See Invisibles", "Reveal Stealth Targets"})
public class SeeInvisiblesModule extends Module {
    MultiSelectSetting<TargetSelectionType> targetsSetting;

    public SeeInvisiblesModule() {
        super(ModuleTab.MISC, "See Invisibles");
        this.targetsSetting = new MultiSelectSetting(Lang.SELECTTARGETS).values(TargetSelectionType.class);
        addSettings(this.targetsSetting);
        register(EntityInvisibilityEvent.class, class198Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded()) {
                if ((class198Var.entity() instanceof LivingEntity livingEntityEntity) && livingEntityEntity.hasStatusEffect(StatusEffects.GLOWING)) {
                    return;
                }
                EntityFilter class095Var = new EntityFilter();
                if (this.targetsSetting.isSelected(TargetSelectionType.SELF)) {
                    class095Var.add(EntityCategory.SELF);
                }
                if (this.targetsSetting.isSelected(TargetSelectionType.PLAYERS)) {
                    class095Var.add(EntityCategory.PLAYER);
                }
                if (this.targetsSetting.isSelected(TargetSelectionType.FRIENDS)) {
                    class095Var.add(EntityCategory.FRIEND);
                }
                if (this.targetsSetting.isSelected(TargetSelectionType.MOBS)) {
                    class095Var.add(EntityCategory.MOB);
                }
                if (this.targetsSetting.isSelected(TargetSelectionType.ANIMALS)) {
                    class095Var.add(EntityCategory.ANIMAL);
                }
                if (class095Var.matches(class198Var.entity())) {
                    class198Var.cancel();
                }
            }
        });
    }
}
