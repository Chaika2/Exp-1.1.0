package aethereal.render;
import aethereal.util.DrawEngine;
import aethereal.net.InputInterceptor;
import aethereal.model.LayoutScaleContext;
import aethereal.model.PixelPoint;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector4f;
import org.joml.Vector4i;

public final class DrawCtx {
    public final InputInterceptor window;
    public final ScreenResolution resolution;
    public final MatrixStack matrixStack;
    public final PixelPoint mousePosition;

    public final DrawEngine drawEngine;

    public final ColorStack colorStack;

    public final PixelPoint lastKnownMousePosition;

    public final Theme theme;
    public final LayoutScaleContext layoutContext;

    public DrawCtx(InputInterceptor class631Var, ScreenResolution class710Var, MatrixStack matrixStack, PixelPoint class708Var, DrawEngine class154Var, ColorStack class115Var, PixelPoint class708Var2, Theme class760Var, LayoutScaleContext class698Var) {
        this.window = class631Var;
        this.resolution = class710Var;
        this.matrixStack = matrixStack;
        this.mousePosition = class708Var;
        this.drawEngine = class154Var;
        this.colorStack = class115Var;
        this.lastKnownMousePosition = class708Var2;
        this.theme = class760Var;
        this.layoutContext = class698Var;
    }

    public void fillRoundedRect(float f, float f2, float f3, float f4, Vector4f vector4f, int i) {
        Vector4f vector4fMethod001 = clampRadii(f3, f4, vector4f);
        this.drawEngine.roundedRectangle(this.matrixStack.peek().getPositionMatrix(), this.layoutContext.toPhysical(f), this.layoutContext.toPhysical(f2), this.layoutContext.toPhysical(f3), this.layoutContext.toPhysical(f4), this.layoutContext.toPhysical(vector4fMethod001.x), this.layoutContext.toPhysical(vector4fMethod001.y), this.layoutContext.toPhysical(vector4fMethod001.z), this.layoutContext.toPhysical(vector4fMethod001.w), i);
    }

    public void fillVerticalGradientRect(float f, float f2, float f3, float f4, int i, int i2) {
        fillGradientRoundedRect(f, f2, f3, f4, 0, i2, i2, i, i);
    }

    public void fillGradientRoundedRect(float f, float f2, float f3, float f4, int i, int i2, int i3, int i4, int i5) {
        this.drawEngine.roundedRectangle(this.matrixStack.peek().getPositionMatrix(), this.layoutContext.toPhysical(f), this.layoutContext.toPhysical(f2), this.layoutContext.toPhysical(f3), this.layoutContext.toPhysical(f4), this.layoutContext.toPhysical(clampRadius(f3, f4, i)), i2, i3, i4, i5);
    }

    public void radialRoundedRectangle(float f, float f2, float f3, float f4, float f5, int i, int i2) {
        this.drawEngine.radialRoundedRectangle(this.matrixStack.peek().getPositionMatrix(), this.layoutContext.toPhysical(f), this.layoutContext.toPhysical(f2), this.layoutContext.toPhysical(f3), this.layoutContext.toPhysical(f4), this.layoutContext.toPhysical(f5), i, i2);
    }

    public void fillGradientOutlinedRoundedRect(float f, float f2, float f3, float f4, int i, float f5, Vector4i vector4i, Vector4i vector4i2) {
        this.drawEngine.roundedRectangle(this.matrixStack.peek().getPositionMatrix(), this.layoutContext.toPhysical(f), this.layoutContext.toPhysical(f2), this.layoutContext.toPhysical(f3), this.layoutContext.toPhysical(f4), this.layoutContext.toPhysical(i), f5, vector4i.x, vector4i.y, vector4i.z, vector4i.w, vector4i2.x, vector4i2.y, vector4i2.z, vector4i2.w);
    }

