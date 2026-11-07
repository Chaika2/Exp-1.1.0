package aethereal.ui;
import aethereal.render.DrawCtx;
import aethereal.model.LayoutScaleContext;

public class SeparatorElement extends ModuleFrame {
    @Override
    public void highlight() {
    }

    @Override
    public void clearHighlight() {
    }

    @Override
    public void draw(DrawCtx class699Var, float f, float f2) {
        class699Var.fillRect(x(), y(), f, 1.0f, class699Var.drawEngine().colorStack().computeColor(class699Var.theme().palette().surfaceOutline().tone(600).argb()));
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
    }

    @Override
    public float height() {
        return 1.0f;
    }
}
