package aethereal.ui;

import java.awt.image.BufferedImage;

public final class GifFrame {
    public final BufferedImage image;
    public final int delay;

    public GifFrame(BufferedImage bufferedImage, int i) {
        this.image = bufferedImage;
        this.delay = i;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "image=" + this.image + ", " + "delay=" + this.delay + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.image, this.delay);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GifFrame)) return false;
        GifFrame o = (GifFrame) obj;
        return java.util.Objects.equals(this.image, o.image) && java.util.Objects.equals(this.delay, o.delay);
    }
public BufferedImage image() {
        return this.image;
    }

    public int delay() {
        return this.delay;
    }
}
