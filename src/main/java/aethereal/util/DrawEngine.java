package aethereal.util;
import aethereal.render.ColorStack;
import aethereal.internal.DirectionIndexMap;
import aethereal.render.Fonts;
import aethereal.model.GlStateSnapshot;
import aethereal.render.GlTexture;
import aethereal.type.ItemSpriteManager;
import aethereal.render.ItemSpriteTextures;
import aethereal.type.Mc;
import aethereal.render.MsdfFont;
import aethereal.accessor.NativeMemoryAccessor;
import aethereal.natives.NativeMemoryBuffer;
import aethereal.resource.IdentifierResource;
import aethereal.model.ScissorBounds;
import aethereal.render.ScissorStack;
import aethereal.render.ShaderProgram;
import aethereal.render.ShaderUniform;
import aethereal.type.ShapeType;
import aethereal.model.SpriteRegion;
import aethereal.render.TextureSlotAllocator;
import aethereal.type.VertexAttributeType;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

public class DrawEngine {

    public final NativeMemoryBuffer vertexBuffer;
    public final NativeMemoryBuffer indexBuffer;

    public final ShaderProgram shaderProgram;

    public final ShaderUniform orthoMatrixUniform;

    public final ShaderUniform textureSamplerUniform;
    public final ShaderUniform resolutionUniform;

    public final TextureSlotAllocator textureSlotAllocator;

    public final ColorStack colorStack;
    public final int vertexArrayId;
    public final int vertexBufferId;
    public final int indexBufferId;
    public int savedActiveTexture;
    public int savedTextureBinding;
    public int indexCount;
    public int vertexCount;

    public GlStateSnapshot stateSnapshot;

    public boolean building;

    public boolean usingStencil;
    public final int maxTextureUnits;
    public final Vector4f tempVector = new Vector4f();

    public final ScissorStack scissorStack = new ScissorStack();
    public float contentScale = 1.0f;
    public int drawCallCount = 0;

    public final ScissorBounds lastBounds = new ScissorBounds();

    public Runnable blendSwitchCallback = null;

    public static GlTexture glintTexture;

    public int glintTextureId() {
        if (glintTexture == null) {
            glintTexture = new GlTexture(new IdentifierResource(Identifier.ofVanilla("textures/misc/enchanted_glint_item.png")));
            glintTexture.wrapX(10497).wrapY(10497);
        }
        return glintTexture.textureWithSTB();
    }

    public DrawEngine(ShaderProgram class381Var, int i) {
        this.maxTextureUnits = i;
        NativeMemoryAccessor class014Var = new NativeMemoryAccessor();
        this.vertexBuffer = new NativeMemoryBuffer(class014Var, 8388608L);
        this.indexBuffer = new NativeMemoryBuffer(class014Var, 8388608L);
        this.shaderProgram = class381Var;
        this.orthoMatrixUniform = class381Var.uniform("orthographicMatrix");
        this.textureSamplerUniform = class381Var.uniform("textureSampler");
        this.resolutionUniform = class381Var.uniform("resolution");
        this.textureSlotAllocator = new TextureSlotAllocator(i);
        this.colorStack = new ColorStack();
        List<VertexAttributeType> listOf = List.of(new VertexAttributeType[]{VertexAttributeType.VEC2, VertexAttributeType.VEC2, VertexAttributeType.VEC2, VertexAttributeType.VEC2, VertexAttributeType.VEC4, VertexAttributeType.NORMALIZED_VEC4, VertexAttributeType.NORMALIZED_VEC4, VertexAttributeType.FLOAT, VertexAttributeType.FLOAT, VertexAttributeType.UNSIGNED_BYTE, VertexAttributeType.UNSIGNED_BYTE, VertexAttributeType.UNSIGNED_BYTE});
        this.vertexArrayId = GL33.glGenVertexArrays();
        this.vertexBufferId = GL33.glGenBuffers();
        this.indexBufferId = GL33.glGenBuffers();
        GL33.glBindVertexArray(this.vertexArrayId);
        GL33.glBindBuffer(34962, this.vertexBufferId);
        GL33.glBindBuffer(34963, this.indexBufferId);
        int iSum = listOf.stream().mapToInt((v0) -> {
            return v0.size();
        }).sum();
        int size = 0;
        for (int i2 = 0; i2 < listOf.size(); i2++) {
            VertexAttributeType class075Var = (VertexAttributeType) listOf.get(i2);
            GL33.glEnableVertexAttribArray(i2);
            if (class075Var.integer()) {
                GL33.glVertexAttribIPointer(i2, class075Var.count(), class075Var.type(), iSum, size);
            } else {
                GL33.glVertexAttribPointer(i2, class075Var.count(), class075Var.type(), class075Var.normalized(), iSum, size);
            }
            size += class075Var.size();
        }
    }

    public void begin() {
        if (this.building) {
            throw new RuntimeException("DrawEngine.begin() called while already building.");
        }
        Window window = Mc.INSTANCE.getWindow();
        updateContentScale(window);
        int iMethod007 = scaledWidth(window);
        int iMethod013 = scaledHeight(window);
        this.drawCallCount = 0;
        setupGlState();
        beginScissor(0.0f, 0.0f, iMethod007, iMethod013);
        this.savedTextureBinding = GL33.glGetInteger(32873);
        this.savedActiveTexture = GL33.glGetInteger(34016);
        this.colorStack.begin();
        this.building = true;
    }

    public void end() {
        if (!this.building) {
            throw new RuntimeException("DrawEngine.end() called while not building.");
        }
        this.building = false;
        draw();
        GL33.glActiveTexture(this.savedActiveTexture);
        GL33.glBindTexture(3553, this.savedTextureBinding);
        restoreGlState();
    }

    public int bindTexture(int i) {
        if (!this.building) {
            throw new RuntimeException("DrawEngine.bindTexture() called while not building.");
        }
        if (!this.textureSlotAllocator.contains(i) && this.textureSlotAllocator.isFull()) {
            draw();
        }
        return this.textureSlotAllocator.bindTexture(i);
    }

    public void draw() {
        Window window = Mc.INSTANCE.getWindow();
        Matrix4f matrix4fOrtho2D = new Matrix4f().ortho2D(0.0f, window.getFramebufferWidth(), window.getFramebufferHeight(), 0.0f);
        ByteBuffer byteBufferDirectByteBuffer = this.vertexBuffer.directByteBuffer();
        ByteBuffer byteBufferDirectByteBuffer2 = this.indexBuffer.directByteBuffer();
        GL33.glBindVertexArray(this.vertexArrayId);
        GL33.glBindBuffer(34962, this.vertexBufferId);
        GL33.glBindBuffer(34963, this.indexBufferId);
        GL33.glBufferData(34962, MemoryUtil.memSlice(byteBufferDirectByteBuffer), 35040);
        GL33.glBufferData(34963, MemoryUtil.memSlice(byteBufferDirectByteBuffer2), 35040);
        this.shaderProgram.bind();
        this.orthoMatrixUniform.uploadMatrix4f(matrix4fOrtho2D);
        this.textureSamplerUniform.uploadIntBuffer(this.textureSlotAllocator.textureSlots());
        this.resolutionUniform.uploadVec2(window.getFramebufferWidth(), window.getFramebufferHeight());
        boolean z = this.blendSwitchCallback != null;
        if (z) {
            this.blendSwitchCallback.run();
            this.blendSwitchCallback = null;
        }
        GL33.glDrawElements(4, this.indexCount, 5125, 0L);
        if (z) {
            GL33.glBlendFuncSeparate(770, 771, 1, 771);
        }
        this.drawCallCount++;
        this.indexCount = 0;
        this.vertexCount = 0;
        this.shaderProgram.unbind();
        this.textureSlotAllocator.clear();
        this.vertexBuffer.reset();
        this.indexBuffer.reset();
    }

