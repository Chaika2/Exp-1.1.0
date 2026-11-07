package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.Expensive;
import aethereal.render.AnimationStack2;
import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.ColorSetting;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.setting.NumberSetting;
import aethereal.util.ColorUtil;
import aethereal.math.Easings;
import aethereal.math.DeltaTimeTracker;
import aethereal.Lang;
import aethereal.util.MathUtil;
import aethereal.type.Mc;
import aethereal.type.PopChamsColorMode;
import aethereal.ui.ModuleTab;
import aethereal.event.PacketReceiveEvent;
import aethereal.event.PlayerTickEvent;
import aethereal.model.PopChamsFakePlayer;
import aethereal.model.PopChamsModel;
import aethereal.util.WeightedEngine;
import aethereal.event.WorldRenderEvent;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Aliases(aliases = {"Pop Chams", "Totem Pop", "Totem Angle", "Chams"})
public class PopChamsModule extends Module {
    public final List<PopChamsModel> models;
    public final BooleanSetting blending;
    public final BooleanSetting textured;
    public final ColorSetting color;
    public final ModeSetting<PopChamsColorMode> colorMode;
    public final NumberSetting startAlpha;
    public final NumberSetting riseHeight;
    public final NumberSetting duration;
    public final NumberSetting scale;
    public final BooleanSetting fullBright;
    public final NumberSetting flatAlpha;
    public final BooleanSetting flatChams;
    public final DeltaTimeTracker deltaTracker;
    public final AnimationStack2 animationStack;

    public PopChamsModule() {
        super(ModuleTab.RENDER, "Pop Chams");
        this.models = new ArrayList();
        this.blending = new BooleanSetting(Lang.POPCHAMS_BLENDING).setValue(true);
        this.textured = new BooleanSetting(Lang.POPCHAMS_TEXTURED);
        this.color = new ColorSetting(Lang.POPCHAMS_COLOR);
        this.colorMode = new ModeSetting(Lang.POPCHAMS_COLOR_MODE).values(PopChamsColorMode.class);
        this.color.visible(() -> Boolean.valueOf(this.colorMode.isSelected(PopChamsColorMode.CUSTOM)));
        this.startAlpha = new NumberSetting(Lang.POPCHAMS_START_ALPHA).currentValue(0.9f).range(0.0f, 1.0f).step(0.05f);
        this.riseHeight = new NumberSetting(Lang.POPCHAMS_RISE_HEIGHT).currentValue(1.2f).range(0.2f, 5.0f).step(0.05f);
        this.duration = new NumberSetting(Lang.POPCHAMS_DURATION).currentValue(1.5f).range(0.2f, 6.0f).step(0.1f);
        this.scale = new NumberSetting(Lang.POPCHAMS_SCALE).currentValue(1.0f).range(0.5f, 2.0f).step(0.05f);
        this.fullBright = new BooleanSetting(Lang.POPCHAMS_FULL_BRIGHT).setValue(true);
        this.flatAlpha = new NumberSetting(Lang.POPCHAMS_FLAT_ALPHA).currentValue(0.4f).range(0.05f, 1.0f).step(0.05f).visible(() -> {
            return Boolean.valueOf(this.colorMode.isSelected(PopChamsColorMode.FLAT));
        });
        this.flatChams = new BooleanSetting(Lang.POPCHAMS_FLAT_CHAMS).visible(() -> {
            return Boolean.valueOf(this.colorMode.isSelected(PopChamsColorMode.FLAT));
        });
        this.deltaTracker = new DeltaTimeTracker();
        this.animationStack = new AnimationStack2();
        addSettings(this.blending, this.textured, this.colorMode, this.color, this.startAlpha, this.riseHeight, this.duration, this.scale, this.fullBright, this.flatAlpha, this.flatChams);
        register(PlayerTickEvent.class, class130Var -> {
            if (Mc.INSTANCE.isWorldLoaded() && class130Var.isPre()) {
                this.models.removeIf((v0) -> {
                    return v0.tick();
                });
            }
        });
        register(WorldRenderEvent.class, class016Var -> {
            if (Mc.INSTANCE.isWorldLoaded() && !this.models.isEmpty()) {
                MatrixStack matrixStack = class016Var.matrixStack();
                WeightedEngine class141Var = new WeightedEngine(this.deltaTracker.elapsedUnit(), this.animationStack);
                this.animationStack.begin();
                this.models.forEach(class563Var -> {
                    class563Var.animate(class141Var);
                });
                this.animationStack.end();
                RenderSystem.enableBlend();
                if (this.blending.isValue()) {
                    RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
                } else {
                    RenderSystem.defaultBlendFunc();
                }
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(MinecraftClient.isFancyGraphicsOrBetter());
                RenderSystem.enableCull();
                this.models.forEach(class563Var2 -> {
                    renderModel(matrixStack, class563Var2);
                });
                RenderSystem.disableCull();
                RenderSystem.disableBlend();
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(true);
            }
        });
        register(PacketReceiveEvent.class, class051Var -> {
            ClientWorld world;
            Entity entity;
            if (isState()) {
                if ((class051Var.getPacket()) instanceof EntityStatusS2CPacket packet ) {
                    EntityStatusS2CPacket entityStatusS2CPacket = packet;
                    if (entityStatusS2CPacket.getStatus() != 35 || (world = Mc.INSTANCE.getWorld()) == null || (entity = entityStatusS2CPacket.getEntity(world)) == null || !(entity instanceof OtherClientPlayerEntity)) {
                        return;
                    }
                    this.models.add(createModel((OtherClientPlayerEntity) entity, this.color.getColor(), this.textured.isValue()));
                }
            }
        });
    }

