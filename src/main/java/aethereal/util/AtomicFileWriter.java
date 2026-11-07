package aethereal.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;

public class AtomicFileWriter {
    public static void writeBytes(Path path, byte[] bArr, OpenOption... openOptionArr) throws IOException {
        Path absolutePath = path.toAbsolutePath();
        Path parent = absolutePath.getParent();
        if (parent == null) {
            throw new IOException("Target must have a parent directory: " + String.valueOf(path));
        }
        Path pathCreateTempFile = Files.createTempFile(parent, absolutePath.getFileName().toString(), ".tmp", new FileAttribute[0]);
        try {
            FileChannel fileChannelOpen = FileChannel.open(pathCreateTempFile, openOptionArr);
            try {
                ByteBuffer byteBufferWrap = ByteBuffer.wrap(bArr);
                while (byteBufferWrap.hasRemaining()) {
                    fileChannelOpen.write(byteBufferWrap);
                }
                fileChannelOpen.force(true);
                if (fileChannelOpen != null) {
                    fileChannelOpen.close();
                }
                moveAtomically(pathCreateTempFile, absolutePath);
                syncDirectory(parent);
                if (1 == 0) {
                    try {
                        Files.deleteIfExists(pathCreateTempFile);
                    } catch (IOException e) {
                    }
                }
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
        } catch (Throwable th3) {
            if (0 == 0) {
                try {
                    Files.deleteIfExists(pathCreateTempFile);
                } catch (IOException e2) {
                }
            }
            throw th3;
        }
    }

    public static void moveAtomically(Path path, Path path2) throws IOException {
        try {
            Files.move(path, path2, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(path, path2, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void syncDirectory(Path path) {
        try {
            FileChannel fileChannelOpen = FileChannel.open(path, StandardOpenOption.READ);
            try {
                fileChannelOpen.force(true);
                if (fileChannelOpen != null) {
                    fileChannelOpen.close();
                }
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
        } catch (IOException e) {
        }
    }
}
