package aethereal.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class AttributeModifierPreset {
    public static final AttributeModifierPreset healthOffhandPreset = new AttributeModifierPreset(Collections.emptyList(), Collections.emptyList(), List.of(new AttributeModifierSpec("minecraft:generic.max_health", 2.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.armor", 2.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.attack_damage", 2.0d, "offhand")));
    public static final AttributeModifierPreset berserkOffhandPreset = new AttributeModifierPreset(Collections.emptyList(), Collections.emptyList(), List.of(new AttributeModifierSpec("minecraft:generic.max_health", -4.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.movement_speed", 0.1d, "offhand"), new AttributeModifierSpec("minecraft:generic.attack_damage", 7.0d, "offhand")));
    public static final AttributeModifierPreset damageArmorPreset = new AttributeModifierPreset(List.of(new AttributeModifierSpec("minecraft:generic.max_health", -4.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.attack_damage", 4.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.armor", -2.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.armor_toughness", -2.0d, "offhand")), List.of(new AttributeModifierSpec("minecraft:generic.max_health", -4.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.attack_damage", 5.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.armor", -2.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.armor_toughness", -2.0d, "offhand")), List.of(new AttributeModifierSpec("minecraft:generic.max_health", -4.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.attack_damage", 6.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.armor", -2.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.armor_toughness", -2.0d, "offhand")));
    public static final AttributeModifierPreset attackDamagePreset = new AttributeModifierPreset(List.of(new AttributeModifierSpec("minecraft:generic.max_health", -4.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.attack_damage", 3.0d, "offhand")), List.of(new AttributeModifierSpec("minecraft:generic.max_health", -4.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.attack_damage", 4.0d, "offhand")), List.of(new AttributeModifierSpec("minecraft:generic.max_health", -4.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.attack_damage", 5.0d, "offhand")));
    public static final AttributeModifierPreset balancedOffhandPreset = new AttributeModifierPreset(List.of(new AttributeModifierSpec("minecraft:generic.armor", 1.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.max_health", -4.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.attack_damage", 2.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.movement_speed", 0.1d, "offhand")), List.of(new AttributeModifierSpec("minecraft:generic.armor", 1.5d, "offhand"), new AttributeModifierSpec("minecraft:generic.max_health", -4.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.attack_damage", 2.5d, "offhand"), new AttributeModifierSpec("minecraft:generic.movement_speed", 0.1d, "offhand")), List.of(new AttributeModifierSpec("minecraft:generic.armor", 2.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.max_health", -4.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.attack_damage", 3.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.movement_speed", 0.15d, "offhand")));
    public static final AttributeModifierPreset attackSpeedPreset = new AttributeModifierPreset(List.of(new AttributeModifierSpec("minecraft:generic.max_health", -2.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.attack_damage", 1.0d, "offhand")), List.of(new AttributeModifierSpec("minecraft:generic.max_health", -2.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.attack_damage", 2.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.attack_speed", 0.1d, "offhand")), List.of(new AttributeModifierSpec("minecraft:generic.max_health", -2.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.attack_damage", 3.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.attack_speed", 0.15d, "offhand")));
    public static final AttributeModifierPreset agilityPreset = new AttributeModifierPreset(List.of(new AttributeModifierSpec("minecraft:generic.armor", -0.1d, "offhand"), new AttributeModifierSpec("minecraft:generic.attack_damage", 0.15d, "offhand"), new AttributeModifierSpec("minecraft:generic.movement_speed", 0.1d, "offhand")), List.of(new AttributeModifierSpec("minecraft:generic.armor", -0.1d, "offhand"), new AttributeModifierSpec("minecraft:generic.attack_damage", 0.2d, "offhand"), new AttributeModifierSpec("minecraft:generic.movement_speed", 0.1d, "offhand")), List.of(new AttributeModifierSpec("minecraft:generic.armor", -0.1d, "offhand"), new AttributeModifierSpec("minecraft:generic.attack_damage", 0.25d, "offhand"), new AttributeModifierSpec("minecraft:generic.movement_speed", 0.1d, "offhand")));
    public static final AttributeModifierPreset toughnessPreset = new AttributeModifierPreset(List.of(new AttributeModifierSpec("minecraft:generic.armor_toughness", 1.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.armor", 1.0d, "offhand")), List.of(new AttributeModifierSpec("minecraft:generic.armor_toughness", 1.5d, "offhand"), new AttributeModifierSpec("minecraft:generic.armor", 1.5d, "offhand"), new AttributeModifierSpec("minecraft:generic.movement_speed", -0.1d, "offhand")), List.of(new AttributeModifierSpec("minecraft:generic.armor_toughness", 2.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.armor", 2.0d, "offhand"), new AttributeModifierSpec("minecraft:generic.movement_speed", -0.15d, "offhand")));
    public final List<AttributeModifierSpec> tier1Modifiers;
    public final List<AttributeModifierSpec> tier2Modifiers;
    public final List<AttributeModifierSpec> tier3Modifiers;

    public static boolean containsSpec(List<AttributeModifierSpec> list, AttributeModifierSpec class421Var) {
        Iterator<AttributeModifierSpec> it = list.iterator();
        while (it.hasNext()) {
            if (it.next().equals(class421Var)) {
                return true;
            }
        }
        return false;
    }

    public static boolean modifiersMatch(List<AttributeModifierSpec> list, List<AttributeModifierSpec> list2) {
        if (list.size() != list2.size()) {
            return false;
        }
        Iterator<AttributeModifierSpec> it = list.iterator();
        while (it.hasNext()) {
            if (!containsSpec(list2, it.next())) {
                return false;
            }
        }
        return true;
    }

    public int level(List<AttributeModifierSpec> list) {
        if (!this.tier1Modifiers.isEmpty() && modifiersMatch(list, this.tier1Modifiers)) {
            return 1;
        }
        if (this.tier2Modifiers.isEmpty() || !modifiersMatch(list, this.tier2Modifiers)) {
            return (this.tier3Modifiers.isEmpty() || !modifiersMatch(list, this.tier3Modifiers)) ? -1 : 3;
        }
        return 2;
    }

    public AttributeModifierPreset(List<AttributeModifierSpec> list, List<AttributeModifierSpec> list2, List<AttributeModifierSpec> list3) {
        this.tier1Modifiers = list;
        this.tier2Modifiers = list2;
        this.tier3Modifiers = list3;
    }
}
