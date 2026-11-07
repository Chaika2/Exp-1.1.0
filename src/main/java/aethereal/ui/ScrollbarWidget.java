package aethereal.ui;
import aethereal.render.ColorStack;
import aethereal.model.CursorMoveInput;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.event.InputEvent;
import aethereal.model.InputEventContext;
import aethereal.model.LayoutScaleContext;
import aethereal.util.MathUtil;
import aethereal.type.MouseButtonAction;
import aethereal.model.MouseButtonInput;
import aethereal.model.ScrollInput;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.util.function.Consumer;
import java.util.function.Supplier;
import org.joml.Vector4f;

public class ScrollbarWidget extends AbstractWidget {
    public final Supplier<Float> scrollOffsetSupplier;
    public final Supplier<Float> contentSizeSupplier;
    public final Supplier<Float> viewportSizeSupplier;
    public final Consumer<Float> scrollConsumer;
    public final float padding;
    public final float thickness;
    public final float minThumbSize;
    public boolean dragging;
    public float dragOffset;
    public long lastScrollTime;
    public final ToggleAnimator visibilityAnimator = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);
    public final ToggleAnimator hoverAnimator = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);
    public final WidgetBounds trackBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
    public final WidgetBounds thumbBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);

    public ScrollbarWidget(Supplier<Float> supplier, Supplier<Float> supplier2, Supplier<Float> supplier3, Consumer<Float> consumer, float f, float f2, float f3) {
        this.scrollOffsetSupplier = supplier;
        this.contentSizeSupplier = supplier2;
        this.viewportSizeSupplier = supplier3;
        this.scrollConsumer = consumer;
        this.padding = f;
        this.thickness = f2;
        this.minThumbSize = f3;
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        float fMethod002 = floatOrZero(this.viewportSizeSupplier.get());
        setSize(this.thickness, fMethod002);
        float fX = x();
        float fY = y() + this.padding;
        this.trackBounds.withPosition(fX, fY).withSize(this.thickness, Math.max(0.0f, fMethod002 - (this.padding * 2.0f)));
        updateThumbBounds();
    }

    @Override
    public void render(DrawCtx class699Var) {
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.colorStack();
        int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(300).argb());
        int iComputeColor2 = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(200).argb());
        class699Var.fillRoundedRect(this.thumbBounds.x(), this.thumbBounds.y(), this.thumbBounds.width(), this.thumbBounds.height(), new Vector4f(2.0f, 2.0f, 2.0f, 2.0f), class115VarColorStack.interpolate(class115VarColorStack.interpolate(iComputeColor, iComputeColor2, this.hoverAnimator, this.hoverAnimator), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(200).argb()), this.visibilityAnimator, this.visibilityAnimator));
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.hoverAnimator.animate(class141Var);
        this.visibilityAnimator.animate(class141Var);
        if (!this.dragging && this.lastScrollTime > 0 && System.currentTimeMillis() - this.lastScrollTime > 1000) {
            this.visibilityAnimator.state(false);
            this.lastScrollTime = 0L;
        }
        super.animation(class141Var);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) throws MatchException {
        if (z) {
            return false;
        }
        InputEvent class691VarInputEvent = class688Var.inputEvent();
        if (class691VarInputEvent instanceof CursorMoveInput) {
            try {
                ((CursorMoveInput) class691VarInputEvent).mousePosition();
                this.hoverAnimator.state(!this.dragging && class688Var.inArea(this.thumbBounds.x(), this.thumbBounds.y(), this.thumbBounds.width(), this.thumbBounds.height()));
                if (!this.dragging) {
                    return false;
                }
                dragTo(class688Var.logicalMousePosition().y());
                return true;
            } catch (Throwable th) {
                throw new MatchException(th.toString(), th);
            }
        }
        if (class691VarInputEvent instanceof ScrollInput) {
            this.visibilityAnimator.state(true);
            this.lastScrollTime = System.currentTimeMillis();
            return false;
        }
        if (!(class691VarInputEvent instanceof MouseButtonInput)) {
            return false;
        }
        MouseButtonInput class693Var = (MouseButtonInput) class691VarInputEvent;
        if (class693Var.button() != 0) {
            return false;
        }
        if (floatOrZero(this.contentSizeSupplier.get()) <= floatOrZero(this.viewportSizeSupplier.get()) + 0.5f) {
            this.dragging = false;
            this.visibilityAnimator.state(false);
            return false;
        }
        MouseButtonAction class706VarAction = class693Var.action();
        boolean zInArea = class688Var.inArea(this.thumbBounds.x(), this.thumbBounds.y(), this.thumbBounds.width(), this.thumbBounds.height());
        boolean zInArea2 = class688Var.inArea(this.trackBounds.x(), this.trackBounds.y(), this.trackBounds.width(), this.trackBounds.height());
        if (class706VarAction.press()) {
            if (zInArea) {
                this.dragging = true;
                this.visibilityAnimator.state(true);
                this.lastScrollTime = 0L;
                this.dragOffset = class688Var.logicalMousePosition().y() - this.thumbBounds.y();
                return true;
            }
            if (zInArea2) {
                applyScrollPosition(class688Var.logicalMousePosition().y() - (this.thumbBounds.height() / 2.0f));
                return true;
            }
        }
        if (!class706VarAction.release() || !this.dragging) {
            return false;
        }
        this.dragging = false;
        this.visibilityAnimator.state(false);
        return true;
    }

    public void updateThumbBounds() {
        float fMethod002 = floatOrZero(this.contentSizeSupplier.get());
        float fMethod003 = floatOrZero(this.viewportSizeSupplier.get());
        float fHeight = this.trackBounds.height();
        if (fHeight <= 0.0f) {
            this.thumbBounds.withPosition(this.trackBounds.x(), this.trackBounds.y()).withSize(this.trackBounds.width(), 0.0f);
            return;
        }
        if (fMethod002 <= fMethod003 + 0.5f) {
            this.thumbBounds.withPosition(this.trackBounds.x(), this.trackBounds.y()).withSize(this.trackBounds.width(), fHeight);
            return;
        }
        float fMin = Math.min(Math.max(this.minThumbSize, (fHeight * fMethod003) / fMethod002), fHeight);
        float fClamp = MathUtil.clamp(floatOrZero(this.scrollOffsetSupplier.get()) / Math.max(0.001f, fMethod002 - fMethod003), 0.0f, 1.0f);
        this.thumbBounds.withPosition(this.trackBounds.x(), this.trackBounds.y() + ((fHeight - fMin) * fClamp)).withSize(this.trackBounds.width(), fMin);
    }

    public void dragTo(double d) {
        applyScrollPosition(((float) d) - this.dragOffset);
    }

    public void applyScrollPosition(float f) {
        float fMethod002 = floatOrZero(this.contentSizeSupplier.get());
        float fMethod003 = floatOrZero(this.viewportSizeSupplier.get());
        float fHeight = this.trackBounds.height();
        if (fMethod002 <= fMethod003 + 0.5f || fHeight <= 0.0f) {
            return;
        }
        float fMax = Math.max(0.001f, fHeight - this.thumbBounds.height());
        this.scrollConsumer.accept(Float.valueOf(((MathUtil.clamp(f, this.trackBounds.y(), this.trackBounds.y() + fMax) - this.trackBounds.y()) / fMax) * Math.max(0.0f, fMethod002 - fMethod003)));
        updateThumbBounds();
    }

    public float floatOrZero(Float f) {
        if (f == null) {
            return 0.0f;
        }
        return f.floatValue();
    }
}
