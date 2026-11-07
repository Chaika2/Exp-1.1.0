package aethereal.ui;
import aethereal.render.DrawCtx;

public abstract class ModuleFrame extends Widget {
    public float viewportTop;
    public float viewportBottom;
    public float viewportOffset;

    public abstract void highlight();

    public abstract void clearHighlight();

    public abstract void draw(DrawCtx class699Var, float f, float f2);

    public void drawOverlay(DrawCtx class699Var) {
    }

    public void handleViewportVisibility(float f, float f2, float f3) {
        this.viewportTop = f;
        this.viewportBottom = f2;
        this.viewportOffset = f3;
    }

    @Override
    public float width() {
        return this.bounds.width();
    }
}
