package aethereal.render;

public final class GlassRenderer {
    private GlassRenderer() {
    }

    public static void panel(DrawCtx class699Var, float f, float f2, float f3, float f4, float f5) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        int i = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(850).argb(), 0.55f);
        int i2 = class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(500).argb(), 0.35f);
        class699Var.fillOutlinedRoundedRect(f, f2, f3, f4, f5, 1.5f, i2, i);
        float fMax = Math.max(6.0f, f4 * 0.42f);
        int iComputeColor = class115VarColorStack.computeColor(ThemePalette.white.argb(), 0.1f);
        int iComputeColor2 = class115VarColorStack.computeColor(ThemePalette.white.argb(), 0.0f);
        class699Var.fillGradientRoundedRect(f + 1.0f, f2 + 1.0f, f3 - 2.0f, fMax, Math.round(Math.max(0.0f, f5 - 1.0f)), iComputeColor, iComputeColor, iComputeColor2, iComputeColor2);
    }

    public static void accent(DrawCtx class699Var, float f, float f2, float f3, float f4, float f5, float f6) {
        if (f6 <= 0.001f) {
            return;
        }
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.accent().argb(), 0.16f * f6);
        int iComputeColor2 = class115VarColorStack.computeColor(class764VarPalette.accent().argb(), 0.0f);
        class699Var.fillGradientRoundedRect(f + 1.0f, f2 + 1.0f, f3 - 2.0f, Math.max(6.0f, f4 * 0.5f), Math.round(Math.max(0.0f, f5 - 1.0f)), iComputeColor, iComputeColor, iComputeColor2, iComputeColor2);
    }
}
