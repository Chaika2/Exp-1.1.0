package aethereal.render;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.function.IntConsumer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

public class ShaderUniform {
    public static final FloatBuffer matrixBuffer = MemoryUtil.memAllocFloat(16);
    public final String name;
    public int location;

    public IntConsumer programCompileCallback() {
        return this::resolveLocation;
    }

    public void uploadInt(int i) {
        GL33.glUniform1i(this.location, i);
    }

    public void uploadFloat(float f) {
        GL33.glUniform1f(this.location, f);
    }

    public void uploadFloatArray(float[] fArr) {
        FloatBuffer floatBufferMemAllocFloat = MemoryUtil.memAllocFloat(fArr.length);
        floatBufferMemAllocFloat.put(fArr).flip();
        GL33.glUniform1fv(this.location, floatBufferMemAllocFloat);
        MemoryUtil.memFree(floatBufferMemAllocFloat);
    }

    public void uploadVec2(float f, float f2) {
        GL33.glUniform2f(this.location, f, f2);
    }

    public void uploadIVec2(int i, int i2) {
        GL33.glUniform2i(this.location, i, i2);
    }

    public void uploadVec3(float f, float f2, float f3) {
        GL33.glUniform3f(this.location, f, f2, f3);
    }

    public void uploadVec4(float f, float f2, float f3, float f4) {
        GL33.glUniform4f(this.location, f, f2, f3, f4);
    }

    public void uploadMatrix4f(Matrix4f matrix4f) {
        GL33.glUniformMatrix4fv(this.location, false, matrix4f.get(matrixBuffer));
    }

    public void uploadIntBuffer(IntBuffer intBuffer) {
        intBuffer.rewind();
        GL33.glUniform1iv(this.location, intBuffer);
    }

    public void uploadFloatBuffer(FloatBuffer floatBuffer) {
        GL33.glUniform1fv(this.location, floatBuffer);
    }

    public void resolveLocation(int i) {
        this.location = GL33.glGetUniformLocation(i, this.name);
    }

    public ShaderUniform(String str) {
        this.name = str;
    }
}
