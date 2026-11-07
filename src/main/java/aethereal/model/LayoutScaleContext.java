package aethereal.model;
import aethereal.render.MsdfFont;
import aethereal.render.ScreenResolution;


public final class LayoutScaleContext {
    public final ScreenResolution screenResolution;
    public final float scaleFactor;

    public LayoutScaleContext(ScreenResolution class710Var) {
        this(class710Var, 1.0f);
    }

    public LayoutScaleContext(ScreenResolution class710Var, float f) {
        this.screenResolution = class710Var;
        this.scaleFactor = f;
    }

    public float textWidthPhysical(MsdfFont class161Var, String str, int i) {
        float f = this.scaleFactor;
        return class161Var.getWidth(str, Math.max(1, Math.round(i * f))) / f;
    }

    public float toPhysical(float f) {
        return f * this.scaleFactor;
    }

    public PixelPoint toLogical(PixelPoint class708Var) {
        return (this.scaleFactor <= 0.0f || this.scaleFactor == 1.0f) ? class708Var : new PixelPoint(class708Var.x() / this.scaleFactor, class708Var.y() / this.scaleFactor);
    }

    public float toPhysicalAligned(float f) {
        return Math.round(f * this.scaleFactor);
    }

    public float logicalWidth() {
        return this.screenResolution.screenWidth() / this.scaleFactor;
    }

    public float logicalHeight() {
        return this.screenResolution.screenHeight() / this.scaleFactor;
    }

    public float alignToScale(float f, float f2) {
        return Math.round(f * f2) / f2;
    }

    public float alignToScale(float f) {
        return alignToScale(f, this.scaleFactor);
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "screenResolution=" + this.screenResolution + ", " + "scaleFactor=" + this.scaleFactor + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.screenResolution, this.scaleFactor);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LayoutScaleContext)) return false;
        LayoutScaleContext o = (LayoutScaleContext) obj;
        return java.util.Objects.equals(this.screenResolution, o.screenResolution) && java.util.Objects.equals(this.scaleFactor, o.scaleFactor);
    }
public ScreenResolution screenResolution() {
        return this.screenResolution;
    }

    public float scaleFactor() {
        return this.scaleFactor;
    }
}
