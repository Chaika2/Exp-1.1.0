package aethereal.model;

import org.lwjgl.opengl.GL11;

public class ScissorRegion {
    public final int x;
    public final int y;
    public final int width;
    public final int height;
    public final float guiX;
    public final float guiY;
    public final float guiWidth;
    public final float guiHeight;

    public ScissorRegion() {
        this(0, 0, 0, 0, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    public ScissorRegion(int i, int i2, int i3, int i4, float f, float f2, float f3, float f4) {
        this.x = i;
        this.y = i2;
        this.width = i3;
        this.height = i4;
        this.guiX = f;
        this.guiY = f2;
        this.guiWidth = f3;
        this.guiHeight = f4;
    }

    public void apply() {
        GL11.glScissor(this.x, this.y, Math.max(0, this.width), Math.max(0, this.height));
    }
}
