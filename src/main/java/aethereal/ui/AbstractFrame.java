package aethereal.ui;
import aethereal.render.DrawCtx;
import aethereal.util.ElementDimmingController;
import aethereal.model.InputEventContext;
import aethereal.model.LayoutScaleContext;
import aethereal.util.OverlayCommandQueue;
import aethereal.util.RenderCommandQueue;
import aethereal.util.WeightedEngine;

public abstract class AbstractFrame extends WidgetContainer {
    public final FrameElementColumn frameElementsContainer;
    public final ElementDimmingController elementDimmingManager = new ElementDimmingController();

    public AbstractFrame(FrameElementColumn class816Var) {
        this.frameElementsContainer = class816Var;
        addChild(class816Var);
    }

    public void renderFrameElements(DrawCtx class699Var) {
        this.frameElementsContainer.render(class699Var);
    }

    public void renderFrameOverlays(DrawCtx class699Var) {
        this.frameElementsContainer.renderOverlays(class699Var);
    }

    public void updateViewportVisibility(float f, float f2, float f3) {
        this.frameElementsContainer.handleViewportVisibility(f, f2, f3);
    }

    public void focusElement(ModuleFrame class758Var) {
        if (class758Var == null) {
            return;
        }
        this.elementDimmingManager.focusElement(class758Var);
        class758Var.highlight();
    }

    public void clearElementFocus() {
        this.elementDimmingManager.clearFocus();
    }

    public abstract float contentHeight();

    @Override
    public abstract void render(DrawCtx class699Var);

    public void renderOverlays(DrawCtx class699Var) {
        renderFrameOverlays(class699Var);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        return super.handleInput(class688Var, z);
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        super.layout(class698Var);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.elementDimmingManager.animate(class141Var);
        super.animation(class141Var);
    }

    @Override
    public void collectBlurElements(OverlayCommandQueue class677Var) {
        super.collectBlurElements(class677Var);
    }

    @Override
    public void collectBloomElements(RenderCommandQueue class676Var) {
        super.collectBloomElements(class676Var);
    }

    @Override
    public void handleClose() {
        clearElementFocus();
        super.handleClose();
    }

    @Override
    public void onMenuDrag(boolean z) {
        super.onMenuDrag(z);
    }

    public FrameElementColumn frameElementsContainer() {
        return this.frameElementsContainer;
    }

    public ElementDimmingController elementDimmingManager() {
        return this.elementDimmingManager;
    }
}