    public PopChamsModel createModel(OtherClientPlayerEntity otherClientPlayerEntity, int i, boolean z) {
        Mc class815Var = Mc.INSTANCE;
        PopChamsFakePlayer class562Var = new PopChamsFakePlayer(this, class815Var.getWorld(), BlockPos.ORIGIN, otherClientPlayerEntity.bodyYaw, new GameProfile(otherClientPlayerEntity.getUuid(), otherClientPlayerEntity.getName().getString()));
        class562Var.copyPositionAndRotation(otherClientPlayerEntity);
        ((PlayerEntity) class562Var).bodyYaw = otherClientPlayerEntity.bodyYaw;
        ((PlayerEntity) class562Var).headYaw = otherClientPlayerEntity.headYaw;
        ((PlayerEntity) class562Var).handSwingProgress = otherClientPlayerEntity.handSwingProgress;
        ((PlayerEntity) class562Var).handSwingTicks = otherClientPlayerEntity.handSwingTicks;
        class562Var.setSneaking(otherClientPlayerEntity.isSneaking());
        ((PlayerEntity) class562Var).limbAnimator.setSpeed(otherClientPlayerEntity.limbAnimator.getSpeed());
        ((PlayerEntity) class562Var).limbAnimator.pos = otherClientPlayerEntity.limbAnimator.getPos();
        ((PlayerEntity) class562Var).limbAnimator.prevSpeed = otherClientPlayerEntity.limbAnimator.prevSpeed;
        return new PopChamsModel(class562Var, otherClientPlayerEntity.getSkinTextures().texture(), i, z, Math.max(200, Math.round(this.duration.currentValue() * 1000.0f)));
    }

    public int resolveColor(float f, int i) {
        switch ((PopChamsColorMode) this.colorMode.currentValue()) {
            case WHITE:
                return 0xFFFFFFFF;
            case RAINBOW:
                return Color.HSBtoRGB(MathHelper.wrapDegrees(f * 360.0f) / 360.0f, 1.0f, 1.0f) | 0xFF000000;
            case CLIENT:
                return Expensive.INSTANCE.theme().palette().accent().argb();
            case FLAT:
                return Expensive.INSTANCE.theme().palette().accent().argb();
            default:
                return i;
        }
    }

