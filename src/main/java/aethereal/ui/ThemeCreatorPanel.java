package aethereal.ui;
import aethereal.resource.ClasspathResource;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.model.InputEventContext;
import aethereal.model.LayoutScaleContext;
import aethereal.render.MsdfFont;
import aethereal.render.ThemePalette;
import aethereal.model.Translation;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

public class ThemeCreatorPanel extends WidgetContainer {
    public final GlTexture brushIcon = new GlTexture(new ClasspathResource("/icons/menu/new/brush.png"));
    public final MsdfFont titleFont = Fonts.INTER_SEMIBOLD.get();
    public final WidgetBounds headerBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 46.0f);
    public final IconLabelBadge titleBadge = new IconLabelBadge(this.brushIcon, Translation.clearText("Цветовые темы"));
    public boolean opened = false;

    @Override
    public void render(DrawCtx class699Var) {
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        this.titleBadge.render(class699Var);
        class699Var.text(this.titleFont, Translation.clearText("Создание цветовой темы").effective(), 16, this.headerBounds.x(), this.headerBounds.y() + this.titleBadge.height() + 8.0f, class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb()));
        super.render(class699Var);
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        this.headerBounds.withSize(width() - 40.0f, 46.0f).withPosition(x() + 20.0f, y() + 16.0f);
        this.titleBadge.layout(class698Var);
        this.titleBadge.setPosition(this.headerBounds.x(), this.headerBounds.y());
        super.layout(class698Var);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        return super.handleInput(class688Var, z);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        super.animation(class141Var);
    }

    public void open() {
        this.opened = true;
    }

    public void close() {
        if (this.opened) {
            this.opened = false;
        }
    }

    @Override
    public float width() {
        return MenuWindow.MENU_WIDTH;
    }

    @Override
    public float height() {
        return MenuWindow.MENU_HEIGHT - MenuWindow.COLLAPSED_HEADER_HEIGHT;
    }

    public boolean isOpened() {
        return this.opened;
    }
}
