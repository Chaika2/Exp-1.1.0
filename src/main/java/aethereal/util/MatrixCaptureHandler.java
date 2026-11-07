package aethereal.util;
import aethereal.event.ClientListener;
import aethereal.Expensive;
import aethereal.event.WorldRenderEvent;

import org.joml.Matrix4f;

public class MatrixCaptureHandler implements ClientListener {
    public MatrixCaptureHandler() {
        Expensive.INSTANCE.eventDispatcher().register(WorldRenderEvent.class, class016Var -> {
            if (class016Var.modelViewMatrix() != null) {
                ProjectionUtil.LAST_MODEL_VIEW.set(class016Var.modelViewMatrix());
            }
            if (class016Var.projectionMatrix() != null) {
                ProjectionUtil.LAST_PROJECTION.set(new Matrix4f(class016Var.projectionMatrix()));
            }
        });
    }
}
