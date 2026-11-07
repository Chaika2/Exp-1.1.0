package aethereal.render;

import aethereal.type.RenderShapeState;

import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;

public class BoxOutlineShape extends RenderShape {
    public Matrix4f matrix;
    public Box box;
    public int color;
    public int color2;
    public boolean flag1;
    public boolean flag2;
    public float lineWidth;
    public boolean depthTest;

    public BoxOutlineShape set(Matrix4f matrix4f, Box box, int i, int i2, boolean z, boolean z2, float f, boolean z3) {
        this.matrix = matrix4f;
        this.box = box;
        this.color = i;
        this.color2 = i2;
        this.flag1 = z;
        this.flag2 = z2;
        this.lineWidth = f;
        this.depthTest = z3;
        return this;
    }

    @Override
    public void emit(MatrixStack matrixStack, BufferBuilder bufferBuilder) {
        ShapeRenderer.INSTANCE.emitBoxOutline(this.matrix, bufferBuilder, this.box, this.color, this.color2, this.flag1, this.flag2, this.depthTest);
    }

    @Override
    public RenderShapeState state() {
        return new RenderShapeState(VertexFormat.DrawMode.LINES, VertexFormats.LINES, ShaderProgramKeys.RENDERTYPE_LINES, null, this.lineWidth, true, false, false, false);
    }
}
