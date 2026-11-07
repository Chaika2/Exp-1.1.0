package ru.expensive.mixin;

import aethereal.Expensive;
import aethereal.module.ItemPhysicsModule;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemEntityRenderer.class})
public class ItemEntityRendererMixin {

    @Unique
    private float expensive$physicsX;

    @Unique
    private boolean expensive$physicsActive;

    @Inject(method = {"updateRenderState(Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;F)V"}, at = {@At("TAIL")})
    private void expensive$capturePhysicsState(ItemEntity itemEntity, ItemEntityRenderState itemEntityRenderState, float f, CallbackInfo callbackInfo) {
        ItemPhysicsModule itemPhysicsModule = (ItemPhysicsModule) Expensive.INSTANCE.moduleRepository().find(ItemPhysicsModule.class).orElse(null);
        this.expensive$physicsActive = itemPhysicsModule != null && itemPhysicsModule.physicsActive();
        if (!this.expensive$physicsActive) {
            this.expensive$physicsX = 0.0f;
            return;
        }
        this.expensive$physicsX = itemEntity.isOnGround()
                ? 90.0f
                : ItemEntity.getRotation(itemEntityRenderState.age, itemEntityRenderState.uniqueOffset) * 300.0f;
    }

    @WrapOperation(method = {"render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", ordinal = 0)})
    private void expensive$skipBobbing(MatrixStack matrixStack, float f, float f2, float f3, Operation<Void> operation) {
        if (this.expensive$physicsActive) {
            return;
        }
        operation.call(matrixStack, f, f2, f3);
    }

    @WrapOperation(method = {"render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionf;)V")})
    private void expensive$applyPhysicsRotation(MatrixStack matrixStack, Quaternionf quaternionf, Operation<Void> operation) {
        if (this.expensive$physicsActive) {
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(this.expensive$physicsX));
            return;
        }
        operation.call(matrixStack, quaternionf);
    }
}