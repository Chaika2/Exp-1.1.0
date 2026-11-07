package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;

public enum AutoPotionType implements DisplayNamed {
    FIRE_RESISTANCE(Lang.AUTOPOTION_POTIONS_FIRERESISTANCE, StatusEffects.FIRE_RESISTANCE),
    STRENGTH(Lang.AUTOPOTION_POTIONS_STRENGTH, StatusEffects.STRENGTH),
    SPEED(Lang.AUTOPOTION_POTIONS_SPEED, StatusEffects.SPEED),
    INSTANT_HEALTH(Lang.AUTOPOTION_POTIONS_HEALTH, StatusEffects.INSTANT_HEALTH);

    public final Translation displayName;
    public final RegistryEntry<StatusEffect> effect;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public RegistryEntry<StatusEffect> getEffect() {
        return this.effect;
    }

    AutoPotionType(Translation class254Var, RegistryEntry registryEntry) {
        this.displayName = class254Var;
        this.effect = registryEntry;
    }
}
