package aethereal.render;
import aethereal.resource.Reloadable;
import aethereal.resource.ResourceSource;
import aethereal.util.StencilBufferUtil;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

public class GlTexture implements Reloadable {
    public final ByteBuffer imageBuffer;
    public Integer textureId;
    public int width;
    public int height;
    public int magFilterMode;
    public int minFilterMode;
    public int wrapModeX;
    public int wrapModeY;
    public boolean mipmap;

    public GlTexture(ResourceSource class178Var) {
        this(class178Var.asDirectByteBuffer());
    }

    @Override
    public void reload() {
        free();
    }

    public void bindToSlot(int i) {
        GL33.glActiveTexture(33984 + i);
        GL33.glBindTexture(3553, textureWithSTB());
    }

    public void free() {
        if (this.textureId != null) {
            GL33.glDeleteTextures(this.textureId.intValue());
            this.textureId = null;
        }
    }

    public int textureWithSTB() {
        if (this.textureId != null) {
            return this.textureId.intValue();
        }
        GL33.glBindTexture(3553, 0);
        if (isWebp(this.imageBuffer)) {
            Integer numValueOf = Integer.valueOf(loadWithImageIo());
            this.textureId = numValueOf;
            return numValueOf.intValue();
        }
        Integer numValueOf2 = Integer.valueOf(loadWithStb());
        this.textureId = numValueOf2;
        return numValueOf2.intValue();
    }

    public int texture() {
        if (this.textureId != null) {
            return this.textureId.intValue();
        }
        Integer numValueOf = Integer.valueOf(buildTexture());
        this.textureId = numValueOf;
        return numValueOf.intValue();
    }

    public GlTexture setDimensions(int i, int i2) {
        this.width = i;
        this.height = i2;
        return this;
    }

    public int buildTexture() {
        if (this.textureId != null) {
            return this.textureId.intValue();
        }
        if (this.width <= 0 || this.height <= 0) {
            throw new IllegalStateException("Width and Height must be set before building the texture without stb_image.");
        }
        int iGlGenTextures = GL33.glGenTextures();
        GL33.glBindTexture(3553, iGlGenTextures);
        GL33.glPixelStorei(3317, 1);
        GL33.glTexParameteri(3553, 10242, this.wrapModeX);
        GL33.glTexParameteri(3553, 10243, this.wrapModeY);
        GL33.glTexParameteri(3553, 10241, this.minFilterMode);
        GL33.glTexParameteri(3553, 10240, this.magFilterMode);
        // Сброс позиции буфера: glTexImage2D читает от текущей позиции,
        // а предыдущие вызовы (особенно STB/LWJGL) могли её сдвинуть ->
        // иначе драйвер выходит за границы буфера (EXCEPTION_ACCESS_VIOLATION).
        this.imageBuffer.rewind();
        GL33.glTexImage2D(3553, 0, 6408, this.width, this.height, 0, 6408, 5121, this.imageBuffer);
        if (this.mipmap) {
            GL33.glGenerateMipmap(3553);
        }
        GL33.glBindTexture(3553, 0);
        this.textureId = Integer.valueOf(iGlGenTextures);
        return iGlGenTextures;
    }

