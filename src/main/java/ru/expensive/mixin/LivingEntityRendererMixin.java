package ru.expensive.mixin;

import aethereal.Expensive;
import aethereal.util.ColorUtil;
import aethereal.util.RotationManager;
import aethereal.module.ChamsModule;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LivingEntityRenderer.class})
public abstract class LivingEntityRendererMixin<S extends LivingEntityRenderState> {

    @Unique
    BufferBuilder currentBuilder;

    @ModifyExpressionValue(method = {"updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F")})
    private float lerpAngleDegreesHook(float f, @Local(ordinal = 0, argsOnly = true) LivingEntity livingEntity, @Local(ordinal = 0, argsOnly = true) float f2) {
        if (!livingEntity.equals(MinecraftClient.getInstance().player) || MinecraftClient.getInstance().currentScreen != null) {
            return f;
        }
        RotationManager class398Var = RotationManager.INSTANCE;
        return MathHelper.lerpAngleDegrees(f2, class398Var.getPreviousRotation().getYaw(), class398Var.getCurrentRotation().getYaw());
    }

    @ModifyExpressionValue(method = {"updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getLerpedPitch(F)F")})
    private float getLerpedPitchHook(float f, @Local(ordinal = 0, argsOnly = true) LivingEntity livingEntity, @Local(ordinal = 0, argsOnly = true) float f2) {
        if (!livingEntity.equals(MinecraftClient.getInstance().player) || MinecraftClient.getInstance().currentScreen != null) {
            return f;
        }
        RotationManager class398Var = RotationManager.INSTANCE;
        return MathHelper.lerp(f2, class398Var.getPreviousRotation().getPitch(), class398Var.getCurrentRotation().getPitch());
    }

    @Redirect(method = {"render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;"))
    public VertexConsumer redirectGetBuffer(VertexConsumerProvider vertexConsumerProvider, RenderLayer renderLayer) {
        ChamsModule class540Var = (ChamsModule) Expensive.INSTANCE.moduleRepository().get(ChamsModule.class);
        if (!class540Var.isState() || !class540Var.shouldRender(class540Var.currentEntity())) {
            return vertexConsumerProvider.getBuffer(renderLayer);
        }
        int color = class540Var.colorSetting().getColor();
        boolean zIsValue = class540Var.blendingSetting().isValue();
        RenderSystem.enableBlend();
        if (zIsValue) {
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        } else {
            RenderSystem.defaultBlendFunc();
        }
        RenderSystem.setShader(ShaderProgramKeys.POSITION);
        RenderSystem.setShaderColor(ColorUtil.red(color) / 255.0f, ColorUtil.green(color) / 255.0f, ColorUtil.blue(color) / 255.0f, ColorUtil.alpha(color) / 255.0f);
        BufferBuilder bufferBuilderBegin = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        this.currentBuilder = bufferBuilderBegin;
        return bufferBuilderBegin;
    }

    @Inject(method = {"render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V", shift = At.Shift.AFTER)})
    private void postRender(S s, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo callbackInfo) {
        if (this.currentBuilder != null) {
            BufferRenderer.drawWithGlobalProgram(this.currentBuilder.end());
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
            this.currentBuilder = null;
        }
    }
}
