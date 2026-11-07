package aethereal.render;

import aethereal.type.RenderShapeState;

import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class TriangleShape extends RenderShape {
    public Matrix4f matrix;
    public Vec3d v1;
    public Vec3d v2;
    public Vec3d v3;
    public int color;
    public boolean depthTest;

    public TriangleShape set(Matrix4f matrix4f, Vec3d vec3d, Vec3d vec3d2, Vec3d vec3d3, int i, boolean z) {
        this.matrix = matrix4f;
        this.v1 = vec3d;
        this.v2 = vec3d2;
        this.v3 = vec3d3;
        this.color = i;
        this.depthTest = z;
        return this;
    }

    @Override
    public void emit(MatrixStack matrixStack, BufferBuilder bufferBuilder) {
        ShapeRenderer.INSTANCE.emitTriangle(this.matrix, bufferBuilder, this.v1, this.v2, this.v3, this.color, this.depthTest);
    }

    @Override
    public RenderShapeState state() {
        return new RenderShapeState(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR, ShaderProgramKeys.POSITION_COLOR, null, 1.0f, true, false, false, false);
    }
}
