package aethereal.model;

import java.util.Iterator;
import java.util.List;

public final class PotionEffectPreset {
    public static final PotionEffectPreset strengthPreset = new PotionEffectPreset(List.of(new PotionEffectSpec("minecraft:resistance", 0, 3600), new PotionEffectSpec("minecraft:strength", 3, 3600)));
    public static final PotionEffectPreset invisibilityPreset = new PotionEffectPreset(List.of(new PotionEffectSpec("minecraft:health_boost", 1, 3600), new PotionEffectSpec("minecraft:invisibility", 0, 18000), new PotionEffectSpec("minecraft:regeneration", 1, 1200), new PotionEffectSpec("minecraft:resistance", 0, 1200)));
    public static final PotionEffectPreset healthPreset = new PotionEffectPreset(List.of(new PotionEffectSpec("minecraft:health_boost", 2, 900), new PotionEffectSpec("minecraft:regeneration", 1, 400)));
    public final List<PotionEffectSpec> effects;

    public boolean contains(PotionEffectSpec class423Var) {
        Iterator<PotionEffectSpec> it = this.effects.iterator();
        while (it.hasNext()) {
            if (it.next().equals(class423Var)) {
                return true;
            }
        }
        return false;
    }

    public boolean equals(List<PotionEffectSpec> list) {
        if (this.effects.size() != list.size()) {
            return false;
        }
        Iterator<PotionEffectSpec> it = list.iterator();
        while (it.hasNext()) {
            if (!contains(it.next())) {
                return false;
            }
        }
        return true;
    }

    public PotionEffectPreset(List<PotionEffectSpec> list) {
        this.effects = list;
    }
}
