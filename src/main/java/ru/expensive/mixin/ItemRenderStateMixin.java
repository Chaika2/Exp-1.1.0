package ru.expensive.mixin;

import aethereal.Expensive;
import aethereal.module.ItemPhysicsModule;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ModelTransformationMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.expensive.mixin.accessors.ItemRenderStateAccessor;

@Mixin({ItemRenderState.class})
public class ItemRenderStateMixin {

    @Inject(method = {"render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V"}, at = {@At("HEAD")})
    private void expensive$shrinkGroundItems(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, CallbackInfo callbackInfo) {
        ItemPhysicsModule itemPhysicsModule = (ItemPhysicsModule) Expensive.INSTANCE.moduleRepository().find(ItemPhysicsModule.class).orElse(null);
        if (itemPhysicsModule == null || !itemPhysicsModule.shrinkActive()) {
            return;
        }
        if (((ItemRenderStateAccessor) this).expensive$getModelTransformationMode() != ModelTransformationMode.GROUND) {
            return;
        }
        matrixStack.scale(0.5f, 0.5f, 0.5f);
    }
}