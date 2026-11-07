package ru.expensive.mixin;

import aethereal.Expensive;
import aethereal.event.VisualEffectEvent;
import aethereal.type.VisualEffectType;
import aethereal.event.GammaEvent;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({LightmapTextureManager.class})
public class LightMapTextureManagerMixin {
    @ModifyExpressionValue(method = {"update(F)V"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;", ordinal = 1)})
    private Object onBrightness(Object obj) {
        GammaEvent class336Var = new GammaEvent();
        Expensive.INSTANCE.eventDispatcher().dispatch(class336Var);
        return class336Var.isCancelled() ? Double.valueOf(class336Var.getGamma()) : obj;
    }

    @Redirect(method = {"getDarknessFactor"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Lnet/minecraft/entity/effect/StatusEffectInstance;"))
    private StatusEffectInstance injectAntiDarkness(ClientPlayerEntity clientPlayerEntity, RegistryEntry<StatusEffect> registryEntry) {
        VisualEffectEvent class258Var = new VisualEffectEvent(VisualEffectType.DARKNESS);
        Expensive.INSTANCE.eventDispatcher().dispatch(class258Var);
        if (class258Var.isCancelled()) {
            return null;
        }
        return clientPlayerEntity.getStatusEffect(registryEntry);
    }
}