    public void fillRoundedRect(float f, float f2, float f3, float f4, float f5, int i) {
        float fMethod002 = clampRadius(f3, f4, f5);
        this.drawEngine.roundedRectangle(this.matrixStack.peek().getPositionMatrix(), this.layoutContext.toPhysical(f), this.layoutContext.toPhysical(f2), this.layoutContext.toPhysical(f3), this.layoutContext.toPhysical(f4), this.layoutContext.toPhysical(fMethod002), this.layoutContext.toPhysical(fMethod002), this.layoutContext.toPhysical(fMethod002), this.layoutContext.toPhysical(fMethod002), i);
    }

    public void fillOutlinedRoundedRect(float f, float f2, float f3, float f4, float f5, float f6, int i, int i2) {
        this.drawEngine.roundedRectangle(this.matrixStack.peek().getPositionMatrix(), this.layoutContext.toPhysical(f), this.layoutContext.toPhysical(f2), this.layoutContext.toPhysical(f3), this.layoutContext.toPhysical(f4), this.layoutContext.toPhysical(clampRadius(f3, f4, f5)), f6, i, i2);
    }

    public void bloom(float f, float f2, float f3, float f4, float f5, int i) {
        float fScreenWidth = this.resolution.screenWidth();
        float fScreenHeight = this.resolution.screenHeight();
        float physical = this.layoutContext.toPhysical(f - f5);
        float physical2 = this.layoutContext.toPhysical(f2 - f5);
        float physical3 = this.layoutContext.toPhysical(f3 + (f5 * 2.0f));
        float physical4 = this.layoutContext.toPhysical(f4 + (f5 * 2.0f));
        this.drawEngine.texture(this.matrixStack.peek().getPositionMatrix(), physical, physical2, physical3, physical4, physical / fScreenWidth, 1.0f - (physical2 / fScreenHeight), (physical + physical3) / fScreenWidth, 1.0f - ((physical2 + physical4) / fScreenHeight), this.drawEngine.bindTexture(i), this.colorStack.computeColor(this.colorStack.white(), 150));
    }

    public void fillOutlinedRoundedRect(float f, float f2, float f3, float f4, Vector4f vector4f, float f5, int i, int i2) {
        Vector4f vector4fMethod001 = clampRadii(f3, f4, vector4f);
        this.drawEngine.roundedRectangle(this.matrixStack.peek().getPositionMatrix(), this.layoutContext.toPhysical(f), this.layoutContext.toPhysical(f2), this.layoutContext.toPhysical(f3), this.layoutContext.toPhysical(f4), this.layoutContext.toPhysical(vector4fMethod001.x), this.layoutContext.toPhysical(vector4fMethod001.y), this.layoutContext.toPhysical(vector4fMethod001.z), this.layoutContext.toPhysical(vector4fMethod001.w), f5, i, i2);
    }

    public void roundedBlur(int i, float f, float f2, float f3, float f4, Vector4f vector4f, int i2) {
        this.drawEngine.roundedBlur(this.matrixStack.peek().getPositionMatrix(), this.layoutContext.toPhysical(f), this.layoutContext.toPhysical(f2), this.layoutContext.toPhysical(f3), this.layoutContext.toPhysical(f4), this.layoutContext.toPhysical(vector4f.x), this.layoutContext.toPhysical(vector4f.y), this.layoutContext.toPhysical(vector4f.z), this.layoutContext.toPhysical(vector4f.w), i2, i);
    }

    public void roundedBlur(int i, float f, float f2, float f3, float f4, float f5, int i2) {
        float physical = this.layoutContext.toPhysical(f5);
        this.drawEngine.roundedBlur(this.matrixStack.peek().getPositionMatrix(), this.layoutContext.toPhysical(f), this.layoutContext.toPhysical(f2), this.layoutContext.toPhysical(f3), this.layoutContext.toPhysical(f4), physical, physical, physical, physical, i2, i);
    }

