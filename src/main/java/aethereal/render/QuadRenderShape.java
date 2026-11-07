package aethereal.render;

import aethereal.type.RenderShapeState;

import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class QuadRenderShape extends RenderShape {
    public Matrix4f matrix;
    public Vec3d v1;
    public Vec3d v2;
    public Vec3d v3;
    public Vec3d v4;
    public int color;
    public boolean depthTest;

    public QuadRenderShape set(Matrix4f matrix4f, Vec3d vec3d, Vec3d vec3d2, Vec3d vec3d3, Vec3d vec3d4, int i, boolean z) {
        this.matrix = matrix4f;
        this.v1 = vec3d;
        this.v2 = vec3d2;
        this.v3 = vec3d3;
        this.v4 = vec3d4;
        this.color = i;
        this.depthTest = z;
        return this;
    }

    @Override
    public void emit(MatrixStack matrixStack, BufferBuilder bufferBuilder) {
        ShapeRenderer.INSTANCE.emitQuad(this.matrix, bufferBuilder, this.v1, this.v2, this.v3, this.v4, this.color, this.depthTest);
    }

    @Override
    public RenderShapeState state() {
        return new RenderShapeState(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR, ShaderProgramKeys.POSITION_COLOR, null, 1.0f, true, false, false, false);
    }
}
