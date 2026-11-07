package ru.expensive.mixin;

import aethereal.Expensive;
import aethereal.event.RenderOverlayEvent;
import aethereal.type.RenderOverlayType;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net.minecraft.client.gui.hud.InGameHud$HeartType"})
public class InGameHudHeartTypeMixin {
    @Inject(method = {"fromPlayerState"}, at = {@At("RETURN")}, cancellable = true)
    private static void onFromPlayerState(PlayerEntity playerEntity, CallbackInfoReturnable<Enum> callbackInfoReturnable) {
        if (((Enum) callbackInfoReturnable.getReturnValue()).name().equals("WITHERED")) {
            RenderOverlayEvent class252Var = new RenderOverlayEvent(RenderOverlayType.WITHER_HEARTS);
            Expensive.INSTANCE.eventDispatcher().dispatch(class252Var);
            if (class252Var.isCancelled()) {
                callbackInfoReturnable.setReturnValue(Enum.valueOf(((Enum) callbackInfoReturnable.getReturnValue()).getDeclaringClass(), "NORMAL"));
            }
        }
    }
}
