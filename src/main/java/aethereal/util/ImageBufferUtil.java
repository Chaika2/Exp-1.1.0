package aethereal.util;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImageBufferUtil {
    public static ByteBuffer convertToByteBuffer(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        int[] iArr = new int[width * height];
        bufferedImage.getRGB(0, 0, width, height, iArr, 0, width);
        ByteBuffer byteBufferOrder = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());
        for (int i : iArr) {
            byteBufferOrder.put((byte) ((i >> 16) & StencilBufferUtil.STENCIL_MASK));
            byteBufferOrder.put((byte) ((i >> 8) & StencilBufferUtil.STENCIL_MASK));
            byteBufferOrder.put((byte) (i & StencilBufferUtil.STENCIL_MASK));
            byteBufferOrder.put((byte) ((i >> 24) & StencilBufferUtil.STENCIL_MASK));
        }
        byteBufferOrder.flip();
        return byteBufferOrder;
    }

    public ImageBufferUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