    public void setupGlState() {
        this.stateSnapshot = GlStateSnapshot.create();
        GL33.glEnable(3042);
        GL33.glBlendEquation(32774);
        GL33.glBlendFuncSeparate(770, 771, 1, 771);
        GL33.glEnable(3089);
        GL33.glDisable(2884);
        GL33.glDisable(2929);
        GL33.glDisable(36765);
        GL33.glPolygonMode(1032, 6914);
        GL33.glPixelStorei(3317, 1);
        GL33.glPixelStorei(3333, 1);
        for (int i = 0; i < this.maxTextureUnits; i++) {
            GL33.glBindSampler(i, 0);
        }
    }

    public void restoreGlState() {
        this.scissorStack.end();
        this.stateSnapshot.revert();
    }

    public void switchBlendFuncAndDraw(Runnable runnable) {
        this.blendSwitchCallback = runnable;
        draw();
    }

    public void beginStencil() {
        if (this.usingStencil) {
            throw new RuntimeException("DrawEngine.beginStencil() called while already using stencil.");
        }
        this.usingStencil = true;
        draw();
        StencilBufferUtil.prepareStencil();
    }

    public void prepareStencil(int i) {
        if (!this.usingStencil) {
            throw new RuntimeException("DrawEngine.prepareStencil() called while not using stencil.");
        }
        draw();
        StencilBufferUtil.prepareElement(i);
    }

    public void endStencil() {
        if (!this.usingStencil) {
            throw new RuntimeException("DrawEngine.endStencil() called while not using stencil.");
        }
        draw();
        StencilBufferUtil.cleanup();
        this.usingStencil = false;
    }

    public void beginScissor(float f, float f2, float f3, float f4) {
        Window window = Mc.INSTANCE.getWindow();
        int iRound = Math.round(applyContentScale(f));
        int iRound2 = Math.round(applyContentScale(f2));
        int iRound3 = Math.round(applyContentScale(f3));
        int iRound4 = Math.round(applyContentScale(f4));
        int framebufferHeight = (window.getFramebufferHeight() - iRound2) - iRound4;
        draw();
        this.scissorStack.push(iRound, framebufferHeight, iRound3, iRound4, f, f2, f3, f4);
    }

    public void beginScissor(Matrix4f matrix4f, float f, float f2, float f3, float f4) {
        Window window = Mc.INSTANCE.getWindow();
        float fTransformX = transformX(matrix4f, f);
        float fTransformY = transformY(matrix4f, f2);
        float fTransformX2 = transformX(matrix4f, f + f3);
        float fTransformY2 = transformY(matrix4f, f2 + f4);
        int iRound = Math.round(applyContentScale(fTransformX));
        int iRound2 = Math.round(applyContentScale(fTransformY));
        int iRound3 = Math.round(applyContentScale(fTransformX2 - fTransformX));
        int iRound4 = Math.round(applyContentScale(fTransformY2 - fTransformY));
        int framebufferHeight = (window.getFramebufferHeight() - iRound2) - iRound4;
        draw();
        this.scissorStack.push(iRound, framebufferHeight, iRound3, iRound4, fTransformX, fTransformY, fTransformX2, fTransformY2);
    }

    public void drawDebugDataIfEnabled() {
        int i = this.drawCallCount;
        ArrayList<String> arrayList = new ArrayList();
        arrayList.add("drawcalls " + i);
        arrayList.add(MinecraftClient.getInstance().fpsDebugString);
        MsdfFont class161Var = Fonts.INTER_SEMIBOLD.get();
        Window window = Mc.INSTANCE.getWindow();
        float height = class161Var.getHeight(12.0f);
        int iMethod007 = scaledWidth(window);
        float fMethod013 = (scaledHeight(window) - 45.0f) - (height * arrayList.size());
        Matrix4f positionMatrix = new MatrixStack().peek().getPositionMatrix();
        for (String str : arrayList) {
            msdfFont(positionMatrix, class161Var, str, (iMethod007 - 10.0f) - class161Var.getWidth(str, 12.0f), fMethod013, 12.0f, 0.05f, -1);
            fMethod013 += height;
        }
    }

    public void endScissor() {
        draw();
        this.scissorStack.pop();
    }

    public float transformY(MatrixStack matrixStack, float f) {
        Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
        Vector4f vector4f = new Vector4f(0.0f, f, 0.0f, 1.0f);
        positionMatrix.transform(vector4f);
        return vector4f.y();
    }

    public float transformX(MatrixStack matrixStack, float f) {
        Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
        Vector4f vector4f = new Vector4f(f, 0.0f, 0.0f, 1.0f);
        positionMatrix.transform(vector4f);
        return vector4f.x();
    }

    public float transformY(Matrix4f matrix4f, float f) {
        Vector4f vector4f = new Vector4f(0.0f, f, 0.0f, 1.0f);
        matrix4f.transform(vector4f);
        return vector4f.y();
    }

    public float transformX(Matrix4f matrix4f, float f) {
        Vector4f vector4f = new Vector4f(f, 0.0f, 0.0f, 1.0f);
        matrix4f.transform(vector4f);
        return vector4f.x();
    }

