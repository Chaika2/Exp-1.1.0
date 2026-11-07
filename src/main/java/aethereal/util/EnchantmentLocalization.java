package aethereal.util;

import java.util.Map;

/**
 * Russian display names for vanilla enchantments, keyed by the enchantment registry path
 * (e.g. "protection"). Used to localize item tooltips independently of the client language,
 * and to decide when an enchantment level exceeds its vanilla maximum (shown in red).
 */
public final class EnchantmentLocalization {
    private static final String[] ROMAN = {
            "", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X",
            "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX"
    };

    private static final Map<String, String> RUSSIAN = Map.ofEntries(
            Map.entry("protection", "Защита"),
            Map.entry("fire_protection", "Огнеупорность"),
            Map.entry("feather_falling", "Невесомость"),
            Map.entry("blast_protection", "Взрывоустойчивость"),
            Map.entry("projectile_protection", "Защита от снарядов"),
            Map.entry("respiration", "Подводное дыхание"),
            Map.entry("aqua_affinity", "Подводник"),
            Map.entry("depth_strider", "Подводная ходьба"),
            Map.entry("frost_walker", "Ледоход"),
            Map.entry("soul_speed", "Скорость души"),
            Map.entry("swift_sneak", "Стремительная крадучесть"),
            Map.entry("efficiency", "Эффективность"),
            Map.entry("silk_touch", "Шёлковое касание"),
            Map.entry("unbreaking", "Прочность"),
            Map.entry("looting", "Добыча"),
            Map.entry("fortune", "Удача"),
            Map.entry("luck_of_the_sea", "Везучий рыбак"),
            Map.entry("lure", "Приманка"),
            Map.entry("power", "Сила"),
            Map.entry("flame", "Воспламенение"),
            Map.entry("punch", "Откидывание"),
            Map.entry("infinity", "Бесконечность"),
            Map.entry("thorns", "Шипы"),
            Map.entry("mending", "Починка"),
            Map.entry("binding_curse", "Проклятие несъёмности"),
            Map.entry("vanishing_curse", "Проклятие утраты"),
            Map.entry("loyalty", "Верность"),
            Map.entry("impaling", "Пронзатель"),
            Map.entry("riptide", "Тягун"),
            Map.entry("channeling", "Громовержец"),
            Map.entry("multishot", "Тройной выстрел"),
            Map.entry("quick_charge", "Быстрая перезарядка"),
            Map.entry("piercing", "Проникающая стрела"),
            Map.entry("sharpness", "Острота"),
            Map.entry("smite", "Небесная кара"),
            Map.entry("bane_of_arthropods", "Бич членистоногих"),
            Map.entry("knockback", "Отдача"),
            Map.entry("fire_aspect", "Заговор огня"),
            Map.entry("sweeping", "Разящий клинок"),
            Map.entry("sweeping_edge", "Разящий клинок"),
            Map.entry("density", "Плотность"),
            Map.entry("breach", "Разрыв"),
            Map.entry("wind_burst", "Порыв ветра"));

    private EnchantmentLocalization() {
    }

    public static String russianName(String path) {
        return RUSSIAN.get(path);
    }

    public static String roman(int level) {
        if (level > 0 && level < ROMAN.length) {
            return ROMAN[level];
        }
        return Integer.toString(level);
    }
}
