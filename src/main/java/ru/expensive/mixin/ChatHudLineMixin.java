package ru.expensive.mixin;

import aethereal.accessor.ChatLineIdAccessor;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({ChatHudLine.class})
public abstract class ChatHudLineMixin implements ChatLineIdAccessor {

    @Unique
    private String expensive_ru$id = null;

    @Override
    @Unique
    public void expensive_ru$setId(String str) {
        this.expensive_ru$id = str;
    }

    @Override
    @Unique
    public String expensive_ru$getId() {
        return this.expensive_ru$id;
    }
}
