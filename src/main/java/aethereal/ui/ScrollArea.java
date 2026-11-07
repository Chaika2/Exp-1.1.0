package aethereal.ui;
import aethereal.render.AnimatedFloat;
import aethereal.render.DrawCtx;
import aethereal.util.DrawEngine;
import aethereal.math.Easings;
import aethereal.event.InputEvent;
import aethereal.model.InputEventContext;
import aethereal.model.LayoutScaleContext;
import aethereal.util.MathUtil;
import aethereal.model.ScrollInput;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class ScrollArea extends Widget {
    public static final float scrollSpeed = 80.0f;
    public float pendingScroll;
    public float scrollOffset;
    public float areaWidth;
    public float areaHeight;
    public float contentHeight;
    public final AnimatedFloat scrollAnimation = new AnimatedFloat(120, Easings.EASE_OUT_CUBIC);
    public final WidgetBounds physicalBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) throws MatchException {
        if (z) {
            return false;
        }
        InputEvent class691VarInputEvent = class688Var.inputEvent();
        if (!(class691VarInputEvent instanceof ScrollInput)) {
            return false;
        }
        try {
            double dDeltaY = ((ScrollInput) class691VarInputEvent).deltaY();
            if (!class688Var.inPhysicalArea(class688Var.currentMousePosition(), this.physicalBounds.x(), this.physicalBounds.y(), this.physicalBounds.width(), this.physicalBounds.height())) {
                return false;
            }
            float f = (float) ((-dDeltaY) * 80.0d * ((double) (Screen.hasControlDown() ? 3.0f : 1.0f)));
            if (this.pendingScroll != 0.0f && Math.signum(f) != Math.signum(this.pendingScroll)) {
                this.pendingScroll = 0.0f;
            }
            this.pendingScroll += f;
            return true;
        } catch (Throwable th) {
            throw new MatchException(th.toString(), th);
        }
    }

    public void beginArea(DrawCtx class699Var, float f, float f2, float f3, float f4) {
        this.scrollOffset += this.pendingScroll;
        this.pendingScroll = 0.0f;
        LayoutScaleContext class698VarLayoutContext = class699Var.layoutContext();
        this.areaWidth = f3;
        this.areaHeight = f4;
        DrawEngine class154VarDrawEngine = class699Var.drawEngine();
        MatrixStack matrixStack = class699Var.matrixStack();
        clampScroll(f4);
        float physical = class698VarLayoutContext.toPhysical(f);
        float physical2 = class698VarLayoutContext.toPhysical(f2);
        float physical3 = class698VarLayoutContext.toPhysical(f3);
        float physical4 = class698VarLayoutContext.toPhysical(f4);
        listen(class699Var, physical, physical2, physical3, physical4);
        class154VarDrawEngine.beginScissor(matrixStack.peek().getPositionMatrix(), physical, physical2, physical3, physical4);
        this.scrollAnimation.destination(this.scrollOffset);
        matrixStack.push();
        matrixStack.translate(0.0f, -class698VarLayoutContext.toPhysical(scrollY()), 0.0f);
    }

    public void scrollToBottomSmooth() {
        float fMax = Math.max(0.0f, this.contentHeight - this.areaHeight);
        if (Math.abs(this.scrollOffset - fMax) < 1.0f) {
            return;
        }
        this.scrollOffset = fMax;
        clampScroll(this.areaHeight);
    }

    public void scrollToBottomImmediate() {
        this.scrollOffset = Math.max(0.0f, this.contentHeight - this.areaHeight);
        clampScroll(this.areaHeight);
        this.scrollAnimation.set(this.scrollOffset);
        this.scrollAnimation.destination(this.scrollOffset);
        this.pendingScroll = 0.0f;
    }

    @Deprecated
    public void scrollToBottom() {
        scrollToBottomSmooth();
    }

    public void endArea(DrawCtx class699Var, float f) {
        DrawEngine class154VarDrawEngine = class699Var.drawEngine();
        MatrixStack matrixStack = class699Var.matrixStack();
        this.contentHeight = f;
        class154VarDrawEngine.endScissor();
        matrixStack.pop();
    }

    public void listen(DrawCtx class699Var, float f, float f2, float f3, float f4) {
        Matrix4f positionMatrix = class699Var.matrixStack().peek().getPositionMatrix();
        DrawEngine class154VarDrawEngine = class699Var.drawEngine();
        Vector4f vector4f = new Vector4f(f, f2, 0.0f, 1.0f);
        Vector4f vector4f2 = new Vector4f(f + f3, f2 + f4, 0.0f, 1.0f);
        vector4f.mul(positionMatrix);
        vector4f2.mul(positionMatrix);
        this.physicalBounds.withPosition(class154VarDrawEngine.transformX(positionMatrix, f), class154VarDrawEngine.transformY(positionMatrix, f2)).withSize(vector4f2.x - vector4f.x, vector4f2.y - vector4f.y);
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        setSize(this.areaWidth, this.areaHeight);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.scrollAnimation.animate(class141Var);
        super.animation(class141Var);
    }

    public void scrollBy(float f) {
        this.scrollOffset += f;
        clampScroll(this.areaHeight);
    }

    public void scrollTo(float f) {
        this.scrollOffset = f;
        clampScroll(this.areaHeight);
        this.scrollAnimation.set(this.scrollOffset);
        this.scrollAnimation.destination(this.scrollOffset);
        this.pendingScroll = 0.0f;
    }

    public float scrollY() {
        return this.scrollAnimation.animatedValue();
    }

    public float visibleTop() {
        return scrollY();
    }

    public float visibleBottom() {
        return scrollY() + this.areaHeight;
    }

    public void clampScroll(float f) {
        if (this.contentHeight > f) {
            this.scrollOffset = MathUtil.clamp(this.scrollOffset, 0.0f, this.contentHeight - f);
        } else {
            this.scrollOffset = 0.0f;
        }
    }
}
