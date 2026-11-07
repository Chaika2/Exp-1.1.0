package aethereal.ui;
import aethereal.render.ColorStack;
import aethereal.model.CursorMoveInput;
import aethereal.render.DrawCtx;
import aethereal.model.InputEventContext;
import aethereal.model.LayoutScaleContext;
import aethereal.model.MouseButtonInput;
import aethereal.util.WeightedEngine;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class ModuleGridLayout extends AbstractTabLayout {
    public static final float as = 50.0f;
    public static final float at = 16.0f;
    public static final float au = 16.0f;
    public static final float av = 15.0f;
    public static final float aw = 14.0f;
    ScrollbarWidget scrollbar;

    @Override
    public void initialize(MenuTabElement class732Var) {
        super.initialize(class732Var);
        ScrollArea class789VarScrollingAreaComponent = class732Var.scrollingAreaComponent();
        Objects.requireNonNull(class789VarScrollingAreaComponent);
        Supplier supplier = class789VarScrollingAreaComponent::scrollY;
        Supplier supplier2 = this::getContentHeight;
        Supplier supplier3 = () -> {
            return Float.valueOf(MenuWindow.MENU_HEIGHT - MenuWindow.COLLAPSED_HEADER_HEIGHT);
        };
        ScrollArea class789VarScrollingAreaComponent2 = class732Var.scrollingAreaComponent();
        Objects.requireNonNull(class789VarScrollingAreaComponent2);
        this.scrollbar = new ScrollbarWidget(supplier, supplier2, supplier3, (v1) -> {
            class789VarScrollingAreaComponent.scrollTo(v1);
        }, 16.0f, 3.0f, 24.0f);
    }

    @Override
    public void render(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        class115VarColorStack.push();
        class115VarColorStack.alphaAnimation(this.tab.currentTabAnimation());
        this.scrollbar.render(class699Var);
        this.tab.scrollingAreaComponent().beginArea(class699Var, originX(), originY(), width(), height());
        renderFrames(class699Var);
        this.tab.scrollingAreaComponent().endArea(class699Var, getContentHeight());
        renderOverlays(class699Var);
        class115VarColorStack.pop();
    }

    public void renderFrames(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        for (int size = this.tab.frames().size() - 1; size >= 0; size--) {
            AbstractFrame class757Var = this.tab.frames().get(size);
            if (class757Var.visible() && isFrameVisible(class757Var, as, originY())) {
                float dimmingForFrame = this.tab.dimmingManager().getDimmingForFrame(class757Var);
                if (dimmingForFrame > 0.01f) {
                    class115VarColorStack.push();
                    class115VarColorStack.alpha(1.0f - (dimmingForFrame * 0.5f));
                }
                class757Var.render(class699Var);
                if (dimmingForFrame > 0.01f) {
                    class115VarColorStack.pop();
                }
            }
        }
    }

    @Override
    public void renderOverlays(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        this.tab.scrollingAreaComponent().scrollY();
        class115VarColorStack.push();
        class699Var.matrixStack().push();
        class699Var.matrixStack().translate(0.0f, -class699Var.layoutContext().toPhysical(this.tab.scrollingAreaComponent().scrollY()), 0.0f);
        for (int size = this.tab.frames().size() - 1; size >= 0; size--) {
            AbstractFrame class757Var = this.tab.frames().get(size);
            if (class757Var.visible() && isFrameVisible(class757Var, as, originY())) {
                float dimmingForFrame = this.tab.dimmingManager().getDimmingForFrame(class757Var);
                if (dimmingForFrame > 0.01f) {
                    class115VarColorStack.push();
                    class115VarColorStack.alpha(1.0f - (dimmingForFrame * 0.5f));
                }
                class757Var.renderOverlays(class699Var);
                if (dimmingForFrame > 0.01f) {
                    class115VarColorStack.pop();
                }
            }
        }
        class699Var.matrixStack().pop();
        class115VarColorStack.pop();
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        this.tab.frames().forEach(class757Var -> {
            class757Var.layout(class698Var);
        });
        layoutScrollbar(class698Var);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.scrollbar.animation(class141Var);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        boolean zHandleInput = z | this.scrollbar.handleInput(class688Var, z);
        boolean z2 = ((((float) class688Var.logicalMousePosition().y()) > this.tab.framesOriginY() ? 1 : (((float) class688Var.logicalMousePosition().y()) == this.tab.framesOriginY() ? 0 : -1)) < 0) && ((class688Var.inputEvent() instanceof MouseButtonInput) || (class688Var.inputEvent() instanceof CursorMoveInput));
        InputEventContext class688VarWithMouseOffset = class688Var.withMouseOffset(0.0f, this.tab.scrollingAreaComponent().scrollY());
        for (AbstractFrame class757Var : this.tab.frames()) {
            if (class757Var.visible() && isFrameVisible(class757Var, as, originY())) {
                if (class757Var.handleInput(class688VarWithMouseOffset, z2 || zHandleInput) && !z2) {
                    zHandleInput = true;
                }
            }
        }
        return zHandleInput | this.tab.scrollingAreaComponent().handleInput(class688Var, zHandleInput);
    }

    @Override
    public float getContentHeight() {
        float f = 0.0f;
        for (AbstractFrame class757Var : this.tab.frames()) {
            if (class757Var.visible()) {
                float fY = (class757Var.y() - this.tab.framesOriginY()) + class757Var.height() + class757Var.contentHeight();
                if (fY > f) {
                    f = fY;
                }
            }
        }
        return f + 10.0f;
    }

    @Override
    public void positionFrames() {
        List<AbstractFrame> listFrames = this.tab.frames();
        listFrames.sort(Comparator.comparing(class757Var -> {
            if (class757Var instanceof ModuleCard) {
                return Boolean.valueOf(!((ModuleCard) class757Var).favorite());
            }
            return false;
        }).thenComparing(class757Var2 -> {
            return class757Var2 instanceof ModuleCard ? ((ModuleCard) class757Var2).name() : "";
        }, String.CASE_INSENSITIVE_ORDER));
        float fFloatValue = ((Float) listFrames.stream().filter((v0) -> {
            return v0.visible();
        }).map((v0) -> {
            return v0.width();
        }).max((v0, v1) -> {
            return Float.compare(v0, v1);
        }).orElse(Float.valueOf(0.0f))).floatValue();
        int iMax = fFloatValue > 0.0f ? Math.max(1, (int) Math.floor((((width() - 32.0f) - this.scrollbar.width()) + av) / (fFloatValue + av))) : 1;
        float[] fArr = new float[iMax];
        float[] fArr2 = new float[iMax];
        for (int i = 0; i < iMax; i++) {
            fArr2[i] = i * (fFloatValue + av);
        }
        for (AbstractFrame class757Var3 : listFrames) {
            if (class757Var3.visible()) {
                int iIndexOfMin = indexOfMin(fArr);
                float f = fArr2[iIndexOfMin];
                float f2 = fArr[iIndexOfMin];
                if (class757Var3 instanceof ModuleCard) {
                    ((ModuleCard) class757Var3).targetPosition(originX() + 16.0f + f, originY() + 16.0f + f2, !this.tab.forceImmediateFramePositioning());
                } else {
                    class757Var3.setPosition(originX() + 16.0f + f, originY() + 16.0f + f2);
                }
                fArr[iIndexOfMin] = fArr[iIndexOfMin] + class757Var3.height() + class757Var3.contentHeight() + aw;
            }
        }
    }

    @Override
    public float width() {
        return MenuWindow.MENU_WIDTH;
    }

    public void layoutScrollbar(LayoutScaleContext class698Var) {
        float f = MenuWindow.MENU_HEIGHT - MenuWindow.COLLAPSED_HEADER_HEIGHT;
        this.scrollbar.setPosition(((originX() + width()) - 16.0f) - this.scrollbar.width(), originY());
        this.scrollbar.setSize(this.scrollbar.width(), f);
        this.scrollbar.layout(class698Var);
    }
}
