package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.ColorSetting;
import aethereal.Lang;
import aethereal.util.MathUtil;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.WorldRenderEvent;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

@Aliases(aliases = {"Skeleton", "Skelet ESP", "Skeleton ESP", "ESP"})
public class SkeletonModule extends Module {
    public final ColorSetting colorSetting;
    public final BooleanSetting blendingSetting;
    public final Quaternionf quaternion;

    public SkeletonModule() {
        super(ModuleTab.RENDER, "Skeleton");
        this.colorSetting = new ColorSetting(Lang.SKELETON_COLOR);
        this.blendingSetting = new BooleanSetting(Lang.SKELETON_BLENDING).setValue(true);
        this.quaternion = new Quaternionf();
        addSettings(this.colorSetting, this.blendingSetting);
        register(WorldRenderEvent.class, class016Var -> {
            if (Mc.INSTANCE.isWorldLoaded() && isState()) {
                Mc class815Var = Mc.INSTANCE;
                ClientWorld world = class815Var.getWorld();
                AbstractClientPlayerEntity player = class815Var.getPlayer();
                MatrixStack matrixStack = class016Var.matrixStack();
                Tessellator tessellatorRenderThreadTesselator = RenderSystem.renderThreadTesselator();
                float tickDelta = class815Var.getTickDelta();
                int color = this.colorSetting.getColor();
                RenderSystem.enableBlend();
                if (this.blendingSetting.isValue()) {
                    RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
                } else {
                    RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
                }
                RenderSystem.disableCull();
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(MinecraftClient.isFancyGraphicsOrBetter());
                RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
                RenderSystem.lineWidth(2.0f);
                GL11.glEnable(2848);
                GL11.glEnable(2881);
                GL11.glHint(3154, 4354);
                GL11.glHint(3155, 4354);
                for (Entity abstractClientPlayerEntity : world.getEntities()) {
                    if (class016Var.frustum().isVisible(abstractClientPlayerEntity.getBoundingBox()) && (abstractClientPlayerEntity instanceof AbstractClientPlayerEntity)) {
                        AbstractClientPlayerEntity abstractClientPlayerEntity2 = (AbstractClientPlayerEntity) abstractClientPlayerEntity;
                        if (class815Var.getGameOptions().getPerspective() != Perspective.FIRST_PERSON || abstractClientPlayerEntity != player) {
                            matrixStack.push();
                            Vec3d entityRenderPosition = MathUtil.getEntityRenderPosition(abstractClientPlayerEntity2, tickDelta);
                            PlayerEntityRenderer renderer = (PlayerEntityRenderer) class815Var.getEntityRenderDispatcher().getRenderer(abstractClientPlayerEntity2);
                            PlayerEntityRenderState playerEntityRenderStateCreateRenderState = renderer.createRenderState();
                            renderer.updateRenderState(abstractClientPlayerEntity2, playerEntityRenderStateCreateRenderState, tickDelta);
                            PlayerEntityModel model = renderer.getModel();
                            float fLerp = MathHelper.lerp(tickDelta, abstractClientPlayerEntity2.prevBodyYaw, abstractClientPlayerEntity2.bodyYaw);
                            float pitch = abstractClientPlayerEntity2.getPitch(tickDelta);
                            model.setAngles(playerEntityRenderStateCreateRenderState);
                            boolean zIsInSwimmingPose = abstractClientPlayerEntity2.isInSwimmingPose();
                            boolean zIsGliding = abstractClientPlayerEntity2.isGliding();
                            boolean z = abstractClientPlayerEntity2.isSneaking() && !abstractClientPlayerEntity2.getAbilities().flying;
                            ModelPart modelPart = model.head;
                            ModelPart modelPart2 = model.leftArm;
                            ModelPart modelPart3 = model.rightArm;
                            ModelPart modelPart4 = model.leftLeg;
                            ModelPart modelPart5 = model.rightLeg;
                            matrixStack.translate(entityRenderPosition.x, entityRenderPosition.y, entityRenderPosition.z);
                            if (zIsInSwimmingPose) {
                                matrixStack.translate(0.0f, 0.35f, 0.0f);
                            }
                            matrixStack.multiply(this.quaternion.setAngleAxis((((double) (fLerp + 180.0f)) * 3.141592653589793d) / 180.0d, 0.0d, -1.0d, 0.0d));
                            if (zIsInSwimmingPose || zIsGliding) {
                                matrixStack.multiply(this.quaternion.setAngleAxis((((double) (90.0f + pitch)) * 3.141592653589793d) / 180.0d, -1.0d, 0.0d, 0.0d));
                            }
                            if (zIsInSwimmingPose) {
                                matrixStack.translate(0.0f, -0.95f, 0.0f);
                            }
                            Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
                            BufferBuilder bufferBuilderBegin = tessellatorRenderThreadTesselator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
                            drawBoneLine(bufferBuilderBegin, positionMatrix, 0.0f, z ? 0.6f : 0.7f, z ? 0.23f : 0.0f, 0.0f, z ? 1.05f : 1.4f, 0.0f, color);
                            drawBoneLine(bufferBuilderBegin, positionMatrix, -0.37f, z ? 1.05f : 1.35f, 0.0f, 0.37f, z ? 1.05f : 1.35f, 0.0f, color);
                            drawBoneLine(bufferBuilderBegin, positionMatrix, -0.15f, z ? 0.6f : 0.7f, z ? 0.23f : 0.0f, 0.15f, z ? 0.6f : 0.7f, z ? 0.23f : 0.0f, color);
                            matrixStack.push();
                            matrixStack.translate(0.0f, z ? 1.05f : 1.4f, 0.0f);
                            applyPartRotation(matrixStack, modelPart);
                            drawBoneLine(bufferBuilderBegin, matrixStack.peek().getPositionMatrix(), 0.0f, 0.0f, 0.0f, 0.0f, 0.15f, 0.0f, color);
                            matrixStack.pop();
                            matrixStack.push();
                            matrixStack.translate(0.15f, z ? 0.6f : 0.7f, z ? 0.23f : 0.0f);
                            applyPartRotation(matrixStack, modelPart5);
                            drawBoneLine(bufferBuilderBegin, matrixStack.peek().getPositionMatrix(), 0.0f, 0.0f, 0.0f, 0.0f, -0.6f, 0.0f, color);
                            matrixStack.pop();
                            matrixStack.push();
                            matrixStack.translate(-0.15f, z ? 0.6f : 0.7f, z ? 0.23f : 0.0f);
                            applyPartRotation(matrixStack, modelPart4);
                            drawBoneLine(bufferBuilderBegin, matrixStack.peek().getPositionMatrix(), 0.0f, 0.0f, 0.0f, 0.0f, -0.6f, 0.0f, color);
                            matrixStack.pop();
                            matrixStack.push();
                            matrixStack.translate(0.37f, z ? 1.05f : 1.35f, 0.0f);
                            applyPartRotation(matrixStack, modelPart3);
                            drawBoneLine(bufferBuilderBegin, matrixStack.peek().getPositionMatrix(), 0.0f, 0.0f, 0.0f, 0.0f, -0.55f, 0.0f, color);
                            matrixStack.pop();
                            matrixStack.push();
                            matrixStack.translate(-0.37f, z ? 1.05f : 1.35f, 0.0f);
                            applyPartRotation(matrixStack, modelPart2);
                            drawBoneLine(bufferBuilderBegin, matrixStack.peek().getPositionMatrix(), 0.0f, 0.0f, 0.0f, 0.0f, -0.55f, 0.0f, color);
                            matrixStack.pop();
                            BufferRenderer.drawWithGlobalProgram(bufferBuilderBegin.end());
                            if (zIsInSwimmingPose) {
                                matrixStack.translate(0.0f, 0.95f, 0.0f);
                            }
                            if (zIsInSwimmingPose || zIsGliding) {
                                matrixStack.multiply(this.quaternion.setAngleAxis((((double) (90.0f + pitch)) * 3.141592653589793d) / 180.0d, 1.0d, 0.0d, 0.0d));
                            }
                            if (zIsInSwimmingPose) {
                                matrixStack.translate(0.0f, -0.35f, 0.0f);
                            }
                            matrixStack.multiply(this.quaternion.setAngleAxis((((double) (fLerp + 180.0f)) * 3.141592653589793d) / 180.0d, 0.0d, 1.0d, 0.0d));
                            matrixStack.translate(-entityRenderPosition.x, -entityRenderPosition.y, -entityRenderPosition.z);
                            matrixStack.pop();
                        }
                    }
                }
                GL11.glDisable(2881);
                GL11.glDisable(2848);
                GL11.glDisable(2848);
                RenderSystem.depthMask(true);
                RenderSystem.enableDepthTest();
                RenderSystem.enableCull();
                RenderSystem.disableBlend();
            }
        });
    }

