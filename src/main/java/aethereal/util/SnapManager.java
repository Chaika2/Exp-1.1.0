package aethereal.util;
import aethereal.model.DragRenderContext;
import aethereal.ui.Draggable;
import aethereal.math.Easings;
import aethereal.render.SnapGuideLine;
import aethereal.model.SnapResult;
import aethereal.render.ToggleAnimator;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.util.math.MatrixStack;

public class SnapManager {
    static final float aG = 8.0f;
    static final float aH = 1.0f;
    public final ToggleAnimator gridAnimation = new ToggleAnimator(150, Easings.EASE_OUT_CUBIC);
    public final List<SnapGuideLine> activeLines = new ArrayList();
    public boolean snapping = false;
    public float aI = -1.0f;
    public float aJ = -1.0f;

    public SnapResult calculateSnap(Draggable class806Var, List<Draggable> list, float f, float f2, float f3, float f4) {
        this.activeLines.clear();
        float fWidth = class806Var.width();
        float fHeight = class806Var.height();
        float f5 = f + fWidth;
        float f6 = f + (fWidth / 2.0f);
        float f7 = f2 + fHeight;
        float f8 = f2 + (fHeight / 2.0f);
        float f9 = f;
        float f10 = f2;
        boolean z = false;
        boolean z2 = false;
        float f11 = f3 / 2.0f;
        float f12 = f4 / 2.0f;
        if (Math.abs(f6 - f11) < aG) {
            f9 = f11 - (fWidth / 2.0f);
            z = true;
            this.activeLines.add(new SnapGuideLine(f11, false, 0.0f, f4));
        }
        if (Math.abs(f8 - f12) < aG) {
            f10 = f12 - (fHeight / 2.0f);
            z2 = true;
            this.activeLines.add(new SnapGuideLine(f12, true, 0.0f, f3));
        }
        for (Draggable class806Var2 : list) {
            if (class806Var2 != class806Var && class806Var2.isVisible()) {
                float x = class806Var2.getX();
                float x2 = class806Var2.getX() + class806Var2.width();
                float x3 = class806Var2.getX() + (class806Var2.width() / 2.0f);
                float y = class806Var2.getY();
                float y2 = class806Var2.getY() + class806Var2.height();
                float y3 = class806Var2.getY() + (class806Var2.height() / 2.0f);
                if (!z) {
                    if (Math.abs(f - x) < aG) {
                        f9 = x;
                        z = true;
                        this.activeLines.add(new SnapGuideLine(x, false, Math.min(f2, y) - 10.0f, Math.max(f7, y2) + 10.0f));
                    } else if (Math.abs(f5 - x2) < aG) {
                        f9 = x2 - fWidth;
                        z = true;
                        this.activeLines.add(new SnapGuideLine(x2, false, Math.min(f2, y) - 10.0f, Math.max(f7, y2) + 10.0f));
                    } else if (Math.abs(f6 - x3) < aG) {
                        f9 = x3 - (fWidth / 2.0f);
                        z = true;
                        this.activeLines.add(new SnapGuideLine(x3, false, Math.min(f2, y) - 10.0f, Math.max(f7, y2) + 10.0f));
                    } else if (Math.abs(f - x2) < aG) {
                        f9 = x2;
                        z = true;
                        this.activeLines.add(new SnapGuideLine(x2, false, Math.min(f2, y) - 10.0f, Math.max(f7, y2) + 10.0f));
                    } else if (Math.abs(f5 - x) < aG) {
                        f9 = x - fWidth;
                        z = true;
                        this.activeLines.add(new SnapGuideLine(x, false, Math.min(f2, y) - 10.0f, Math.max(f7, y2) + 10.0f));
                    }
                }
                if (!z2) {
                    if (Math.abs(f2 - y) < aG) {
                        f10 = y;
                        z2 = true;
                        this.activeLines.add(new SnapGuideLine(y, true, Math.min(f, x) - 10.0f, Math.max(f5, x2) + 10.0f));
                    } else if (Math.abs(f7 - y2) < aG) {
                        f10 = y2 - fHeight;
                        z2 = true;
                        this.activeLines.add(new SnapGuideLine(y2, true, Math.min(f, x) - 10.0f, Math.max(f5, x2) + 10.0f));
                    } else if (Math.abs(f8 - y3) < aG) {
                        f10 = y3 - (fHeight / 2.0f);
                        z2 = true;
                        this.activeLines.add(new SnapGuideLine(y3, true, Math.min(f, x) - 10.0f, Math.max(f5, x2) + 10.0f));
                    } else if (Math.abs(f2 - y2) < aG) {
                        f10 = y2;
                        z2 = true;
                        this.activeLines.add(new SnapGuideLine(y2, true, Math.min(f, x) - 10.0f, Math.max(f5, x2) + 10.0f));
                    } else if (Math.abs(f7 - y) < aG) {
                        f10 = y - fHeight;
                        z2 = true;
                        this.activeLines.add(new SnapGuideLine(y, true, Math.min(f, x) - 10.0f, Math.max(f5, x2) + 10.0f));
                    }
                }
            }
        }
        this.snapping = z || z2;
        this.aI = z ? f9 : -1.0f;
        this.aJ = z2 ? f10 : -1.0f;
        return new SnapResult(f9, f10, z, z2);
    }

    public void animate(WeightedEngine class141Var, boolean z) {
        this.gridAnimation.state(z && this.snapping).animate(class141Var);
    }

    public void render(DragRenderContext class809Var) {
        float fSmoothAnimation = this.gridAnimation.smoothAnimation();
        if (fSmoothAnimation <= 0.0f || this.activeLines.isEmpty()) {
            return;
        }
        DrawEngine class154VarDrawEngine = class809Var.drawEngine();
        MatrixStack matrixStack = class809Var.matrixStack();
        int i = (((int) (255.0f * fSmoothAnimation)) << 24) | 16777215;
        for (SnapGuideLine class812Var : this.activeLines) {
            if (class812Var.horizontal) {
                class154VarDrawEngine.rectangle(matrixStack.peek().getPositionMatrix(), class812Var.start, class812Var.position, class812Var.end - class812Var.start, aH, i);
            } else {
                class154VarDrawEngine.rectangle(matrixStack.peek().getPositionMatrix(), class812Var.position, class812Var.start, aH, class812Var.end - class812Var.start, i);
            }
        }
    }

    public void clear() {
        this.activeLines.clear();
        this.snapping = false;
    }

    public ToggleAnimator getGridAnimation() {
        return this.gridAnimation;
    }

    public List<SnapGuideLine> getActiveLines() {
        return this.activeLines;
    }

    public boolean isSnapping() {
        return this.snapping;
    }

    public float getLastSnapX() {
        return this.aI;
    }

    public float getLastSnapY() {
        return this.aJ;
    }
}