    public int loadWithStb() {
        MemoryStack memoryStackStackPush = MemoryStack.stackPush();
        try {
            IntBuffer intBufferMallocInt = memoryStackStackPush.mallocInt(1);
            IntBuffer intBufferMallocInt2 = memoryStackStackPush.mallocInt(1);
            ByteBuffer byteBufferStbi_load_from_memory = STBImage.stbi_load_from_memory(this.imageBuffer, intBufferMallocInt, intBufferMallocInt2, memoryStackStackPush.mallocInt(1), 4);
            if (byteBufferStbi_load_from_memory == null) {
                throw new IllegalStateException(String.format("Failed to load texture from memory.%n  STB Reason: %s%n  Buffer info: [capacity=%d, position=%d, limit=%d, remaining=%d, isDirect=%b]%n  First 16 bytes: %s", STBImage.stbi_failure_reason(), Integer.valueOf(this.imageBuffer.capacity()), Integer.valueOf(this.imageBuffer.position()), Integer.valueOf(this.imageBuffer.limit()), Integer.valueOf(this.imageBuffer.remaining()), Boolean.valueOf(this.imageBuffer.isDirect()), hexDump(this.imageBuffer, 16)));
            }
            int i = intBufferMallocInt.get();
            int i2 = intBufferMallocInt2.get();
            if (memoryStackStackPush != null) {
                memoryStackStackPush.close();
            }
            int iMethod003 = uploadTexture(byteBufferStbi_load_from_memory, i, i2);
            STBImage.stbi_image_free(byteBufferStbi_load_from_memory);
            this.width = i;
            this.height = i2;
            return iMethod003;
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

    public int loadWithImageIo() {
        try {
            byte[] bArr = new byte[this.imageBuffer.remaining()];
            this.imageBuffer.duplicate().get(bArr);
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bArr));
            if (bufferedImage == null) {
                throw new IllegalStateException("ImageIO failed to decode image (unsupported format or corrupt data). First 16 bytes: " + hexDump(this.imageBuffer, 16));
            }
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            int[] rgb = bufferedImage.getRGB(0, 0, width, height, (int[]) null, 0, width);
            ByteBuffer byteBufferOrder = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());
            for (int i : rgb) {
                byteBufferOrder.put((byte) ((i >> 16) & StencilBufferUtil.STENCIL_MASK));
                byteBufferOrder.put((byte) ((i >> 8) & StencilBufferUtil.STENCIL_MASK));
                byteBufferOrder.put((byte) (i & StencilBufferUtil.STENCIL_MASK));
                byteBufferOrder.put((byte) ((i >> 24) & StencilBufferUtil.STENCIL_MASK));
            }
            byteBufferOrder.flip();
            this.width = width;
            this.height = height;
            return uploadTexture(byteBufferOrder, width, height);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e2) {
            throw new IllegalStateException("Failed to decode image via ImageIO", e2);
        }
    }

    public int uploadTexture(ByteBuffer byteBuffer, int i, int i2) {
        int iGlGenTextures = GL33.glGenTextures();
        GL33.glBindTexture(3553, iGlGenTextures);
        GL33.glPixelStorei(3312, 0);
        GL33.glPixelStorei(3313, 0);
        GL33.glPixelStorei(3314, 0);
        GL33.glPixelStorei(32878, 0);
        GL33.glPixelStorei(3315, 0);
        GL33.glPixelStorei(3316, 0);
        GL33.glPixelStorei(32877, 0);
        GL33.glPixelStorei(3317, 1);
        GL33.glTexParameteri(3553, 10242, this.wrapModeX);
        GL33.glTexParameteri(3553, 10243, this.wrapModeY);
        GL33.glTexParameteri(3553, 10241, this.minFilterMode);
        GL33.glTexParameteri(3553, 10240, this.magFilterMode);
        GL33.glTexImage2D(3553, 0, 6408, i, i2, 0, 6408, 5121, byteBuffer);
        if (this.mipmap) {
            GL33.glGenerateMipmap(3553);
        }
        return iGlGenTextures;
    }

    public boolean isWebp(ByteBuffer byteBuffer) {
        if (byteBuffer.remaining() < 12) {
            return false;
        }
        int iPosition = byteBuffer.position();
        return byteBuffer.get(iPosition) == 82 && byteBuffer.get(iPosition + 1) == 73 && byteBuffer.get(iPosition + 2) == 70 && byteBuffer.get(iPosition + 3) == 70 && byteBuffer.get(iPosition + 8) == 87 && byteBuffer.get(iPosition + 9) == 69 && byteBuffer.get(iPosition + 10) == 66 && byteBuffer.get(iPosition + 11) == 80;
    }

    public String hexDump(ByteBuffer byteBuffer, int i) {
        int iPosition = byteBuffer.position();
        byteBuffer.position(0);
        StringBuilder sb = new StringBuilder("[");
        int iMin = Math.min(i, byteBuffer.remaining());
        for (int i2 = 0; i2 < iMin; i2++) {
            if (i2 > 0) {
                sb.append(" ");
            }
            sb.append(String.format("%02X", Integer.valueOf(byteBuffer.get() & 255)));
        }
        byteBuffer.position(iPosition);
        return sb.append("]").toString();
    }

    public GlTexture(ByteBuffer byteBuffer) {
        this.magFilterMode = 9729;
        this.minFilterMode = 9729;
        this.wrapModeX = 10497;
        this.wrapModeY = 10497;
        this.imageBuffer = byteBuffer;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public GlTexture magFilter(int i) {
        this.magFilterMode = i;
        return this;
    }

    public GlTexture minFilter(int i) {
        this.minFilterMode = i;
        return this;
    }

    public GlTexture wrapX(int i) {
        this.wrapModeX = i;
        return this;
    }

    public GlTexture wrapY(int i) {
        this.wrapModeY = i;
        return this;
    }

    public GlTexture generateMipMap(boolean z) {
        this.mipmap = z;
        return this;
    }
}