    public void emitShape(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, ShapeType class328Var) {
        Window window = Mc.INSTANCE.getWindow();
        Vector4f vector4fTransform = matrix4f.transform(this.tempVector.set(f, f2, 0.0f, 1.0f));
        float fX = vector4fTransform.x();
        float fY = vector4fTransform.y();
        float fX2 = vector4fTransform.x();
        float fY2 = vector4fTransform.y();
        Vector4f vector4fTransform2 = matrix4f.transform(this.tempVector.set(f + f3, f2 + f4, 0.0f, 1.0f));
        float fX3 = vector4fTransform2.x() - fX;
        float fY3 = vector4fTransform2.y() - fY;
        float fX4 = vector4fTransform2.x() - fX2;
        float fY4 = vector4fTransform2.y() - fY2;
        this.lastBounds.setGui(fX, fY, fX3, fY3);
        float fMethod016 = applyContentScale(fX2);
        float fMethod017 = applyContentScale(fY2);
        float fMethod018 = applyContentScale(fX4);
        float fMethod019 = applyContentScale(fY4);
        float framebufferHeight = (window.getFramebufferHeight() - fMethod019) - fMethod017;
        this.lastBounds.setGl(fMethod016, framebufferHeight, fMethod018, fMethod019);
        float fMax = Math.max(matrix4f.m00(), matrix4f.m11()) * this.contentScale;
        float f15 = f9 * fMax;
        float f16 = f10 * fMax;
        float f17 = f11 * fMax;
        float f18 = f12 * fMax;
        float f19 = f13 * this.contentScale;
        float f20 = f14 * this.contentScale;
        int iMode = class328Var.mode();
        writeTransformedPosition(matrix4f, f, f2 + f4);
        writeVec2(fMethod016, framebufferHeight);
        writeVec2(fMethod018, fMethod019);
        writeVec2(f5, f8);
        writeVec4(f15, f16, f17, f18);
        writeColor(i5);
        writeColor(i);
        writeFloat(f19);
        writeFloat(f20);
        writeByte(i9);
        writeByte(iMode);
        writeByte(i10);
        writeTransformedPosition(matrix4f, f + f3, f2 + f4);
        writeVec2(fMethod016, framebufferHeight);
        writeVec2(fMethod018, fMethod019);
        writeVec2(f7, f8);
        writeVec4(f15, f16, f17, f18);
        writeColor(i6);
        writeColor(i2);
        writeFloat(f19);
        writeFloat(f20);
        writeByte(i9);
        writeByte(iMode);
        writeByte(i10);
        writeTransformedPosition(matrix4f, f + f3, f2);
        writeVec2(fMethod016, framebufferHeight);
        writeVec2(fMethod018, fMethod019);
        writeVec2(f7, f6);
        writeVec4(f15, f16, f17, f18);
        writeColor(i7);
        writeColor(i3);
        writeFloat(f19);
        writeFloat(f20);
        writeByte(i9);
        writeByte(iMode);
        writeByte(i10);
        writeTransformedPosition(matrix4f, f, f2);
        writeVec2(fMethod016, framebufferHeight);
        writeVec2(fMethod018, fMethod019);
        writeVec2(f5, f6);
        writeVec4(f15, f16, f17, f18);
        writeColor(i8);
        writeColor(i4);
        writeFloat(f19);
        writeFloat(f20);
        writeByte(i9);
        writeByte(iMode);
        writeByte(i10);
        writeTriangle(this.vertexCount, this.vertexCount + 1, this.vertexCount + 2);
        writeTriangle(this.vertexCount, this.vertexCount + 2, this.vertexCount + 3);
        this.vertexCount += 4;
        this.indexCount += 6;
    }

    public void emitTexturedQuad(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, int i, int i2, int i3, int i4, int i5, int i6, ShapeType class328Var) {
        Window window = Mc.INSTANCE.getWindow();
        Vector4f vector4f = new Vector4f();
        matrix4f.transform(vector4f.set(f, f2, 0.0f, 1.0f));
        float fX = vector4f.x();
        float fY = vector4f.y();
        matrix4f.transform(vector4f.set(f3, f4, 0.0f, 1.0f));
        float fX2 = vector4f.x();
        float fY2 = vector4f.y();
        matrix4f.transform(vector4f.set(f5, f6, 0.0f, 1.0f));
        float fX3 = vector4f.x();
        float fY3 = vector4f.y();
        matrix4f.transform(vector4f.set(f7, f8, 0.0f, 1.0f));
        float fX4 = vector4f.x();
        float fY4 = vector4f.y();
        float fMin = Math.min(Math.min(fX, fX2), Math.min(fX3, fX4));
        float fMax = Math.max(Math.max(fX, fX2), Math.max(fX3, fX4));
        float fMin2 = Math.min(Math.min(fY, fY2), Math.min(fY3, fY4));
        float fMax2 = Math.max(Math.max(fY, fY2), Math.max(fY3, fY4));
        float f13 = fMax - fMin;
        float f14 = fMax2 - fMin2;
        float framebufferHeight = window.getFramebufferHeight() - applyContentScale(fMax2);
        float fMethod016 = applyContentScale(fX);
        float fMethod017 = applyContentScale(fY);
        float fMethod018 = applyContentScale(fX2);
        float fMethod019 = applyContentScale(fY2);
        float fMethod0110 = applyContentScale(fX3);
        float fMethod0111 = applyContentScale(fY3);
        float fMethod0112 = applyContentScale(fX4);
        float fMethod0113 = applyContentScale(fY4);
        float fMethod0114 = applyContentScale(fMin);
        float fMethod0115 = applyContentScale(f13);
        float fMethod0116 = applyContentScale(f14);
        this.lastBounds.setGui(fMin, fMin2, f13, f14);
        this.lastBounds.setGl(fMethod0114, framebufferHeight, fMethod0115, fMethod0116);
        int iMode = class328Var.mode();
        writeVec2(fMethod016, fMethod017);
        writeVec2(fMethod0114, framebufferHeight);
        writeVec2(fMethod0115, fMethod0116);
        writeVec2(f9, f12);
        writeVec4(0.0f, 0.0f, 0.0f, 0.0f);
        writeColor(i);
        writeColor(-1);
        writeFloat(0.0f);
        writeFloat(0.0f);
        writeByte(i5);
        writeByte(iMode);
        writeByte(i6);
        writeVec2(fMethod018, fMethod019);
        writeVec2(fMethod0114, framebufferHeight);
        writeVec2(fMethod0115, fMethod0116);
        writeVec2(f11, f12);
        writeVec4(0.0f, 0.0f, 0.0f, 0.0f);
        writeColor(i2);
        writeColor(-1);
        writeFloat(0.0f);
        writeFloat(0.0f);
        writeByte(i5);
        writeByte(iMode);
        writeByte(i6);
        writeVec2(fMethod0110, fMethod0111);
        writeVec2(fMethod0114, framebufferHeight);
        writeVec2(fMethod0115, fMethod0116);
        writeVec2(f11, f10);
        writeVec4(0.0f, 0.0f, 0.0f, 0.0f);
        writeColor(i3);
        writeColor(-1);
        writeFloat(0.0f);
        writeFloat(0.0f);
        writeByte(i5);
        writeByte(iMode);
        writeByte(i6);
        writeVec2(fMethod0112, fMethod0113);
        writeVec2(fMethod0114, framebufferHeight);
        writeVec2(fMethod0115, fMethod0116);
        writeVec2(f9, f10);
        writeVec4(0.0f, 0.0f, 0.0f, 0.0f);
        writeColor(i4);
        writeColor(-1);
        writeFloat(0.0f);
        writeFloat(0.0f);
        writeByte(i5);
        writeByte(iMode);
        writeByte(i6);
        writeTriangle(this.vertexCount, this.vertexCount + 1, this.vertexCount + 2);
        writeTriangle(this.vertexCount, this.vertexCount + 2, this.vertexCount + 3);
        this.vertexCount += 4;
        this.indexCount += 6;
    }

