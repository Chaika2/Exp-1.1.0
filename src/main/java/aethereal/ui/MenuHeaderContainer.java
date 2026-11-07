package aethereal.ui;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.util.DrawEngine;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.event.InputEvent;
import aethereal.model.InputEventContext;
import aethereal.model.KeyInput;
import aethereal.model.LayoutScaleContext;
import aethereal.util.MathUtil;
import aethereal.util.OverlayCommandQueue;
import aethereal.util.RenderCommandQueue;
import aethereal.render.ToggleAnimator;
import aethereal.util.WeightedEngine;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.joml.Vector4f;

public class MenuHeaderContainer extends WidgetContainer {
    final ExpandedHeaderBar expandedBar = new ExpandedHeaderBar(this::exchange);

    final CollapsedHeaderBar collapsedBar = new CollapsedHeaderBar(this::exchange);
    final ToggleAnimator transitionAnimation = ToggleAnimator.times(3, 70, Easings.EASE_IN_OUT_CUBIC);
    final Set<Integer> pressedKeys = new HashSet();

    @Override
    public void render(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.colorStack();
        ChatPanel class780VarChat = Expensive.INSTANCE.menuWindow().chat();
        class699Var.fillRoundedRect(x(), y(), width(), height(), new Vector4f(8.0f, 0.0f, 8.0f, 0.0f), class115VarColorStack.computeColor(16777215, 0.01f));
        if (this.transitionAnimation.smoothAnimation() >= 1.0f) {
            DrawEngine class154VarDrawEngine = class699Var.drawEngine();
            class699Var.roundedBlur(Expensive.INSTANCE.windowController().headerBlur().getBlurFramebuffer().getColorAttachment(), x(), y(), width(), height(), new Vector4f(class780VarChat.isOpened() ? 0.0f : 8.0f, 0.0f, 8.0f, 0.0f), class154VarDrawEngine.colorStack().white());
            class699Var.fillRoundedRect(x(), y(), width(), height(), new Vector4f(class780VarChat.isOpened() ? 0.0f : 8.0f, 0.0f, 8.0f, 0.0f), class154VarDrawEngine.colorStack().computeColor(class699Var.theme().palette().surfaceBackground().tone(900).argb(), 0.8f));
        }
        class115VarColorStack.push();
        class115VarColorStack.alpha(computeContentAlpha());
        if (this.transitionAnimation.smoothAnimation() >= 1.0f) {
            class699Var.fillRoundedRect(x(), (y() + MenuWindow.EXPANDED_HEADER_HEIGHT) - 1.0f, width(), 296.0f, new Vector4f(0.0f, 8.0f, 0.0f, 8.0f), class699Var.drawEngine().colorStack().computeColor(1052947, 0.95f));
        }
        currentHeaderState().render(class699Var);
        class115VarColorStack.pop();
        class699Var.fillRect(x(), (y() + height()) - 1.0f, width(), 1.0f, class115VarColorStack.computeColor(class699Var.theme().palette().surfaceBackground().tone(700).argb()));
        super.render(class699Var);
    }

    public void exchange() {
        this.transitionAnimation.invert();
        this.pressedKeys.clear();
        if (currentHeaderState() == this.expandedBar) {
            this.expandedBar.resetHotkeysState();
        }
    }

    @Override
    public void onMenuDrag(boolean z) {
        currentHeaderState().onMenuDrag(z);
        super.onMenuDrag(z);
    }

    public boolean expandedState() {
        return currentHeaderState() == this.expandedBar;
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        WidgetContainer class683VarCurrentHeaderState = currentHeaderState();
        boolean zHandleInput = (class683VarCurrentHeaderState == this.expandedBar || (class688Var.inputEvent() instanceof KeyInput)) ? z | this.expandedBar.handleInput(class688Var, z) : z | class683VarCurrentHeaderState.handleInput(class688Var, z);
        if (!zHandleInput) {
            InputEvent class691VarInputEvent = class688Var.inputEvent();
            if (class691VarInputEvent instanceof KeyInput) {
                KeyInput class696Var = (KeyInput) class691VarInputEvent;
                int iKeyCode = class696Var.keyCode();
                boolean zPress = class696Var.keyAction().press();
                boolean zRelease = class696Var.keyAction().release();
                if (zPress) {
                    this.pressedKeys.add(Integer.valueOf(iKeyCode));
                } else if (zRelease) {
                    this.pressedKeys.remove(Integer.valueOf(iKeyCode));
                }
                boolean zMethod001 = isBindingPressed(this.expandedBar.exchangeEditableBinding());
                if (class683VarCurrentHeaderState == this.expandedBar && zPress && (iKeyCode == 256 || zMethod001)) {
                    exchange();
                    return true;
                }
                if (class683VarCurrentHeaderState != this.expandedBar && zPress && zMethod001) {
                    exchange();
                    return true;
                }
            }
        }
        return zHandleInput;
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        setSize(MenuWindow.MENU_WIDTH, computeHeight());
        currentHeaderState().setPosition(x(), y());
        currentHeaderState().layout(class698Var);
        super.layout(class698Var);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.transitionAnimation.animate(class141Var);
        currentHeaderState().animation(class141Var);
        super.animation(class141Var);
    }

    @Override
    public void handleClose() {
        this.pressedKeys.clear();
        currentHeaderState().handleClose();
        super.handleClose();
    }

    @Override
    public void collectBloomElements(RenderCommandQueue class676Var) {
        currentHeaderState().collectBloomElements(class676Var);
        super.collectBloomElements(class676Var);
    }

    @Override
    public void collectBlurElements(OverlayCommandQueue class677Var) {
        if (currentHeaderState() == this.collapsedBar) {
            class677Var.recordElement(currentHeaderState());
        }
        super.collectBlurElements(class677Var);
    }

    public WidgetContainer currentHeaderState() {
        return this.transitionAnimation.smoothAnimation() <= 1.5f ? this.collapsedBar : this.expandedBar;
    }

    public float computeContentAlpha() {
        float fSmoothAnimation = this.transitionAnimation.smoothAnimation();
        if (fSmoothAnimation < 1.0f || fSmoothAnimation > 2.0f) {
            return fSmoothAnimation < 1.0f ? 1.0f - fSmoothAnimation : fSmoothAnimation - 2.0f;
        }
        return 0.0f;
    }

    public float computeHeight() {
        float fSmoothAnimation = this.transitionAnimation.smoothAnimation();
        float f = MenuWindow.COLLAPSED_HEADER_HEIGHT;
        float f2 = MenuWindow.EXPANDED_HEADER_HEIGHT;
        if (fSmoothAnimation <= 1.0f) {
            return f;
        }
        return fSmoothAnimation >= 2.0f ? f2 : MathUtil.interpolate(f, f2, fSmoothAnimation - 1.0f);
    }

    public boolean isBindingPressed(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        return this.pressedKeys.containsAll(list);
    }
}
