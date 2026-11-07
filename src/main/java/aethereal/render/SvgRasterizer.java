package aethereal.render;
import aethereal.resource.ResourceSource;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import org.jetbrains.annotations.NotNull;

public class SvgRasterizer {
    @NotNull
    static BufferedImage rasterize(ResourceSource class178Var, SVGDiagram sVGDiagram, int i, int i2) {
        BufferedImage bufferedImage = new BufferedImage(i, i2, 2);
        Graphics2D graphics2DCreateGraphics = bufferedImage.createGraphics();
        try {
            try {
                graphics2DCreateGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2DCreateGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics2DCreateGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                graphics2DCreateGraphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                double width = sVGDiagram.getWidth() > 0.0f ? sVGDiagram.getWidth() : sVGDiagram.getViewRect().getWidth();
                double height = sVGDiagram.getHeight() > 0.0f ? sVGDiagram.getHeight() : sVGDiagram.getViewRect().getHeight();
                double dMin = Math.min(((double) i) / width, ((double) i2) / height);
                graphics2DCreateGraphics.scale(dMin, dMin);
                graphics2DCreateGraphics.translate(((((double) i) / dMin) - width) / 2.0d, ((((double) i2) / dMin) - height) / 2.0d);
                sVGDiagram.render(graphics2DCreateGraphics);
                graphics2DCreateGraphics.dispose();
                return bufferedImage;
            } catch (SVGException e) {
                throw new RuntimeException("Error rendering SVG from resource: " + String.valueOf(class178Var), e);
            }
        } catch (Throwable th) {
            graphics2DCreateGraphics.dispose();
            throw th;
        }
    }
}
