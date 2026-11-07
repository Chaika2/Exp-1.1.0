package aethereal.ui;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.util.DrawEngine;
import aethereal.render.GlTexture;
import aethereal.model.LayoutScaleContext;
import aethereal.render.ThemePalette;
import aethereal.util.WeightedEngine;

public class IconTileButton extends AbstractWidget {
    public final ClickableBehavior clickable = new ClickableBehavior();
    public final GlTexture icon;
    public final float cornerRadius;
    public final float paddingX;
    public final float paddingY;
    public float computedWidth;
    public float computedHeight;

    public IconTileButton(Runnable runnable, GlTexture class073Var, float f, float f2, float f3) {
        this.clickable.clickCallback(runnable);
        this.icon = class073Var;
        this.cornerRadius = f;
        this.paddingX = f2;
        this.paddingY = f3;
    }

    @Override
    public void render(DrawCtx class699Var) {
        DrawEngine class154VarDrawEngine = class699Var.drawEngine();
        ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        float fHeight = this.icon.height() + (this.paddingY * 2.0f);
        float fWidth = this.icon.width() + (this.paddingX * 2.0f);
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.computeColor(0, 30, 31, 40), class115VarColorStack.computeColor(140, 30, 31, 40), this.clickable.hoverAnimation());
        int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb());
        class699Var.fillOutlinedRoundedRect(x(), y(), fWidth, fHeight, this.cornerRadius, 2.5f, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb()), iInterpolate);
        class699Var.textureVerticalCHorizontalC(this.icon, x() + (width() / 2.0f), y() + (height() / 2.0f), class154VarDrawEngine.colorStack().computeColor(iComputeColor));
        this.computedWidth = fWidth;
        this.computedHeight = fHeight;
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        setSize(this.computedWidth, this.computedHeight);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.clickable.animate(class141Var);
    }
}
