package aethereal.resource;

import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {
    public final ByteBuffer buffer;

    @Override
    public int read() {
        if (this.buffer.hasRemaining()) {
            return this.buffer.get() & 255;
        }
        return -1;
    }

    public static ByteBufferInputStream of(ByteBuffer byteBuffer) {
        return new ByteBufferInputStream(byteBuffer);
    }

    public ByteBufferInputStream(ByteBuffer byteBuffer) {
        this.buffer = byteBuffer;
    }
}
