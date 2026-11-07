package aethereal.model;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class ConfigErrorNotice {
    public static final String discordUrl = "https://discord.gg/gPQ7yBY9zy";

    public ConfigErrorNotice() {
    }

    public static Text withDiscord() {
        return withDiscord("сохранении");
    }

    public static Text withDiscord(String str) {
        return Text.literal("").append(Text.literal("Ошибка конфигурации ").formatted(Formatting.RED)).append(Text.literal("[Исправить]").setStyle(Text.literal("[Исправить]").getStyle().withColor(Formatting.GREEN).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("").append(Text.literal("Произошла ошибка при %s конфигурации.\n".formatted(str)).formatted(Formatting.GRAY)).append(Text.literal("Пожалуйста, отправьте latest.log файл\n").formatted(Formatting.GRAY)).append(Text.literal("администрации в Discord.\n\n").formatted(Formatting.GRAY)).append(Text.literal("Нажмите, чтобы перейти на сервер.").formatted(Formatting.YELLOW)))).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, discordUrl))));
    }
}
