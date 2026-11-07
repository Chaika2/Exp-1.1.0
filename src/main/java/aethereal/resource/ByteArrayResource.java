package aethereal.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteArrayResource implements ResourceSource {
    public final byte[] bytes;

    @Override
    public InputStream stream() {
        return new ByteArrayInputStream(this.bytes);
    }

    @Override
    public void writeToByteBuffer(ByteBuffer byteBuffer) {
        byteBuffer.put(this.bytes);
    }

    @Override
    public ByteBuffer asDirectByteBuffer() {
        ByteBuffer byteBufferAllocateDirect = ByteBuffer.allocateDirect(this.bytes.length);
        byteBufferAllocateDirect.put(this.bytes);
        return byteBufferAllocateDirect.flip();
    }

    @Override
    public byte[] bytes() {
        return this.bytes;
    }

    public ByteArrayResource(byte[] bArr) {
        this.bytes = bArr;
    }
}