    public void circle(float f, float f2, float f3, int i) {
        this.drawEngine.circle(this.matrixStack.peek().getPositionMatrix(), this.layoutContext.toPhysical(f), this.layoutContext.toPhysical(f2), this.layoutContext.toPhysical(f3), i);
    }

    public void outlineCircle(float f, float f2, float f3, float f4, int i, int i2) {
        this.drawEngine.circle(this.matrixStack.peek().getPositionMatrix(), this.layoutContext.toPhysical(f), this.layoutContext.toPhysical(f2), this.layoutContext.toPhysical(f3), f4, i, i2);
    }

    public void fillRect(float f, float f2, float f3, float f4, int i) {
        this.drawEngine.rectangle(this.matrixStack.peek().getPositionMatrix(), this.layoutContext.toPhysical(f), this.layoutContext.toPhysical(f2), this.layoutContext.toPhysical(f3), this.layoutContext.toPhysical(f4), i);
    }

    public void texture(int i, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int i2) {
        this.drawEngine.texture(this.matrixStack.peek().getPositionMatrix(), this.layoutContext.toPhysical(f), this.layoutContext.toPhysical(f2), this.layoutContext.toPhysical(f3), this.layoutContext.toPhysical(f4), f5, f6, f7, f8, this.drawEngine.bindTexture(i), i2);
    }

    public void texture(int i, float f, float f2, float f3, float f4, int i2) {
        this.drawEngine.texture(this.matrixStack.peek().getPositionMatrix(), this.layoutContext.toPhysical(f), this.layoutContext.toPhysical(f2), this.layoutContext.toPhysical(f3), this.layoutContext.toPhysical(f4), this.drawEngine.bindTexture(i), i2);
    }

    public void texture(GlTexture class073Var, float f, float f2, float f3, float f4, int i) {
        this.drawEngine.texture(this.matrixStack.peek().getPositionMatrix(), this.layoutContext.toPhysical(f), this.layoutContext.toPhysical(f2), this.layoutContext.toPhysical(f3), this.layoutContext.toPhysical(f4), this.drawEngine.bindTexture(class073Var.textureWithSTB()), i);
    }

    public void roundedTexture(GlTexture class073Var, float f, float f2, float f3, float f4, float f5, int i) {
        this.drawEngine.roundedTexture(this.matrixStack.peek().getPositionMatrix(), this.layoutContext.toPhysical(f), this.layoutContext.toPhysical(f2), this.layoutContext.toPhysical(f3), this.layoutContext.toPhysical(f4), this.layoutContext.toPhysical(f5), this.drawEngine.bindTexture(class073Var.textureWithSTB()), i);
    }

    public void textureVerticalC(GlTexture class073Var, float f, float f2, int i, int i2, int i3) {
        texture(class073Var, f, f2 - (MathHelper.ceil(i2) / 2.0f), i, i2, i3);
    }

    public void textureVerticalCHorizontalC(GlTexture class073Var, float f, float f2, int i, int i2, int i3) {
        texture(class073Var, f - (MathHelper.ceil(class073Var.width()) / 2.0f), f2 - (MathHelper.ceil(class073Var.height()) / 2.0f), i, i2, i3);
    }

    public void roundedTextureVerticalCHorizontalC(GlTexture class073Var, float f, float f2, int i, int i2, float f3, int i3) {
        roundedTexture(class073Var, f - (MathHelper.ceil(class073Var.width()) / 2.0f), f2 - (MathHelper.ceil(class073Var.height()) / 2.0f), i, i2, f3, i3);
    }

    public void textureVerticalCHorizontalC(GlTexture class073Var, float f, float f2, int i) {
        texture(class073Var, f - (MathHelper.ceil(class073Var.width()) / 2.0f), f2 - (MathHelper.ceil(class073Var.height()) / 2.0f), class073Var.width(), class073Var.height(), i);
    }

