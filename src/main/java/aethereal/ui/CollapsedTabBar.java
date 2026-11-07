package aethereal.ui;
import aethereal.render.AnimatedFloat;
import aethereal.resource.ClasspathResource;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.render.GlTexture;
import aethereal.model.InputEventContext;
import aethereal.model.LayoutScaleContext;
import aethereal.util.RenderCommandQueue;
import aethereal.util.WeightedEngine;

import java.util.Iterator;
import java.util.List;

public class CollapsedTabBar extends WidgetContainer {
    final AnimatedFloat indicatorPosition = new AnimatedFloat(100, Easings.EASE_IN_OUT_CUBIC);
    final GlTexture triangleIcon = new GlTexture(new ClasspathResource("/icons/menu/new/triangle.png"));
    final List<MenuTabElement> tabElements = Expensive.INSTANCE.tabsController().tabElements();

    @Override
    public void render(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.colorStack();
        class699Var.fillOutlinedRoundedRect(x(), y(), width(), height(), 20.0f, 2.5f, class115VarColorStack.computeColor(class699Var.theme().palette().surfaceOutline().tone(600).argb()), class115VarColorStack.computeColor(class699Var.theme().palette().surfaceOutline().tone(600).argb(), 0));
        Iterator<MenuTabElement> it = this.tabElements.iterator();
        while (it.hasNext()) {
            it.next().drawCollapsed(class699Var);
        }
        class699Var.fillRect(x() + 2.0f + this.indicatorPosition.animatedValue(), (y() + height()) - 1.0f, 10.0f, 1.0f, class115VarColorStack.computeColor(class699Var.theme().palette().accent().argb()));
        super.render(class699Var);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        Iterator<MenuTabElement> it = this.tabElements.iterator();
        while (it.hasNext()) {
            if (it.next().handleInput(class688Var, z)) {
                return true;
            }
        }
        return super.handleInput(class688Var, z);
    }

    @Override
    public void onMenuDrag(boolean z) {
        this.tabElements.forEach(class732Var -> {
            class732Var.onMenuDrag(z);
        });
        super.onMenuDrag(z);
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        setSize(270.0f, 36.0f);
        setPosition((this.parent.x() + (this.parent.width() / 2.0f)) - (width() / 2.0f), (this.parent.y() + (parent().height() / 2.0f)) - (height() / 2.0f));
        float fX = x() + 20.0f;
        for (MenuTabElement class732Var : this.tabElements) {
            class732Var.layout(class698Var);
            class732Var.setPosition(fX, (y() + (height() / 2.0f)) - (class732Var.collapsedHeight() / 2.0f));
            fX += class732Var.collapsedWidth() + 22.0f;
        }
        this.indicatorPosition.destination(Expensive.INSTANCE.tabsController().current().x() - x());
        super.layout(class698Var);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        Iterator<MenuTabElement> it = this.tabElements.iterator();
        while (it.hasNext()) {
            it.next().animation(class141Var);
        }
        this.indicatorPosition.animate(class141Var);
        super.animation(class141Var);
    }

    @Override
    public void handleClose() {
        this.tabElements.forEach((v0) -> {
            v0.handleClose();
        });
        super.handleClose();
    }

    @Override
    public void collectBloomElements(RenderCommandQueue class676Var) {
        this.tabElements.forEach(class732Var -> {
            class732Var.collectBloomElements(class676Var);
        });
        super.collectBloomElements(class676Var);
    }
}
