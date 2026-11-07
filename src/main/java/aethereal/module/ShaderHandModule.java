package aethereal.module;

import aethereal.Lang;
import aethereal.Expensive;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;
import aethereal.render.FullscreenQuad;
import aethereal.render.ShaderProgram;
import aethereal.render.ShaderUniform;
import aethereal.resource.ClasspathResource;
import aethereal.resource.ResourceRouter;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.ExpandableSetting;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.setting.NumberSetting;
import aethereal.util.FramebufferUtil;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.WindowFramebuffer;
import net.minecraft.util.Arm;
import org.lwjgl.opengl.GL11;

public class ShaderHandModule extends Module {
    public final ExpandableSetting effectGroup;
    public final ModeSetting<ShaderHandEffect> effectMode;
    public final BooleanSetting animation;
    public final NumberSetting timeSpeed;
    public final NumberSetting effectAlpha;
    public final NumberSetting patternSpeed;
    public final NumberSetting shift;
    public final Mc mc;

    public Framebuffer handFramebuffer;
    public ShaderProgram handShader;
    public String currentShaderFile;
    public ShaderUniform colorTextureUniform;
    public ShaderUniform depthTextureUniform;
    public ShaderUniform resolutionUniform;
    public ShaderUniform timeUniform;
    public ShaderUniform speedUniform;
    public ShaderUniform shiftUniform;
    public ShaderUniform handMotionUniform;
    public ShaderUniform effectAlphaUniform;
    public final FullscreenQuad fullscreenQuad = new FullscreenQuad();
    public long shaderHandTimestamp = -1L;
    public float shaderHandAmountPrevious;

    public ShaderHandModule() {
        super(ModuleTab.RENDER, "Shader Hand");
        this.effectGroup = new ExpandableSetting(Lang.SHADER_HAND_GROUP, Lang.SHADER_HAND_GROUP_DESC).setValue(true);
        this.effectMode = new ModeSetting(Lang.SHADER_HAND_EFFECT).values(ShaderHandEffect.class).action(this::refreshShader);
        this.animation = new BooleanSetting(Lang.SHADER_HAND_ANIMATION).setValue(true);
        this.timeSpeed = new NumberSetting(Lang.SHADER_HAND_TIME_SPEED).currentValue(1.0f).range(0.0f, 5.0f).step(0.05f);
        this.effectAlpha = new NumberSetting(Lang.SHADER_HAND_EFFECT_ALPHA).currentValue(100.0f).range(0.0f, 100.0f).step(1.0f);
        this.patternSpeed = new NumberSetting(Lang.SHADER_HAND_PATTERN_SPEED).currentValue(1.0f).range(0.0f, 5.0f).step(0.05f).visible(this::usesPatternSettings);
        this.shift = new NumberSetting(Lang.SHADER_HAND_SHIFT).currentValue(1.6f).range(0.0f, 24.0f).step(0.1f).visible(this::usesPatternSettings);
        this.mc = Mc.INSTANCE;
        this.effectGroup.setSubSettings(List.of(this.effectMode, this.animation, this.timeSpeed, this.effectAlpha, this.patternSpeed, this.shift));
        addSettings(this.effectGroup);
    }

    public ShaderHandEffect currentEffect() {
        return (ShaderHandEffect) this.effectMode.currentValue();
    }

    public boolean usesPatternSettings() {
        return currentEffect().usesPatternSettings;
    }

    public boolean shouldCapture() {
        return isState() && this.mc.isWorldLoaded();
    }

    public void refreshShader() {
        if (this.handShader != null) {
            this.handShader.release();
            this.handShader = null;
        }
        this.currentShaderFile = null;
    }

    public ShaderProgram getHandShader() {
        ShaderHandEffect effect = currentEffect();
        if (this.handShader == null || !effect.fileName.equals(this.currentShaderFile)) {
            refreshShader();
            ResourceRouter router = new ResourceRouter("/", ClasspathResource::new);
            this.handShader = new ShaderProgram(router.route("shaders/hand/" + effect.fileName + ".fsh"), router.route("shaders/hand.vsh"));
            this.colorTextureUniform = this.handShader.uniform("ColorTexture");
            this.depthTextureUniform = this.handShader.uniform("DepthTexture");
            this.resolutionUniform = this.handShader.uniform("resolution");
            this.timeUniform = this.handShader.uniform("time");
            this.speedUniform = this.handShader.uniform("speed");
            this.shiftUniform = this.handShader.uniform("shift");
            this.handMotionUniform = this.handShader.uniform("handMotion");
            this.effectAlphaUniform = this.handShader.uniform("effectAlpha");
            this.currentShaderFile = effect.fileName;
        }
        return this.handShader;
    }

