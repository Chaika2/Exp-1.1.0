package aethereal.event;

import net.minecraft.client.gui.DrawContext;

public final class HandledScreenRenderEvent implements Event {
    public final DrawContext drawContext;
    public final int backgroundWidth;
    public final int backgroundHeight;

    public HandledScreenRenderEvent(DrawContext drawContext, int i, int i2) {
        this.drawContext = drawContext;
        this.backgroundWidth = i;
        this.backgroundHeight = i2;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "drawContext=" + this.drawContext + ", " + "backgroundWidth=" + this.backgroundWidth + ", " + "backgroundHeight=" + this.backgroundHeight + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.drawContext, this.backgroundWidth, this.backgroundHeight);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof HandledScreenRenderEvent)) return false;
        HandledScreenRenderEvent o = (HandledScreenRenderEvent) obj;
        return java.util.Objects.equals(this.drawContext, o.drawContext) && java.util.Objects.equals(this.backgroundWidth, o.backgroundWidth) && java.util.Objects.equals(this.backgroundHeight, o.backgroundHeight);
    }
public DrawContext drawContext() {
        return this.drawContext;
    }

    public int backgroundWidth() {
        return this.backgroundWidth;
    }

    public int backgroundHeight() {
        return this.backgroundHeight;
    }
}
