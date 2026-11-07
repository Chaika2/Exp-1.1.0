package aethereal.render;
import aethereal.util.ImageBufferUtil;
import aethereal.resource.ResourceSource;
import aethereal.ui.TextureAnimationFrame;

import java.util.List;
import java.util.stream.Collectors;

public class GifPlayer {
    public final List<TextureAnimationFrame> frames;
    public int currentFrame = 0;
    public long lastFrameTime = System.currentTimeMillis();

    public GifPlayer(ResourceSource class178Var) {
        this.frames = loadFrames(class178Var);
    }

    public List<TextureAnimationFrame> loadFrames(ResourceSource class178Var) {
        return (List) new GifDecoder(class178Var.stream()).frames().stream().map(class333Var -> {
            return new TextureAnimationFrame(class333Var, new GlTexture(ImageBufferUtil.convertToByteBuffer(class333Var.image())).setDimensions(class333Var.image().getWidth(), class333Var.image().getHeight()));
        }).collect(Collectors.toList());
    }

    public void updateFrame() {
        if (System.currentTimeMillis() - this.lastFrameTime >= this.frames.get(this.currentFrame).delay()) {
            this.currentFrame = (this.currentFrame + 1) % this.frames.size();
            this.lastFrameTime = System.currentTimeMillis();
        }
    }

    public GlTexture currentImage() {
        if (this.frames.isEmpty()) {
            throw new IllegalStateException("No frames loaded");
        }
        return this.frames.get(this.currentFrame).texture();
    }

    public int getCurrentFrame() {
        return this.currentFrame;
    }
}

