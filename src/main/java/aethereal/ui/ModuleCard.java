package aethereal.ui;
import aethereal.render.AnimatedFloat;
import aethereal.type.BindMode;
import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.ButtonSetting;
import aethereal.ui.setting.ButtonSettingElement;
import aethereal.ui.setting.CheckboxSettingElement;
import aethereal.resource.ClasspathResource;
import aethereal.ui.setting.ColorSetting;
import aethereal.ui.setting.ColorSettingElement;
import aethereal.render.ColorStack;
import aethereal.util.ConfigAutoSaveScheduler;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.ui.setting.EnumSetting;
import aethereal.ui.setting.ExpandableSetting;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.ui.setting.GroupSettingElement;
import aethereal.render.HighlightAnimation;
import aethereal.model.InputEventContext;
import aethereal.ui.setting.KeybindSetting;
import aethereal.ui.setting.KeybindSettingElement;
import aethereal.Lang;
import aethereal.model.LayoutScaleContext;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.setting.ModeSettingElement;
import aethereal.module.Module;
import aethereal.render.MsdfFont;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.ui.setting.MultiSelectSettingElement;
import aethereal.ui.setting.NumberSetting;
import aethereal.ui.setting.OrderedEnumSetting;
import aethereal.ui.setting.OrderedListSettingElement;
import aethereal.util.OverlayCommandQueue;
import aethereal.util.RenderCommandQueue;
import aethereal.render.ScreenResolution;
import aethereal.ui.setting.SeparatorSetting;
import aethereal.ui.setting.Setting;
import aethereal.ui.setting.SliderSettingElement;
import aethereal.ui.setting.SoundModeSettingElement;
import aethereal.ui.setting.TextFieldSetting;
import aethereal.ui.setting.TextFieldSettingElement;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.model.Translation;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModuleCard extends AbstractFrame {
    public final GlTexture frameIcon;
    public final GlTexture starIcon;
    public final GlTexture starFilledIcon;
    public final MsdfFont font;
    static final float height = 48.0f;
    public final Module module;
    public final String[] aliases;
    public final String name;
    public final ToggleSwitch toggleSwitch;

    public final KeybindField keybindField;
    static final float columnWidth = 252.0f;
    static final float padding = 14.0f;
    public boolean favorite;
    public final TextureToggleWidget favoriteToggle;
    public final ToggleAnimator nameHoverAnimation;
    public float nameX;
    public float nameY;
    public float nameWidth;
    public float nameHeight;
    public final AnimatedFloat xAnimation;
    public final AnimatedFloat yAnimation;

    public final KeybindConfigWindow keybindWindow;
    public final WidgetBounds keybindBounds;
    public float scrollOffset;

    public final HighlightAnimation highlightAnimation;

    public ScreenResolution resolution;
    public boolean positionInitialized;
    public static final float gap = 8.0f;
    public static final float screenMargin = 5.0f;
    public boolean dragging;

    public ModuleCard(Module class605Var, String[] strArr) {
        super(new FrameElementColumn(columnWidth, padding));
        this.frameIcon = new GlTexture(new ClasspathResource("/icons/menu/new/frame.png"));
        this.starIcon = new GlTexture(new ClasspathResource("/icons/menu/new/star.png"));
        this.starFilledIcon = new GlTexture(new ClasspathResource("/icons/menu/new/star_fill.png"));
        this.font = Fonts.INTER_SEMIBOLD.get();
        this.nameHoverAnimation = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);
        this.xAnimation = new AnimatedFloat(200, Easings.EASE_IN_OUT_CUBIC);
        this.yAnimation = new AnimatedFloat(200, Easings.EASE_IN_OUT_CUBIC);
        this.keybindBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
        this.highlightAnimation = new HighlightAnimation(300, Easings.EASE_IN_OUT_CUBIC);
        this.module = class605Var;
        this.aliases = strArr;
        this.name = class605Var.getName();
        this.favorite = Expensive.INSTANCE.configManager().menuStateConfig().isModuleFavorite(this.name);
        Objects.requireNonNull(class605Var);
        this.toggleSwitch = new ToggleSwitch(24.0f, 17.0f, class605Var::isState);
        ToggleSwitch class754Var = this.toggleSwitch;
        Objects.requireNonNull(class605Var);
        class754Var.runnable(wrapWithAutoSave(class605Var::switchState));
        Translation class254Var = Lang.BINDING_MODULE;
        String name = class605Var.getName();
        Objects.requireNonNull(class605Var);
        this.keybindWindow = new KeybindConfigWindow(class254Var, name, class605Var::getKeyBind);
        KeybindConfigWindow class755Var = this.keybindWindow;
        Objects.requireNonNull(class605Var);
        Supplier<BindMode> supplier = class605Var::getType;
        Objects.requireNonNull(class605Var);
        class755Var.bindType(supplier, class605Var::setType);
        KeybindConfigWindow class755Var2 = this.keybindWindow;
        Objects.requireNonNull(class605Var);
        class755Var2.onChange(wrapConsumerWithAutoSave(class605Var::setKey));
        Expensive.INSTANCE.menuWindow().priorityOverlayHandler().registerPopup(this.keybindWindow);
        this.favoriteToggle = new TextureToggleWidget(this.starIcon, this.starFilledIcon, () -> {
            return Boolean.valueOf(this.favorite);
        }, padding, padding);
        this.favoriteToggle.runnable(this::invertFavorite);
        this.keybindField = new KeybindField(Fonts.INTER_SEMIBOLD.get(), () -> {
            toggleKeybindWindow();
        }, gap, 4.0f, 6.0f, 11).addMutableKeys(class605Var.getKeyBind());
        KeybindField class743Var = this.keybindField;
        Objects.requireNonNull(class605Var);
        class743Var.onChange(wrapConsumerWithAutoSave(class605Var::setKey));
        class605Var.getSettings().forEach(class661Var -> {
            if (class661Var instanceof BooleanSetting) {
                BooleanSetting class665Var = (BooleanSetting) class661Var;
                Translation name2 = class665Var.getName();
                Translation description = class665Var.getDescription();
                Objects.requireNonNull(class665Var);
                CheckboxSettingElement class822Var = new CheckboxSettingElement(name2, description, class665Var::getKeyBind);
                Objects.requireNonNull(class665Var);
                class822Var.toggledSupplier(class665Var::isValue);
                Objects.requireNonNull(class665Var);
                class822Var.changeRunnable(wrapWithAutoSave(class665Var::switchValue));
                Objects.requireNonNull(class665Var);
                class822Var.onChange(wrapConsumerWithAutoSave(class665Var::setKey));
                Objects.requireNonNull(class665Var);
                Supplier<BindMode> supplier2 = class665Var::getType;
                Objects.requireNonNull(class665Var);
                class822Var.setupBindType(supplier2, class665Var::setType);
                class822Var.visibleSupplier(class665Var.getVisible());
                this.frameElementsContainer.addFrameElement(class822Var);
                return;
            }
            if (class661Var instanceof NumberSetting) {
                NumberSetting class613Var = (NumberSetting) class661Var;
                SliderSettingElement class830Var = new SliderSettingElement(class613Var.getName(), class613Var.getDescription(), class613Var.valueUnit(), class613Var.max(), class613Var.min(), class613Var.step());
                Objects.requireNonNull(class613Var);
                class830Var.currentValueSupplier(class613Var::currentValue);
                Objects.requireNonNull(class613Var);
                class830Var.onValueChanged(wrapConsumerWithAutoSave((v1) -> {
                    class613Var.setCurrentValue(v1);
                }));
                class830Var.visibleSupplier(class613Var.getVisible());
                this.frameElementsContainer.addFrameElement(class830Var);
                return;
            }
            if (class661Var instanceof ModeSetting) {
                addModeElement((ModeSetting) class661Var);
                return;
            }
            if (class661Var instanceof EnumSetting) {
                addSoundModeElement((EnumSetting) class661Var);
                return;
            }
            if (class661Var instanceof MultiSelectSetting) {
                addMultiSelectElement((MultiSelectSetting) class661Var);
                return;
            }
            if (class661Var instanceof TextFieldSetting) {
                TextFieldSetting class612Var = (TextFieldSetting) class661Var;
                Translation name3 = class612Var.getName();
                Translation description2 = class612Var.getDescription();
                Translation placeholder = class612Var.getPlaceholder();
                boolean zIsOnlyDigits = class612Var.isOnlyDigits();
                int max = class612Var.getMax();
                boolean zIsPassword = class612Var.isPassword();
                Objects.requireNonNull(class612Var);
                TextFieldSettingElement class835Var = new TextFieldSettingElement(name3, description2, placeholder, zIsOnlyDigits, max, zIsPassword, class612Var::getText);
                Objects.requireNonNull(class612Var);
                class835Var.onChange(wrapConsumerWithAutoSave(class612Var::setText));
                class835Var.visibleSupplier(class612Var.getVisible());
                this.frameElementsContainer.addFrameElement(class835Var);
                return;
            }
            if (class661Var instanceof ButtonSetting) {
                ButtonSetting class666Var = (ButtonSetting) class661Var;
                ButtonSettingElement class821Var = new ButtonSettingElement(class666Var.getName(), class666Var.getDescription(), class666Var.getButtonName(), wrapWithAutoSave(class666Var.getRunnable()));
                class821Var.visibleSupplier(class666Var.getVisible());
                this.frameElementsContainer.addFrameElement(class821Var);
                return;
            }
            if (class661Var instanceof OrderedEnumSetting) {
                addOrderedListElement((OrderedEnumSetting) class661Var);
                return;
            }
            if (class661Var instanceof ColorSetting) {
                ColorSetting class667Var = (ColorSetting) class661Var;
                Translation name4 = class667Var.getName();
                Translation description3 = class667Var.getDescription();
                Objects.requireNonNull(class667Var);
                Supplier supplier3 = class667Var::getColorWithoutAlpha;
                Objects.requireNonNull(class667Var);
                ColorSettingElement class823Var = new ColorSettingElement(name4, description3, supplier3, class667Var::getAlpha);
                class823Var.visibleSupplier(class667Var.getVisible());
                Objects.requireNonNull(class667Var);
                class823Var.colorConsumer((v1) -> {
                    class667Var.setColor(v1);
                });
                Objects.requireNonNull(class667Var);
                class823Var.alphaConsumer((v1) -> {
                    class667Var.setAlpha(v1);
                });
                class823Var.commitRunnable(this::scheduleAutoSave);
                this.frameElementsContainer.addFrameElement(class823Var);
                return;
            }
            if (class661Var instanceof ExpandableSetting) {
                ExpandableSetting class670Var = (ExpandableSetting) class661Var;
                GroupSettingElement class827Var = new GroupSettingElement(class670Var.getName(), class670Var.getDescription(), class670Var.getSubSettings(), class670Var.getKeybinds());
                Objects.requireNonNull(class670Var);
                class827Var.toggledSupplier(class670Var::isValue);
                Objects.requireNonNull(class670Var);
                class827Var.changeRunnable(wrapWithAutoSave(class670Var::switchValue));
                class827Var.visibleSupplier(class670Var.getVisible());
                Objects.requireNonNull(class670Var);
                class827Var.onChange(wrapConsumerWithAutoSave(class670Var::setKey));
                this.frameElementsContainer.addFrameElement(class827Var);
                return;
            }
            if (!(class661Var instanceof KeybindSetting)) {
                if (class661Var instanceof SeparatorSetting) {
                    this.frameElementsContainer.addFrameElement(new SeparatorElement());
                    return;
                }
                return;
            }
            KeybindSetting class663Var = (KeybindSetting) class661Var;
            Translation name5 = class663Var.getName();
            Translation description4 = class663Var.getDescription();
            Objects.requireNonNull(class663Var);
            KeybindSettingElement class828Var = new KeybindSettingElement(name5, description4, class663Var::getKeyBind);
            class828Var.visibleSupplier(class663Var.getVisible());
            Objects.requireNonNull(class663Var);
            class828Var.onChange(wrapConsumerWithAutoSave(class663Var::setKey));
            this.frameElementsContainer.addFrameElement(class828Var);
        });
        addChild(this.toggleSwitch);
        addChild(this.favoriteToggle);
        addChild(this.keybindField);
    }

    public <E extends Enum<E>> void addOrderedListElement(OrderedEnumSetting<E> class609Var) {
        Objects.requireNonNull(class609Var);
        Supplier<List<E>> supplier = class609Var::orderedValues;
        Objects.requireNonNull(class609Var);
        Supplier<Set<E>> supplier2 = class609Var::getSelected;
        Objects.requireNonNull(class609Var);
        OrderedListSettingElement<E> class831Var = new OrderedListSettingElement<>(supplier, supplier2, class609Var::optionName, wrapConsumerWithAutoSave(e -> {
            if (class609Var.isSelected(e)) {
                class609Var.deselect(e);
            } else {
                class609Var.select(e);
            }
        }), (num, num2) -> {
            class609Var.move(num.intValue(), num2.intValue());
            scheduleAutoSave();
        });
        class831Var.visibleSupplier(class609Var.getVisible());
        this.frameElementsContainer.addFrameElement(class831Var);
    }

    public void toggleKeybindWindow() {
        float fDpiScaleFactor = Expensive.INSTANCE.windowController().dpiScaleFactor();
        if (this.keybindWindow.isOpen() || this.resolution == null) {
            this.keybindWindow.closeWindow();
            return;
        }
        float fScreenWidth = this.resolution.screenWidth() / fDpiScaleFactor;
        float fScreenHeight = this.resolution.screenHeight() / fDpiScaleFactor;
        float fWidth = this.keybindWindow.width();
        float f = this.keybindWindow.totalHeight();
        float fX = this.keybindBounds.x();
        float fY = this.keybindBounds.y() - this.scrollOffset;
        float fWidth2 = this.keybindBounds.width();
        float fHeight = this.keybindBounds.height();
        float f2 = fX + fWidth2 + gap;
        float f3 = (fX - gap) - fWidth;
        if (f2 + fWidth > fScreenWidth - screenMargin && f3 >= screenMargin) {
            f2 = f3;
        }
        float fMax = Math.max(screenMargin, Math.min(f2, (fScreenWidth - fWidth) - screenMargin));
        float f4 = (fY + (fHeight / 2.0f)) - (f / 2.0f);
        if (f4 + f > fScreenHeight - screenMargin) {
            f4 = (fScreenHeight - f) - screenMargin;
        }
        if (f4 < screenMargin) {
            f4 = 5.0f;
        }
        this.keybindWindow.openWindow();
        this.keybindWindow.setPosition(fMax, f4);
    }

    public <E extends Enum<E>> void addModeElement(ModeSetting<E> class669Var) {
        ModeSettingElement class825Var = new ModeSettingElement(class669Var.getName(), class669Var.getDescription(), class669Var.options());
        Objects.requireNonNull(class669Var);
        class825Var.onChangeCallback(wrapConsumerWithAutoSave(class669Var::setCurrentValue));
        Objects.requireNonNull(class669Var);
        class825Var.currentValueSupplier(class669Var::currentValue);
        class825Var.visibleSupplier(class669Var.getVisible());
        this.frameElementsContainer.addFrameElement(class825Var);
    }

    public <E extends Enum<E>> void addMultiSelectElement(MultiSelectSetting<E> class671Var) {
        Translation name = class671Var.getName();
        Translation description = class671Var.getDescription();
        Enum[] enumArrOptions = class671Var.options();
        Objects.requireNonNull(class671Var);
        Supplier supplier = class671Var::selectedValues;
        Objects.requireNonNull(class671Var);
        MultiSelectSettingElement class836Var = new MultiSelectSettingElement(name, description, enumArrOptions, supplier, wrapConsumerWithAutoSave(class671Var::toggle));
        class836Var.visibleSupplier(class671Var.getVisible());
        this.frameElementsContainer.addFrameElement(class836Var);
    }

    public <E extends Enum<E>> void addSoundModeElement(EnumSetting<E> class610Var) {
        SoundModeSettingElement class833Var = new SoundModeSettingElement(class610Var.getName(), class610Var.getDescription(), class610Var.options(), class610Var.soundAction());
        Objects.requireNonNull(class610Var);
        class833Var.onChangeCallback(wrapConsumerWithAutoSave(class610Var::setCurrentValue));
        Objects.requireNonNull(class610Var);
        class833Var.currentValueSupplier(class610Var::currentValue);
        class833Var.visibleSupplier(class610Var.getVisible());
        this.frameElementsContainer.addFrameElement(class833Var);
    }

    public void scheduleAutoSave() {
        ConfigAutoSaveScheduler.scheduleAutoSave();
    }

    public Runnable wrapWithAutoSave(Runnable runnable) {
        return () -> {
            if (runnable != null) {
                runnable.run();
            }
            scheduleAutoSave();
        };
    }

    public <T> Consumer<T> wrapConsumerWithAutoSave(Consumer<T> consumer) {
        return obj -> {
            consumer.accept(obj);
            scheduleAutoSave();
        };
    }

    public List<Setting> settings() {
        return this.module.getSettings();
    }

    @Override
    public void updateViewportVisibility(float f, float f2, float f3) {
        this.scrollOffset = f;
        super.updateViewportVisibility(f, f2, f3);
    }

    @Override
    public void render(DrawCtx class699Var) {
        this.resolution = class699Var.resolution();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        this.favoriteToggle.inactiveColor(class764VarPalette.text().tone(600));
        this.favoriteToggle.activeColor(ThemePalette.golden);
        class699Var.fillOutlinedRoundedRect(x(), y(), width(), height() + contentHeight(), 12.0f, 2.5f, class115VarColorStack.computeColor(ThemePalette.white.argb(), 0.03f), class115VarColorStack.computeColor(class764VarPalette.frameBackground().argb()));
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()), class115VarColorStack.computeColor(class764VarPalette.accent().argb()), this.toggleSwitch.toggleSwitchAnimation());
        int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.text().tone(100).argb());
        class699Var.texture(this.frameIcon, x() + padding, (y() + (height() / 2.0f)) - (this.frameIcon.height() / 2.0f), this.frameIcon.width(), this.frameIcon.height(), iInterpolate);
        this.nameX = x() + padding + this.frameIcon.width() + gap;
        this.nameY = (y() + (height() / 2.0f)) - (this.font.getHeight(15.0f) / 2.0f);
        this.nameWidth = class699Var.textWidthPhysical(this.font, this.name, 15);
        this.nameHeight = this.font.getHeight(15.0f);
        float fX = this.favoriteToggle.x() - 20.0f;
        String str = this.name;
        boolean z = this.nameX + this.nameWidth > fX;
        float fSmoothAnimation = z ? 1.0f - this.nameHoverAnimation.smoothAnimation() : 1.0f;
        if (fSmoothAnimation > 0.1f && z) {
            int iMax = Math.max(0, this.name.length() - ((int) Math.ceil(((this.nameX + this.nameWidth) - fX) / (this.nameWidth / this.name.length()))));
            String strSubstring = this.name.substring(0, iMax);
            if (iMax < this.name.length()) {
                strSubstring = strSubstring + "...";
            }
            str = strSubstring;
        }
        class699Var.text(this.font, str, 15, this.nameX, this.nameY, iComputeColor);
        renderFrameElements(class699Var);
        class115VarColorStack.push();
        class115VarColorStack.alpha(fSmoothAnimation);
        this.favoriteToggle.render(class699Var);
        this.keybindField.render(class699Var);
        this.keybindBounds.withPosition(this.keybindField.x(), this.keybindField.y()).withSize(this.keybindField.width(), this.keybindField.height());
        this.toggleSwitch.render(class699Var);
        this.toggleSwitch.setClickableArea(x(), y(), width(), height());
        class115VarColorStack.pop();
    }

    @Override
    public void onMenuDrag(boolean z) {
        this.dragging = z;
        if (z && this.positionInitialized) {
            this.xAnimation.set(x());
            this.yAnimation.set(y());
            this.xAnimation.destination(x());
            this.yAnimation.destination(y());
        }
    }

    @Override
    public void renderOverlays(DrawCtx class699Var) {
        renderFrameOverlays(class699Var);
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        this.keybindWindow.layout(class698Var);
        this.frameIcon.setDimensions(14, 14);
        this.starIcon.setDimensions(14, 14);
        this.starFilledIcon.setDimensions(14, 14);
        this.toggleSwitch.setPosition(((x() + width()) - padding) - this.toggleSwitch.width(), (y() + (height() / 2.0f)) - (this.toggleSwitch.height() / 2.0f));
        this.keybindField.rawX(() -> {
            return Float.valueOf(((((x() + width()) - padding) - this.toggleSwitch.width()) - gap) - this.keybindField.computeDesiredWidth(class698Var));
        });
        float fX = ((((x() + width()) - padding) - this.toggleSwitch.width()) - gap) - this.keybindField.width();
        this.keybindField.setPosition(fX, (y() + (height() / 2.0f)) - (this.keybindField.height() / 2.0f));
        this.favoriteToggle.setPosition((fX - gap) - this.favoriteToggle.width(), (y() + (height() / 2.0f)) - (this.favoriteToggle.height() / 2.0f));
        this.favoriteToggle.hitboxPadding(4.0f);
        this.frameElementsContainer.setPosition(x() + padding, Math.max(Math.max(this.toggleSwitch.y() + this.toggleSwitch.height(), this.keybindField.y() + this.keybindField.height()), this.favoriteToggle.y() + this.favoriteToggle.height()) + padding);
        super.layout(class698Var);
    }

    public void invertFavorite() {
        this.favorite = !this.favorite;
        try {
            Expensive.INSTANCE.configManager().menuStateConfig().setModuleFavorite(this.name, this.favorite);
            Expensive.INSTANCE.configManager().saveMenuState();
        } catch (IOException e) {
            Expensive.LOGGER.error("Failed to save module favorite state", e);
        }
        Expensive.INSTANCE.tabsController().current().markFramesDirty();
    }

    public void targetPosition(float f, float f2, boolean z) {
        if (!this.positionInitialized || !z) {
            this.positionInitialized = true;
            snapTo(f, f2);
        } else if (this.dragging) {
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

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        this.nameHoverAnimation.state(class688Var.inArea(this.nameX, this.nameY, this.nameWidth, this.nameHeight) && !z);
        return super.handleInput(class688Var, z);
    }

    @Override
    public void collectBlurElements(OverlayCommandQueue class677Var) {
        super.collectBlurElements(class677Var);
    }

    @Override
    public void collectBloomElements(RenderCommandQueue class676Var) {
        this.keybindWindow.collectBloomElements(class676Var);
        super.collectBloomElements(class676Var);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.nameHoverAnimation.animate(class141Var);
        this.keybindWindow.animation(class141Var);
        this.keybindField.syncKeys(this.module.getKeyBind());
        if (this.positionInitialized) {
            this.xAnimation.animate(class141Var);
            this.yAnimation.animate(class141Var);
            setPosition(this.xAnimation.animatedValue(), this.yAnimation.animatedValue());
        }
        this.highlightAnimation.animate(class141Var);
        super.animation(class141Var);
    }

    public void highlight() {
        this.highlightAnimation.trigger();
    }

    @Override
    public float width() {
        return 280.0f;
    }

    @Override
    public float height() {
        return height;
    }

    @Override
    public float contentHeight() {
        if (this.frameElementsContainer.height() <= 0.0f) {
            return 0.0f;
        }
        return padding + this.frameElementsContainer.height();
    }

    public Module module() {
        return this.module;
    }

    public String[] aliases() {
        return this.aliases;
    }

    public String name() {
        return this.name;
    }

    public boolean favorite() {
        return this.favorite;
    }

    public HighlightAnimation highlightAnimation() {
        return this.highlightAnimation;
    }
}
