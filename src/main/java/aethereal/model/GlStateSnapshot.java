package aethereal.model;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

public final class GlStateSnapshot {
    public final int lastActiveTexture;
    public final int lastProgram;
    public final int[] lastTextures;
    public final int[] lastSamplers;
    public final int lastArrayBuffer;
    public final int lastElementArrayBuffer;
    public final int lastVertexArrayObject;
    public final int[] lastPolygonMode;
    public final int[] lastViewport;
    public final int[] lastScissorBox;
    public final int lastBlendSrcRgb;
    public final int lastBlendDstRgb;
    public final int lastBlendSrcAlpha;
    public final int lastBlendDstAlpha;
    public final int lastBlendEquationRgb;
    public final int lastBlendEquationAlpha;
    public final boolean lastEnableBlend;
    public final boolean lastEnableCullFace;
    public final boolean lastEnableDepthTest;
    public final boolean lastEnableStencilTest;
    public final boolean lastEnableScissorTest;
    public final boolean lastEnablePrimitiveRestart;

    public GlStateSnapshot(int i, int i2, int[] iArr, int[] iArr2, int i3, int i4, int i5, int[] iArr3, int[] iArr4, int[] iArr5, int i6, int i7, int i8, int i9, int i10, int i11, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6) {
        this.lastActiveTexture = i;
        this.lastProgram = i2;
        this.lastTextures = iArr;
        this.lastSamplers = iArr2;
        this.lastArrayBuffer = i3;
        this.lastElementArrayBuffer = i4;
        this.lastVertexArrayObject = i5;
        this.lastPolygonMode = iArr3;
        this.lastViewport = iArr4;
        this.lastScissorBox = iArr5;
        this.lastBlendSrcRgb = i6;
        this.lastBlendDstRgb = i7;
        this.lastBlendSrcAlpha = i8;
        this.lastBlendDstAlpha = i9;
        this.lastBlendEquationRgb = i10;
        this.lastBlendEquationAlpha = i11;
        this.lastEnableBlend = z;
        this.lastEnableCullFace = z2;
        this.lastEnableDepthTest = z3;
        this.lastEnableStencilTest = z4;
        this.lastEnableScissorTest = z5;
        this.lastEnablePrimitiveRestart = z6;
    }

    public void revert() {
        if ((this.lastProgram == 0) | GL20.glIsProgram(this.lastProgram)) {
            GL20.glUseProgram(this.lastProgram);
        }
        for (int i = 0; i < this.lastTextures.length; i++) {
            GL13.glActiveTexture(33984 + i);
            GL11.glBindTexture(3553, this.lastTextures[i]);
            GL33.glBindSampler(i, this.lastSamplers[i]);
        }
        GL13.glActiveTexture(this.lastActiveTexture);
        GL30.glBindVertexArray(this.lastVertexArrayObject);
        GL20.glBindBuffer(34962, this.lastArrayBuffer);
        GL20.glBindBuffer(34963, this.lastElementArrayBuffer);
        GL20.glBlendEquationSeparate(this.lastBlendEquationRgb, this.lastBlendEquationAlpha);
        GL14.glBlendFuncSeparate(this.lastBlendSrcRgb, this.lastBlendDstRgb, this.lastBlendSrcAlpha, this.lastBlendDstAlpha);
        setEnabled(3042, this.lastEnableBlend);
        setEnabled(2884, this.lastEnableCullFace);
        setEnabled(2929, this.lastEnableDepthTest);
        setEnabled(2960, this.lastEnableStencilTest);
        setEnabled(3089, this.lastEnableScissorTest);
        setEnabled(36765, this.lastEnablePrimitiveRestart);
        GL11.glPolygonMode(1032, this.lastPolygonMode[0]);
        GL11.glViewport(this.lastViewport[0], this.lastViewport[1], this.lastViewport[2], this.lastViewport[3]);
        GL11.glScissor(this.lastScissorBox[0], this.lastScissorBox[1], this.lastScissorBox[2], this.lastScissorBox[3]);
    }

