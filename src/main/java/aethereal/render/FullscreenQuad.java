package aethereal.render;

import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class FullscreenQuad {
    public Integer vaoId;

    public void draw() {
        GL30.glBindVertexArray(getOrCreateVao());
        GL15.glDrawElements(4, 6, 5121, 0L);
    }

    public int getOrCreateVao() {
        if (this.vaoId == null) {
            Integer numValueOf = Integer.valueOf(GL30.glGenVertexArrays());
            this.vaoId = numValueOf;
            GL30.glBindVertexArray(numValueOf.intValue());
            GL15.glBindBuffer(34962, GL15.glGenBuffers());
            GL15.glBindBuffer(34963, GL15.glGenBuffers());
            GL20.glEnableVertexAttribArray(0);
            GL20.glEnableVertexAttribArray(1);
            GL20.glVertexAttribPointer(0, 2, 5126, false, 16, 0L);
            GL20.glVertexAttribPointer(1, 2, 5126, false, 16, 8L);
            MemoryStack memoryStackStackPush = MemoryStack.stackPush();
            try {
                ByteBuffer byteBufferMalloc = memoryStackStackPush.malloc(64);
                ByteBuffer byteBufferMalloc2 = memoryStackStackPush.malloc(24);
                byteBufferMalloc.putFloat(-1.0f).putFloat(-1.0f).putFloat(0.0f).putFloat(0.0f);
                byteBufferMalloc.putFloat(1.0f).putFloat(-1.0f).putFloat(1.0f).putFloat(0.0f);
                byteBufferMalloc.putFloat(1.0f).putFloat(1.0f).putFloat(1.0f).putFloat(1.0f);
                byteBufferMalloc.putFloat(-1.0f).putFloat(1.0f).putFloat(0.0f).putFloat(1.0f);
                byteBufferMalloc2.put((byte) 0).put((byte) 1).put((byte) 2);
                byteBufferMalloc2.put((byte) 0).put((byte) 2).put((byte) 3);
                GL15.glBufferData(34962, MemoryUtil.memSlice(byteBufferMalloc.flip()), 35044);
                GL15.glBufferData(34963, MemoryUtil.memSlice(byteBufferMalloc2.flip()), 35044);
                if (memoryStackStackPush != null) {
                    memoryStackStackPush.close();
                }
            } catch (Throwable th) {
                if (memoryStackStackPush != null) {
                    try {
                        memoryStackStackPush.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        }
        return this.vaoId.intValue();
    }
}
