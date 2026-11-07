package aethereal.ui;
import aethereal.util.ActionDialogBuilder;
import aethereal.render.AnimatedFloat;
import aethereal.render.AvatarCache;
import aethereal.resource.ClasspathResource;
import aethereal.render.ColorStack;
import aethereal.model.CursorMoveInput;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.model.InputEventContext;
import aethereal.Lang;
import aethereal.model.LayoutScaleContext;
import aethereal.render.MsdfFont;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.io.IOException;
import java.util.function.Consumer;

public class ConfigCard extends AbstractFrame {
    static final GlTexture saveIcon = loadTexture("/icons/menu/new/save.png");
    static final GlTexture loadIcon = loadTexture("/icons/menu/new/display.png");
    static final GlTexture infoIcon = loadTexture("/icons/menu/new/text_scroll.png");
    public final GlTexture folderIcon;
    public final GlTexture calendarIcon;
    public final GlTexture defaultAvatar;
    public final AnimatedFloat animX;
    public final AnimatedFloat animY;
    public final ToggleAnimator selectAnimator;
    public final ToggleAnimator favoriteAnimator;
    public final ToggleAnimator hoverAnimator;
    public final ClickableBehavior favoriteClickable;
    public final WidgetBounds iconBounds;
    public final IconButtonWidget loadButton;
    public final IconButtonWidget saveButton;
    public final IconButtonWidget infoButton;

    public final CloudConfigCard configContext;
    public boolean favorite;
    public boolean selected;
    public boolean positioned;
    public boolean dragging;
    public volatile GlTexture avatarTexture;
    public volatile boolean avatarLoading;

    static GlTexture loadTexture(String str) {
        return new GlTexture(new ClasspathResource(str));
    }

    public ConfigCard(CloudConfigCard class768Var, Consumer<CloudConfigCard> consumer, Consumer<CloudConfigCard> consumer2) {
        super(new FrameElementColumn(0.0f, 0.0f));
        this.folderIcon = loadTexture("/icons/menu/new/folder_fill.png");
        this.calendarIcon = loadTexture("/icons/menu/new/calendar.png");
        this.defaultAvatar = loadTexture("assets/expensive/textures/avatar.png");
        this.animX = new AnimatedFloat(200, Easings.EASE_IN_OUT_CUBIC);
        this.animY = new AnimatedFloat(200, Easings.EASE_IN_OUT_CUBIC);
        this.selectAnimator = new ToggleAnimator(100, Easings.LINEAR);
        this.favoriteAnimator = new ToggleAnimator(250, Easings.EASE_IN_OUT_CUBIC);
        this.hoverAnimator = new ToggleAnimator(100, Easings.LINEAR);
        this.favoriteClickable = new ClickableBehavior();
        this.iconBounds = new WidgetBounds(0.0f, 0.0f, 36.0f, 36.0f);
        this.configContext = class768Var;
        this.favorite = Expensive.INSTANCE.configManager().menuStateConfig().isConfigFavorite(class768Var.cloudId());
        this.favoriteAnimator.state(this.favorite);
        this.favoriteClickable.clickCallback(this::invertFavorite);
        this.loadButton = new IconButtonWidget(loadIcon, () -> {
            Expensive.INSTANCE.cloudConfigService().downloadConfig(class768Var.cloudId()).thenAccept(class089Var -> {
                if (!this.selected) {
                    Expensive.INSTANCE.cloudConfigService().applyConfig(class089Var);
                }
                Expensive.INSTANCE.configManager().saveLastConfigID();
                consumer2.accept(class768Var);
            }).exceptionally(th -> {
                Expensive.LOGGER.error("Load failed", th);
                return null;
            });
        }, 14.0f, 14.0f).tooltip(Lang.CONFIG_BUTTON_LOAD_TOOLTIP, loadIcon);
        this.saveButton = new IconButtonWidget(saveIcon, this::openSaveDialog, 14.0f, 14.0f).tooltip(Lang.CONFIG_BUTTON_SAVE_TOOLTIP, saveIcon);
        this.infoButton = new IconButtonWidget(infoIcon, () -> {
            consumer.accept(class768Var);
        }, 14.0f, 14.0f).tooltip(Lang.CONFIG_BUTTON_INFO_TOOLTIP, infoIcon);
        addChild(this.infoButton);
        addChild(this.loadButton);
        addChild(this.saveButton);
    }

