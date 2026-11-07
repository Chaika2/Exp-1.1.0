package aethereal.render;

import aethereal.type.RenderShapeState;

import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;

public class BoxRenderShape extends RenderShape {
    public Matrix4f matrix;
    public Box box;
    public int color;
    public boolean flag1;
    public boolean flag2;
    public boolean depthTest;

    public BoxRenderShape set(Matrix4f matrix4f, Box box, int i, boolean z, boolean z2, boolean z3) {
        this.matrix = matrix4f;
        this.box = box;
        this.color = i;
        this.flag1 = z;
        this.flag2 = z2;
        this.depthTest = z3;
        return this;
    }

    @Override
    public void emit(MatrixStack matrixStack, BufferBuilder bufferBuilder) {
        ShapeRenderer.INSTANCE.emitBox(this.matrix, bufferBuilder, this.box, this.color, this.depthTest);
    }

    @Override
    public RenderShapeState state() {
        return new RenderShapeState(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR, ShaderProgramKeys.POSITION_COLOR, null, 1.0f, true, false, false, false);
    }
}
