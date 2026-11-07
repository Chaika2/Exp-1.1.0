package aethereal.ui;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.model.LayoutScaleContext;
import aethereal.render.MsdfFont;
import aethereal.render.ThemePalette;
import aethereal.model.Translation;

public class IconLabelBadge extends AbstractWidget {
    public final MsdfFont font = Fonts.INTER_EXTRA_BOLD.get();
    static final int textSize = 9;
    static final float padding = 6.0f;
    public final Translation label;
    public final GlTexture icon;
    public float width;
    public Integer baseColor;
    public Integer textColor;

    public IconLabelBadge(GlTexture class073Var, Translation class254Var) {
        this.label = class254Var;
        this.icon = class073Var;
    }

    @Override
    public void render(DrawCtx class699Var) {
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.colorStack();
        int iIntValue = this.baseColor != null ? this.baseColor.intValue() : class764VarPalette.accent().argb();
        int iIntValue2 = this.textColor != null ? this.textColor.intValue() : iIntValue;
        float height = this.font.getHeight(9.0f);
        float fHeight = height() / 2.0f;
        class699Var.fillRoundedRect(x(), y(), width(), height(), 5.0f, class115VarColorStack.computeColor(iIntValue, 0.15f));
        class699Var.textureVerticalC(this.icon, x() + padding, y() + fHeight, 10, 10, class115VarColorStack.computeColor(iIntValue2));
        class699Var.text(this.font, this.label.effective().toUpperCase(), textSize, x() + padding + 10.0f + 4.0f, (y() + fHeight) - (height / 2.0f), class115VarColorStack.computeColor(iIntValue2));
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        this.width = 26.0f + class698Var.textWidthPhysical(this.font, this.label.effective().toUpperCase(), textSize);
    }

    @Override
    public float width() {
        return this.width;
    }

    @Override
    public float height() {
        return 19.0f;
    }

    public void setBaseColor(Integer num) {
        this.baseColor = num;
    }

    public void setTextColor(Integer num) {
        this.textColor = num;
    }
}