    public GlTexture resolveAvatar() {
        if (!this.avatarLoading) {
            this.avatarLoading = true;
            AvatarCache.load(this.configContext.author().avatarUrl(), this.defaultAvatar, Expensive.INSTANCE.executor()).thenAccept(class073Var -> {
                this.avatarTexture = class073Var;
            });
        }
        return this.avatarTexture != null ? this.avatarTexture : this.defaultAvatar;
    }

    @Override
    public void render(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        MsdfFont class161Var = Fonts.INTER_BOLD.get();
        MsdfFont class161Var2 = Fonts.INTER_SEMIBOLD.get();
        float fMax = this.selected ? Math.max(this.selectAnimator.smoothAnimation(), 0.5f) : !this.selectAnimator.isZero() ? this.selectAnimator.smoothAnimation() : Math.min(this.hoverAnimator.smoothAnimation(), 0.5f);
        if (fMax > 0.0f) {
            class115VarColorStack.push();
            class115VarColorStack.alpha(fMax);
            class699Var.fillOutlinedRoundedRect(x(), y(), width(), height(), 10.0f, 2.5f, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(800).argb()));
            class115VarColorStack.pop();
        }
        class699Var.fillOutlinedRoundedRect(this.iconBounds.x(), this.iconBounds.y(), this.iconBounds.width(), this.iconBounds.height(), 10.0f, 2.5f, class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(400).argb(), 0), class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(400).argb()), this.selectAnimator.smoothAnimation()), class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(600).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(500).argb()), this.favoriteClickable.hoverAnimation().smoothAnimation()));
        class699Var.textureVerticalC(this.folderIcon, (this.iconBounds.x() + (this.iconBounds.width() / 2.0f)) - 7.0f, this.iconBounds.y() + (this.iconBounds.height() / 2.0f), 14, 14, class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(600).argb()), class115VarColorStack.computeColor(ThemePalette.golden.argb()), this.favoriteAnimator.smoothAnimation()));
        float fX = ((x() + width()) - 10.0f) - class699Var.textWidthPhysical(class161Var2, this.configContext.author().name(), 12);
        class115VarColorStack.push();
        class115VarColorStack.alpha(this.hoverAnimator.state() ? 0.0f : 1.0f - this.hoverAnimator.smoothAnimation());
        class699Var.text(class161Var2, this.configContext.author().name(), 12, fX, (this.iconBounds.y() + (this.iconBounds.height() / 2.0f)) - (class161Var2.getHeight(12.0f) / 2.0f), class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb()));
        class699Var.roundedTexture(resolveAvatar(), fX - 26.0f, (this.iconBounds.y() + (this.iconBounds.height() / 2.0f)) - 10.0f, 20.0f, 20.0f, 6.0f, class115VarColorStack.white());
        class115VarColorStack.pop();
        class115VarColorStack.push();
        class115VarColorStack.alpha(this.hoverAnimator.smoothAnimation());
        this.loadButton.render(class699Var);
        this.saveButton.render(class699Var);
        this.infoButton.render(class699Var);
        class115VarColorStack.pop();
        float fX2 = this.iconBounds.x() + this.iconBounds.width() + 8.0f;
        class699Var.texture(this.calendarIcon, fX2, this.iconBounds.y() + 2.0f, 11.0f, 11.0f, class115VarColorStack.computeColor(class764VarPalette.text().tone(700).argb()));
        class699Var.text(class161Var, this.configContext.date(), 10, fX2 + 15.0f, this.iconBounds.y() + 2.0f, class115VarColorStack.computeColor(class764VarPalette.text().tone(700).argb()));
        int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb());
        float f = (fX - 46.0f) - fX2;
        float fY = this.iconBounds.y() + 2.0f + class161Var.getHeight(10.0f) + 4.0f;
        if (class699Var.textWidthPhysical(class161Var, this.configContext.name(), 13) <= f) {
            class699Var.text(class161Var, this.configContext.name(), 13, fX2, fY, iComputeColor);
            return;
        }
        String str = "";
        for (int length = this.configContext.name().length(); length > 0; length--) {
            String strSubstring = this.configContext.name().substring(0, length);
            if (class699Var.textWidthPhysical(class161Var, strSubstring, 13) <= f) {
                str = strSubstring;
                break;
            }
        }
        class699Var.textWithHorizontalGradient(class161Var, str, 13, fX2, fY, iComputeColor, iComputeColor & 16777215);
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        this.folderIcon.setDimensions(14, 14);
        this.calendarIcon.setDimensions(11, 11);
        this.iconBounds.withPosition(x() + 10.0f, (y() + (height() / 2.0f)) - 18.0f);
        this.favoriteClickable.setDimensions(this.iconBounds.x(), this.iconBounds.y(), this.iconBounds.width(), this.iconBounds.height());
        float fX = ((x() + width()) - 16.0f) - this.saveButton.width();
        float fY = (this.iconBounds.y() + (this.iconBounds.height() / 2.0f)) - (this.saveButton.height() / 2.0f);
        this.infoButton.setPosition(fX, fY);
        this.saveButton.setPosition((fX - 10.0f) - this.saveButton.width(), fY);
        this.loadButton.setPosition(((fX - 20.0f) - this.saveButton.width()) - this.loadButton.width(), fY);
        super.layout(class698Var);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        if (this.positioned) {
            this.animX.animate(class141Var);
            this.animY.animate(class141Var);
            setPosition(this.animX.animatedValue(), this.animY.animatedValue());
        }
        this.favoriteClickable.animate(class141Var);
        this.selectAnimator.state(this.selected);
        this.selectAnimator.animate(class141Var);
        this.favoriteAnimator.animate(class141Var);
        this.hoverAnimator.animate(class141Var);
        super.animation(class141Var);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        boolean z2 = z;
        if (this.favoriteClickable.handleInput(class688Var, z)) {
            z2 = true;
        }
        if (class688Var.inputEvent() instanceof CursorMoveInput) {
            this.hoverAnimator.state(class688Var.inArea(x(), y() + 2.0f, width(), height() - 4.0f) && !z);
        }
        if (super.handleInput(class688Var, z2)) {
            z2 = true;
        }
        return z2;
    }

    @Override
    public void onMenuDrag(boolean z) {
        this.dragging = z;
        if (z && this.positioned) {
            this.animX.set(x());
            this.animY.set(y());
            this.animX.destination(x());
            this.animY.destination(y());
        }
    }

    @Override
    public float contentHeight() {
        return 0.0f;
    }

    @Override
    public float width() {
        return 281.0f;
    }

    @Override
    public float height() {
        return 62.0f;
    }

    public void targetPosition(float f, float f2, boolean z) {
        if (this.positioned && z && !this.dragging) {
            this.animX.destination(f);
            this.animY.destination(f2);
        } else {
            this.positioned = true;
            snapTo(f, f2);
        }
    }

    public void snapTo(float f, float f2) {
        setPosition(f, f2);
        this.animX.set(f);
        this.animY.set(f2);
    }

    public void invertFavorite() {
        this.favorite = !this.favorite;
        this.favoriteAnimator.state(this.favorite);
        try {
            Expensive.INSTANCE.configManager().menuStateConfig().setConfigFavorite(this.configContext.cloudId(), this.favorite);
            Expensive.INSTANCE.configManager().saveMenuState();
        } catch (IOException e) {
            Expensive.LOGGER.error("Failed to save favorite", e);
        }
        Expensive.INSTANCE.tabsController().current().markFramesDirty();
    }

    public void openSaveDialog() {
        ActionConfirmDialog class777VarActionConfirmationDialogContainer = Expensive.INSTANCE.menuWindow().actionConfirmationDialogContainer();
        class777VarActionConfirmationDialogContainer.open(new ActionDialogBuilder().title(Lang.CONFIG_SAVE_TITLE).description(Lang.CONFIG_SAVE_DESCRIPTION).placeholderText(Lang.CONFIG_SAVE_PLACEHOLDER).icon(saveIcon).confirmLabel(Lang.CONFIG_SAVE_CONFIRM).onConfirm(() -> {
            class777VarActionConfirmationDialogContainer.setLoading(true, Lang.CONFIG_SAVE_LOADING.effective());
            Expensive.INSTANCE.cloudConfigService().saveConfig(this.configContext.cloudId(), Expensive.INSTANCE.moduleRepository(), Expensive.INSTANCE.widgetStack()).thenRun(() -> {
                class777VarActionConfirmationDialogContainer.setLoading(false, "");
                class777VarActionConfirmationDialogContainer.close();
            }).exceptionally(th -> {
                class777VarActionConfirmationDialogContainer.setLoading(false, "");
                Expensive.LOGGER.error("Save failed", th);
                class777VarActionConfirmationDialogContainer.showError(Expensive.INSTANCE.cloudConfigService().errorMessage(th).effective());
                return null;
            });
        }).build());
    }

    public CloudConfigCard configContext() {
        return this.configContext;
    }

    public boolean favorite() {
        return this.favorite;
    }

    public boolean selected() {
        return this.selected;
    }

    public ConfigCard selected(boolean z) {
        this.selected = z;
        return this;
    }
}
