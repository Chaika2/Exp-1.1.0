package aethereal.render;

import aethereal.type.RenderShapeState;

import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class LineRenderShape extends RenderShape {
    public Matrix4f matrix;
    public Vec3d start;
    public Vec3d end;
    public int color;
    public float lineWidth;
    public boolean depthTest;

    public LineRenderShape set(Matrix4f matrix4f, Vec3d vec3d, Vec3d vec3d2, int i, float f, boolean z) {
        this.matrix = matrix4f;
        this.start = vec3d;
        this.end = vec3d2;
        this.color = i;
        this.lineWidth = f;
        this.depthTest = z;
        return this;
    }

    @Override
    public void emit(MatrixStack matrixStack, BufferBuilder bufferBuilder) {
        ShapeRenderer.INSTANCE.emitLine(this.matrix, bufferBuilder, this.start, this.end, this.color, this.depthTest);
    }

    @Override
    public RenderShapeState state() {
        return new RenderShapeState(VertexFormat.DrawMode.LINES, VertexFormats.LINES, ShaderProgramKeys.RENDERTYPE_LINES, null, this.lineWidth, true, false, false, false);
    }
}
