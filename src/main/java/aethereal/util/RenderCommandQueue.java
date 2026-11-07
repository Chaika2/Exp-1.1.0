package aethereal.util;
import aethereal.render.DrawCtx;
import aethereal.ui.WidgetParent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class RenderCommandQueue {
    public final List<Consumer<DrawCtx>> commands = new ArrayList();

    public void record(Consumer<DrawCtx> consumer) {
        if (consumer == null) {
            return;
        }
        this.commands.add(consumer);
    }

    public void recordElement(WidgetParent class679Var) {
        if (class679Var == null) {
            return;
        }
        Objects.requireNonNull(class679Var);
        record(class679Var::render);
    }

    public void renderRecorded(DrawCtx class699Var) {
        Iterator<Consumer<DrawCtx>> it = this.commands.iterator();
        while (it.hasNext()) {
            it.next().accept(class699Var);
        }
        this.commands.clear();
    }

    public void clear() {
        this.commands.clear();
    }

    public boolean isEmpty() {
        return this.commands.isEmpty();
    }
}
