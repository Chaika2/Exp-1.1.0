package aethereal.render;
import aethereal.ui.GifFrame;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GifDecoder {
    public static final int STATUS_OK = 0;
    public static final int STATUS_FORMAT_ERROR = 1;
    public static final int STATUS_OPEN_ERROR = 2;

    public int status = STATUS_OK;
    public int width;
    public int height;
    public int frameCount = 0;
    public int loopCount = 0;
    public List<GifFrame> frames;

    public GifDecoder(InputStream inputStream) {
        this.frames = new ArrayList();
        if (inputStream == null) {
            this.status = 2;
            return;
        }
        try {
            ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
            if (imageInputStream == null) {
                this.status = 2;
                return;
            }
            Iterator<ImageReader> iterator = ImageIO.getImageReaders(imageInputStream);
            if (!iterator.hasNext()) {
                this.status = 1;
                imageInputStream.close();
                return;
            }
            ImageReader imageReader = (ImageReader) iterator.next();
            try {
                imageReader.setInput(imageInputStream, false, true);
                this.width = imageReader.getWidth(0);
                this.height = imageReader.getHeight(0);
                int iCount = imageReader.getNumImages(true);
                BufferedImage canvas = new BufferedImage(this.width, this.height, 2);
                int iLastX = 0;
                int iLastY = 0;
                int iLastW = this.width;
                int iLastH = this.height;
                for (int i = 0; i < iCount; i++) {
                    int iDelay = 50;
                    int iDispose = 1;
                    int iX = 0;
                    int iY = 0;
                    int iW = this.width;
                    int iH = this.height;
                    IIOMetadata classIIIOMetadata = imageReader.getImageMetadata(i);
                    if (classIIIOMetadata != null) {
                        Element elementRoot = (Element) classIIIOMetadata.getAsTree("javax_imageio_gif_image_1.0");
                        NodeList nodeListDescriptors = elementRoot.getElementsByTagName("ImageDescriptor");
                        if (nodeListDescriptors.getLength() > 0) {
                            Element elementDescriptor = (Element) nodeListDescriptors.item(0);
                            iX = parseIntAttribute(elementDescriptor.getAttribute("imageLeftPosition"), 0);
                            iY = parseIntAttribute(elementDescriptor.getAttribute("imageTopPosition"), 0);
                            iW = parseIntAttribute(elementDescriptor.getAttribute("imageWidth"), this.width);
                            iH = parseIntAttribute(elementDescriptor.getAttribute("imageHeight"), this.height);
                        }
                        NodeList nodeListGraphicControl = elementRoot.getElementsByTagName("GraphicControlExtension");
                        if (nodeListGraphicControl.getLength() > 0) {
                            Element elementGraphicControl = (Element) nodeListGraphicControl.item(0);
                            iDelay = parseIntAttribute(elementGraphicControl.getAttribute("delayTime"), 5) * 10;
                            if (iDelay <= 0) {
                                iDelay = 50;
                            }
                            iDispose = parseIntAttribute(elementGraphicControl.getAttribute("disposalMethod"), 1);
                        }
                    }
                    BufferedImage bufferedImageFrame = imageReader.read(i);
                    Graphics2D graphics2D = canvas.createGraphics();
                    if (iDispose == 2 || iDispose == 3) {
                        graphics2D.setComposite(AlphaComposite.Src);
                        graphics2D.clearRect(iLastX, iLastY, iLastW, iLastH);
                    }
                    graphics2D.setComposite(AlphaComposite.SrcOver);
                    graphics2D.drawImage(bufferedImageFrame, iX, iY, null);
                    graphics2D.dispose();
                    BufferedImage bufferedImageCopy = new BufferedImage(this.width, this.height, 2);
                    Graphics2D graphics2DCopy = bufferedImageCopy.createGraphics();
                    graphics2DCopy.drawImage(canvas, 0, 0, null);
                    graphics2DCopy.dispose();
                    this.frames.add(new GifFrame(bufferedImageCopy, iDelay));
                    this.frameCount++;
                    iLastX = iX;
                    iLastY = iY;
                    iLastW = iW;
                    iLastH = iH;
                }
                this.status = 0;
            } finally {
                imageReader.dispose();
                imageInputStream.close();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            this.status = 2;
        }
    }

    static int parseIntAttribute(String str, int iDefault) {
        if (str == null || str.isEmpty()) {
            return iDefault;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return iDefault;
        }
    }

    static BufferedImage cropToContent(BufferedImage bufferedImage) {
        int iWidth = bufferedImage.getWidth();
        int iHeight = bufferedImage.getHeight();
        int[] iArr = bufferedImage.getRGB(0, 0, iWidth, iHeight, (int[]) null, 0, iWidth);
        int iMinX = iWidth;
        int iMinY = iHeight;
        int iMaxX = -1;
        int iMaxY = -1;
        int iIndex = 0;
        for (int i = 0; i < iHeight; i++) {
            for (int i2 = 0; i2 < iWidth; i2++) {
                if (((iArr[iIndex] >>> 24) & 255) > 0) {
                    if (i2 < iMinX) {
                        iMinX = i2;
                    }
                    if (i2 > iMaxX) {
                        iMaxX = i2;
                    }
                    if (i < iMinY) {
                        iMinY = i;
                    }
                    if (i > iMaxY) {
                        iMaxY = i;
                    }
                }
                iIndex++;
            }
        }
        if (iMaxX < 0 || iMaxY < 0) {
            return bufferedImage;
        }
        return bufferedImage.getSubimage(iMinX, iMinY, iMaxX - iMinX + 1, iMaxY - iMinY + 1);
    }

    public int frameDelay(int i) {
        return i >= 0 && i < this.frameCount ? ((GifFrame) this.frames.get(i)).delay() : -1;
    }

    public List<GifFrame> frames() {
        return this.frames;
    }

    public int frameCount() {
        return this.frameCount;
    }

    public boolean hasError() {
        return this.status != 0;
    }

    public GifDecoder loopCount(int i) {
        this.loopCount = i;
        return this;
    }

    public GifFrame frameImage(int i) {
        return i >= 0 && i < this.frameCount ? (GifFrame) this.frames.get(i) : null;
    }
}