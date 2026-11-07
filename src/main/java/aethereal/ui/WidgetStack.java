package aethereal.ui;
import aethereal.render.AnimationStack2;
import aethereal.type.ButtonAction;
import aethereal.math.DeltaTimeTracker;
import aethereal.model.DragRenderContext;
import aethereal.util.DrawEngine;
import aethereal.Expensive;
import aethereal.type.Mc;
import aethereal.event.MouseButtonEvent2;
import aethereal.model.MouseButtonInput2;
import aethereal.model.MouseMoveInput;
import aethereal.render.ScreenResolution;
import aethereal.render.ThemeManager;
import aethereal.util.SnapManager;
import aethereal.util.WeightedEngine;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;

public class WidgetStack {
    public ScreenResolution resolution;
    public final List<Draggable> widgets = new ArrayList();
    public final DeltaTimeTracker deltaTimeTracker = new DeltaTimeTracker();
    public final AnimationStack2 animationStack = new AnimationStack2();
    public final SnapManager snapManager = new SnapManager();

    public Draggable activeWidget = null;

    public void draw() {
        ThemeManager.INSTANCE.sync();
        Mc class815Var = Mc.INSTANCE;
        ScreenResolution class710VarResolution = ScreenResolution.resolution();
        float fMax = Math.max(Expensive.INSTANCE.windowController().dpiScaleFactor(), 0.01f);
        this.resolution = new ScreenResolution(Math.max(1, Math.round(class710VarResolution.screenWidth() / fMax)), Math.max(1, Math.round(class710VarResolution.screenHeight() / fMax)));
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.scale(fMax, fMax, 1.0f);
        WeightedEngine class141Var = new WeightedEngine(this.deltaTimeTracker.elapsedUnit(), this.animationStack);
        Mouse mouse = class815Var.getMouse();
        for (Draggable class806Var : this.widgets) {
            class806Var.setSnapGrid(this.snapManager);
            class806Var.setSiblingWidgets(this.widgets);
        }
        if (class815Var.getCurrentScreen() instanceof ChatScreen) {
            boolean z = false;
            for (int size = this.widgets.size() - 1; size >= 0; size--) {
                if (this.widgets.get(size).onCursor(new MouseMoveInput(z, mouse, this.resolution, fMax))) {
                    z = true;
                }
            }
        } else {
            this.widgets.forEach((v0) -> {
                v0.resetCursor();
            });
            this.snapManager.clear();
        }
        boolean zMethod005 = isAnyDragging();
        this.snapManager.animate(class141Var, zMethod005);
        this.animationStack.begin();
        this.widgets.forEach(class806Var2 -> {
            class806Var2.animation(class141Var);
        });
        this.animationStack.end();
        DrawEngine class154VarDrawEngine = Expensive.INSTANCE.drawEngine();
        DragRenderContext class809Var = new DragRenderContext(this.resolution, mouse, matrixStack, class154VarDrawEngine, Expensive.INSTANCE.theme(), fMax);
        class154VarDrawEngine.begin();
        this.widgets.forEach(class806Var3 -> {
            class806Var3.layout(class809Var);
            class806Var3.draw(class809Var);
        });
        this.widgets.forEach(class806Var4 -> {
            if (class806Var4.hasTooltip()) {
                class806Var4.drawTooltip(class809Var);
            }
            class806Var4.updateDrag(class809Var);
        });
        if (zMethod005) {
            this.snapManager.render(class809Var);
        }
        class154VarDrawEngine.end();
    }

    public void click(MouseButtonEvent2 class300Var) {
        Mc class815Var = Mc.INSTANCE;
        if (class815Var.getCurrentScreen() instanceof ChatScreen) {
            Mouse mouse = class815Var.getMouse();
            boolean z = false;
            for (int size = this.widgets.size() - 1; size >= 0; size--) {
                Draggable class806Var = this.widgets.get(size);
                if (class806Var.onClick(new MouseButtonInput2(z, mouse, class300Var.action(), this.resolution, class300Var.button(), Math.max(Expensive.INSTANCE.windowController().dpiScaleFactor(), 0.01f)))) {
                    if (!z) {
                        markActive(class806Var);
                    }
                    z = true;
                }
            }
            if (class300Var.action() == ButtonAction.RELEASE) {
                this.snapManager.clear();
            }
        }
    }

    public boolean isAnyDragging() {
        for (Draggable class806Var : this.widgets) {
            if (class806Var.isDragging() && class806Var.isVisible()) {
                return true;
            }
        }
        return false;
    }

    public void handleResize(int i, int i2) {
        this.widgets.forEach(class806Var -> {
            class806Var.handleResize(i, i2);
        });
    }

    public void update() {
        this.widgets.forEach((v0) -> {
            v0.updateWidget();
        });
    }

    public void markActive(Draggable class806Var) {
        if (class806Var == this.activeWidget || class806Var == null) {
            return;
        }
        this.activeWidget = class806Var;
        if (this.widgets.size() > 1) {
            bringToFront(class806Var);
        }
    }

    public void newWidget(Draggable class806Var) {
        if (this.widgets.contains(class806Var)) {
            return;
        }
        this.widgets.add(class806Var);
        class806Var.setSnapGrid(this.snapManager);
        class806Var.setSiblingWidgets(this.widgets);
    }

    public void bringToFront(Draggable class806Var) {
        ArrayList arrayList = new ArrayList(this.widgets);
        int size = this.widgets.size() - 1;
        int iIndexOf = this.widgets.indexOf(class806Var);
        if (iIndexOf == size) {
            return;
        }
        int i = 0;
        for (int i2 = size; i2 >= 0; i2--) {
            if (i2 == iIndexOf) {
                i = 1;
            } else {
                this.widgets.set((i2 - 1) + i, (Draggable) arrayList.get(i2));
            }
        }
        this.widgets.set(size, class806Var);
    }

    public List<Draggable> widgets() {
        return this.widgets;
    }
}
