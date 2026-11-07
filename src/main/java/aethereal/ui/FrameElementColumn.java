package aethereal.ui;
import aethereal.render.ColorStack;
import aethereal.model.CursorMoveInput;
import aethereal.render.DrawCtx;
import aethereal.model.InputEventContext;
import aethereal.model.LayoutScaleContext;
import aethereal.util.WeightedEngine;

import java.util.ArrayList;
import java.util.List;

public class FrameElementColumn extends WidgetContainer {
    public final List<ModuleFrame> frames = new ArrayList();
    public float width;
    public final float gap;

    public FrameElementColumn(float f, float f2) {
        this.width = f;
        this.gap = f2;
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        float fY = y();
        for (ModuleFrame class758Var : this.frames) {
            if (class758Var.visible()) {
                class758Var.setPosition(x(), fY);
                fY += class758Var.height() + this.gap;
            }
        }
        super.layout(class698Var);
    }

    @Override
    public void render(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        AbstractFrame class757VarMethod001 = findParentFrame();
        for (int size = this.frames.size() - 1; size >= 0; size--) {
            ModuleFrame class758Var = this.frames.get(size);
            if (class758Var.visible()) {
                float dimmingForElement = 0.0f;
                if (class757VarMethod001 != null && class757VarMethod001.elementDimmingManager().isDimming()) {
                    dimmingForElement = class757VarMethod001.elementDimmingManager().getDimmingForElement(class758Var);
                }
                if (dimmingForElement > 0.01f) {
                    class115VarColorStack.push();
                    class115VarColorStack.alpha(1.0f - (dimmingForElement * 0.5f));
                }
                class758Var.draw(class699Var, width(), height());
                if (dimmingForElement > 0.01f) {
                    class115VarColorStack.pop();
                }
            }
        }
    }

    public void renderOverlays(DrawCtx class699Var) {
        for (int size = this.frames.size() - 1; size >= 0; size--) {
            ModuleFrame class758Var = this.frames.get(size);
            if (class758Var.visible()) {
                class758Var.drawOverlay(class699Var);
            }
        }
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        if (!visible()) {
            return z;
        }
        boolean zHandleInput = z;
        boolean z2 = class688Var.inputEvent() instanceof CursorMoveInput;
        for (ModuleFrame class758Var : this.frames) {
            if (class758Var.visible()) {
                if (!z2 && zHandleInput) {
                    break;
                }
                zHandleInput |= class758Var.handleInput(class688Var, zHandleInput);
            }
        }
        return zHandleInput;
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        super.animation(class141Var);
    }

    public void handleViewportVisibility(float f, float f2, float f3) {
        for (ModuleFrame class758Var : this.frames) {
            if (class758Var.visible()) {
                class758Var.handleViewportVisibility(f, f2, f3);
            }
        }
    }

    @Override
    public final float width() {
        return this.width;
    }

    @Override
    public float height() {
        return computeHeight();
    }

    public void addFrameElement(ModuleFrame class758Var) {
        if (class758Var == null) {
            return;
        }
        this.frames.add(class758Var);
        addChild(class758Var);
    }

    public float computeHeight() {
        float fHeight = 0.0f;
        int i = 0;
        for (ModuleFrame class758Var : this.frames) {
            if (class758Var.visible()) {
                fHeight += class758Var.height();
                i++;
            }
        }
        if (i > 1) {
            fHeight += this.gap * (i - 1);
        }
        return fHeight;
    }

    public AbstractFrame findParentFrame() {
        WidgetContainer class683VarParent = parent();
        while (true) {
            WidgetContainer class683Var = class683VarParent;
            if (class683Var == null) {
                return null;
            }
            if (class683Var instanceof AbstractFrame) {
                return (AbstractFrame) class683Var;
            }
            class683VarParent = class683Var.parent();
        }
    }

    public void clearElements() {
        this.frames.clear();
    }

    @Override
    public void removeChild(Widget class682Var) {
        super.removeChild(class682Var);
        if (class682Var instanceof ModuleFrame) {
            this.frames.remove((ModuleFrame) class682Var);
        }
    }

    public void setWidth(float f) {
        this.width = f;
    }
}
