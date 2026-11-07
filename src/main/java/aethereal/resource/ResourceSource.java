package aethereal.resource;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public interface ResourceSource {
    InputStream stream();

    default void writeToByteBuffer(ByteBuffer byteBuffer) {
        byteBuffer.put(bytes());
    }

    default ByteBuffer asDirectByteBuffer() {
        byte[] bArrBytes = bytes();
        ByteBuffer byteBufferAllocateDirect = ByteBuffer.allocateDirect(bArrBytes.length);
        byteBufferAllocateDirect.put(bArrBytes);
        return byteBufferAllocateDirect.flip();
    }

    default byte[] bytes() {
        try (InputStream inputStreamStream = stream()) {
            return inputStreamStream.readAllBytes();
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    default String utf8() {
        return new String(bytes(), StandardCharsets.UTF_8);
    }
}
