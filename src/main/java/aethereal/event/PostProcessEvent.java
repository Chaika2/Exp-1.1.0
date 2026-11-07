package aethereal.event;
import aethereal.render.DrawCtx;
import aethereal.util.OverlayCommandQueue;
import aethereal.util.RenderCommandQueue;

import java.util.function.Consumer;

public class PostProcessEvent implements Event {
    public final OverlayCommandQueue blurQueue;

    public final RenderCommandQueue bloomQueue;

    public final DrawCtx context;

    public void addBlur(Consumer<DrawCtx> consumer) {
        this.blurQueue.record(consumer);
    }

    public void addBloom(Consumer<DrawCtx> consumer) {
        this.bloomQueue.record(consumer);
    }

    public DrawCtx context() {
        return this.context;
    }

    public PostProcessEvent(OverlayCommandQueue class677Var, RenderCommandQueue class676Var, DrawCtx class699Var) {
        this.blurQueue = class677Var;
        this.bloomQueue = class676Var;
        this.context = class699Var;
    }
}
