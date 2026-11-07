package aethereal.module;

import aethereal.Expensive;
import aethereal.Lang;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;
import aethereal.render.FullscreenQuad;
import aethereal.render.ShaderProgram;
import aethereal.render.ShaderUniform;
import aethereal.resource.ClasspathResource;
import aethereal.resource.ResourceRouter;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.ColorSetting;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.setting.NumberSetting;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class ShaderSkyModule extends Module {
    public final ModeSetting<SkyMode> mode;
    public final ColorSetting firstColor;
    public final ColorSetting secondColor;
    public final NumberSetting intensity;
    public final NumberSetting softness;
    public final NumberSetting coverage;
    public final NumberSetting speed;
    public final Mc mc;

    public ShaderProgram skyShader;
    public String currentShaderFile;
    public ShaderUniform invViewProjUniform;
    public ShaderUniform auroraAUniform;
    public ShaderUniform auroraBUniform;
    public ShaderUniform paramsUniform;
    public final FullscreenQuad fullscreenQuad = new FullscreenQuad();
    public long skyTimestamp = -1L;
    public float skyTime;

    public ShaderSkyModule() {
        super(ModuleTab.RENDER, "Shader Sky");
        this.mode = new ModeSetting(Lang.SHADER_SKY_MODE).values(SkyMode.class).action(this::refreshShader);
        this.firstColor = new ColorSetting(Lang.SHADER_SKY_FIRST_COLOR).value(-14425478);
        this.secondColor = new ColorSetting(Lang.SHADER_SKY_SECOND_COLOR).value(-8766209);
        this.intensity = new NumberSetting(Lang.SHADER_SKY_INTENSITY).currentValue(1.5f).range(0.0f, 3.0f).step(0.05f).visible(this::usesAuroraMode);
        this.softness = new NumberSetting(Lang.SHADER_SKY_SOFTNESS).currentValue(0.4f).range(0.0f, 1.0f).step(0.01f).visible(this::usesAuroraMode);
        this.coverage = new NumberSetting(Lang.SHADER_SKY_COVERAGE).currentValue(0.5f).range(0.0f, 0.67f).step(0.01f).visible(this::usesAuroraMode);
        this.speed = new NumberSetting(Lang.SHADER_SKY_SPEED).currentValue(1.0f).range(0.0f, 5.0f).step(0.05f);
        this.mc = Mc.INSTANCE;
        addSettings(this.mode, this.firstColor, this.secondColor, this.intensity, this.softness, this.coverage, this.speed);
    }

    public SkyMode currentMode() {
        return (SkyMode) this.mode.currentValue();
    }

    public boolean usesAuroraMode() {
        return currentMode() == SkyMode.AURORA;
    }

    public void refreshShader() {
        if (this.skyShader != null) {
            this.skyShader.release();
            this.skyShader = null;
        }
        this.currentShaderFile = null;
    }

    public ShaderProgram getSkyShader() {
        SkyMode modeValue = currentMode();
        if (this.skyShader == null || !modeValue.fileName.equals(this.currentShaderFile)) {
            refreshShader();
            ResourceRouter router = new ResourceRouter("/", ClasspathResource::new);
            this.skyShader = new ShaderProgram(router.route("shaders/" + modeValue.fileName + ".fsh"), router.route("shaders/sky.vsh"));
            this.invViewProjUniform = this.skyShader.uniform("inv_view_proj");
            this.auroraAUniform = this.skyShader.uniform("aurora_a");
            this.auroraBUniform = this.skyShader.uniform("aurora_b");
            this.paramsUniform = this.skyShader.uniform("params");
            this.currentShaderFile = modeValue.fileName;
        }
        return this.skyShader;
    }

    public boolean shouldReplaceSky() {
        return isState() && this.mc.isWorldLoaded();
    }

    public void drawSky(Framebuffer framebuffer, float tickDelta) {
        if (framebuffer == null) {
            return;
        }
        try {
            ShaderProgram shader = getSkyShader();
            shader.bind();
            int framebufferWidth = framebuffer.textureWidth;
            int framebufferHeight = framebuffer.textureHeight;
            GL11.glViewport(0, 0, framebufferWidth, framebufferHeight);
            Matrix4f viewProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
            viewProjection.mul(RenderSystem.getModelViewMatrix());
            this.invViewProjUniform.uploadMatrix4f(viewProjection.invert());
            this.auroraAUniform.uploadVec4(colorR(this.firstColor), colorG(this.firstColor), colorB(this.firstColor), this.firstColor.getAlpha());
            this.auroraBUniform.uploadVec4(colorR(this.secondColor), colorG(this.secondColor), colorB(this.secondColor), this.secondColor.getAlpha());
            this.paramsUniform.uploadVec4(getSkyAnimationTime(), this.intensity.currentValue(), this.softness.currentValue(), this.coverage.currentValue());
            RenderSystem.disableDepthTest();
            GL11.glDepthMask(false);
            this.fullscreenQuad.draw();
            GL11.glDepthMask(true);
            RenderSystem.enableDepthTest();
            shader.unbind();
        } catch (Throwable throwable) {
            Expensive.LOGGER.error("Failed to apply sky shader", throwable);
            setState(false);
        }
    }

    private float colorR(ColorSetting setting) {
        return setting.getRed() / 255.0f;
    }

    private float colorG(ColorSetting setting) {
        return setting.getGreen() / 255.0f;
    }

    private float colorB(ColorSetting setting) {
        return setting.getBlue() / 255.0f;
    }

    public float getSkyAnimationTime() {
        long l = System.nanoTime();
        if (this.skyTimestamp < 0L) {
            this.skyTimestamp = l;
            return this.skyTime;
        }
        float f = Math.min((float) (l - this.skyTimestamp) / 1.0E9F, 0.1F);
        this.skyTimestamp = l;
        this.skyTime = (this.skyTime + f * Math.max(0.0F, this.speed.currentValue())) % 100000.0F;
        return this.skyTime;
    }

    public enum SkyMode implements DisplayNamed {
        AURORA(Lang.SHADER_SKY_MODE_AURORA, "sky_aurora"),
        BOREALIS(Lang.SHADER_SKY_MODE_BOREALIS, "sky_borealis");

        public final Translation displayName;
        public final String fileName;

        SkyMode(Translation translation, String fileName) {
            this.displayName = translation;
            this.fileName = fileName;
        }

        @Override
        public Translation getDisplayName() {
            return this.displayName;
        }
    }
}