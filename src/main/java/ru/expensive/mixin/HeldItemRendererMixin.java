package ru.expensive.mixin;

import aethereal.render.GifDecoder;
import aethereal.Expensive;
import aethereal.event.EquipAnimationEvent;
import aethereal.event.EquipAnimationEvent2;
import aethereal.event.HeldItemRenderEvent;
import aethereal.event.ArmRenderEvent;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({HeldItemRenderer.class})
public class HeldItemRendererMixin {

    @Shadow
    @Final
    private MinecraftClient field_4050;

    @Shadow
    private void method_3217(MatrixStack matrixStack, Arm arm, float f) {
    }

    @ModifyConstant(method = {"applyEquipOffset"}, constant = {@Constant(floatValue = -0.6f)})
    private float modifyEquipOffsetConstant(float f) {
        EquipAnimationEvent class176Var = new EquipAnimationEvent();
        Expensive.INSTANCE.eventDispatcher().dispatch(class176Var);
        if (class176Var.isCancelled()) {
            return 0.0f;
        }
        return f;
    }

    @Redirect(method = {"swingArm"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", ordinal = 0))
    private void cancelSpecificTranslate(MatrixStack matrixStack, float f, float f2, float f3) {
        EquipAnimationEvent2 class287Var = new EquipAnimationEvent2();
        Expensive.INSTANCE.eventDispatcher().dispatch(class287Var);
        if (class287Var.isCancelled()) {
            return;
        }
        matrixStack.translate(f, f2, f3);
    }

    @Inject(method = {"renderFirstPersonItem"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER)})
    private void renderFirstPersonItemHook(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float f2, Hand hand, float f3, ItemStack itemStack, float f4, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo callbackInfo) {
        Expensive.INSTANCE.eventDispatcher().dispatch(new HeldItemRenderEvent(hand, matrixStack, itemStack));
    }

    @WrapOperation(method = {"renderFirstPersonItem"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;swingArm(FFLnet/minecraft/client/util/math/MatrixStack;ILnet/minecraft/util/Arm;)V", ordinal = GifDecoder.STATUS_OPEN_ERROR)})
    private void handAnimationHook(HeldItemRenderer heldItemRenderer, float f, float f2, MatrixStack matrixStack, int i, Arm arm, Operation<Void> operation, @Local(ordinal = 0, argsOnly = true) AbstractClientPlayerEntity abstractClientPlayerEntity, @Local(ordinal = 0, argsOnly = true) Hand hand) {
        ArmRenderEvent class360Var = new ArmRenderEvent(arm, matrixStack, f, f2);
        Expensive.INSTANCE.eventDispatcher().dispatch(class360Var);
        if (!class360Var.isCancelled() || arm == Arm.LEFT) {
            operation.call(new Object[]{heldItemRenderer, Float.valueOf(f), Float.valueOf(f2), matrixStack, Integer.valueOf(i), arm});
        }
    }

    @ModifyExpressionValue(method = {"updateHeldItems"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;")})
    private ItemStack injectSilentHotbar(ItemStack itemStack) {
        return this.field_4050.player != null ? (ItemStack) (Object) this.field_4050.player.getInventory().main.get(Expensive.INSTANCE.inventoryService().hotbarSlotSwapper().getClientsideSlot()) : itemStack;
    }
}
