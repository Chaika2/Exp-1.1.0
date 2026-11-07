package aethereal.ui;
import aethereal.model.CursorMoveInput;
import aethereal.event.InputEvent;
import aethereal.model.InputEventContext;
import aethereal.model.LayoutScaleContext;
import aethereal.util.MathUtil;
import aethereal.model.MouseButtonInput;
import aethereal.render.ScreenResolution;

public class DraggableContainer extends WidgetContainer {
    public boolean dragging;
    public float dragOffsetX;
    public float dragOffsetY;
    public ScreenResolution screenResolution;
    public float logicalWidth;
    public float logicalHeight;

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        InputEvent class691VarInputEvent = class688Var.inputEvent();
        if (z && !this.dragging) {
            return false;
        }
        if (class691VarInputEvent instanceof CursorMoveInput) {
            boolean zInArea = class688Var.inArea(x(), y(), width(), height());
            if (!this.dragging) {
                return zInArea;
            }
            float fX = class688Var.logicalMousePosition().x() - this.dragOffsetX;
            float fY = class688Var.logicalMousePosition().y() - this.dragOffsetY;
            if (shouldClampPosition(this.screenResolution.width(), this.screenResolution.height())) {
                float fWidth = this.logicalWidth - this.screenResolution.width();
                float fHeight = this.logicalHeight - this.screenResolution.height();
                fX = MathUtil.clamp(fX, 0.0f, fWidth);
                fY = MathUtil.clamp(fY, 0.0f, fHeight);
            }
            this.screenResolution.setPosition(fX, fY);
            return true;
        }
        if (class691VarInputEvent instanceof MouseButtonInput) {
            MouseButtonInput class693Var = (MouseButtonInput) class691VarInputEvent;
            if (class693Var.button() == 0) {
                if (class693Var.action().press()) {
                    boolean zInArea2 = class688Var.inArea(x(), y(), width(), height());
                    if (!z && zInArea2) {
                        this.dragging = true;
                        this.dragOffsetX = class688Var.logicalMousePosition().x() - this.screenResolution.x();
                        this.dragOffsetY = class688Var.logicalMousePosition().y() - this.screenResolution.y();
                        return true;
                    }
                }
                if (class693Var.action().release() && this.dragging) {
                    this.dragging = false;
                    return true;
                }
            }
        }
        return super.handleInput(class688Var, z);
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        this.screenResolution = class698Var.screenResolution();
        this.logicalWidth = class698Var.logicalWidth();
        this.logicalHeight = class698Var.logicalHeight();
        if (shouldClampPosition(this.screenResolution.width(), this.screenResolution.height())) {
            this.screenResolution.setPosition(MathUtil.clamp(this.screenResolution.x(), 0.0f, this.logicalWidth - this.screenResolution.width()), MathUtil.clamp(this.screenResolution.y(), 0.0f, this.logicalHeight - this.screenResolution.height()));
        }
        super.layout(class698Var);
    }

    public boolean shouldClampPosition(float f, float f2) {
        return this.screenResolution != null && this.logicalWidth > f && this.logicalHeight > f2;
    }

    public boolean isDragging() {
        return this.dragging;
    }

    public void setDragging(boolean z) {
        this.dragging = z;
    }
}
