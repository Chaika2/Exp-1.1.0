package aethereal.event;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public final class WorldRenderEvent implements Event {
    public final MatrixStack matrixStack;
    public final Matrix4f modelViewMatrix;
    public final Matrix4f projectionMatrix;

    public final RenderTickCounter renderTickCounter;

    public final Frustum frustum;

    public WorldRenderEvent(MatrixStack matrixStack, Matrix4f matrix4f, Matrix4f matrix4f2, RenderTickCounter renderTickCounter, Frustum frustum) {
        this.matrixStack = matrixStack;
        this.modelViewMatrix = matrix4f;
        this.projectionMatrix = matrix4f2;
        this.renderTickCounter = renderTickCounter;
        this.frustum = frustum;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "matrixStack=" + this.matrixStack + ", " + "modelViewMatrix=" + this.modelViewMatrix + ", " + "projectionMatrix=" + this.projectionMatrix + ", " + "renderTickCounter=" + this.renderTickCounter + ", " + "frustum=" + this.frustum + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.matrixStack, this.modelViewMatrix, this.projectionMatrix, this.renderTickCounter, this.frustum);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WorldRenderEvent)) return false;
        WorldRenderEvent o = (WorldRenderEvent) obj;
        return java.util.Objects.equals(this.matrixStack, o.matrixStack) && java.util.Objects.equals(this.modelViewMatrix, o.modelViewMatrix) && java.util.Objects.equals(this.projectionMatrix, o.projectionMatrix) && java.util.Objects.equals(this.renderTickCounter, o.renderTickCounter) && java.util.Objects.equals(this.frustum, o.frustum);
    }
public MatrixStack matrixStack() {
        return this.matrixStack;
    }

    public Matrix4f modelViewMatrix() {
        return this.modelViewMatrix;
    }

    public Matrix4f projectionMatrix() {
        return this.projectionMatrix;
    }

    public RenderTickCounter renderTickCounter() {
        return this.renderTickCounter;
    }

    public Frustum frustum() {
        return this.frustum;
    }
}
