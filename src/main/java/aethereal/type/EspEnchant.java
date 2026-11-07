package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public enum EspEnchant implements DisplayNamed {
    PROTECTION(Translation.clearText("Защита"), "protection", "P"),
    FIRE_PROTECTION(Translation.clearText("Огнеупорность"), "fire_protection", "F"),
    BLAST_PROTECTION(Translation.clearText("Взрывоустойчивость"), "blast_protection", "B"),
    PROJECTILE_PROTECTION(Translation.clearText("Защита от снарядов"), "projectile_protection", "P"),
    FEATHER_FALLING(Translation.clearText("Невесомость"), "feather_falling", "F"),
    THORNS(Translation.clearText("Шипы"), "thorns", "T"),
    RESPIRATION(Translation.clearText("Подводное дыхание"), "respiration", "R"),
    AQUA_AFFINITY(Translation.clearText("Подводник"), "aqua_affinity", "A"),
    DEPTH_STRIDER(Translation.clearText("Подводная ходьба"), "depth_strider", "D"),
    FROST_WALKER(Translation.clearText("Ледоход"), "frost_walker", "F"),
    SOUL_SPEED(Translation.clearText("Скорость души"), "soul_speed", "S"),
    SWIFT_SNEAK(Translation.clearText("Стремительная крадучесть"), "swift_sneak", "S"),
    SHARPNESS(Translation.clearText("Острота"), "sharpness", "s"),
    SMITE(Translation.clearText("Небесная кара"), "smite", "S"),
    BANE_OF_ARTHROPODS(Translation.clearText("Бич членистоногих"), "bane_of_arthropods", "B"),
    KNOCKBACK(Translation.clearText("Отдача"), "knockback", "K"),
    FIRE_ASPECT(Translation.clearText("Заговор огня"), "fire_aspect", "F"),
    LOOTING(Translation.clearText("Добыча"), "looting", "L"),
    SWEEPING_EDGE(Translation.clearText("Разящий клинок"), "sweeping_edge", "S"),
    EFFICIENCY(Translation.clearText("Эффективность"), "efficiency", "E"),
    SILK_TOUCH(Translation.clearText("Шёлковое касание"), "silk_touch", "S"),
    UNBREAKING(Translation.clearText("Прочность"), "unbreaking", "U"),
    FORTUNE(Translation.clearText("Удача"), "fortune", "F"),
    POWER(Translation.clearText("Сила"), "power", "P"),
    PUNCH(Translation.clearText("Откидывание"), "punch", "P"),
    FLAME(Translation.clearText("Воспламенение"), "flame", "F"),
    INFINITY(Translation.clearText("Бесконечность"), "infinity", "I"),
    LUCK_OF_THE_SEA(Translation.clearText("Везучий рыбак"), "luck_of_the_sea", "L"),
    LURE(Translation.clearText("Приманка"), "lure", "L"),
    LOYALTY(Translation.clearText("Верность"), "loyalty", "L"),
    IMPALING(Translation.clearText("Пронзатель"), "impaling", "I"),
    RIPTIDE(Translation.clearText("Тягун"), "riptide", "R"),
    CHANNELING(Translation.clearText("Громовержец"), "channeling", "C"),
    MULTISHOT(Translation.clearText("Тройной выстрел"), "multishot", "M"),
    PIERCING(Translation.clearText("Проникающая стрела"), "piercing", "P"),
    QUICK_CHARGE(Translation.clearText("Быстрая перезарядка"), "quick_charge", "Q"),
    MENDING(Translation.clearText("Починка"), "mending", "M"),
    DENSITY(Translation.clearText("Плотность"), "density", "D"),
    BREACH(Translation.clearText("Разрыв"), "breach", "B"),
    WIND_BURST(Translation.clearText("Порыв ветра"), "wind_burst", "W");

    public final Translation displayName;
    public final String id;
    public final String shortName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public int getLevel(ItemStack itemStack) {
        ItemEnchantmentsComponent enchantments = (ItemEnchantmentsComponent) itemStack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        if (enchantments.isEmpty()) {
            return 0;
        }
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : enchantments.getEnchantmentEntries()) {
            String entryId = entry.getKey().getKey().map(RegistryKey::getValue).map(Identifier::getPath).orElse("");
            if (entryId.equals(this.id)) {
                return entry.getIntValue();
            }
        }
        return 0;
    }

    public int getMaxLevel() {
        Mc mc = Mc.INSTANCE;
        if (mc == null || mc.getWorld() == null) {
            return 1;
        }
        Registry<Enchantment> registry = mc.getWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        Enchantment enchantment = registry.get(Identifier.of(this.id));
        return enchantment != null ? enchantment.getMaxLevel() : 1;
    }

    EspEnchant(Translation class254Var, String str, String str2) {
        this.displayName = class254Var;
        this.id = str;
        this.shortName = str2;
    }
}
