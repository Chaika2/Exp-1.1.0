package aethereal.ui;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.model.InputEventContext;
import aethereal.type.Language;
import aethereal.event.LanguageChangeEvent;
import aethereal.model.LayoutScaleContext;
import aethereal.util.OverlayCommandQueue;
import aethereal.render.ToggleAnimator;
import aethereal.util.WeightedEngine;

import java.io.IOException;
import org.joml.Vector4f;

public class MenuContentArea extends WidgetContainer {
    public final ToggleAnimator languageAnimation = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);
    public boolean pendingLanguageChange = false;
    public Language pendingLanguage = null;

    public void requestNextLanguage() {
        Language class313VarCurrent = Expensive.INSTANCE.languages().current();
        Language[] class313VarArrValues = Language.values();
        requestLanguage(class313VarArrValues[(class313VarCurrent.ordinal() + 1) % class313VarArrValues.length]);
    }

    public void requestLanguage(Language class313Var) {
        if (class313Var == null || class313Var == Expensive.INSTANCE.languages().current()) {
            return;
        }
        this.pendingLanguage = class313Var;
        this.pendingLanguageChange = true;
        this.languageAnimation.state(true);
    }

    @Override
    public void render(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        Expensive.INSTANCE.tabsController().current().drawFrames(class699Var);
        int colorAttachment = Expensive.INSTANCE.windowController().headerBlur().getBlurFramebuffer().getColorAttachment();
        class115VarColorStack.push();
        class115VarColorStack.alpha(this.languageAnimation.smoothAnimation());
        class699Var.roundedBlur(colorAttachment, x(), y(), width(), height(), new Vector4f(0.0f, 8.0f, 0.0f, 8.0f), class115VarColorStack.white());
        class699Var.fillRoundedRect(x(), y(), width(), height(), new Vector4f(0.0f, 8.0f, 0.0f, 8.0f), class115VarColorStack.computeColor(class699Var.theme().palette().surfaceBackground().tone(900).argb(), 0.8f));
        class115VarColorStack.pop();
        super.render(class699Var);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.languageAnimation.animate(class141Var);
        if (this.pendingLanguageChange && this.languageAnimation.isOne()) {
            this.pendingLanguageChange = false;
            Expensive.INSTANCE.languages().language(this.pendingLanguage);
            Expensive.INSTANCE.eventDispatcher().dispatch(new LanguageChangeEvent());
            try {
                Expensive.INSTANCE.configManager().menuStateConfig().setLanguage(this.pendingLanguage);
                Expensive.INSTANCE.configManager().saveMenuState();
            } catch (IOException e) {
                Expensive.LOGGER.error("Failed to save menu language state", e);
            }
            Expensive.INSTANCE.tabsController().current().markFramesDirty();
        }
        if (!this.pendingLanguageChange && this.languageAnimation.isOne()) {
            this.languageAnimation.state(false);
        }
        super.animation(class141Var);
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        setSize(MenuWindow.MENU_WIDTH, MenuWindow.MENU_HEIGHT - MenuWindow.COLLAPSED_HEADER_HEIGHT);
        Expensive.INSTANCE.tabsController().current().layout(class698Var, x(), y());
        super.layout(class698Var);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        boolean zHandleInput = z | super.handleInput(class688Var, z);
        return zHandleInput | Expensive.INSTANCE.tabsController().current().handleInputFrames(class688Var, zHandleInput);
    }

    @Override
    public void collectBlurElements(OverlayCommandQueue class677Var) {
        Expensive.INSTANCE.tabsController().current().collectBlurElements(class677Var);
        super.collectBlurElements(class677Var);
    }
}
