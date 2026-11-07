package aethereal.render;
import aethereal.resource.ResourceSource;
import aethereal.util.StencilBufferUtil;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;
import java.io.IOException;
import java.awt.image.DataBufferInt;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import lombok.NonNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

public class SvgTexture {
    public static final SVGUniverse svgUniverse = new SVGUniverse();

    @NonNull
    public final ResourceSource resource;
    public final int width;
    public final int height;
    public int magFilter = 9728;
    public int minFilter = 9728;
    public int textureId = 0;

    public int id() {
        if (this.textureId == 0) {
            try {
                this.textureId = uploadTexture();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return this.textureId;
    }

    public int uploadTexture() throws IOException {
        int[] data = ((DataBufferInt) SvgRasterizer.rasterize(this.resource, loadDiagram(this.resource), this.width, this.height).getRaster().getDataBuffer()).getData();
        ByteBuffer byteBufferMemAlloc = MemoryUtil.memAlloc(data.length * 4);
        try {
            for (int i : data) {
                byteBufferMemAlloc.put((byte) ((i >> 16) & StencilBufferUtil.STENCIL_MASK));
                byteBufferMemAlloc.put((byte) ((i >> 8) & StencilBufferUtil.STENCIL_MASK));
                byteBufferMemAlloc.put((byte) (i & StencilBufferUtil.STENCIL_MASK));
                byteBufferMemAlloc.put((byte) ((i >> 24) & StencilBufferUtil.STENCIL_MASK));
            }
            byteBufferMemAlloc.flip();
            int iGlGenTextures = GL11.glGenTextures();
            GL11.glBindTexture(3553, iGlGenTextures);
            GL11.glTexParameteri(3553, 10241, this.minFilter);
            GL11.glTexParameteri(3553, 10240, this.magFilter);
            GL11.glTexParameteri(3553, 10242, 33071);
            GL11.glTexParameteri(3553, 10243, 33071);
            GL11.glPixelStorei(3317, 1);
            GL11.glTexImage2D(3553, 0, 6408, this.width, this.height, 0, 6408, 5121, byteBufferMemAlloc);
            GL11.glPixelStorei(3317, 4);
            GL30.glGenerateMipmap(3553);
            GL11.glBindTexture(3553, 0);
            return iGlGenTextures;
        } finally {
            MemoryUtil.memFree(byteBufferMemAlloc);
        }
    }

    public static SVGDiagram loadDiagram(ResourceSource class178Var) {
        SVGDiagram diagram;
        try {
            InputStream inputStreamStream = class178Var.stream();
            try {
                if (inputStreamStream == null) {
                    throw new IOException("Stream is null");
                }
                synchronized (svgUniverse) {
                    diagram = svgUniverse.getDiagram(svgUniverse.loadSVG(inputStreamStream, class178Var.toString()));
                    if (diagram == null) {
                        throw new IOException("Diagram is null");
                    }
                    diagram.setIgnoringClipHeuristic(true);
                }
                if (inputStreamStream != null) {
                    inputStreamStream.close();
                }
                return diagram;
            } catch (Throwable th) {
                if (inputStreamStream != null) {
                    try {
                        inputStreamStream.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void dispose() {
        if (this.textureId != 0) {
            GL11.glDeleteTextures(this.textureId);
            this.textureId = 0;
        }
    }

    public SvgTexture(@NonNull ResourceSource class178Var, int i, int i2) {
        if (class178Var == null) {
            throw new NullPointerException("resource is marked non-null but is null");
        }
        this.resource = class178Var;
        this.width = i;
        this.height = i2;
    }

    public void setMagFilter(int i) {
        this.magFilter = i;
    }

    public void setMinFilter(int i) {
        this.minFilter = i;
    }
}
