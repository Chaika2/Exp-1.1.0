package ru.expensive.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.InactivityFpsLimiter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({InactivityFpsLimiter.class})
public class InactivityFpsLimiterMixin {

    @Shadow
    private int field_52732;

    @ModifyReturnValue(method = {"update"}, at = {@At("RETURN")})
    public int update(int i) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        if (minecraftClient.world != null || (minecraftClient.currentScreen == null && minecraftClient.getOverlay() == null)) {
            return this.field_52732;
        }
        return 200;
    }
}
