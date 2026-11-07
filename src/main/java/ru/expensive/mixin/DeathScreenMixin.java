package ru.expensive.mixin;

import aethereal.Expensive;
import aethereal.event.DeathTickEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DeathScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({DeathScreen.class})
public class DeathScreenMixin {

    @Shadow
    private int field_2451;

    @Inject(method = {"render"}, at = {@At("HEAD")})
    public void render(DrawContext drawContext, int i, int i2, float f, CallbackInfo callbackInfo) {
        Expensive.INSTANCE.eventDispatcher().dispatch(new DeathTickEvent(this.field_2451));
    }
}
