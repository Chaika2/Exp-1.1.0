package aethereal.ui;
import aethereal.model.CursorMoveInput;
import aethereal.render.DrawCtx;
import aethereal.util.DrawEngine;
import aethereal.math.Easings;
import aethereal.event.InputEvent;
import aethereal.model.InputEventContext;
import aethereal.type.MouseButtonAction;
import aethereal.model.MouseButtonInput;
import aethereal.model.ScissorBounds;
import aethereal.render.ToggleAnimator;
import aethereal.util.WeightedEngine;

public class ClickableBehavior {
    public static final int hoverDuration = 200;
    public final ToggleAnimator hoverAnimation = new ToggleAnimator(hoverDuration, Easings.EASE_IN_OUT_CUBIC);
    public Runnable clickAction;
    public Runnable rightClickAction;
    public float ownerX;
    public float ownerY;
    public float ownerW;
    public float ownerH;

    public boolean handleInput(InputEventContext class688Var, boolean z) {
        float f = this.ownerX;
        float f2 = this.ownerY;
        float f3 = this.ownerW;
        float f4 = this.ownerH;
        if (class688Var.inputEvent() instanceof CursorMoveInput) {
            boolean z2 = class688Var.inArea(f, f2, f3, f4) && !z;
            this.hoverAnimation.state(z2);
            return z2;
        }
        InputEvent class691VarInputEvent = class688Var.inputEvent();
        if (!(class691VarInputEvent instanceof MouseButtonInput)) {
            return false;
        }
        MouseButtonInput class693Var = (MouseButtonInput) class691VarInputEvent;
        MouseButtonAction class706VarAction = class693Var.action();
        boolean zInArea = class688Var.inArea(f, f2, f3, f4);
        if (class693Var.button() == 0 && this.clickAction != null && class706VarAction.press() && zInArea && !z) {
            this.clickAction.run();
            return true;
        }
        if (class693Var.button() != 1 || this.rightClickAction == null || !class706VarAction.press() || !zInArea || z) {
            return false;
        }
        this.rightClickAction.run();
        return true;
    }

    public void determineFromLastDrawEngineOperation(DrawCtx class699Var) {
        DrawEngine class154VarDrawEngine = class699Var.drawEngine();
        ScissorBounds class156VarLastBounds = class154VarDrawEngine.lastBounds();
        float fScaleFactor = class699Var.layoutContext().scaleFactor();
        float fTransformX = class154VarDrawEngine.transformX(class699Var.matrixStack(), class156VarLastBounds.x);
        float fTransformY = class154VarDrawEngine.transformY(class699Var.matrixStack(), class156VarLastBounds.y);
        if (fScaleFactor <= 0.0f || fScaleFactor == 1.0f) {
            this.ownerX = fTransformX;
            this.ownerY = fTransformY;
            this.ownerW = class156VarLastBounds.w;
            this.ownerH = class156VarLastBounds.h;
            return;
        }
        this.ownerX = fTransformX / fScaleFactor;
        this.ownerY = fTransformY / fScaleFactor;
        this.ownerW = class156VarLastBounds.w / fScaleFactor;
        this.ownerH = class156VarLastBounds.h / fScaleFactor;
    }

    public void setDimensions(float f, float f2, float f3, float f4) {
        this.ownerX = f;
        this.ownerY = f2;
        this.ownerW = f3;
        this.ownerH = f4;
    }

    public void animate(WeightedEngine class141Var) {
        this.hoverAnimation.animate(class141Var);
    }

    public ToggleAnimator hoverAnimation() {
        return this.hoverAnimation;
    }

    public ClickableBehavior clickCallback(Runnable runnable) {
        this.clickAction = runnable;
        return this;
    }

    public ClickableBehavior rightClickCallback(Runnable runnable) {
        this.rightClickAction = runnable;
        return this;
    }

    public float ownerX() {
        return this.ownerX;
    }

    public float ownerY() {
        return this.ownerY;
    }

    public float ownerW() {
        return this.ownerW;
    }

    public float ownerH() {
        return this.ownerH;
    }
}
