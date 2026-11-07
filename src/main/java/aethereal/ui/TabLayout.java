package aethereal.ui;
import aethereal.render.DrawCtx;
import aethereal.model.InputEventContext;
import aethereal.model.LayoutScaleContext;
import aethereal.util.WeightedEngine;

public interface TabLayout {
    default void initialize(MenuTabElement class732Var) {
    }

    void render(DrawCtx class699Var);

    default void renderOverlays(DrawCtx class699Var) {
    }

    void layout(LayoutScaleContext class698Var);

    void animation(WeightedEngine class141Var);

    boolean handleInput(InputEventContext class688Var, boolean z);

    float getContentHeight();

    void positionFrames();

    float width();

    float height();
}
