package aethereal.render;
import aethereal.util.FramebufferUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.WindowFramebuffer;

public final class ScaledRenderTarget {
    public final int scaleFactor;
    public Framebuffer framebuffer;
    public boolean initialized;
    public final MinecraftClient client = MinecraftClient.getInstance();
    public final List<Runnable> renderQueue = new ArrayList();

    public ScaledRenderTarget(int i) {
        this.scaleFactor = Math.max(1, i);
    }

    public void init() {
        if (this.initialized) {
            return;
        }
        ensureFramebuffer();
        this.initialized = true;
    }

    public void add(Runnable runnable) {
        ensureInitialized();
        if (runnable != null) {
            this.renderQueue.add(runnable);
        }
    }

    public void addAll(Runnable... runnableArr) {
        ensureInitialized();
        if (runnableArr == null) {
            return;
        }
        for (Runnable runnable : runnableArr) {
            if (runnable != null) {
                this.renderQueue.add(runnable);
            }
        }
    }

    public void clearQueue() {
        this.renderQueue.clear();
    }

    public void renderToFramebuffer() {
        ensureInitialized();
        ensureFramebuffer();
        this.framebuffer.beginWrite(false);
        Iterator<Runnable> it = this.renderQueue.iterator();
        while (it.hasNext()) {
            it.next().run();
        }
        this.framebuffer.endWrite();
        clearQueue();
    }

    public Framebuffer getFramebuffer() {
        ensureInitialized();
        ensureFramebuffer();
        return this.framebuffer;
    }

    public void ensureFramebuffer() {
        int iMax = Math.max(1, this.client.getWindow().getFramebufferWidth() / this.scaleFactor);
        int iMax2 = Math.max(1, this.client.getWindow().getFramebufferHeight() / this.scaleFactor);
        this.framebuffer = FramebufferUtil.ensureFramebuffer(this.framebuffer, iMax, iMax2, () -> {
            return new WindowFramebuffer(iMax, iMax2);
        });
        FramebufferUtil.resizeIfNeeded(this.framebuffer, this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight());
    }

    public void ensureInitialized() {
        if (this.initialized) {
            return;
        }
        init();
    }
}
