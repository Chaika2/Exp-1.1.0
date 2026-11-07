package aethereal.ui;
import aethereal.render.DrawCtx;
import aethereal.model.InputEventContext;
import aethereal.event.LayoutCallback;
import aethereal.model.LayoutScaleContext;
import aethereal.util.OverlayCommandQueue;
import aethereal.util.RenderCommandQueue;
import aethereal.util.WeightedEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class WidgetContainer extends AbstractWidget {
    public final List<Widget> childWidgets = new ArrayList();

    public List<Widget> children() {
        return Collections.unmodifiableList(this.childWidgets);
    }

    public void addChild(Widget class682Var) {
        if (class682Var == null) {
            return;
        }
        if (class682Var.parent() != null) {
            class682Var.parent().removeChild(class682Var);
        }
        class682Var.parent(this);
        this.childWidgets.add(class682Var);
    }

    @Override
    public void onMenuDrag(boolean z) {
        Iterator<Widget> it = this.childWidgets.iterator();
        while (it.hasNext()) {
            it.next().onMenuDrag(z);
        }
    }

    public void addChild(int i, Widget class682Var) {
        if (class682Var == null) {
            return;
        }
        if (class682Var.parent() != null) {
            class682Var.parent().removeChild(class682Var);
        }
        class682Var.parent(this);
        this.childWidgets.add(i, class682Var);
    }

    public void removeChild(Widget class682Var) {
        if (this.childWidgets.remove(class682Var)) {
            class682Var.parent(null);
        }
    }

    public void clearChildren() {
        Iterator<Widget> it = this.childWidgets.iterator();
        while (it.hasNext()) {
            it.next().parent(null);
        }
        this.childWidgets.clear();
    }

    public void center(float f, float f2) {
        setPosition((f - width()) / 2.0f, (f2 - height()) / 2.0f);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        Iterator<Widget> it = this.childWidgets.iterator();
        while (it.hasNext()) {
            it.next().animation(class141Var);
        }
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        Iterator<Widget> it = this.childWidgets.iterator();
        while (it.hasNext()) {
            it.next().layout(class698Var);
        }
    }

    @Override
    public void render(DrawCtx class699Var) {
        for (LayoutCallback class681Var : this.childWidgets) {
            if (class681Var instanceof WidgetParent) {
                ((WidgetParent) class681Var).render(class699Var);
            }
        }
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        if (!visible()) {
            return z;
        }
        boolean zHandleInput = z;
        for (int size = this.childWidgets.size() - 1; size >= 0; size--) {
            Widget class682Var = this.childWidgets.get(size);
            if (class682Var.visible()) {
                zHandleInput |= class682Var.handleInput(class688Var, zHandleInput);
            }
        }
        return zHandleInput | super.handleInput(class688Var, zHandleInput);
    }

    @Override
    public void collectBlurElements(OverlayCommandQueue class677Var) {
        Iterator<Widget> it = this.childWidgets.iterator();
        while (it.hasNext()) {
            it.next().collectBlurElements(class677Var);
        }
    }

    @Override
    public void handleClose() {
        Iterator<Widget> it = this.childWidgets.iterator();
        while (it.hasNext()) {
            it.next().handleClose();
        }
    }

    @Override
    public void collectBloomElements(RenderCommandQueue class676Var) {
        Iterator<Widget> it = this.childWidgets.iterator();
        while (it.hasNext()) {
            it.next().collectBloomElements(class676Var);
        }
    }
}
