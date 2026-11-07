package ru.expensive.mixin;

import aethereal.Expensive;
import aethereal.module.ShieldStatusModule;
import aethereal.util.ColorUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.item.model.special.ShieldModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({ShieldModelRenderer.class})
public class ShieldModelRendererMixin {

    @WrapOperation(method = {"render(Lnet/minecraft/component/ComponentMap;Lnet/minecraft/item/ModelTransformationMode;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IIZ)V"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", ordinal = 0)})
    private void expensive$shieldHandleTint(ModelPart modelPart, MatrixStack matrixStack, VertexConsumer vertexConsumer, int i, int j, Operation<Void> operation) {
        ShieldStatusModule shieldStatusModule = (ShieldStatusModule) Expensive.INSTANCE.moduleRepository().find(ShieldStatusModule.class).orElse(null);
        if (shieldStatusModule == null || !shieldStatusModule.isState()) {
            operation.call(new Object[]{modelPart, matrixStack, vertexConsumer, Integer.valueOf(i), Integer.valueOf(j)});
            return;
        }
        modelPart.render(matrixStack, vertexConsumer, i, expensive$tint(shieldStatusModule.currentShieldColor()), j);
    }

    @WrapOperation(method = {"render(Lnet/minecraft/component/ComponentMap;Lnet/minecraft/item/ModelTransformationMode;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IIZ)V"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", ordinal = 1)})
    private void expensive$shieldPlateTint(ModelPart modelPart, MatrixStack matrixStack, VertexConsumer vertexConsumer, int i, int j, Operation<Void> operation) {
        ShieldStatusModule shieldStatusModule = (ShieldStatusModule) Expensive.INSTANCE.moduleRepository().find(ShieldStatusModule.class).orElse(null);
        if (shieldStatusModule == null || !shieldStatusModule.isState()) {
            operation.call(new Object[]{modelPart, matrixStack, vertexConsumer, Integer.valueOf(i), Integer.valueOf(j)});
            return;
        }
        modelPart.render(matrixStack, vertexConsumer, i, expensive$tint(shieldStatusModule.currentShieldColor()), j);
    }

    private int expensive$tint(int i) {
        return ColorUtil.argb(255, ColorUtil.red(i), ColorUtil.green(i), ColorUtil.blue(i));
    }
}