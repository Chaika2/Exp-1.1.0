package ru.expensive.mixin;

import aethereal.event.WorldRenderEvent;
import aethereal.Expensive;
import aethereal.event.EntityRenderEvent2;
import aethereal.module.ChamsModule;
import aethereal.module.ShaderSkyModule;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderPass;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({WorldRenderer.class})
public class WorldRendererMixin {

    @Shadow
    public Frustum field_27740;

    @Shadow
    public DefaultFramebufferSet framebufferSet;

    @Inject(method = {"renderSky(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/render/Fog;)V"}, at = {@At("HEAD")}, cancellable = true)
    private void onRenderSky(FrameGraphBuilder frameGraphBuilder, Camera camera, float tickDelta, Fog fog, CallbackInfo ci) {
        ShaderSkyModule shaderSkyModule = (ShaderSkyModule) Expensive.INSTANCE.moduleRepository().find(ShaderSkyModule.class).orElse(null);
        if (shaderSkyModule == null || !shaderSkyModule.shouldReplaceSky()) {
            return;
        }
        Framebuffer framebuffer = (Framebuffer) this.framebufferSet.mainFramebuffer.get();
        RenderPass renderPass = frameGraphBuilder.createPass("expensive_sky");
        this.framebufferSet.mainFramebuffer = renderPass.transfer(this.framebufferSet.mainFramebuffer);
        renderPass.setRenderer(() -> shaderSkyModule.drawSky(framebuffer, tickDelta));
        ci.cancel();
    }

    @Redirect(method = {"getEntitiesToRender"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSleeping()Z"))
    private boolean hookRenderEntityFromAllPerspectives(LivingEntity livingEntity) {
        EntityRenderEvent2 class377Var = new EntityRenderEvent2(livingEntity);
        Expensive.INSTANCE.eventDispatcher().dispatch(class377Var);
        if (class377Var.isCancelled()) {
            return true;
        }
        return livingEntity.isSleeping();
    }

    @Inject(method = {"renderEntity(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V"}, at = {@At("HEAD")})
    private void onRenderEntity(Entity entity, double d, double d2, double d3, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, CallbackInfo callbackInfo) {
        ((ChamsModule) Expensive.INSTANCE.moduleRepository().get(ChamsModule.class)).currentEntity(entity);
    }

    @Inject(method = {"render"}, at = {@At("TAIL")}, require = 0)
    private void onRender(ObjectAllocator objectAllocator, RenderTickCounter renderTickCounter, boolean z, Camera camera, GameRenderer gameRenderer, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo callbackInfo) {
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiplyPositionMatrix(matrix4f);
        Expensive.INSTANCE.eventDispatcher().dispatch(new WorldRenderEvent(matrixStack, matrix4f, matrix4f2, renderTickCounter, this.field_27740));
    }
}