    public static GlStateSnapshot create() {
        int iGlGetInteger = GL11.glGetInteger(34016);
        int iGlGetInteger2 = GL11.glGetInteger(35725);
        int iGlGetInteger3 = GL11.glGetInteger(35661);
        int[] iArr = new int[iGlGetInteger3];
        int[] iArr2 = new int[iGlGetInteger3];
        for (int i = 0; i < iGlGetInteger3; i++) {
            GL13.glActiveTexture(33984 + i);
            iArr[i] = GL11.glGetInteger(32873);
            iArr2[i] = GL11.glGetInteger(35097);
        }
        GL13.glActiveTexture(iGlGetInteger);
        int iGlGetInteger4 = GL11.glGetInteger(34964);
        int iGlGetInteger5 = GL11.glGetInteger(34965);
        int iGlGetInteger6 = GL11.glGetInteger(34229);
        int[] iArr3 = new int[2];
        GL11.glGetIntegerv(2880, iArr3);
        int[] iArr4 = new int[4];
        GL11.glGetIntegerv(2978, iArr4);
        int[] iArr5 = new int[4];
        GL11.glGetIntegerv(3088, iArr5);
        return new GlStateSnapshot(iGlGetInteger, iGlGetInteger2, iArr, iArr2, iGlGetInteger4, iGlGetInteger5, iGlGetInteger6, iArr3, iArr4, iArr5, GL11.glGetInteger(32969), GL11.glGetInteger(32968), GL11.glGetInteger(32971), GL11.glGetInteger(32970), GL11.glGetInteger(32777), GL11.glGetInteger(34877), GL11.glIsEnabled(3042), GL11.glIsEnabled(2884), GL11.glIsEnabled(2929), GL11.glIsEnabled(2960), GL11.glIsEnabled(3089), GL11.glIsEnabled(36765));
    }

    public void setEnabled(int i, boolean z) {
        if (z) {
            GL11.glEnable(i);
        } else {
            GL11.glDisable(i);
        }
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "lastActiveTexture=" + this.lastActiveTexture + ", " + "lastProgram=" + this.lastProgram + ", " + "lastTextures=" + this.lastTextures + ", " + "lastSamplers=" + this.lastSamplers + ", " + "lastArrayBuffer=" + this.lastArrayBuffer + ", " + "lastElementArrayBuffer=" + this.lastElementArrayBuffer + ", " + "lastVertexArrayObject=" + this.lastVertexArrayObject + ", " + "lastPolygonMode=" + this.lastPolygonMode + ", " + "lastViewport=" + this.lastViewport + ", " + "lastScissorBox=" + this.lastScissorBox + ", " + "lastBlendSrcRgb=" + this.lastBlendSrcRgb + ", " + "lastBlendDstRgb=" + this.lastBlendDstRgb + ", " + "lastBlendSrcAlpha=" + this.lastBlendSrcAlpha + ", " + "lastBlendDstAlpha=" + this.lastBlendDstAlpha + ", " + "lastBlendEquationRgb=" + this.lastBlendEquationRgb + ", " + "lastBlendEquationAlpha=" + this.lastBlendEquationAlpha + ", " + "lastEnableBlend=" + this.lastEnableBlend + ", " + "lastEnableCullFace=" + this.lastEnableCullFace + ", " + "lastEnableDepthTest=" + this.lastEnableDepthTest + ", " + "lastEnableStencilTest=" + this.lastEnableStencilTest + ", " + "lastEnableScissorTest=" + this.lastEnableScissorTest + ", " + "lastEnablePrimitiveRestart=" + this.lastEnablePrimitiveRestart + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.lastActiveTexture, this.lastProgram, this.lastTextures, this.lastSamplers, this.lastArrayBuffer, this.lastElementArrayBuffer, this.lastVertexArrayObject, this.lastPolygonMode, this.lastViewport, this.lastScissorBox, this.lastBlendSrcRgb, this.lastBlendDstRgb, this.lastBlendSrcAlpha, this.lastBlendDstAlpha, this.lastBlendEquationRgb, this.lastBlendEquationAlpha, this.lastEnableBlend, this.lastEnableCullFace, this.lastEnableDepthTest, this.lastEnableStencilTest, this.lastEnableScissorTest, this.lastEnablePrimitiveRestart);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GlStateSnapshot)) return false;
        GlStateSnapshot o = (GlStateSnapshot) obj;
        return java.util.Objects.equals(this.lastActiveTexture, o.lastActiveTexture) && java.util.Objects.equals(this.lastProgram, o.lastProgram) && java.util.Objects.equals(this.lastTextures, o.lastTextures) && java.util.Objects.equals(this.lastSamplers, o.lastSamplers) && java.util.Objects.equals(this.lastArrayBuffer, o.lastArrayBuffer) && java.util.Objects.equals(this.lastElementArrayBuffer, o.lastElementArrayBuffer) && java.util.Objects.equals(this.lastVertexArrayObject, o.lastVertexArrayObject) && java.util.Objects.equals(this.lastPolygonMode, o.lastPolygonMode) && java.util.Objects.equals(this.lastViewport, o.lastViewport) && java.util.Objects.equals(this.lastScissorBox, o.lastScissorBox) && java.util.Objects.equals(this.lastBlendSrcRgb, o.lastBlendSrcRgb) && java.util.Objects.equals(this.lastBlendDstRgb, o.lastBlendDstRgb) && java.util.Objects.equals(this.lastBlendSrcAlpha, o.lastBlendSrcAlpha) && java.util.Objects.equals(this.lastBlendDstAlpha, o.lastBlendDstAlpha) && java.util.Objects.equals(this.lastBlendEquationRgb, o.lastBlendEquationRgb) && java.util.Objects.equals(this.lastBlendEquationAlpha, o.lastBlendEquationAlpha) && java.util.Objects.equals(this.lastEnableBlend, o.lastEnableBlend) && java.util.Objects.equals(this.lastEnableCullFace, o.lastEnableCullFace) && java.util.Objects.equals(this.lastEnableDepthTest, o.lastEnableDepthTest) && java.util.Objects.equals(this.lastEnableStencilTest, o.lastEnableStencilTest) && java.util.Objects.equals(this.lastEnableScissorTest, o.lastEnableScissorTest) && java.util.Objects.equals(this.lastEnablePrimitiveRestart, o.lastEnablePrimitiveRestart);
    }
