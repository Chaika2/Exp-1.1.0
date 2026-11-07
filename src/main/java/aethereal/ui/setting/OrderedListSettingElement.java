package aethereal.ui.setting;
import aethereal.resource.ClasspathResource;
import aethereal.render.ColorStack;
import aethereal.model.CursorMoveInput;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.render.HighlightAnimation;
import aethereal.event.InputEvent;
import aethereal.model.InputEventContext;
import aethereal.model.LayoutScaleContext;
import aethereal.ui.ModuleFrame;
import aethereal.type.MouseButtonAction;
import aethereal.model.MouseButtonInput;
import aethereal.render.MsdfFont;
import aethereal.ui.OrderedListRow;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.ui.ToggleSwitch;
import aethereal.model.Translation;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.lang.Enum;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class OrderedListSettingElement<E extends Enum<E>> extends ModuleFrame {
    public static final float C = 3.0f;
    public static final float D = 10.0f;
    public static final float E = 20.0f;
    public static final float F = 6.0f;
    public static final float G = 4.0f;
    public static final float H = 12.0f;
    public static final float I = 4.0f;
    public static final float J = 22.0f;
    public static final float K = 15.0f;
    public static final int fontSize = 12;
    public final Supplier<List<E>> orderSupplier;
    public final Supplier<Set<E>> selectedSupplier;
    public final Function<E, Translation> nameFunction;

    public final Consumer<E> toggleConsumer;
    public final BiConsumer<Integer, Integer> moveConsumer;
    public Supplier<Boolean> visibilitySupplier;
    public boolean dragging;

    public E draggedValue;
    public float dragOffset;
    public float L;
    public final MsdfFont font = Fonts.INTER_BOLD.get();
    public final GlTexture grabTexture = new GlTexture(new ClasspathResource("/icons/menu/new/grab.png"));
    public final HighlightAnimation highlightAnimation = new HighlightAnimation(300, Easings.EASE_IN_OUT_CUBIC);
    public final Map<E, OrderedListRow<E>> rowCache = new HashMap();
    public final List<OrderedListRow<E>> rows = new ArrayList();
    public final List<E> currentOrder = new ArrayList();
    public final WidgetBounds bounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
    public int draggedIndex = -1;

    public OrderedListSettingElement(Supplier<List<E>> supplier, Supplier<Set<E>> supplier2, Function<E, Translation> function, Consumer<E> consumer, BiConsumer<Integer, Integer> biConsumer) {
        this.orderSupplier = supplier;
        this.selectedSupplier = supplier2;
        this.nameFunction = function;
        this.toggleConsumer = consumer;
        this.moveConsumer = biConsumer;
    }

    @Override
    public void highlight() {
        this.highlightAnimation.trigger();
    }

    @Override
    public void clearHighlight() {
        this.highlightAnimation.clearHighlight();
    }

    @Override
    public void draw(DrawCtx class699Var, float f, float f2) {
        rebuildRows();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        float fY = y();
        float fMethod001 = computeHeight();
        this.bounds.withPosition(x(), fY).withSize(f, fMethod001);
        for (int i = 0; i < this.rows.size(); i++) {
            OrderedListRow<E> class832Var = this.rows.get(i);
            float f3 = fY + (i * 26.0f);
            float fClamp = f3;
            if (this.dragging && class832Var.value() == this.draggedValue) {
                fClamp = clamp(this.L, fY, (fY + fMethod001) - E);
            }
            class832Var.rowRect().withPosition(x(), f3).withSize(f, E);
            class832Var.grabRect().withPosition(x(), f3).withSize(24.0f, E);
            float fX = x();
            class699Var.texture(this.grabTexture, fX, (fClamp + D) - F, H, H, class115VarColorStack.computeColor(ThemePalette.grey.argb()));
            class699Var.text(this.font, this.nameFunction.apply(class832Var.value()).effective(), fontSize, fX + H + 4.0f, (fClamp + D) - (this.font.getHeight(H) / 2.0f), class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(600).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(200).argb()), class832Var.toggleAnimation()));
            float fX2 = (x() + f) - J;
            float f4 = (fClamp + D) - 7.5f;
            class832Var.toggleSwitch().setPosition(fX2, f4);
            class832Var.toggleSwitch().setClickableArea(x(), f4, f, K);
            class832Var.toggleSwitch().render(class699Var);
        }
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        boolean zHandleInput = super.handleInput(class688Var, z);
        if (zHandleInput || !visible()) {
            return zHandleInput;
        }
        InputEvent class691VarInputEvent = class688Var.inputEvent();
        if (class691VarInputEvent instanceof MouseButtonInput) {
            MouseButtonInput class693Var = (MouseButtonInput) class691VarInputEvent;
            if (class693Var.button() != 0) {
                return false;
            }
            MouseButtonAction class706VarAction = class693Var.action();
            if (class706VarAction.press() && !z) {
                for (int i = 0; i < this.rows.size(); i++) {
                    OrderedListRow<E> class832Var = this.rows.get(i);
                    WidgetBounds class678VarGrabRect = class832Var.grabRect();
                    if (class688Var.inArea(class678VarGrabRect.x(), class678VarGrabRect.y(), class678VarGrabRect.width(), class678VarGrabRect.height())) {
                        startDrag(class688Var.logicalMousePosition().y(), i, class832Var.value());
                        return true;
                    }
                }
            }
            if (class706VarAction.release() && this.dragging) {
                endDrag();
                return true;
            }
        }
        if ((class688Var.inputEvent() instanceof CursorMoveInput) && this.dragging) {
            updateDrag(class688Var.logicalMousePosition().y());
            return true;
        }
        if (this.dragging) {
            return false;
        }
        boolean z2 = z;
        Iterator<OrderedListRow<E>> it = this.rows.iterator();
        while (it.hasNext()) {
            if (it.next().toggleSwitch().handleInput(class688Var, z2)) {
                z2 = true;
            }
        }
        return z2;
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        if (this.visibilitySupplier != null) {
            visible(this.visibilitySupplier.get().booleanValue());
        }
        if (visible()) {
            rebuildRows();
            for (OrderedListRow<E> class832Var : this.rows) {
                class832Var.toggleSwitch().animation(class141Var);
                class832Var.toggleAnimation().state(this.selectedSupplier.get().contains(class832Var.value()));
                class832Var.toggleAnimation().animate(class141Var);
            }
            this.highlightAnimation.animate(class141Var);
            super.animation(class141Var);
        }
    }

    @Override
    public void handleClose() {
        endDrag();
        super.handleClose();
    }

    @Override
    public float height() {
        rebuildRows();
        return computeHeight();
    }

    public void rebuildRows() {
        List<E> list = this.orderSupplier.get();
        if (list == null) {
            return;
        }
        if (!this.currentOrder.equals(list)) {
            this.currentOrder.clear();
            this.currentOrder.addAll(list);
        }
        this.rowCache.keySet().retainAll(new HashSet(this.currentOrder));
        this.rows.clear();
        Iterator<E> it = this.currentOrder.iterator();
        while (it.hasNext()) {
            this.rows.add(this.rowCache.computeIfAbsent(it.next(), this::createRow));
        }
    }

    public OrderedListRow<E> createRow(E e) {
        ToggleSwitch class754Var = new ToggleSwitch(J, K, () -> {
            return Boolean.valueOf(this.selectedSupplier.get().contains(e));
        });
        ToggleAnimator class323Var = new ToggleAnimator(220, Easings.EASE_IN_OUT_CUBIC);
        class323Var.force(this.selectedSupplier.get().contains(e));
        OrderedListRow<E> class832Var = new OrderedListRow<>(e, class754Var, class323Var);
        class754Var.runnable(() -> {
            boolean z = !this.selectedSupplier.get().contains(e);
            this.toggleConsumer.accept(e);
            class832Var.toggleAnimation().state(z);
        });
        return class832Var;
    }

    public float computeHeight() {
        int size = this.rows.size();
        if (size == 0) {
            return 0.0f;
        }
        return (E * size) + (F * Math.max(0, size - 1));
    }

    public void startDrag(float f, int i, E e) {
        this.dragging = true;
        this.draggedIndex = i;
        this.draggedValue = e;
        this.dragOffset = f - (this.bounds.y() + (i * 26.0f));
        this.L = f - this.dragOffset;
    }

    public void updateDrag(float f) {
        float fY = this.bounds.y();
        float fY2 = (this.bounds.y() + this.bounds.height()) - E;
        if (fY2 < fY) {
            fY2 = fY;
        }
        this.L = clamp(f - this.dragOffset, fY, fY2);
        int iMethod006 = indexFromY(f);
        if (iMethod006 == this.draggedIndex || iMethod006 < 0 || iMethod006 >= this.rows.size()) {
            return;
        }
        moveRow(this.draggedIndex, iMethod006);
        this.moveConsumer.accept(Integer.valueOf(this.draggedIndex), Integer.valueOf(iMethod006));
        this.draggedIndex = iMethod006;
    }

    public int indexFromY(float f) {
        float fY = f - this.bounds.y();
        if (fY <= 0.0f) {
            return 0;
        }
        return Math.max(0, Math.min(this.rows.size() - 1, (int) Math.floor(fY / 26.0f)));
    }

    public void moveRow(int i, int i2) {
        if (i == i2 || i < 0 || i2 < 0 || i >= this.rows.size() || i2 >= this.rows.size()) {
            return;
        }
        this.rows.add(i2, this.rows.remove(i));
        this.currentOrder.add(i2, this.currentOrder.remove(i));
    }

    public void endDrag() {
        this.dragging = false;
        this.draggedIndex = -1;
        this.draggedValue = null;
    }

    public float clamp(float f, float f2, float f3) {
        return Math.max(f2, Math.min(f3, f));
    }

    public OrderedListSettingElement<E> visibleSupplier(Supplier<Boolean> supplier) {
        this.visibilitySupplier = supplier;
        return this;
    }
}
