package aethereal.ui;
import aethereal.render.AnimatedFloat;
import aethereal.resource.ClasspathResource;
import aethereal.render.ColorStack;
import aethereal.type.ConfigOrigin;
import aethereal.model.CursorMoveInput;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.model.InputEventContext;
import aethereal.model.LayoutScaleContext;
import aethereal.render.MsdfFont;
import aethereal.type.ThemeMode;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.model.Translation;
import aethereal.module.ThemesModule;
import aethereal.render.ThemeManager;
import aethereal.type.ThemeSelection;
import aethereal.type.UiColorMode;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.util.ArrayList;
import java.util.List;

public class ThemeCard2 extends AbstractFrame {
    public final GlTexture themeIcon;
    public final GlTexture smileIcon;
    public final GlTexture checkmarkIcon;
    public final GlTexture calendarIcon;
    static final GlTexture loadIcon = new GlTexture(new ClasspathResource("/icons/menu/new/click.png"));
    static final GlTexture copyIcon = new GlTexture(new ClasspathResource("/icons/menu/new/copy.png"));
    static final GlTexture starIcon = new GlTexture(new ClasspathResource("/icons/menu/new/star.png"));
    static final GlTexture editIcon = new GlTexture(new ClasspathResource("/icons/menu/new/pencil.png"));
    static final GlTexture deleteIcon = new GlTexture(new ClasspathResource("/icons/menu/new/trash.png"));
    public final MsdfFont boldFont;
    public final MsdfFont semiboldFont;
    public final AnimatedFloat xAnimation;
    public final AnimatedFloat yAnimation;
    public final WidgetBounds iconBounds;
    public final ToggleAnimator selectedAnimator;
    public final ToggleAnimator favoriteAnimator;
    public final ToggleAnimator hoverAnimator;
    public final ToggleAnimator visibilityAnimator;
    public final WidgetBounds checkBounds;
    public final ThemeCard themeContext;
    public final IconLabelBadge modeBadge;
    public final List<IconButtonWidget> actionButtons;
    public final IconButtonWidget authorButton;
    public List<Integer> paletteColors;
    public boolean pendingHide;
    public boolean selected;
    public boolean favorite;
    public boolean positionInitialized;
    public boolean menuDragging;

    public ThemeCard2(ThemeCard class772Var) {
        super(new FrameElementColumn(0.0f, 0.0f));
        this.themeIcon = new GlTexture(new ClasspathResource("/icons/menu/new/tabs/themes.png"));
        this.smileIcon = new GlTexture(new ClasspathResource("/icons/menu/new/smile.png"));
        this.checkmarkIcon = new GlTexture(new ClasspathResource("/icons/menu/new/checkmark.png"));
        this.calendarIcon = new GlTexture(new ClasspathResource("/icons/menu/new/calendar.png"));
        this.boldFont = Fonts.INTER_BOLD.get();
        this.semiboldFont = Fonts.INTER_SEMIBOLD.get();
        this.xAnimation = new AnimatedFloat(200, Easings.EASE_IN_OUT_CUBIC);
        this.yAnimation = new AnimatedFloat(200, Easings.EASE_IN_OUT_CUBIC);
        this.iconBounds = new WidgetBounds(0.0f, 0.0f, 36.0f, 36.0f);
        this.selectedAnimator = new ToggleAnimator(100, Easings.LINEAR);
        this.favoriteAnimator = new ToggleAnimator(250, Easings.EASE_IN_OUT_CUBIC);
        this.hoverAnimator = new ToggleAnimator(100, Easings.LINEAR);
        this.visibilityAnimator = new ToggleAnimator(250, Easings.EASE_IN_OUT_CUBIC);
        this.checkBounds = new WidgetBounds(0.0f, 0.0f, 19.0f, 19.0f);
        this.actionButtons = new ArrayList();
        this.paletteColors = new ArrayList();
        this.selected = false;
        this.favorite = false;
        this.themeContext = class772Var;
        this.modeBadge = new IconLabelBadge(this.themeIcon, class772Var.type() == ThemeMode.LIGHT ? Translation.clearText("Светлая тема") : Translation.clearText("Тёмная тема"));
        this.paletteColors = extractPaletteColors(class772Var.palette());
        this.authorButton = new IconButtonWidget(this.smileIcon, () -> {
        }, 12.0f, 12.0f).tooltip(Translation.clearText("Вы являетесь автором этой темы"), this.smileIcon);
        this.actionButtons.addAll(List.of(new IconButtonWidget(loadIcon, this::selectExclusive, 14.0f, 14.0f).tooltip(Translation.clearText("Загрузить тему"), loadIcon), new IconButtonWidget(copyIcon, () -> {
        }, 14.0f, 14.0f).tooltip(Translation.clearText("Скопировать Cloud ID темы"), copyIcon), new IconButtonWidget(starIcon, this::invertFavorite, 14.0f, 14.0f).tooltip(Translation.clearText("Добавить тему в избранные"), starIcon), new IconButtonWidget(editIcon, () -> {
        }, 14.0f, 14.0f).tooltip(Translation.clearText("Редактировать тему"), editIcon), new IconButtonWidget(deleteIcon, () -> {
        }, 14.0f, 14.0f).tooltip(Translation.clearText("Удалить тему"), deleteIcon)));
        addChild(this.authorButton);
        this.actionButtons.forEach((v1) -> {
            addChild(v1);
        });
    }

