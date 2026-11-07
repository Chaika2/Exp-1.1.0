package aethereal.util;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public final class StencilBufferUtil {
    public static final int STENCIL_MASK = 255;
    public static final int NON_EQUALS = 0;
    public static final int EQUALS = 1;
    public static int framebufferId = -1;
    public static int renderbufferId = -1;
    public static int width = -1;
    public static int height = -1;
    public static int textureId = -1;
    public static int previousFramebuffer = -1;
    public static final int[] savedViewport = {0, 0, 0, 0};

    public static void initFramebuffer(int i, int i2, int i3) {
        if (framebufferId != -1) {
            GL30.glDeleteFramebuffers(framebufferId);
            GL30.glDeleteRenderbuffers(renderbufferId);
        }
        framebufferId = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(36160, framebufferId);
        textureId = i3;
        GL30.glFramebufferTexture2D(36160, 36064, 3553, textureId, 0);
        renderbufferId = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(36161, renderbufferId);
        GL30.glRenderbufferStorage(36161, 35056, i, i2);
        GL30.glBindRenderbuffer(36161, 0);
        GL30.glFramebufferRenderbuffer(36160, 33306, 36161, renderbufferId);
        int iGlCheckFramebufferStatus = GL30.glCheckFramebufferStatus(36160);
        if (iGlCheckFramebufferStatus != 36053) {
            throw new IllegalStateException("Stencil framebuffer error: 0x" + Integer.toHexString(iGlCheckFramebufferStatus));
        }
        width = i;
        height = i2;
    }

    public static void prepareStencil() {
        GL11.glGetIntegerv(2978, savedViewport);
        int i = savedViewport[2];
        int i2 = savedViewport[3];
        previousFramebuffer = GL11.glGetInteger(36006);
        int iGlGetFramebufferAttachmentParameteri = GL30.glGetFramebufferAttachmentParameteri(36009, 36064, 36049);
        if (framebufferId == -1 || textureId != iGlGetFramebufferAttachmentParameteri || width != i || height != i2) {
            initFramebuffer(i, i2, iGlGetFramebufferAttachmentParameteri);
        }
        GL30.glBindFramebuffer(36160, framebufferId);
        GL11.glViewport(0, 0, i, i2);
        GL11.glClear(1024);
        GL11.glEnable(2960);
        GL11.glStencilFunc(519, 1, STENCIL_MASK);
        GL11.glStencilOp(7681, 7681, 7681);
        GL11.glColorMask(false, false, false, false);
        GL11.glDepthMask(false);
    }

    public static void prepareElement(int i) {
        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);
        GL11.glStencilFunc(514, i, STENCIL_MASK);
        GL11.glStencilOp(7680, 7680, 7680);
    }

    public static void cleanup() {
        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);
        GL11.glDisable(2960);
        GL30.glBindFramebuffer(36160, previousFramebuffer == -1 ? 0 : previousFramebuffer);
        GL11.glViewport(savedViewport[0], savedViewport[1], savedViewport[2], savedViewport[3]);
        previousFramebuffer = -1;
    }

    public StencilBufferUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
