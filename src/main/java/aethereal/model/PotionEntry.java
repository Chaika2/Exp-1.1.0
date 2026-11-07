package aethereal.model;
import aethereal.render.ToggleAnimator;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;

public final class PotionEntry {
    public RegistryEntry<StatusEffect> effect;
    public int amplifier;
    public int duration;
    public int maxDuration;
    public int lastUpdateTick;
    public boolean infinite;
    public boolean hasBeenActive;
    public boolean expiryNotified;
    public boolean harmful;
    public final ToggleAnimator animator = ToggleAnimator.times(2, 80);
    public String translationKey = "";
    public String durationText = "";
    public int previousDuration = Integer.MAX_VALUE;

    public PotionEntry() {
    }

    public String getDisplayName() {
        String strTranslate = I18n.translate(this.translationKey, new Object[0]);
        return this.amplifier > 0 ? strTranslate + " " + (this.amplifier + 1) : strTranslate;
    }
}
