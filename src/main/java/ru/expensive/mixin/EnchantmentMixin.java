package ru.expensive.mixin;

import aethereal.util.EnchantmentLocalization;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({Enchantment.class})
public class EnchantmentMixin {

    @ModifyReturnValue(method = {"getName(Lnet/minecraft/registry/entry/RegistryEntry;I)Lnet/minecraft/text/Text;"}, at = {@At("RETURN")})
    private static Text expensive_ru$localizeName(Text original, RegistryEntry<Enchantment> enchantment, int level) {
        String path = enchantment.getKey().map(RegistryKey::getValue).map(Identifier::getPath).orElse("");
        String russian = EnchantmentLocalization.russianName(path);
        if (russian == null) {
            return original;
        }
        Enchantment value = enchantment.value();
        boolean overLevel = level > value.getMaxLevel();
        boolean curse = enchantment.isIn(EnchantmentTags.CURSE);

        MutableText text = Text.literal(russian);
        if (overLevel) {
            // Необычный (переуровневый) зачар - подсвечиваем красным и жирным.
            text.setStyle(Style.EMPTY.withColor(Formatting.RED).withBold(true));
        } else if (curse) {
            text.setStyle(Style.EMPTY.withColor(Formatting.RED));
        } else {
            text.setStyle(Style.EMPTY.withColor(Formatting.GRAY));
        }

        if (level != 1 || value.getMaxLevel() != 1) {
            text.append(Text.literal(" "));
            text.append(Text.literal(EnchantmentLocalization.roman(level)));
        }
        return text;
    }
}
