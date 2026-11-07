package aethereal.render;
import aethereal.resource.ClasspathResource;
import aethereal.util.FramebufferUtil;
import aethereal.type.Mc;
import aethereal.resource.ResourceRouter;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.FloatBuffer;
import java.util.HashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.WindowFramebuffer;
import net.minecraft.client.util.Window;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class BlurEffect {
    public final Mc mc = Mc.INSTANCE;
    public final FullscreenQuad fullscreenQuad = new FullscreenQuad();

    public final HashMap<Integer, float[]> weightCache = new HashMap<>();
    public final HashMap<Integer, FloatBuffer> kernelBufferCache = new HashMap<>();

    public final HashMap<Integer, FloatBuffer> offsetBufferCache = new HashMap<>();
    public static final int downscaleFactor = 2;
    public ShaderUniform directionUniform;
    public ShaderUniform pairCountUniform;
    public ShaderUniform kernelUniform;
    public ShaderUniform offsetsUniform;
    public ShaderUniform textureUniform;
    public ShaderUniform brightnessUniform;

    public ShaderProgram blurShader;

    public Framebuffer horizontalFramebuffer;

    public Framebuffer blurFramebuffer;

    public void init() {
        ResourceRouter class149Var = new ResourceRouter("/", ClasspathResource::new);
        this.blurShader = new ShaderProgram(class149Var.route("shaders/blur.fsh"), class149Var.route("shaders/framebuffer.vsh"));
        this.directionUniform = this.blurShader.uniform("uDirection");
        this.textureUniform = this.blurShader.uniform("uTexture");
        this.pairCountUniform = this.blurShader.uniform("uPairCount");
        this.brightnessUniform = this.blurShader.uniform("uBrightness");
        this.kernelUniform = this.blurShader.uniform("uKernel");
        this.offsetsUniform = this.blurShader.uniform("uOffsets");
        Window window = MinecraftClient.getInstance().getWindow();
        int framebufferWidth = window.getFramebufferWidth() / 2;
        int framebufferHeight = window.getFramebufferHeight() / 2;
        this.horizontalFramebuffer = FramebufferUtil.ensureFramebuffer(this.horizontalFramebuffer, framebufferWidth, framebufferHeight, () -> {
            return new WindowFramebuffer(framebufferWidth, framebufferHeight);
        });
        this.blurFramebuffer = FramebufferUtil.ensureFramebuffer(this.blurFramebuffer, framebufferWidth, framebufferHeight, () -> {
            return new WindowFramebuffer(framebufferWidth, framebufferHeight);
        });
    }

    public void apply(int i) {
        if (this.blurShader == null) {
            return;
        }
        int iMax = Math.max(1, Math.min(i, 60));
        Window window = this.mc.getWindow();
        int framebufferWidth = window.getFramebufferWidth() / 2;
        int framebufferHeight = window.getFramebufferHeight() / 2;
        this.horizontalFramebuffer = FramebufferUtil.ensureFramebuffer(this.horizontalFramebuffer, framebufferWidth, framebufferHeight, () -> {
            return new WindowFramebuffer(framebufferWidth, framebufferHeight);
        });
        this.blurFramebuffer = FramebufferUtil.ensureFramebuffer(this.blurFramebuffer, framebufferWidth, framebufferHeight, () -> {
            return new WindowFramebuffer(framebufferWidth, framebufferHeight);
        });
        FramebufferUtil.resizeIfNeeded(this.horizontalFramebuffer, framebufferWidth, framebufferHeight);
        FramebufferUtil.resizeIfNeeded(this.blurFramebuffer, framebufferWidth, framebufferHeight);
        this.blurShader.bind();
        this.textureUniform.uploadInt(0);
        this.pairCountUniform.uploadInt(iMax / 2);
        this.kernelUniform.uploadFloatBuffer(getKernelBuffer(iMax, 3.0f));
        this.offsetsUniform.uploadFloatBuffer(getOffsetBuffer(iMax, 3.0f));
        this.brightnessUniform.uploadFloat(0.0f);
        int iGlGetInteger = GL11.glGetInteger(34016);
        this.horizontalFramebuffer.beginWrite(true);
        this.directionUniform.uploadVec2(1.0f / framebufferWidth, 0.0f);
        RenderSystem.activeTexture(33984);
        MinecraftClient.getInstance().getFramebuffer().beginRead();
        this.fullscreenQuad.draw();
        MinecraftClient.getInstance().getFramebuffer().endRead();
        this.horizontalFramebuffer.endWrite();
        this.blurFramebuffer.beginWrite(true);
        this.directionUniform.uploadVec2(0.0f, 1.0f / framebufferHeight);
        RenderSystem.activeTexture(33984);
        this.horizontalFramebuffer.beginRead();
        this.fullscreenQuad.draw();
        this.horizontalFramebuffer.endRead();
        this.blurFramebuffer.endWrite();
        RenderSystem.activeTexture(iGlGetInteger);
        this.blurShader.unbind();
    }

    public void apply(int i, float f) {
        if (this.blurShader == null) {
            return;
        }
        int iMax = Math.max(1, Math.min(i, 60));
        Window window = this.mc.getWindow();
        int framebufferWidth = window.getFramebufferWidth() / 2;
        int framebufferHeight = window.getFramebufferHeight() / 2;
        this.horizontalFramebuffer = FramebufferUtil.ensureFramebuffer(this.horizontalFramebuffer, framebufferWidth, framebufferHeight, () -> {
            return new WindowFramebuffer(framebufferWidth, framebufferHeight);
        });
        this.blurFramebuffer = FramebufferUtil.ensureFramebuffer(this.blurFramebuffer, framebufferWidth, framebufferHeight, () -> {
            return new WindowFramebuffer(framebufferWidth, framebufferHeight);
        });
        FramebufferUtil.resizeIfNeeded(this.horizontalFramebuffer, framebufferWidth, framebufferHeight);
        FramebufferUtil.resizeIfNeeded(this.blurFramebuffer, framebufferWidth, framebufferHeight);
        this.blurShader.bind();
        this.textureUniform.uploadInt(0);
        this.pairCountUniform.uploadInt(iMax / 2);
        this.kernelUniform.uploadFloatBuffer(getKernelBuffer(iMax, f));
        this.offsetsUniform.uploadFloatBuffer(getOffsetBuffer(iMax, f));
        this.brightnessUniform.uploadFloat(0.0f);
        int iGlGetInteger = GL11.glGetInteger(34016);
        this.horizontalFramebuffer.beginWrite(true);
        this.directionUniform.uploadVec2(1.0f / framebufferWidth, 0.0f);
        RenderSystem.activeTexture(33984);
        MinecraftClient.getInstance().getFramebuffer().beginRead();
        this.fullscreenQuad.draw();
        MinecraftClient.getInstance().getFramebuffer().endRead();
        this.horizontalFramebuffer.endWrite();
        this.blurFramebuffer.beginWrite(true);
        this.directionUniform.uploadVec2(0.0f, 1.0f / framebufferHeight);
        RenderSystem.activeTexture(33984);
        this.horizontalFramebuffer.beginRead();
        this.fullscreenQuad.draw();
        this.horizontalFramebuffer.endRead();
        this.blurFramebuffer.endWrite();
        RenderSystem.activeTexture(iGlGetInteger);
        this.blurShader.unbind();
    }

    public void apply(Framebuffer framebuffer, int i) {
        if (this.blurShader == null) {
            return;
        }
        int iMax = Math.max(1, Math.min(i, 60));
        int i2 = framebuffer.textureWidth;
        int i3 = framebuffer.textureHeight;
        this.horizontalFramebuffer = FramebufferUtil.ensureFramebuffer(this.horizontalFramebuffer, i2, i3, () -> {
            return new WindowFramebuffer(i2, i3);
        });
        this.blurFramebuffer = FramebufferUtil.ensureFramebuffer(this.blurFramebuffer, i2, i3, () -> {
            return new WindowFramebuffer(i2, i3);
        });
        int i4 = iMax / 2;
        float f = framebuffer.textureWidth;
        float f2 = framebuffer.textureHeight;
        this.blurShader.bind();
        this.textureUniform.uploadInt(0);
        this.pairCountUniform.uploadInt(i4);
        this.kernelUniform.uploadFloatBuffer(getKernelBuffer(iMax, 3.0f));
        this.offsetsUniform.uploadFloatBuffer(getOffsetBuffer(iMax, 3.0f));
        this.brightnessUniform.uploadFloat(1.0f);
        int iGlGetInteger = GL11.glGetInteger(34016);
        this.horizontalFramebuffer.beginWrite(true);
        this.directionUniform.uploadVec2((1.0f / f) * 2.0f, 0.0f);
        RenderSystem.activeTexture(33984);
        framebuffer.beginRead();
        this.fullscreenQuad.draw();
        framebuffer.endRead();
        this.horizontalFramebuffer.endWrite();
        this.blurFramebuffer.beginWrite(true);
        this.directionUniform.uploadVec2(0.0f, (1.0f / f2) * 2.0f);
        RenderSystem.activeTexture(33984);
        this.horizontalFramebuffer.beginRead();
        this.fullscreenQuad.draw();
        this.horizontalFramebuffer.endRead();
        this.blurFramebuffer.endWrite();
        RenderSystem.activeTexture(iGlGetInteger);
        this.blurShader.unbind();
        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
    }

    public Framebuffer getBlurFramebuffer() {
        return this.blurFramebuffer;
    }

    public float[] computeGaussianWeights(int i, float f) {
        float[] fArr = this.weightCache.get(Integer.valueOf(i));
        if (fArr != null) {
            return fArr;
        }
        float[] fArr2 = new float[i];
        float f2 = i / f;
        if (f2 <= 0.0f) {
            f2 = 1.0f;
        }
        float f3 = 0.0f;
        int i2 = 0;
        while (i2 < i) {
            float f4 = i2 / f2;
            fArr2[i2] = (float) (Math.exp(((-0.5f) * f4) * f4) / ((double) (Math.abs(f2) * 2.5066283f)));
            f3 += i2 == 0 ? fArr2[i2] : fArr2[i2] * 2.0f;
            i2++;
        }
        for (int i3 = 0; i3 < i; i3++) {
            int i4 = i3;
            fArr2[i4] = fArr2[i4] / f3;
        }
        this.weightCache.put(Integer.valueOf(i), fArr2);
        return fArr2;
    }

    public FloatBuffer getKernelBuffer(int i, float f) {
        FloatBuffer floatBufferCreateFloatBuffer = this.kernelBufferCache.get(Integer.valueOf(i));
        if (floatBufferCreateFloatBuffer == null) {
            float[] fArrMethod004 = computeGaussianWeights(i, f);
            float[] fArr = new float[(i / 2) + 1];
            fArr[0] = fArrMethod004[0];
            int i2 = 1;
            for (int i3 = 1; i3 < i; i3 += 2) {
                int i4 = i2;
                i2++;
                fArr[i4] = fArrMethod004[i3] + (i3 + 1 < i ? fArrMethod004[i3 + 1] : 0.0f);
            }
            floatBufferCreateFloatBuffer = BufferUtils.createFloatBuffer(fArr.length);
            floatBufferCreateFloatBuffer.put(fArr);
            floatBufferCreateFloatBuffer.flip();
            this.kernelBufferCache.put(Integer.valueOf(i), floatBufferCreateFloatBuffer);
        } else {
            floatBufferCreateFloatBuffer.rewind();
        }
        return floatBufferCreateFloatBuffer;
    }

    public FloatBuffer getOffsetBuffer(int i, float f) {
        FloatBuffer floatBufferCreateFloatBuffer = this.offsetBufferCache.get(Integer.valueOf(i));
        if (floatBufferCreateFloatBuffer == null) {
            float[] fArrMethod004 = computeGaussianWeights(i, f);
            int i2 = i / 2;
            float[] fArr = i2 == 0 ? new float[]{0.0f} : new float[i2];
            int i3 = 0;
            for (int i4 = 1; i4 < i; i4 += 2) {
                float f2 = fArrMethod004[i4];
                float f3 = i4 + 1 < i ? fArrMethod004[i4 + 1] : 0.0f;
                float f4 = f2 + f3;
                int i5 = i3;
                i3++;
                fArr[i5] = f4 == 0.0f ? i4 : ((i4 * f2) + ((i4 + 1) * f3)) / f4;
            }
            floatBufferCreateFloatBuffer = BufferUtils.createFloatBuffer(fArr.length);
            floatBufferCreateFloatBuffer.put(fArr);
            floatBufferCreateFloatBuffer.flip();
            this.offsetBufferCache.put(Integer.valueOf(i), floatBufferCreateFloatBuffer);
        } else {
            floatBufferCreateFloatBuffer.rewind();
        }
        return floatBufferCreateFloatBuffer;
    }
}