    public void quadTexture(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, int i, int i2, int i3, int i4, int i5) {
        emitTexturedQuad(matrix4f, f, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, i2, i3, i4, i5, i, 0, ShapeType.TEXTURE);
    }

    public void rectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, int i, int i2, int i3, int i4) {
        emitShape(matrix4f, f, f2, f3, f4, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1, -1, -1, -1, i, i2, i3, i4, 0, 0, ShapeType.COLOR);
    }

    public void roundedBlur(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, int i, int i2) {
        emitShape(matrix4f, f, f2, f3, f4, 0.0f, 0.0f, 1.0f, 1.0f, f5, f6, f7, f8, f9, f10, -1, -1, -1, -1, i, i, i, i, bindTexture(i2), 0, ShapeType.BLUR);
    }

    public void outerMask(Matrix4f matrix4f, int i, int i2) {
        Window window = Mc.INSTANCE.getWindow();
        emitShape(matrix4f, 0.0f, 0.0f, scaledWidth(window), scaledHeight(window), 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0, 0, 0, 0, 0, 0, 0, 0, bindTexture(i), bindTexture(i2), ShapeType.OUTER_MASK);
    }

    public void alphaMask(Matrix4f matrix4f, int i, int i2) {
        Window window = Mc.INSTANCE.getWindow();
        float fMethod007 = scaledWidth(window);
        float fMethod013 = scaledHeight(window);
        int iBindTexture = bindTexture(i);
        int iArgb = this.colorStack.argb(i2, StencilBufferUtil.STENCIL_MASK, StencilBufferUtil.STENCIL_MASK, StencilBufferUtil.STENCIL_MASK);
        emitShape(matrix4f, 0.0f, 0.0f, fMethod007, fMethod013, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0, 0, 0, 0, iArgb, iArgb, iArgb, iArgb, iBindTexture, 0, ShapeType.ALPHA_MASK);
    }

    public void roundedBlur(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, int i, int i2) {
        roundedBlur(matrix4f, f, f2, f3, f4, f5, f5, f5, f5, 0.0f, 0.0f, i, i2);
    }

    public void roundedBlur(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, int i, int i2) {
        roundedBlur(matrix4f, f, f2, f3, f4, f5, f5, f5, f5, f6, 0.0f, i, i2);
    }

    public void roundedBlur(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, int i, int i2) {
        roundedBlur(matrix4f, f, f2, f3, f4, f5, f5, f5, f5, f6, f7, i, i2);
    }

    public void roundedBlur(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int i, int i2) {
        roundedBlur(matrix4f, f, f2, f3, f4, f5, f6, f7, f8, 0.0f, 0.0f, i, i2);
    }

    public void roundedBlur(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, int i, int i2) {
        roundedBlur(matrix4f, f, f2, f3, f4, f5, f6, f7, f8, f9, 0.0f, i, i2);
    }

    public void rectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, int i) {
        rectangle(matrix4f, f, f2, f3, f4, i, i, i, i);
    }

    public void texture(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int i, int i2, int i3, int i4, int i5) {
        emitShape(matrix4f, f, f2, f3, f4, f5, f6, f7, f8, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1, -1, -1, -1, i2, i3, i4, i5, i, 0, ShapeType.TEXTURE);
    }

    public void textureMsdf(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int i, int i2, int i3, int i4, int i5) {
        emitShape(matrix4f, f, f2, f3, f4, f5, f6, f7, f8, 0.0f, 0.0f, 0.0f, 0.0f, 0.05f, 0.5f, -1, -1, -1, -1, i2, i3, i4, i5, i, 0, ShapeType.MSDF_FONT);
    }

    public void itemStack(Matrix4f matrix4f, ItemStack itemStack, float f, float f2, float f3, float f4) {
        itemStackTinted(matrix4f, itemStack, f, f2, f3, f4, -1);
    }

    public void itemStackTinted(Matrix4f matrix4f, ItemStack itemStack, float f, float f2, float f3, float f4, int iTintColor) {
        ItemSpriteTextures spriteTextures;
        Map<Direction, SpriteRegion> mapSprites;
        float f5;
        if (itemStack == null || itemStack.isEmpty() || (spriteTextures = ItemSpriteManager.INSTANCE.getSpriteTextures(itemStack)) == null || (mapSprites = spriteTextures.sprites()) == null || mapSprites.isEmpty()) {
            return;
        }
        float f6 = 32.0f * f3;
        float f7 = f6 / 2.0f;
        float f8 = f6 / 4.0f;
        float f9 = f6 / 16.0f;
        float f10 = (f2 - f8) - (f9 / 2.0f);
        int iTintR = iTintColor == -1 ? 255 : ((iTintColor >> 16) & 0xFF);
        int iTintG = iTintColor == -1 ? 255 : ((iTintColor >> 8) & 0xFF);
        int iTintB = iTintColor == -1 ? 255 : (iTintColor & 0xFF);
        for (Map.Entry<Direction, SpriteRegion> entry : mapSprites.entrySet()) {
            Direction direction = (Direction) entry.getKey();
            SpriteRegion class220Var = (SpriteRegion) entry.getValue();
            if (class220Var != null) {
                int iBindTexture = bindTexture(Mc.INSTANCE.getTextureManager().getTexture(class220Var.atlasId()).getGlId());
                float fMinU = class220Var.minU() + 1.0E-4f;
                float fMinV = class220Var.minV() + 1.0E-4f;
                float fMaxU = class220Var.maxU() - 1.0E-4f;
                float fMaxV = class220Var.maxV() - 1.0E-4f;
                switch (DirectionIndexMap.directionOrdinals[direction.ordinal()]) {
                    case 1:
                        f5 = 1.0f;
                        break;
                    case 2:
                        f5 = 0.7f;
                        break;
                    case 3:
                        f5 = 0.85f;
                        break;
                    default:
                        f5 = 1.0f;
                        break;
                }
                float f11 = f5;
                int iComputeColor = this.colorStack.computeColor(Math.max(0, Math.min(StencilBufferUtil.STENCIL_MASK, (int) (f4 * 255.0f))), (int) (iTintR * f11), (int) (iTintG * f11), (int) (iTintB * f11));
                if (spriteTextures.type().equals("3D")) {
                    switch (DirectionIndexMap.directionOrdinals[direction.ordinal()]) {
                        case 1:
                            quadTexture(matrix4f, f + f7, f10 + f8, f + f6, f10 + f7, f + f7, (f10 + f6) - f8, f, f10 + f7, fMinU, fMinV, fMaxU, fMaxV, iBindTexture, iComputeColor, iComputeColor, iComputeColor, iComputeColor);
                            break;
                        case 2:
                            float f12 = f + f7;
                            float f13 = f10 + f7 + f8;
                            quadTexture(matrix4f, f + f6, f10 + f7, f + f6, f10 + f6 + f9, f + f7, f10 + f6 + f8 + f9, f12, f13, fMinU, fMinV, fMaxU, fMaxV, iBindTexture, iComputeColor, iComputeColor, iComputeColor, iComputeColor);
                            break;
                        case 3:
                            quadTexture(matrix4f, f, f10 + f6 + f9, f + f7, f10 + f6 + f8 + f9, (f + f6) - f7, f10 + f7 + f8, f, f10 + f7, fMinU, fMinV, fMaxU, fMaxV, iBindTexture, iComputeColor, iComputeColor, iComputeColor, iComputeColor);
                            break;
                    }
                } else {
                    f6 = 32.0f * f3;
                    float f14 = f + f6;
                    float f15 = f2 + f6;
                    int iComputeColor2 = this.colorStack.computeColor(iTintColor == -1 ? class220Var.color() : iTintColor, f4);
                    quadTexture(matrix4f, f, f15, f14, f15, f14, f2, f, f2, fMinU, fMinV, fMaxU, fMaxV, iBindTexture, iComputeColor2, iComputeColor2, iComputeColor2, iComputeColor2);
                    if (spriteTextures.glint()) {
                        renderGlint(matrix4f, f, f2, f6, f4, class220Var);
                    }
                }
            }
        }
    }

    public void renderGlint(Matrix4f matrix4f, float f, float f2, float f3, float f4, SpriteRegion region) {
        float fMinU = region.minU() + 1.0E-4f;
        float fMinV = region.minV() + 1.0E-4f;
        float fMaxU = region.maxU() - 1.0E-4f;
        float fMaxV = region.maxV() - 1.0E-4f;
        beginStencil();
        int iBindTexture = bindTexture(Mc.INSTANCE.getTextureManager().getTexture(region.atlasId()).getGlId());
        float f14 = f + f3;
        float f15 = f2 + f3;
        quadTexture(matrix4f, f, f15, f14, f15, f14, f2, f, f2, fMinU, fMinV, fMaxU, fMaxV, iBindTexture, -1, -1, -1, -1);
        prepareStencil(1);
        int iGlintTexture = bindTexture(glintTextureId());
        long j = System.currentTimeMillis() * 8L;
        float fScroll = (j % 110000L) / 110000.0f;
        float fScrollY = (j % 30000L) / 30000.0f;
        double d = 0.17453292519943295d;
        float fCos = (float) Math.cos(d);
        float fSin = (float) Math.sin(d);
        float fScale = 8.0f;
        float u0 = -fScroll;
        float v0 = fScrollY;
        float u1 = (fScale * fCos) - fScroll;
        float v1 = (fScale * fSin) + fScrollY;
        float u2 = (fScale * (fCos - fSin)) - fScroll;
        float v2 = (fScale * (fSin + fCos)) + fScrollY;
        float u3 = (fScale * (-fSin)) - fScroll;
        float v3 = (fScale * fCos) + fScrollY;
        glintQuad(matrix4f, f, f2, f3, f3, u0, v0, u1, v1, u2, v2, u3, v3, iGlintTexture, this.colorStack.computeColor(f4, 255, 255, 255));
        switchBlendFuncAndDraw(() -> GL33.glBlendFuncSeparate(770, 1, 1, 1));
        endStencil();
    }

    public void glintQuad(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, int i, int i2) {
        Window window = Mc.INSTANCE.getWindow();
        Vector4f vector4f = new Vector4f();
        matrix4f.transform(vector4f.set(f, f2, 0.0f, 1.0f));
        float fX = vector4f.x();
        float fY = vector4f.y();
        matrix4f.transform(vector4f.set(f + f3, f2, 0.0f, 1.0f));
        float fX2 = vector4f.x();
        float fY2 = vector4f.y();
        matrix4f.transform(vector4f.set(f + f3, f2 + f4, 0.0f, 1.0f));
        float fX3 = vector4f.x();
        float fY3 = vector4f.y();
        matrix4f.transform(vector4f.set(f, f2 + f4, 0.0f, 1.0f));
        float fX4 = vector4f.x();
        float fY4 = vector4f.y();
        float fMin = Math.min(Math.min(fX, fX2), Math.min(fX3, fX4));
        float fMax = Math.max(Math.max(fX, fX2), Math.max(fX3, fX4));
        float fMin2 = Math.min(Math.min(fY, fY2), Math.min(fY3, fY4));
        float fMax2 = Math.max(Math.max(fY, fY2), Math.max(fY3, fY4));
        float framebufferHeight = window.getFramebufferHeight() - applyContentScale(fMax2);
        float fMethod016 = applyContentScale(fX);
        float fMethod017 = applyContentScale(fY);
        float fMethod018 = applyContentScale(fX2);
        float fMethod019 = applyContentScale(fY2);
        float fMethod0110 = applyContentScale(fX3);
        float fMethod0111 = applyContentScale(fY3);
        float fMethod0112 = applyContentScale(fX4);
        float fMethod0113 = applyContentScale(fY4);
        float fMethod0114 = applyContentScale(fMin);
        float fMethod0115 = applyContentScale(fMax - fMin);
        float fMethod0116 = applyContentScale(fMax2 - fMin2);
        int iMode = ShapeType.TEXTURE.mode();
        writeVec2(fMethod016, fMethod017);
        writeVec2(fMethod0114, framebufferHeight);
        writeVec2(fMethod0115, fMethod0116);
        writeVec2(f5, f6);
        writeVec4(0.0f, 0.0f, 0.0f, 0.0f);
        writeColor(i2);
        writeColor(-1);
        writeFloat(0.0f);
        writeFloat(0.0f);
        writeByte(i);
        writeByte(iMode);
        writeByte(0);
        writeVec2(fMethod018, fMethod019);
        writeVec2(fMethod0114, framebufferHeight);
        writeVec2(fMethod0115, fMethod0116);
        writeVec2(f7, f8);
        writeVec4(0.0f, 0.0f, 0.0f, 0.0f);
        writeColor(i2);
        writeColor(-1);
        writeFloat(0.0f);
        writeFloat(0.0f);
        writeByte(i);
        writeByte(iMode);
        writeByte(0);
        writeVec2(fMethod0110, fMethod0111);
        writeVec2(fMethod0114, framebufferHeight);
        writeVec2(fMethod0115, fMethod0116);
        writeVec2(f9, f10);
        writeVec4(0.0f, 0.0f, 0.0f, 0.0f);
        writeColor(i2);
        writeColor(-1);
        writeFloat(0.0f);
        writeFloat(0.0f);
        writeByte(i);
        writeByte(iMode);
        writeByte(0);
        writeVec2(fMethod0112, fMethod0113);
        writeVec2(fMethod0114, framebufferHeight);
        writeVec2(fMethod0115, fMethod0116);
        writeVec2(f11, f12);
        writeVec4(0.0f, 0.0f, 0.0f, 0.0f);
        writeColor(i2);
        writeColor(-1);
        writeFloat(0.0f);
        writeFloat(0.0f);
        writeByte(i);
        writeByte(iMode);
        writeByte(0);
        writeTriangle(this.vertexCount, this.vertexCount + 1, this.vertexCount + 2);
        writeTriangle(this.vertexCount, this.vertexCount + 2, this.vertexCount + 3);
        this.vertexCount += 4;
        this.indexCount += 6;
    }

    public void drawLine(MatrixStack matrixStack, MsdfFont class161Var, int i, int i2, float f, String str, String str2, int i3, int i4) {
        int width = (int) ((i2 - (class161Var.getWidth(str, i) + class161Var.getWidth(str2, i))) / 2.0f);
        msdfFont(matrixStack.peek().getPositionMatrix(), class161Var, str, width, f, i, 0.0f, i3);
        msdfFont(matrixStack.peek().getPositionMatrix(), class161Var, str2, width + class161Var.getWidth(str, i), f, i, 0.0f, i4);
    }

    public void circle(Matrix4f matrix4f, float f, float f2, float f3, int i) {
        circle(matrix4f, f, f2, f3, 0.0f, 0.0f, 360.0f, 0.0f, -1, i, i, i, i);
    }

    public void circle(Matrix4f matrix4f, float f, float f2, float f3, float f4, int i) {
        circle(matrix4f, f, f2, f3, f4, 0.0f, 360.0f, 0.0f, -1, i, i, i, i);
    }

    public void circle(Matrix4f matrix4f, float f, float f2, float f3, int i, int i2, int i3, int i4) {
        circle(matrix4f, f, f2, f3, 0.0f, 0.0f, 360.0f, 0.0f, -1, i, i2, i3, i4);
    }

    public void circle(Matrix4f matrix4f, float f, float f2, float f3, float f4, int i, int i2, int i3, int i4) {
        circle(matrix4f, f, f2, f3, f4, 0.0f, 360.0f, 0.0f, -1, i, i2, i3, i4);
    }

    public void circle(Matrix4f matrix4f, float f, float f2, float f3, float f4, int i, int i2) {
        circle(matrix4f, f, f2, f3, 0.0f, 0.0f, 360.0f, f4, i, i2, i2, i2, i2);
    }

    public void circle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, int i, int i2) {
        circle(matrix4f, f, f2, f3, f4, 0.0f, 360.0f, f5, i, i2, i2, i2, i2);
    }

    public void circle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, int i) {
        circle(matrix4f, f, f2, f3, 0.0f, f4, f5, 0.0f, -1, i, i, i, i);
    }

    public void circle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, int i) {
        circle(matrix4f, f, f2, f3, f4, f5, f6, 0.0f, -1, i, i, i, i);
    }

    public void circle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, int i, int i2) {
        circle(matrix4f, f, f2, f3, 0.0f, f4, f5, f6, i, i2, i2, i2, i2);
    }

    public void circle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, int i, int i2) {
        circle(matrix4f, f, f2, f3, f4, f5, f6, f7, i, i2, i2, i2, i2);
    }

    public void circle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, int i, int i2, int i3, int i4, int i5) {
        circle(matrix4f, f, f2, f3, 0.0f, f4, f5, f6, i, i2, i3, i4, i5);
    }

    public void circle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, int i, int i2, int i3, int i4, int i5) {
        float f8 = f3 * 2.0f;
        float fRad = MathUtil.rad(f5);
        float fRad2 = MathUtil.rad(f6);
        float f9 = f7 > 0.0f ? 1.0f : 0.0f;
        float f10 = (f7 > 0.0f ? f7 * 0.5f : 0.0f) + (f7 > 0.0f ? f9 : f9 + 1.0f) + 1.0f;
        float f11 = (f3 * 2.0f) + (f10 * 2.0f);
        emitShape(matrix4f, (f - f3) - f10, (f2 - f3) - f10, f11, f11, fRad, fRad2, fRad, fRad2, f3, f4, 0.0f, 0.0f, f7, f9, i, i, i, i, i2, i3, i4, i5, 0, 0, ShapeType.CIRCLE);
    }

    public void msdfFont(Matrix4f matrix4f, MsdfFont class161Var, String str, float f, float f2, float f3, float f4, int i) {
        class161Var.applyGlyphs(matrix4f, this, bindTexture(class161Var.getTextureId()), str, f3, 0.0f, 0.0f, f, f2 + ((class161Var.metrics().ascent() != 0.0f ? class161Var.metrics().ascent() : class161Var.metrics().lineHeight()) * f3), i);
    }

    public void msdfText(Matrix4f matrix4f, MsdfFont class161Var, Text text, float f, float f2, float f3, float f4, float f5, int i) {
        int iBindTexture = bindTexture(class161Var.getTextureId());
        float fAscent = class161Var.metrics().ascent() != 0.0f ? class161Var.metrics().ascent() : class161Var.metrics().lineHeight();
        float height = class161Var.getHeight(f3);
        int i2 = (i >> 24) & StencilBufferUtil.STENCIL_MASK;
        float[] fArr = {0.0f};
        float[] fArr2 = {0.0f};
        int[] iArr = {i};
        boolean[] zArr = {false};
        int[] iArrGlyphCodes = class161Var.glyphCodes();
        ThreadLocalRandom threadLocalRandomCurrent = ThreadLocalRandom.current();
        int[] iArr2 = {-1};
        text.asOrderedText().accept((charIndex, style, codePoint) -> {
            int i3;
            char c = (char) codePoint;
            if (c == '\n') {
                fArr[0] = 0.0f;
                fArr2[0] = fArr2[0] + height;
                iArr2[0] = -1;
                return true;
            }
            if (style.getColor() != null) {
                iArr[0] = (i2 << 24) | (style.getColor().getRgb() & 16777215);
                zArr[0] = true;
            } else if (zArr[0]) {
                iArr[0] = i;
                zArr[0] = false;
            }
            int i4 = c;
            if (style.isObfuscated() && !Character.isWhitespace(c)) {
                if (iArrGlyphCodes.length == 0) {
                    return true;
                }
                int i5 = 0;
                do {
                    i3 = iArrGlyphCodes[threadLocalRandomCurrent.nextInt(iArrGlyphCodes.length)];
                    i5++;
                    if (class161Var.glyphWidth(i3, f3) > 0.01f) {
                        break;
                    }
                } while (i5 < 20);
                i4 = i3;
            }
            int iGlyphIndex = class161Var.glyphIndex(i4);
            if (iGlyphIndex == -1) {
                iArr2[0] = -1;
                return true;
            }
            if (iArr2[0] != -1) {
                fArr[0] = fArr[0] + (class161Var.kerningAdvance(iArr2[0], i4) * f3);
            }
            fArr[0] = fArr[0] + class161Var.applyGlyph(matrix4f, this, iGlyphIndex, f3, f + fArr[0], f2 + fArr2[0] + (fAscent * f3), iArr[0], iBindTexture) + f4 + f5;
            iArr2[0] = i4;
            return true;
        });
    }

    public void msdfText(Matrix4f matrix4f, MsdfFont class161Var, Text text, float f, float f2, float f3, int i) {
        msdfText(matrix4f, class161Var, text, f, f2, f3, 0.05f, 0.0f, i);
    }

    public void hollowCircle(Matrix4f matrix4f, float f, float f2, float f3, float f4, int i) {
        circle(matrix4f, f, f2, f3, Math.max(0.0f, f3 - f4), 0.0f, 360.0f, 0.0f, -1, i, i, i, i);
    }

    public void arc(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, int i) {
        circle(matrix4f, f, f2, f3, Math.max(0.0f, f3 - f6), f4, f5, 0.0f, -1, i, i, i, i);
    }

    public void circleChecker(Matrix4f matrix4f, float f, float f2, float f3, int i, int i2) {
        roundedChecker(matrix4f, f - f3, f2 - f3, f3 * 2.0f, f3 * 2.0f, f3, 2.0f, i, i2);
    }

    public void roundedChecker(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, int i, int i2) {
        emitShape(matrix4f, f, f2, f3, f4, 0.0f, 0.0f, 1.0f, 1.0f, f5, f5, f5, f5, 0.0f, f6, i, i, i, i, i2, i2, i2, i2, 0, 0, ShapeType.CHECKER);
    }

    public void msdfFontHorizontalC(Matrix4f matrix4f, MsdfFont class161Var, String str, float f, float f2, float f3, float f4, int i) {
        msdfFont(matrix4f, class161Var, str, f - Math.round(class161Var.getWidth(str, f3) / 2.0f), f2, f3, f4, i);
    }

    public void msdfFontVerticalC(Matrix4f matrix4f, MsdfFont class161Var, String str, float f, float f2, float f3, float f4, int i) {
        msdfFont(matrix4f, class161Var, str, f, f2 - Math.round(class161Var.getHeight(f3) / 2.0f), f3, f4, i);
    }

    public void msdfFontVerticalCHorizontalC(Matrix4f matrix4f, MsdfFont class161Var, String str, float f, float f2, float f3, float f4, int i) {
        msdfFont(matrix4f, class161Var, str, f - Math.round(class161Var.getWidth(str, f3) / 2.0f), Math.round(f2 - (class161Var.getHeight(f3) / 2.0f)), f3, f4, i);
    }

    public void msdfFontWithHorizontalGradient(Matrix4f matrix4f, MsdfFont class161Var, String str, float f, float f2, float f3, float f4, int i, int i2) {
        class161Var.applyGlyphsWithHorizontalGradient(matrix4f, this, bindTexture(class161Var.getTextureId()), str, f3, 0.0f, 0.0f, f, f2 + ((class161Var.metrics().ascent() != 0.0f ? class161Var.metrics().ascent() : class161Var.metrics().lineHeight()) * f3), i, i2);
    }

    public void textureMsdf(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int i, int i2) {
        textureMsdf(matrix4f, f, f2, f3, f4, f5, f6, f7, f8, i, i2, i2, i2, i2);
    }

    public void texture(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int i, int i2) {
        texture(matrix4f, f, f2, f3, f4, f5, f6, f7, f8, i, i2, i2, i2, i2);
    }

    public void texture(Matrix4f matrix4f, float f, float f2, float f3, float f4, int i, int i2, int i3, int i4, int i5) {
        texture(matrix4f, f, f2, f3, f4, 0.0f, 0.0f, 1.0f, 1.0f, i, i2, i3, i4, i5);
    }

    public void textureVerticalC(Matrix4f matrix4f, GlTexture class073Var, float f, float f2, int i, int i2, int i3) {
        texture(matrix4f, f, f2 - (MathHelper.ceil(i2) / 2.0f), i, i2, bindTexture(class073Var.textureWithSTB()), i3);
    }

    public void textureVerticalCHorizontalC(Matrix4f matrix4f, GlTexture class073Var, float f, float f2, int i, int i2, int i3) {
        texture(matrix4f, f - (MathHelper.ceil(i) / 2.0f), f2 - (MathHelper.ceil(i2) / 2.0f), i, i2, bindTexture(class073Var.textureWithSTB()), i3);
    }

    public void texture(Matrix4f matrix4f, float f, float f2, float f3, float f4, int i, int i2) {
        texture(matrix4f, f, f2, f3, f4, 0.0f, 0.0f, 1.0f, 1.0f, i, i2, i2, i2, i2);
    }

    public void texture(Matrix4f matrix4f, float f, float f2, float f3, float f4, int i, int i2, boolean z) {
        if (z) {
            texture(matrix4f, f, f2, f3, f4, 0.0f, 1.0f, 1.0f, 0.0f, i, i2, i2, i2, i2);
        } else {
            texture(matrix4f, f, f2, f3, f4, 0.0f, 0.0f, 1.0f, 1.0f, i, i2, i2, i2, i2);
        }
    }

    public void roundedRectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        emitShape(matrix4f, f, f2, f3, f4, 0.0f, 0.0f, 1.0f, 1.0f, f5, f6, f7, f8, f9, f10, i, i2, i3, i4, i5, i6, i7, i8, 0, 0, ShapeType.ROUNDED_RECTANGLE);
    }

    public void radialRoundedRectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, int i, int i2) {
        emitShape(matrix4f, f, f2, f3, f4, 0.0f, 0.0f, 1.0f, 1.0f, f5, f5, f5, f5, 0.0f, 0.0f, i2, i2, i2, i2, i, i, i, i, 0, 0, ShapeType.RADIAL_ROUNDED_RECTANGLE);
    }

    public void roundedRectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, int i) {
        roundedRectangle(matrix4f, f, f2, f3, f4, f5, f5, f5, f5, 0.0f, 0.0f, -1, -1, -1, -1, i, i, i, i);
    }

    public void roundedRectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, int i, int i2) {
        roundedRectangle(matrix4f, f, f2, f3, f4, f5, f5, f5, f5, f6, 1.0f, i, i, i, i, i2, i2, i2, i2);
    }

    public void roundedRectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        roundedRectangle(matrix4f, f, f2, f3, f4, f5, f5, f5, f5, f6, 1.0f, i, i2, i3, i4, i5, i6, i7, i8);
    }

    public void roundedRectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, int i, int i2, int i3, int i4) {
        roundedRectangle(matrix4f, f, f2, f3, f4, f5, f5, f5, f5, 0.0f, 0.0f, -1, -1, -1, -1, i, i2, i3, i4);
    }

    public void roundedRectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, int i, int i2, int i3, int i4, int i5) {
        roundedRectangle(matrix4f, f, f2, f3, f4, f5, f5, f5, f5, f6, 1.0f, i, i, i, i, i2, i3, i4, i5);
    }

    public void roundedRectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, int i, int i2, int i3, int i4, int i5) {
        roundedRectangle(matrix4f, f, f2, f3, f4, f5, f6, f7, f8, f9, 1.0f, i, i, i, i, i2, i3, i4, i5);
    }

    public void roundedRectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int i) {
        roundedRectangle(matrix4f, f, f2, f3, f4, f5, f6, f7, f8, 0.0f, 0.0f, -1, -1, -1, -1, i, i, i, i);
    }

    public void roundedRectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, int i, int i2) {
        roundedRectangle(matrix4f, f, f2, f3, f4, f5, f6, f7, f8, f9, f10, i, i, i, i, i2, i2, i2, i2);
    }

    public void roundedRectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, int i) {
        roundedRectangle(matrix4f, f, f2, f3, f4, f5, f6, f7, f8, 0.0f, f9, -1, -1, -1, -1, i, i, i, i);
    }

    public void roundedRectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, int i, int i2) {
        roundedRectangle(matrix4f, f, f2, f3, f4, f5, f6, f7, f8, f9, 0.0f, i, i, i, i, i2, i2, i2, i2);
    }

    public void roundedTexture(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int i, int i2, int i3, int i4, int i5) {
        emitShape(matrix4f, f, f2, f3, f4, 0.0f, 0.0f, 1.0f, 1.0f, f5, f6, f7, f8, 0.0f, 0.0f, -1, -1, -1, -1, i2, i3, i4, i5, i, 0, ShapeType.ROUNDED_TEXTURE);
    }

    public void roundedTexture(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, int i, int i2, int i3, int i4, int i5) {
        emitShape(matrix4f, f, f2, f3, f4, f9, f10, f11, f12, f5, f6, f7, f8, 0.0f, 0.0f, -1, -1, -1, -1, i2, i3, i4, i5, i, 0, ShapeType.ROUNDED_TEXTURE);
    }

    public void roundedTexture(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int i, int i2) {
        roundedTexture(matrix4f, f, f2, f3, f4, f5, f6, f7, f8, i, i2, i2, i2, i2);
    }

    public void roundedTexture(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, int i, int i2, int i3, int i4, int i5) {
        roundedTexture(matrix4f, f, f2, f3, f4, f5, f5, f5, f5, i, i2, i3, i4, i5);
    }

    public void roundedTexture(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, int i, int i2) {
        roundedTexture(matrix4f, f, f2, f3, f4, f5, f5, f5, f5, i, i2, i2, i2, i2);
    }

    public void roundedTexture(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, int i, int i2) {
        roundedTexture(matrix4f, f, f2, f3, f4, f5, f5, f5, f5, f6, f7, f8, f9, i, i2, i2, i2, i2);
    }

    public void writeTransformedPosition(Matrix4f matrix4f, float f, float f2) {
        Vector4f vector4fTransform = matrix4f.transform(this.tempVector.set(f, f2, 0.0f, 1.0f));
        writeVec2(applyContentScale(vector4fTransform.x()), applyContentScale(vector4fTransform.y()));
    }

    public void updateContentScale(Window window) {
        if (WindowUtil.isMac()) {
            this.contentScale = WindowUtil.getWindowContentScale(window.getHandle());
        } else {
            this.contentScale = 1.0f;
        }
    }

    public float applyContentScale(float f) {
        return this.contentScale == 1.0f ? f : f * this.contentScale;
    }

    public int scaledWidth(Window window) {
        return Math.max(1, Math.round(window.getFramebufferWidth() / this.contentScale));
    }

    public int scaledHeight(Window window) {
        return Math.max(1, Math.round(window.getFramebufferHeight() / this.contentScale));
    }

    public void writeVec2(float f, float f2) {
        this.vertexBuffer.requireMoreFreeBytes(8L);
        long jEffectiveAddress = this.vertexBuffer.effectiveAddress();
        this.vertexBuffer.writeFloat(jEffectiveAddress, f);
        this.vertexBuffer.writeFloat(jEffectiveAddress + 4, f2);
        this.vertexBuffer.offset(8L);
    }

    public void writeTriangle(int i, int i2, int i3) {
        this.indexBuffer.requireMoreFreeBytes(12L);
        long jEffectiveAddress = this.indexBuffer.effectiveAddress();
        this.indexBuffer.writeInt(jEffectiveAddress, i);
        this.indexBuffer.writeInt(jEffectiveAddress + 4, i2);
        this.indexBuffer.writeInt(jEffectiveAddress + 8, i3);
        this.indexBuffer.offset(12L);
    }

    public void writeVec4(float f, float f2, float f3, float f4) {
        this.vertexBuffer.requireMoreFreeBytes(16L);
        long jEffectiveAddress = this.vertexBuffer.effectiveAddress();
        this.vertexBuffer.writeFloat(jEffectiveAddress, f);
        this.vertexBuffer.writeFloat(jEffectiveAddress + 4, f2);
        this.vertexBuffer.writeFloat(jEffectiveAddress + 8, f3);
        this.vertexBuffer.writeFloat(jEffectiveAddress + 12, f4);
        this.vertexBuffer.offset(16L);
    }

    public void writeColorBytes(int i, int i2, int i3, int i4) {
        this.vertexBuffer.requireMoreFreeBytes(4L);
        long jEffectiveAddress = this.vertexBuffer.effectiveAddress();
        this.vertexBuffer.writeByte(jEffectiveAddress, i);
        this.vertexBuffer.writeByte(jEffectiveAddress + 1, i2);
        this.vertexBuffer.writeByte(jEffectiveAddress + 2, i3);
        this.vertexBuffer.writeByte(jEffectiveAddress + 3, i4);
        this.vertexBuffer.offset(4L);
    }

    public void writeInt(int i) {
        this.vertexBuffer.requireMoreFreeBytes(4L);
        this.vertexBuffer.writeInt(this.vertexBuffer.effectiveAddress(), i);
        this.vertexBuffer.offset(4L);
    }

    public void writeFloat(float f) {
        this.vertexBuffer.requireMoreFreeBytes(4L);
        this.vertexBuffer.writeFloat(this.vertexBuffer.effectiveAddress(), f);
        this.vertexBuffer.offset(4L);
    }

    public void writeByte(int i) {
        this.vertexBuffer.requireMoreFreeBytes(1L);
        this.vertexBuffer.writeByte(this.vertexBuffer.effectiveAddress(), i);
        this.vertexBuffer.offset(1L);
    }

    public void writeColor(int i) {
        writeColorBytes((i >> 16) & StencilBufferUtil.STENCIL_MASK, (i >> 8) & StencilBufferUtil.STENCIL_MASK, i & StencilBufferUtil.STENCIL_MASK, (i >> 24) & StencilBufferUtil.STENCIL_MASK);
    }

    public ColorStack colorStack() {
        return this.colorStack;
    }

    public ScissorStack scissorStack() {
        return this.scissorStack;
    }

    public boolean building() {
        return this.building;
    }

    public ScissorBounds lastBounds() {
        return this.lastBounds;
    }
}
