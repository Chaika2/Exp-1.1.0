package aethereal.type;

import java.util.Objects;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;

public class RenderShapeState {
    public static boolean textureEnabled;

    public VertexFormat.DrawMode mode;
    public VertexFormat format;
    public ShaderProgramKey shader;
    public Identifier texture;
    public float lineWidth;
    public boolean blend;
    public boolean additiveBlend;
    public boolean depthTest;
    public boolean cull;

    public RenderShapeState() {
    }

    public RenderShapeState(VertexFormat.DrawMode drawMode, VertexFormat vertexFormat, ShaderProgramKey shaderProgramKey, Identifier identifier, float f, boolean z, boolean z2, boolean z3, boolean z4) {
        this.mode = drawMode;
        this.format = vertexFormat;
        this.shader = shaderProgramKey;
        this.texture = identifier;
        this.lineWidth = f;
        this.blend = z;
        this.additiveBlend = z2;
        this.depthTest = z3;
        this.cull = z4;
    }

    public boolean additiveBlend() {
        return this.additiveBlend;
    }

    public boolean blend() {
        return this.blend;
    }

    public boolean cull() {
        return this.cull;
    }

    public boolean depthTest() {
        return this.depthTest;
    }

    public VertexFormat format() {
        return this.format;
    }

    public float lineWidth() {
        return this.lineWidth;
    }

    public VertexFormat.DrawMode mode() {
        return this.mode;
    }

    public ShaderProgramKey shader() {
        return this.shader;
    }

    public Identifier texture() {
        return this.texture;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RenderShapeState)) {
            return false;
        }
        RenderShapeState class281Var = (RenderShapeState) obj;
        return this.blend == class281Var.blend && this.additiveBlend == class281Var.additiveBlend && this.depthTest == class281Var.depthTest && this.cull == class281Var.cull && Float.compare(this.lineWidth, class281Var.lineWidth) == 0 && this.mode == class281Var.mode && Objects.equals(this.format, class281Var.format) && Objects.equals(this.shader, class281Var.shader) && Objects.equals(this.texture, class281Var.texture);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.mode, this.format, this.shader, this.texture, Float.valueOf(this.lineWidth), Boolean.valueOf(this.blend), Boolean.valueOf(this.additiveBlend), Boolean.valueOf(this.depthTest), Boolean.valueOf(this.cull));
    }
}