    public void text(MsdfFont class161Var, String str, int i, float f, float f2, int i2) {
        float fScaleFactor = this.layoutContext.scaleFactor();
        drawEngine().msdfFont(matrixStack().peek().getPositionMatrix(), class161Var, str, this.layoutContext.toPhysical(f), this.layoutContext.toPhysical(f2), Math.max(1, Math.round(i * fScaleFactor)), 0.05f * fScaleFactor, i2);
    }

    public void textWithHorizontalGradient(MsdfFont class161Var, String str, int i, float f, float f2, int i2, int i3) {
        float fScaleFactor = this.layoutContext.scaleFactor();
        drawEngine().msdfFontWithHorizontalGradient(matrixStack().peek().getPositionMatrix(), class161Var, str, this.layoutContext.toPhysical(f), this.layoutContext.toPhysical(f2), Math.max(1, Math.round(i * fScaleFactor)), 0.05f * fScaleFactor, i2, i3);
    }

    public float textWidthPhysical(MsdfFont class161Var, String str, int i) {
        float fScaleFactor = this.layoutContext.scaleFactor();
        return class161Var.getWidth(str, Math.max(1, Math.round(i * fScaleFactor))) / fScaleFactor;
    }

    public float clampRadius(float f, float f2, float f3) {
        if (f3 <= 0.0f) {
            return 0.0f;
        }
        return Math.min(f3, 0.5f * Math.min(f, f2));
    }

    public Vector4f clampRadii(float f, float f2, Vector4f vector4f) {
        float fMin = 0.5f * Math.min(f, f2);
        return new Vector4f(Math.min(Math.max(vector4f.x, 0.0f), fMin), Math.min(Math.max(vector4f.y, 0.0f), fMin), Math.min(Math.max(vector4f.z, 0.0f), fMin), Math.min(Math.max(vector4f.w, 0.0f), fMin));
    }

    public PixelPoint logicalMousePosition() {
        return this.layoutContext.toLogical(this.mousePosition);
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "window=" + this.window + ", " + "resolution=" + this.resolution + ", " + "matrixStack=" + this.matrixStack + ", " + "mousePosition=" + this.mousePosition + ", " + "drawEngine=" + this.drawEngine + ", " + "colorStack=" + this.colorStack + ", " + "lastKnownMousePosition=" + this.lastKnownMousePosition + ", " + "theme=" + this.theme + ", " + "layoutContext=" + this.layoutContext + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.window, this.resolution, this.matrixStack, this.mousePosition, this.drawEngine, this.colorStack, this.lastKnownMousePosition, this.theme, this.layoutContext);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DrawCtx)) return false;
        DrawCtx o = (DrawCtx) obj;
        return java.util.Objects.equals(this.window, o.window) && java.util.Objects.equals(this.resolution, o.resolution) && java.util.Objects.equals(this.matrixStack, o.matrixStack) && java.util.Objects.equals(this.mousePosition, o.mousePosition) && java.util.Objects.equals(this.drawEngine, o.drawEngine) && java.util.Objects.equals(this.colorStack, o.colorStack) && java.util.Objects.equals(this.lastKnownMousePosition, o.lastKnownMousePosition) && java.util.Objects.equals(this.theme, o.theme) && java.util.Objects.equals(this.layoutContext, o.layoutContext);
    }
public InputInterceptor window() {
        return this.window;
    }

    public ScreenResolution resolution() {
        return this.resolution;
    }

    public MatrixStack matrixStack() {
        return this.matrixStack;
    }

    public PixelPoint mousePosition() {
        return this.mousePosition;
    }

    public DrawEngine drawEngine() {
        return this.drawEngine;
    }

    public ColorStack colorStack() {
        return this.colorStack;
    }

    public PixelPoint lastKnownMousePosition() {
        return this.lastKnownMousePosition;
    }

    public Theme theme() {
        return this.theme;
    }

    public LayoutScaleContext layoutContext() {
        return this.layoutContext;
    }
}
