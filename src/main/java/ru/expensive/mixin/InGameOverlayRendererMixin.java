package ru.expensive.mixin;

import aethereal.Expensive;
import aethereal.event.RenderOverlayEvent;
import aethereal.type.RenderOverlayType;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({InGameOverlayRenderer.class})
public class InGameOverlayRendererMixin {
    @Inject(method = {"renderFireOverlay"}, at = {@At("HEAD")}, cancellable = true)
    private static void renderFireOverlay(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, CallbackInfo callbackInfo) {
        RenderOverlayEvent class252Var = new RenderOverlayEvent(RenderOverlayType.FIRE_OVERLAY);
        Expensive.INSTANCE.eventDispatcher().dispatch(class252Var);
        if (class252Var.isCancelled()) {
            callbackInfo.cancel();
        }
    }
}
