package aethereal.type;
import aethereal.util.ArgumentParser;
import aethereal.Lang;
import aethereal.model.TranslatedException;
import aethereal.model.Translation;

import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public class PlayerArgumentType implements ArgumentParser<AbstractClientPlayerEntity> {
    @Override
    public AbstractClientPlayerEntity parse(String str) throws TranslatedException {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        if (minecraftClient.world == null) {
            throw new TranslatedException(Lang.TYPE_WORLD_NOT_LOADED);
        }
        return (AbstractClientPlayerEntity) minecraftClient.world.getPlayers().stream().filter(abstractClientPlayerEntity -> {
            return abstractClientPlayerEntity.getName().getString().equalsIgnoreCase(str);
        }).findFirst().orElseThrow(() -> {
            return new TranslatedException(Translation.clearText(Lang.TYPE_PLAYER_NOT_FOUND.effective().replace("{input}", str)));
        });
    }

    @Override
    public List<String> getSuggestions(String str) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        if (minecraftClient.world == null) {
            return List.of();
        }
        String lowerCase = str.toLowerCase();
        return (List) minecraftClient.world.getPlayers().stream().map(abstractClientPlayerEntity -> {
            return abstractClientPlayerEntity.getName().getString();
        }).filter(str2 -> {
            return str2.toLowerCase().startsWith(lowerCase);
        }).collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return "player";
    }
}