    public void renderModel(MatrixStack matrixStack, PopChamsModel class563Var) {
        float fSmooth = class563Var.getAnimation().smoothAnimation();
        float f = MathHelper.clamp(1.0f - fSmooth, 0.0f, 1.0f);
        float fAlpha = MathHelper.clamp(this.startAlpha.currentValue() * fSmooth, 0.0f, 1.0f);
        if (fAlpha <= 0.01f) {
            return;
        }
        if (this.colorMode.isSelected(PopChamsColorMode.FLAT)) {
            renderFlatModel(matrixStack, class563Var, f, MathHelper.clamp(fAlpha * (this.flatChams.isValue() ? 1.0f : this.flatAlpha.currentValue()), 0.0f, 1.0f));
            return;
        }        BufferBuilder bufferBuilderBegin;
        Mc class815Var = Mc.INSTANCE;
        Vec3d pos = class815Var.getEntityRenderDispatcher().camera.getPos();
        Tessellator tesselator = class815Var.getTesselator();
        PlayerEntityModel model = class563Var.getModel();
        PlayerEntity player = class563Var.getPlayer();
        Identifier texture = class563Var.getTexture();
        int iResolved = resolveColor(f, class563Var.getColor());
        int iApplyOpacity = ColorUtil.applyOpacity(iResolved, ColorUtil.alpha(iResolved) * fAlpha);
        double d = this.riseHeight.currentValue() * (double) Easings.EASE_OUT_CUBIC.ease(f);
        double x = player.getX() - pos.getX();
        double y = player.getY() - pos.getY() + d;
        double z = player.getZ() - pos.getZ();
        float pos2 = player.limbAnimator.getPos();
        float speed = player.limbAnimator.getSpeed();
        PlayerEntityRenderState playerEntityRenderState = new PlayerEntityRenderState();
        playerEntityRenderState.bodyYaw = player.getBodyYaw();
        playerEntityRenderState.yawDegrees = player.headYaw - player.bodyYaw;
        playerEntityRenderState.pitch = player.getPitch();
        playerEntityRenderState.deathTime = player.deathTime;
        playerEntityRenderState.limbFrequency = pos2;
        playerEntityRenderState.limbAmplitudeMultiplier = speed;
        model.setAngles(playerEntityRenderState);
        if (!class563Var.isTextured() || texture == null) {
            RenderSystem.setShader(ShaderProgramKeys.POSITION);
            bufferBuilderBegin = tesselator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        } else {
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
            bufferBuilderBegin = tesselator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        }
        RenderSystem.setShaderColor(ColorUtil.red(iApplyOpacity) / 255.0f, ColorUtil.green(iApplyOpacity) / 255.0f, ColorUtil.blue(iApplyOpacity) / 255.0f, ColorUtil.alpha(iApplyOpacity) / 255.0f);
        float f2 = this.scale.currentValue();
        matrixStack.push();
        matrixStack.translate((float) x, (float) y, (float) z);
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtil.rad(180.0f - player.bodyYaw)));
        matrixStack.scale(-f2, -f2, f2);
        matrixStack.translate(0.0f, -1.501f, 0.0f);
        model.render(matrixStack, bufferBuilderBegin, this.fullBright.isValue() ? LightmapTextureManager.MAX_LIGHT_COORDINATE : 10, 0);
        BufferRenderer.drawWithGlobalProgram(bufferBuilderBegin.end());
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        matrixStack.pop();
    }

    public void renderFlatModel(MatrixStack matrixStack, PopChamsModel class563Var, float f, float fAlpha) {
        Mc class815Var = Mc.INSTANCE;
        Vec3d pos = class815Var.getEntityRenderDispatcher().camera.getPos();
        PlayerEntityModel model = class563Var.getModel();
        PlayerEntity player = class563Var.getPlayer();
        int iResolved = resolveColor(f, class563Var.getColor());
        int iApplyOpacity = ColorUtil.applyOpacity(iResolved, ColorUtil.alpha(iResolved) * fAlpha);
        PlayerEntityRenderState playerEntityRenderState = new PlayerEntityRenderState();
        playerEntityRenderState.bodyYaw = player.getBodyYaw();
        playerEntityRenderState.yawDegrees = player.headYaw - player.bodyYaw;
        playerEntityRenderState.pitch = player.getPitch();
        playerEntityRenderState.deathTime = player.deathTime;
        playerEntityRenderState.limbFrequency = player.limbAnimator.getPos();
        playerEntityRenderState.limbAmplitudeMultiplier = player.limbAnimator.getSpeed();
        model.setAngles(playerEntityRenderState);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        if (this.flatChams.isValue()) {
            RenderSystem.disableDepthTest();
        }
        RenderSystem.setShaderColor(ColorUtil.red(iApplyOpacity) / 255.0f, ColorUtil.green(iApplyOpacity) / 255.0f, ColorUtil.blue(iApplyOpacity) / 255.0f, ColorUtil.alpha(iApplyOpacity) / 255.0f);
        BufferBuilder bufferBuilder = class815Var.getTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        double d = this.riseHeight.currentValue() * (double) Easings.EASE_OUT_CUBIC.ease(f);
        float f2 = this.scale.currentValue();
        matrixStack.push();
        matrixStack.translate((float) (player.getX() - pos.getX()), (float) (player.getY() - pos.getY() + d), (float) (player.getZ() - pos.getZ()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtil.rad(180.0f - player.bodyYaw)));
        matrixStack.scale(-f2, -f2, f2);
        matrixStack.translate(0.0f, -1.501f, 0.0f);
        int iLight = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        renderFlatPart(model.head, matrixStack, bufferBuilder, iLight, iApplyOpacity);
        renderFlatPart(model.body, matrixStack, bufferBuilder, iLight, iApplyOpacity);
        renderFlatPart(model.rightArm, matrixStack, bufferBuilder, iLight, iApplyOpacity);
        renderFlatPart(model.leftArm, matrixStack, bufferBuilder, iLight, iApplyOpacity);
        renderFlatPart(model.rightLeg, matrixStack, bufferBuilder, iLight, iApplyOpacity);
        renderFlatPart(model.leftLeg, matrixStack, bufferBuilder, iLight, iApplyOpacity);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        matrixStack.pop();
        if (this.flatChams.isValue()) {
            RenderSystem.enableDepthTest();
        }
    }

    public void renderFlatPart(ModelPart modelPart, MatrixStack matrixStack, BufferBuilder bufferBuilder, int i, int j) {
        if (modelPart.visible && !modelPart.hidden) {
            modelPart.render(matrixStack, bufferBuilder, i, j, OverlayTexture.DEFAULT_UV);
        }
    }
}
