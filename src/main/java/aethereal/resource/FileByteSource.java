package aethereal.resource;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;

public class FileByteSource implements ResourceSource {
    public final Path path;

    @Override
    public InputStream stream() {
        return new ByteBufferInputStream(readBuffer());
    }

    @Override
    public void writeToByteBuffer(ByteBuffer byteBuffer) {
        byteBuffer.put(readBuffer());
    }

    @Override
    public ByteBuffer asDirectByteBuffer() {
        return readBuffer();
    }

    public ByteBuffer readBuffer() {
        try {
            FileChannel fileChannelOpen = FileChannel.open(this.path, new OpenOption[0]);
            try {
                MappedByteBuffer map = fileChannelOpen.map(FileChannel.MapMode.READ_ONLY, 0L, fileChannelOpen.size());
                if (fileChannelOpen != null) {
                    fileChannelOpen.close();
                }
                return map;
            } catch (Throwable th) {
                if (fileChannelOpen != null) {
                    try {
                        fileChannelOpen.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    public FileByteSource(Path path) {
        this.path = path;
    }
}
