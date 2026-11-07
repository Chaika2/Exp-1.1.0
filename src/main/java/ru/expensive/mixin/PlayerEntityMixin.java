package ru.expensive.mixin;

import aethereal.Expensive;
import aethereal.event.PlayerTravelEvent;
import aethereal.event.PushEvent;
import aethereal.type.PushType;
import aethereal.event.SprintStopEvent;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PlayerEntity.class})
public class PlayerEntityMixin {
    @Inject(method = {"isPushedByFluids"}, at = {@At("HEAD")}, cancellable = true)
    public void isPushedByFluids(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        PushEvent class231Var = new PushEvent(PushType.WATER);
        Expensive.INSTANCE.eventDispatcher().dispatch(class231Var);
        if ((((PlayerEntity) (Object) this) instanceof ClientPlayerEntity) && class231Var.isCancelled()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    @ModifyExpressionValue(method = {"tick"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;")})
    private ItemStack injectSilentHotbar(ItemStack itemStack) {
        PlayerEntity playerEntity = (PlayerEntity) (Object) this;
        return playerEntity instanceof ClientPlayerEntity ? (ItemStack) playerEntity.getInventory().main.get(Expensive.INSTANCE.inventoryService().hotbarSlotSwapper().getClientsideSlot()) : itemStack;
    }

    @Redirect(method = {"attack"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;multiply(DDD)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d hookSlowVelocity(Vec3d vec3d, double d, double d2, double d3) {
        if ((Object) this == MinecraftClient.getInstance().player) {
            SprintStopEvent class337Var = new SprintStopEvent();
            Expensive.INSTANCE.eventDispatcher().dispatch(class337Var);
            if (class337Var.isCancelled()) {
                d3 = 1.0d;
                d = 1.0d;
            }
        }
        return vec3d.multiply(d, d2, d3);
    }

    @Inject(method = {"travel"}, at = {@At("HEAD")})
    private void onTravel(Vec3d vec3d, CallbackInfo callbackInfo) {
        if (!(((PlayerEntity) (Object) this) instanceof ClientPlayerEntity)) {
            return;
        }
        PlayerTravelEvent playerTravelEvent = new PlayerTravelEvent(0.0f);
        Expensive.INSTANCE.eventDispatcher().dispatch(playerTravelEvent);
    }
}
