package aethereal.render;

import aethereal.type.RenderShapeState;

import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class TextureQuadShape extends RenderShape {
    public Matrix4f matrix;
    public float x0;
    public float y0;
    public float x1;
    public float y1;
    public Identifier texture;
    public int color;
    public boolean flag1;
    public boolean flag2;
    public boolean additiveBlend;

    public TextureQuadShape set(Matrix4f matrix4f, float f, float f2, float f3, float f4, Identifier identifier, int i, boolean z, boolean z2) {
        this.matrix = matrix4f;
        this.x0 = f;
        this.y0 = f2;
        this.x1 = f3;
        this.y1 = f4;
        this.texture = identifier;
        this.color = i;
        this.flag1 = z;
        this.flag2 = z2;
        this.additiveBlend = false;
        return this;
    }

    public TextureQuadShape set(Matrix4f matrix4f, float f, float f2, float f3, float f4, Identifier identifier, int i, boolean z, boolean z2, boolean z3) {
        set(matrix4f, f, f2, f3, f4, identifier, i, z, z2);
        this.additiveBlend = z3;
        return this;
    }

    @Override
    public void emit(MatrixStack matrixStack, BufferBuilder bufferBuilder) {
        ShapeRenderer.INSTANCE.emitTextureQuad(this.matrix, bufferBuilder, this.x0, this.y0, this.x1, this.y1, this.color);
    }

    @Override
    public RenderShapeState state() {
        return new RenderShapeState(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR, ShaderProgramKeys.POSITION_TEX_COLOR, this.texture, 1.0f, true, this.additiveBlend, false, false);
    }
}
