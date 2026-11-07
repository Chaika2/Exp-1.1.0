package aethereal.render;

import net.minecraft.client.gl.Framebuffer;

public class BloomEffect {
    public static final int downscaleFactor = 2;
    public final BlurEffect blurEffect = new BlurEffect();

    public Framebuffer bloomFramebuffer;

    public void init() {
        this.blurEffect.init();
    }

    public void apply(Framebuffer framebuffer, int i) {
        this.blurEffect.apply(framebuffer, i);
        this.bloomFramebuffer = this.blurEffect.getBlurFramebuffer();
    }

    public Framebuffer getBloomFramebuffer() {
        return this.bloomFramebuffer;
    }
}