    public void beginHandCapture() {
        Framebuffer framebuffer = this.mc.getFramebuffer();
        if (framebuffer == null) {
            return;
        }
        int framebufferWidth = framebuffer.textureWidth;
        int framebufferHeight = framebuffer.textureHeight;
        this.handFramebuffer = FramebufferUtil.ensureFramebuffer(this.handFramebuffer, framebufferWidth, framebufferHeight, () -> new WindowFramebuffer(framebufferWidth, framebufferHeight));
        FramebufferUtil.resizeIfNeeded(this.handFramebuffer, framebufferWidth, framebufferHeight);
        framebuffer.endWrite();
        this.handFramebuffer.beginWrite(true);
        GL11.glClearDepth(1.0);
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void endHandCaptureAndComposite() {
        Framebuffer framebuffer = this.mc.getFramebuffer();
        if (this.handFramebuffer == null || framebuffer == null) {
            return;
        }
        try {
            ShaderHandEffect effect = currentEffect();
            ShaderProgram shader = getHandShader();
            this.handFramebuffer.copyDepthFrom(framebuffer);
            this.handFramebuffer.endWrite();
            framebuffer.beginWrite(true);
            shader.bind();
            this.colorTextureUniform.uploadInt(0);
            this.depthTextureUniform.uploadInt(1);
            this.resolutionUniform.uploadVec2(this.handFramebuffer.textureWidth, this.handFramebuffer.textureHeight);
            this.timeUniform.uploadFloat(getAnimationTime() * effect.multiplier);
            if (effect.usesPatternSettings) {
                float fPatternSpeed = this.patternSpeed.currentValue();
                this.speedUniform.uploadVec2(fPatternSpeed, fPatternSpeed);
                this.shiftUniform.uploadFloat(this.shift.currentValue());
            }
            this.handMotionUniform.uploadVec2(getHandMotion(), 0.22f);
            this.effectAlphaUniform.uploadFloat(Math.max(0.0f, Math.min(100.0f, this.effectAlpha.currentValue())) / 100.0f);
            int iGlGetInteger = GL11.glGetInteger(34016);
            RenderSystem.activeTexture(33984);
            this.handFramebuffer.beginRead();
            RenderSystem.activeTexture(33985);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.handFramebuffer.getDepthAttachment());
            RenderSystem.disableDepthTest();
            GL11.glDepthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            this.fullscreenQuad.draw();
            RenderSystem.disableBlend();
            GL11.glDepthMask(true);
            RenderSystem.enableDepthTest();
            this.handFramebuffer.endRead();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            RenderSystem.activeTexture(iGlGetInteger);
            shader.unbind();
        } catch (Throwable throwable) {
            Expensive.LOGGER.error("Failed to apply hand shader", throwable);
            setState(false);
            try {
                this.mc.getFramebuffer().beginWrite(true);
            } catch (Throwable throwable2) {
            }
        }
    }

    public float getAnimationTime() {
        long l = System.nanoTime();
        if (this.shaderHandTimestamp < 0L) {
            this.shaderHandTimestamp = l;
            return this.shaderHandAmountPrevious;
        }
        float f = Math.min((float) (l - this.shaderHandTimestamp) / 1.0E9F, 0.1F);
        this.shaderHandTimestamp = l;
        if (this.animation.isValue()) {
            this.shaderHandAmountPrevious = (this.shaderHandAmountPrevious + f * Math.max(0.0F, this.timeSpeed.currentValue())) % 100000.0F;
        }
        return this.shaderHandAmountPrevious;
    }

    public float getHandMotion() {
        return this.mc.getPlayer() != null && this.mc.getPlayer().getMainArm() == Arm.LEFT ? 0.28f : 0.72f;
    }

    public enum ShaderHandEffect implements DisplayNamed {
        AQUA(Lang.SHADER_HAND_EFFECT_AQUA, "sirius_aqua", 0.6f, false),
        FLOW(Lang.SHADER_HAND_EFFECT_FLOW, "sirius_flow", 0.06f, false),
        SMOKE(Lang.SHADER_HAND_EFFECT_SMOKE, "sirius_smoke", 3.0f, false),
        HOLY_FUCK(Lang.SHADER_HAND_EFFECT_HOLY_FUCK, "sirius_holyfuck", 0.6f, true),
        GANG(Lang.SHADER_HAND_EFFECT_GANG, "sirius_gang", 0.6f, true),
        GAMER(Lang.SHADER_HAND_EFFECT_GAMER, "sirius_gamer", 1.8f, false),
        GALAXY(Lang.SHADER_HAND_EFFECT_GALAXY, "sirius_galaxy", 0.06f, false),
        TECHNO(Lang.SHADER_HAND_EFFECT_TECHNO, "sirius_techno", 0.6f, false),
        GOLDEN(Lang.SHADER_HAND_EFFECT_GOLDEN, "sirius_golden", 0.6f, false),
        GUI_SHADER(Lang.SHADER_HAND_EFFECT_GUI_SHADER, "sirius_guishader", 1.2f, false),
        HI_DEF(Lang.SHADER_HAND_EFFECT_HI_DEF, "sirius_hidef", 3.0f, false),
        HOMIE(Lang.SHADER_HAND_EFFECT_HOMIE, "sirius_homie", 0.06f, false),
        SHELDON(Lang.SHADER_HAND_EFFECT_SHELDON, "sirius_sheldon", 0.06f, false),
        SMOKY(Lang.SHADER_HAND_EFFECT_SMOKY, "sirius_smoky", 0.06f, false),
        YIPPIE_OWNS(Lang.SHADER_HAND_EFFECT_YIPPIE_OWNS, "sirius_yippieowns", 6.0f, false),
        PURPLE(Lang.SHADER_HAND_EFFECT_PURPLE, "sirius_purple", 3.0f, false);

        public final Translation displayName;
        public final String fileName;
        public final float multiplier;
        public final boolean usesPatternSettings;

        ShaderHandEffect(Translation translation, String fileName, float f, boolean flag) {
            this.displayName = translation;
            this.fileName = fileName;
            this.multiplier = f;
            this.usesPatternSettings = flag;
        }

        @Override
        public Translation getDisplayName() {
            return this.displayName;
        }
    }
}