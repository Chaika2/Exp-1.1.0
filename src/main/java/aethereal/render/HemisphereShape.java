package aethereal.render;

import aethereal.type.RenderShapeState;

import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class HemisphereShape extends RenderShape {
    public Matrix4f matrix;
    public Vec3d center;
    public float radius;
    public int color;
    public int segments;
    public boolean depthTest;

    public HemisphereShape set(Matrix4f matrix4f, Vec3d vec3d, float f, int i, int i2, boolean z) {
        this.matrix = matrix4f;
        this.center = vec3d;
        this.radius = f;
        this.color = i;
        this.segments = i2;
        this.depthTest = z;
        return this;
    }

    @Override
    public void emit(MatrixStack matrixStack, BufferBuilder bufferBuilder) {
        ShapeRenderer.INSTANCE.emitHemisphere(this.matrix, bufferBuilder, this.center, this.radius, this.color, this.segments, this.depthTest);
    }

    @Override
    public RenderShapeState state() {
        return new RenderShapeState(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR, ShaderProgramKeys.POSITION_COLOR, null, 1.0f, true, false, false, false);
    }
}