    public void drawBoneLine(BufferBuilder bufferBuilder, Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, int i) {
        float f7;
        float f8;
        float f9;
        float f10 = f4 - f;
        float f11 = f5 - f2;
        float f12 = f6 - f3;
        float fSqrt = MathHelper.sqrt((f10 * f10) + (f11 * f11) + (f12 * f12));
        if (fSqrt != 0.0f) {
            f9 = f10 / fSqrt;
            f8 = f11 / fSqrt;
            f7 = f12 / fSqrt;
        } else {
            f7 = 0.0f;
            f8 = 0.0f;
            f9 = 0.0f;
        }
        bufferBuilder.vertex(matrix4f, f, f2, f3).color(i).normal(f9, f8, f7);
        bufferBuilder.vertex(matrix4f, f4, f5, f6).color(i).normal(f9, f8, f7);
    }

    public void applyPartRotation(MatrixStack matrixStack, ModelPart modelPart) {
        if (modelPart.roll != 0.0f) {
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotation(modelPart.roll));
        }
        if (modelPart.yaw != 0.0f) {
            matrixStack.multiply(RotationAxis.NEGATIVE_Y.rotation(modelPart.yaw));
        }
        if (modelPart.pitch != 0.0f) {
            matrixStack.multiply(RotationAxis.NEGATIVE_X.rotation(modelPart.pitch));
        }
    }
}
