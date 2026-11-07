package aethereal.ui;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.render.GlTexture;
import aethereal.type.ModuleCategory;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;

public class CategoryChip {
    public final ModuleCategory category;
    public final ToggleAnimator hoverAnimator = new ToggleAnimator(160, Easings.LINEAR);
    public final ToggleAnimator selectedAnimator = new ToggleAnimator(180, Easings.EASE_IN_OUT_CUBIC);
    public float x;
    public float y;
    public float width;
    public float height;

    final CategoryChipBar parentBar;

    public CategoryChip(CategoryChipBar class787Var, ModuleCategory class672Var) {
        this.parentBar = class787Var;
        this.category = class672Var;
    }

    public ModuleCategory category() {
        return this.category;
    }

    public void bounds(float f, float f2, float f3, float f4) {
        this.x = f;
        this.y = f2;
        this.width = f3;
        this.height = f4;
    }

    public boolean contains(float f, float f2) {
        return f >= this.x && f <= this.x + this.width && f2 >= this.y && f2 <= this.y + this.height;
    }

    public void selected(boolean z) {
        this.selectedAnimator.state(z);
    }

    public void render(DrawCtx class699Var, ThemePalette class764Var, ColorStack class115Var) {
        float fSmoothAnimation = this.hoverAnimator.smoothAnimation();
        float fSmoothAnimation2 = this.selectedAnimator.smoothAnimation();
        int iComputeColor = class115Var.computeColor(class764Var.surfaceOutline().tone(400).argb());
        class115Var.computeColor(class764Var.surfaceOutline().tone(300).argb());
        class699Var.fillOutlinedRoundedRect(this.x, this.y, this.width, this.height, 14.0f, 2.5f, iComputeColor, class115Var.computeColor(class764Var.surfaceBackground().tone(900).argb(), 0));
        GlTexture class073VarIcon = this.category.icon();
        class073VarIcon.setDimensions(12, 12);
        int iInterpolate = class115Var.interpolate(class115Var.interpolate(class115Var.computeColor(class764Var.text().tone(500).argb()), class115Var.computeColor(class764Var.text().tone(300).argb()), fSmoothAnimation), class115Var.computeColor(class764Var.text().tone(200).argb()), fSmoothAnimation2);
        float f = this.x + 12.0f;
        class699Var.texture(class073VarIcon, f, (this.y + (this.height / 2.0f)) - 6.0f, 12.0f, 12.0f, iInterpolate);
        class699Var.text(this.parentBar.labelFont, this.category.displayName(), 12, f + 12.0f + 5.0f, (this.y + (this.height / 2.0f)) - (this.parentBar.labelFont.getHeight(12.0f) / 2.0f), iInterpolate);
    }
}