    @Override
    public float contentHeight() {
        return 0.0f;
    }

    @Override
    public void render(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        class115VarColorStack.push();
        class115VarColorStack.alpha(this.visibilityAnimator.smoothAnimation());
        float fSmoothAnimation = 0.0f;
        if (this.selected) {
            fSmoothAnimation = 1.0f;
        } else if (!this.selectedAnimator.isZero()) {
            fSmoothAnimation = this.selectedAnimator.smoothAnimation();
        } else if (!this.hoverAnimator.isZero()) {
            fSmoothAnimation = this.hoverAnimator.smoothAnimation();
        }
        if (fSmoothAnimation > 0.0f) {
            class115VarColorStack.push();
            class115VarColorStack.alpha(fSmoothAnimation);
            class699Var.fillOutlinedRoundedRect(x(), y(), width(), height() + contentHeight(), 10.0f, 2.5f, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(800).argb()));
            class115VarColorStack.pop();
        }
        class699Var.fillOutlinedRoundedRect(this.iconBounds.x(), this.iconBounds.y(), this.iconBounds.width(), this.iconBounds.height(), 10.0f, 2.5f, class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(400).argb(), 0), class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(400).argb()), this.selectedAnimator.smoothAnimation()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(600).argb()));
        class699Var.textureVerticalC(this.themeIcon, (this.iconBounds.x() + (this.iconBounds.width() / 2.0f)) - (this.themeIcon.width() / 2.0f), this.iconBounds.y() + (this.iconBounds.height() / 2.0f), this.themeIcon.width(), this.themeIcon.height(), class115VarColorStack.interpolate(class115VarColorStack.computeColor(this.themeContext.kind() == ConfigOrigin.OFFICIAL ? class764VarPalette.accent().argb() : class764VarPalette.text().tone(600).argb()), class115VarColorStack.computeColor(ThemePalette.golden.argb()), this.favoriteAnimator.smoothAnimation()));
        int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb());
        float fX = this.iconBounds.x() + this.iconBounds.width() + 8.0f;
        class699Var.texture(this.calendarIcon, fX, this.iconBounds.y() + 2.0f, this.calendarIcon.width(), this.calendarIcon.height(), class115VarColorStack.computeColor(class764VarPalette.text().tone(700).argb()));
        class699Var.text(this.boldFont, this.themeContext.date(), 10, fX + this.calendarIcon.width() + 4.0f, this.iconBounds.y() + 2.0f, class115VarColorStack.computeColor(class764VarPalette.text().tone(700).argb()));
        if (this.themeContext.author().name().equals(Expensive.INSTANCE.userSession().username())) {
            class699Var.circle(fX + this.calendarIcon.width() + 4.0f + class699Var.textWidthPhysical(this.boldFont, this.themeContext.date(), 10) + 6.0f + 2.0f, this.iconBounds.y() + 2.0f + (this.boldFont.getHeight(10.0f) / 2.0f), 1.0f, class115VarColorStack.computeColor(class764VarPalette.text().tone(700).argb()));
            this.authorButton.setColor(class764VarPalette.text().tone(400).argb());
            this.authorButton.render(class699Var);
            this.authorButton.setPosition(fX + this.calendarIcon.width() + 4.0f + class699Var.textWidthPhysical(this.boldFont, this.themeContext.date(), 10) + 6.0f + 2.0f + 6.0f, this.iconBounds.y() + 2.0f);
            class699Var.texture(this.smileIcon, fX + this.calendarIcon.width() + 4.0f + class699Var.textWidthPhysical(this.boldFont, this.themeContext.date(), 10) + 6.0f + 2.0f + 6.0f, this.iconBounds.y() + 2.0f, this.smileIcon.width(), this.smileIcon.height(), class115VarColorStack.computeColor(class764VarPalette.text().tone(400).argb()));
        }
        float fX2 = ((x() + width()) - 10.0f) - class699Var.textWidthPhysical(this.semiboldFont, this.themeContext.author().name(), 12);
        class699Var.text(this.semiboldFont, this.themeContext.author().name(), 12, fX2, (this.iconBounds.y() + (this.iconBounds.height() / 2.0f)) - (this.semiboldFont.getHeight(12.0f) / 2.0f), class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb()));
        class699Var.roundedTexture(this.themeContext.author().avatar(), (fX2 - 6.0f) - 20.0f, (this.iconBounds.y() + (this.iconBounds.height() / 2.0f)) - 10.0f, 20.0f, 20.0f, 6.0f, class115VarColorStack.white());
        float f = (((fX2 - 6.0f) - 20.0f) - 20.0f) - fX;
        if (class699Var.textWidthPhysical(this.boldFont, this.themeContext.name(), 13) > f) {
            int i = iComputeColor & 16777215;
            String str = "";
            for (int length = this.themeContext.name().length(); length > 0; length--) {
                String strSubstring = this.themeContext.name().substring(0, length);
                if (class699Var.textWidthPhysical(this.boldFont, strSubstring, 13) <= f) {
                    str = strSubstring;
                    break;
                }
            }
            class699Var.textWithHorizontalGradient(this.boldFont, str, 13, fX, this.iconBounds.y() + 2.0f + this.boldFont.getHeight(10.0f) + 4.0f, iComputeColor, i);
        } else {
            class699Var.text(this.boldFont, this.themeContext.name(), 13, fX, this.iconBounds.y() + 2.0f + this.boldFont.getHeight(10.0f) + 4.0f, iComputeColor);
        }
        boolean z = this.themeContext.type() == ThemeMode.LIGHT;
        class115VarColorStack.push();
        class115VarColorStack.alpha(this.hoverAnimator.state() ? 0.0f : 1.0f - this.hoverAnimator.smoothAnimation());
        this.modeBadge.setBaseColor(Integer.valueOf(z ? class115VarColorStack.computeColor(14343394, 0.1f) : class115VarColorStack.computeColor(6316390, 0.1f)));
        this.modeBadge.setTextColor(Integer.valueOf(z ? class115VarColorStack.computeColor(class764VarPalette.text().tone(200).argb()) : class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb())));
        this.modeBadge.render(class699Var);
        class115VarColorStack.pop();
        class115VarColorStack.push();
        class115VarColorStack.alpha(this.selectedAnimator.smoothAnimation());
        class699Var.fillRoundedRect(this.checkBounds.x(), this.checkBounds.y(), this.checkBounds.width(), this.checkBounds.height(), 5.0f, class115VarColorStack.computeColor(class764VarPalette.accent().argb(), 0.15f));
        class699Var.textureVerticalCHorizontalC(this.checkmarkIcon, this.checkBounds.x() + (this.checkBounds.width() / 2.0f), this.checkBounds.y() + (this.checkBounds.height() / 2.0f), this.checkmarkIcon.width(), this.checkmarkIcon.height(), class115VarColorStack.computeColor(class764VarPalette.accent().argb()));
        class115VarColorStack.pop();
        class115VarColorStack.push();
        class115VarColorStack.alpha(this.hoverAnimator.smoothAnimation());
        for (int i2 = 0; i2 < this.actionButtons.size(); i2++) {
            if (!this.selected || i2 != 0) {
                this.actionButtons.get(i2).render(class699Var);
            }
        }
        class115VarColorStack.pop();
        float fX3 = (x() + width()) - 10.0f;
        float fY = this.modeBadge.y() + (this.modeBadge.height() / 2.0f);
        for (int size = this.paletteColors.size() - 1; size >= 0; size--) {
            class699Var.circle(fX3, fY, 5.0f, class115VarColorStack.computeColor(this.paletteColors.get(size).intValue()));
            fX3 -= 16.0f;
        }
        class115VarColorStack.pop();
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        this.checkmarkIcon.setDimensions(14, 14);
        this.themeIcon.setDimensions(14, 14);
        this.calendarIcon.setDimensions(11, 11);
        this.smileIcon.setDimensions(12, 12);
        this.iconBounds.withPosition(x() + 10.0f, y() + 10.0f);
        this.modeBadge.layout(class698Var);
        this.modeBadge.setPosition(this.iconBounds.x() + ((this.checkBounds.width() + 3.0f) * this.selectedAnimator.animation()), ((y() + height()) - 10.0f) - this.modeBadge.height());
        this.checkBounds.withPosition(this.iconBounds.x(), ((y() + height()) - 10.0f) - this.checkBounds.height());
        super.layout(class698Var);
        float fX = this.modeBadge.x() + (5.0f * this.selectedAnimator.animation());
        float fY = (this.modeBadge.y() + (this.modeBadge.height() / 2.0f)) - 7.0f;
        for (int i = 0; i < this.actionButtons.size(); i++) {
            IconButtonWidget class742Var = this.actionButtons.get(i);
            if (!this.selected || i != 0) {
                class742Var.setPosition(fX, fY);
                fX += class742Var.width() + 10.0f;
            }
        }
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        if (this.positionInitialized) {
            this.xAnimation.animate(class141Var);
            this.yAnimation.animate(class141Var);
            setPosition(this.xAnimation.animatedValue(), this.yAnimation.animatedValue());
        }
        this.selectedAnimator.state(this.selected);
        this.favoriteAnimator.state(this.favorite);
        this.selectedAnimator.animate(class141Var);
        this.favoriteAnimator.animate(class141Var);
        this.hoverAnimator.animate(class141Var);
        this.visibilityAnimator.animate(class141Var);
        if (this.pendingHide && this.visibilityAnimator.isZero()) {
            this.pendingHide = false;
            if (visible()) {
                visible(false);
                Expensive.INSTANCE.tabsController().current().markFramesDirty();
            }
        }
        super.animation(class141Var);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        boolean z2 = z;
        if (class688Var.inputEvent() instanceof CursorMoveInput) {
            this.hoverAnimator.state(class688Var.inArea(x(), y() + 3.0f, width(), height() - (3.0f * 2.0f)) && !z);
        }
        boolean z3 = z2;
        for (int i = 0; i < this.actionButtons.size(); i++) {
            IconButtonWidget class742Var = this.actionButtons.get(i);
            if (this.selected && i == 0 && class688Var.inArea(class742Var.x(), class742Var.y(), class742Var.width(), class742Var.height())) {
                z3 = true;
            }
            if (class742Var.handleInput(class688Var, z3)) {
                z2 = true;
                z3 = true;
            }
        }
        if (this.authorButton.handleInput(class688Var, z2)) {
            z2 = true;
        }
        return z2;
    }

    @Override
    public void onMenuDrag(boolean z) {
        this.menuDragging = z;
        if (z && this.positionInitialized) {
            this.xAnimation.set(x());
            this.yAnimation.set(y());
            this.xAnimation.destination(x());
            this.yAnimation.destination(y());
        }
    }

    @Override
    public float width() {
        return 281.0f;
    }

    @Override
    public float height() {
        return 85.0f;
    }

    public void updateFilterVisibility(boolean z) {
        if (!z) {
            if (visible()) {
                this.visibilityAnimator.state(false);
                this.pendingHide = true;
                return;
            }
            return;
        }
        this.pendingHide = false;
        if (!visible()) {
            visible(true);
            Expensive.INSTANCE.tabsController().current().markFramesDirty();
        }
        this.visibilityAnimator.state(true);
    }

    public void targetPosition(float f, float f2, boolean z) {
        if (!this.positionInitialized || !z) {
            this.positionInitialized = true;
            snapTo(f, f2);
        } else if (this.menuDragging) {
            snapTo(f, f2);
        } else {
            this.xAnimation.destination(f);
            this.yAnimation.destination(f2);
        }
    }

    public void snapTo(float f, float f2) {
        setPosition(f, f2);
        this.xAnimation.set(f);
        this.yAnimation.set(f2);
    }

    public void invertFavorite() {
        this.favorite = !this.favorite;
        Expensive.INSTANCE.tabsController().current().markFramesDirty();
    }

    public void selectExclusive() {
        MenuTabElement class732VarCurrent = Expensive.INSTANCE.tabsController().current();
        for (AbstractFrame class757Var : class732VarCurrent.frames()) {
            if (class757Var instanceof ThemeCard2) {
                ((ThemeCard2) class757Var).selected(false);
            }
        }
        selected(true);
        applyTheme();
        class732VarCurrent.markFramesDirty();
    }

    public void applyTheme() {
        if (this.themeContext == null || this.themeContext.theme() == null) {
            return;
        }
        ThemeManager.INSTANCE.setTheme(this.themeContext.theme());
        ThemesModule themesModule = ThemesModule.instance();
        if (themesModule != null) {
            String id = this.themeContext.theme().id();
            for (ThemeSelection themeSelection : ThemeSelection.values()) {
                if (ThemeManager.idFor(themeSelection).equals(id)) {
                    themesModule.themeMode.select(themeSelection);
                    themesModule.uiColorMode.select(UiColorMode.DEFAULT);
                    break;
                }
            }
        }
    }

    public List<Integer> extractPaletteColors(ThemePalette class764Var) {
        return List.of(Integer.valueOf(class764Var.accent().argb()), Integer.valueOf(class764Var.accentBright().argb()), Integer.valueOf(class764Var.favorite().argb()), Integer.valueOf(class764Var.surfaceBackground().tone(400).argb()), Integer.valueOf(class764Var.surfaceBackground().tone(700).argb()), Integer.valueOf(class764Var.text().tone(200).argb()), Integer.valueOf(class764Var.text().tone(500).argb()));
    }

    public ThemeCard themeContext() {
        return this.themeContext;
    }

    public ThemeCard2 selected(boolean z) {
        this.selected = z;
        return this;
    }

    public boolean selected() {
        return this.selected;
    }

    public boolean favorite() {
        return this.favorite;
    }
}
