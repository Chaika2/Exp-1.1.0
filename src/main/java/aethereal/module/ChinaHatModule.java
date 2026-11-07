package aethereal.module;
import aethereal.event.EntityModelRenderEvent;
import aethereal.Expensive;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class ChinaHatModule extends Module {
    public final Mc mc;

    public ChinaHatModule() {
        super(ModuleTab.RENDER, "China Hat");
        this.mc = Mc.INSTANCE;
        register(EntityModelRenderEvent.class, class053Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                PlayerEntity player = this.mc.getPlayer();
                if ((class053Var.model()) instanceof ModelWithHead modelWithHeadModel ) {
                    ModelWithHead modelWithHead = modelWithHeadModel;
                    if ((class053Var.livingEntity() instanceof PlayerEntity playerEntity) && playerEntity == player) {
                        int iComputeColor = Expensive.INSTANCE.drawEngine().colorStack().computeColor(128, 51, 204);
                        MatrixStack matrixStack = class053Var.matrixStack();
                        float width = playerEntity.getWidth();
                        float f = player.getEquippedStack(EquipmentSlot.HEAD).isEmpty() ? 0.38f : 0.5f;
                        Tessellator tessellator = Tessellator.getInstance();
                        matrixStack.push();
                        modelWithHead.getHead().rotate(matrixStack);
                        matrixStack.translate(0.0f, -f, 0.0f);
                        matrixStack.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(180.0f));
                        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0f));
                        Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
                        RenderSystem.enableBlend();
                        RenderSystem.enableDepthTest();
                        RenderSystem.disableCull();
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
                        BufferBuilder bufferBuilderBegin = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
                        bufferBuilderBegin.vertex(positionMatrix, 0.0f, 0.3f, 0.0f).color(iComputeColor);
                        for (int i = 0; i <= 60; i++) {
                            bufferBuilderBegin.vertex(positionMatrix, (float) ((-Math.sin(((((double) i) * 3.141592653589793d) * 2.0d) / ((double) 60))) * ((double) width)), 0.0f, (float) (Math.cos(((((double) i) * 3.141592653589793d) * 2.0d) / ((double) 60)) * ((double) width))).color(iComputeColor);
                        }
                        BufferRenderer.drawWithGlobalProgram(bufferBuilderBegin.end());
                        RenderSystem.enableCull();
                        RenderSystem.disableDepthTest();
                        RenderSystem.disableBlend();
                        matrixStack.pop();
                    }
                }
            }
        });
    }
}