public int lastActiveTexture() {
        return this.lastActiveTexture;
    }

    public int lastProgram() {
        return this.lastProgram;
    }

    public int[] lastTextures() {
        return this.lastTextures;
    }

    public int[] lastSamplers() {
        return this.lastSamplers;
    }

    public int lastArrayBuffer() {
        return this.lastArrayBuffer;
    }

    public int lastElementArrayBuffer() {
        return this.lastElementArrayBuffer;
    }

    public int lastVertexArrayObject() {
        return this.lastVertexArrayObject;
    }

    public int[] lastPolygonMode() {
        return this.lastPolygonMode;
    }

    public int[] lastViewport() {
        return this.lastViewport;
    }

    public int[] lastScissorBox() {
        return this.lastScissorBox;
    }

    public int lastBlendSrcRgb() {
        return this.lastBlendSrcRgb;
    }

    public int lastBlendDstRgb() {
        return this.lastBlendDstRgb;
    }

    public int lastBlendSrcAlpha() {
        return this.lastBlendSrcAlpha;
    }

    public int lastBlendDstAlpha() {
        return this.lastBlendDstAlpha;
    }

    public int lastBlendEquationRgb() {
        return this.lastBlendEquationRgb;
    }

    public int lastBlendEquationAlpha() {
        return this.lastBlendEquationAlpha;
    }

    public boolean lastEnableBlend() {
        return this.lastEnableBlend;
    }

    public boolean lastEnableCullFace() {
        return this.lastEnableCullFace;
    }

    public boolean lastEnableDepthTest() {
        return this.lastEnableDepthTest;
    }

    public boolean lastEnableStencilTest() {
        return this.lastEnableStencilTest;
    }

    public boolean lastEnableScissorTest() {
        return this.lastEnableScissorTest;
    }

    public boolean lastEnablePrimitiveRestart() {
        return this.lastEnablePrimitiveRestart;
    }
}
