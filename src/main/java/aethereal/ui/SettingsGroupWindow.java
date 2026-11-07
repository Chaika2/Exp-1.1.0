package aethereal.ui;
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
import aethereal.model.CursorMoveInput;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.ui.setting.EnumSetting;
import aethereal.ui.setting.ExpandableSetting;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.ui.setting.GroupSettingElement;
import aethereal.event.InputEvent;
import aethereal.model.InputEventContext;
import aethereal.model.KeyInput;
import aethereal.ui.setting.KeybindSetting;
import aethereal.ui.setting.KeybindSettingElement;
import aethereal.model.LayoutScaleContext;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.setting.ModeSettingElement;
import aethereal.model.MouseButtonInput;
import aethereal.render.MsdfFont;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.ui.setting.MultiSelectSettingElement;
import aethereal.ui.setting.NumberSetting;
import aethereal.ui.setting.OrderedEnumSetting;
import aethereal.ui.setting.OrderedListSettingElement;
import aethereal.util.RenderCommandQueue;
import aethereal.model.ScrollInput;
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

import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SettingsGroupWindow extends OverlayWidget {
    public final MsdfFont font;
    public final GlTexture propertyIcon;
    public final GlTexture groupIcon;
    public final IconLabelBadge settingsBadge;
    public final ToggleAnimator openAnimator;
    public final Translation title;

    public final FrameElementColumn settingsColumn;

    public final ScrollArea scrollArea;

    public final ScrollbarWidget scrollbar;
    public final WidgetBounds titleBounds;
    public final float headerHeight;
    public final ToggleAnimator hoverAnimator;

    public SettingsGroupWindow(Translation class254Var, List<Setting> list) {
        this(class254Var, list, true);
    }

    public SettingsGroupWindow(Translation class254Var, List<Setting> list, boolean z) {
        this.font = Fonts.INTER_SEMIBOLD.get();
        this.propertyIcon = new GlTexture(new ClasspathResource("/icons/menu/new/property.png"));
        this.groupIcon = new GlTexture(new ClasspathResource("/icons/menu/new/group.png"));
        this.settingsBadge = new IconLabelBadge(this.propertyIcon, Translation.clearText("SETTINGS GROUP"));
        this.openAnimator = new ToggleAnimator(Easings.EASE_IN_OUT_CUBIC);
        this.scrollArea = new ScrollArea();
        this.titleBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
        this.hoverAnimator = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);
        setSize(290.0f, 48.0f);
        ScrollArea class789Var = this.scrollArea;
        Objects.requireNonNull(class789Var);
        Supplier supplier = class789Var::scrollY;
        Supplier supplier2 = this::contentHeight;
        ScrollArea class789Var2 = this.scrollArea;
        Objects.requireNonNull(class789Var2);
        Supplier supplier3 = class789Var2::height;
        ScrollArea class789Var3 = this.scrollArea;
        Objects.requireNonNull(class789Var3);
        this.scrollbar = new ScrollbarWidget(supplier, supplier2, supplier3, (v1) -> {
            class789Var.scrollTo(v1);
        }, 8.0f, 4.0f, 30.0f);
        this.settingsColumn = new FrameElementColumn(computeColumnWidth(), 14.0f);
        this.settingsColumn.clearElements();
        this.headerHeight = height();
        this.title = class254Var;
        list.forEach(class661Var -> {
            if (class661Var instanceof BooleanSetting) {
                BooleanSetting class665Var = (BooleanSetting) class661Var;
                Translation name = class665Var.getName();
                Translation description = class665Var.getDescription();
                Objects.requireNonNull(class665Var);
                CheckboxSettingElement class822Var = new CheckboxSettingElement(name, description, class665Var::getKeyBind, z);
                Objects.requireNonNull(class665Var);
                class822Var.toggledSupplier(class665Var::isValue);
                Objects.requireNonNull(class665Var);
                class822Var.changeRunnable(wrapWithAutoSave(class665Var::switchValue));
                Objects.requireNonNull(class665Var);
                class822Var.onChange(wrapConsumerWithAutoSave(class665Var::setKey));
                Objects.requireNonNull(class665Var);
                Supplier<BindMode> supplier4 = class665Var::getType;
                Objects.requireNonNull(class665Var);
                class822Var.setupBindType(supplier4, class665Var::setType);
                class822Var.visibleSupplier(class665Var.getVisible());
                this.settingsColumn.addFrameElement(class822Var);
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
                this.settingsColumn.addFrameElement(class830Var);
                return;
            }
            if (class661Var instanceof ModeSetting) {
                addModeSetting((ModeSetting) class661Var);
                return;
            }
            if (class661Var instanceof EnumSetting) {
                addEnumSetting((EnumSetting) class661Var);
                return;
            }
            if (class661Var instanceof MultiSelectSetting) {
                addMultiSelectSetting((MultiSelectSetting) class661Var);
                return;
            }
            if (class661Var instanceof TextFieldSetting) {
                TextFieldSetting class612Var = (TextFieldSetting) class661Var;
                Translation name2 = class612Var.getName();
                Translation description2 = class612Var.getDescription();
                Translation placeholder = class612Var.getPlaceholder();
                boolean zIsOnlyDigits = class612Var.isOnlyDigits();
                int max = class612Var.getMax();
                boolean zIsPassword = class612Var.isPassword();
                Objects.requireNonNull(class612Var);
                TextFieldSettingElement class835Var = new TextFieldSettingElement(name2, description2, placeholder, zIsOnlyDigits, max, zIsPassword, class612Var::getText);
                Objects.requireNonNull(class612Var);
                class835Var.onChange(wrapConsumerWithAutoSave(class612Var::setText));
                class835Var.visibleSupplier(class612Var.getVisible());
                this.settingsColumn.addFrameElement(class835Var);
                return;
            }
            if (class661Var instanceof ButtonSetting) {
                ButtonSetting class666Var = (ButtonSetting) class661Var;
                ButtonSettingElement class821Var = new ButtonSettingElement(class666Var.getName(), class666Var.getDescription(), class666Var.getButtonName(), wrapWithAutoSave(class666Var.getRunnable()));
                class821Var.visibleSupplier(class666Var.getVisible());
                this.settingsColumn.addFrameElement(class821Var);
                return;
            }
            if (class661Var instanceof ColorSetting) {
                ColorSetting class667Var = (ColorSetting) class661Var;
                Translation name3 = class667Var.getName();
                Translation description3 = class667Var.getDescription();
                Objects.requireNonNull(class667Var);
                Supplier supplier5 = class667Var::getColorWithoutAlpha;
                Objects.requireNonNull(class667Var);
                ColorSettingElement class823Var = new ColorSettingElement(name3, description3, supplier5, class667Var::getAlpha);
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
                this.settingsColumn.addFrameElement(class823Var);
                return;
            }
            if (class661Var instanceof OrderedEnumSetting) {
                addOrderedEnumSetting((OrderedEnumSetting) class661Var);
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
                this.settingsColumn.addFrameElement(class827Var);
                return;
            }
            if (!(class661Var instanceof KeybindSetting)) {
                if (class661Var instanceof SeparatorSetting) {
                    this.settingsColumn.addFrameElement(new SeparatorElement());
                    return;
                }
                return;
            }
            KeybindSetting class663Var = (KeybindSetting) class661Var;
            Translation name4 = class663Var.getName();
            Translation description4 = class663Var.getDescription();
            Objects.requireNonNull(class663Var);
            KeybindSettingElement class828Var = new KeybindSettingElement(name4, description4, class663Var::getKeyBind);
            class828Var.visibleSupplier(class663Var.getVisible());
            Objects.requireNonNull(class663Var);
            class828Var.onChange(wrapConsumerWithAutoSave(class663Var::setKey));
            this.settingsColumn.addFrameElement(class828Var);
        });
        addChild(this.settingsColumn);
    }

    public <E extends Enum<E>> void addModeSetting(ModeSetting<E> class669Var) {
        ModeSettingElement class825Var = new ModeSettingElement(class669Var.getName(), class669Var.getDescription(), class669Var.options());
        Objects.requireNonNull(class669Var);
        class825Var.onChangeCallback(wrapConsumerWithAutoSave(class669Var::setCurrentValue));
        Objects.requireNonNull(class669Var);
        class825Var.currentValueSupplier(class669Var::currentValue);
        class825Var.visibleSupplier(class669Var.getVisible());
        this.settingsColumn.addFrameElement(class825Var);
    }

    public <E extends Enum<E>> void addMultiSelectSetting(MultiSelectSetting<E> class671Var) {
        Translation name = class671Var.getName();
        Translation description = class671Var.getDescription();
        Enum[] enumArrOptions = class671Var.options();
        Objects.requireNonNull(class671Var);
        Supplier supplier = class671Var::selectedValues;
        Objects.requireNonNull(class671Var);
        MultiSelectSettingElement class836Var = new MultiSelectSettingElement(name, description, enumArrOptions, supplier, wrapConsumerWithAutoSave(class671Var::toggle));
        class836Var.visibleSupplier(class671Var.getVisible());
        this.settingsColumn.addFrameElement(class836Var);
    }

    public <E extends Enum<E>> void addOrderedEnumSetting(OrderedEnumSetting<E> class609Var) {
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
        this.settingsColumn.addFrameElement(class831Var);
    }

    public <E extends Enum<E>> void addEnumSetting(EnumSetting<E> class610Var) {
        SoundModeSettingElement class833Var = new SoundModeSettingElement(class610Var.getName(), class610Var.getDescription(), class610Var.options(), class610Var.soundAction());
        Objects.requireNonNull(class610Var);
        class833Var.onChangeCallback(wrapConsumerWithAutoSave(class610Var::setCurrentValue));
        Objects.requireNonNull(class610Var);
        class833Var.currentValueSupplier(class610Var::currentValue);
        class833Var.visibleSupplier(class610Var.getVisible());
        this.settingsColumn.addFrameElement(class833Var);
    }

    @Override
    public void render(DrawCtx class699Var) {
        if (this.openAnimator.isZero()) {
            return;
        }
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        class115VarColorStack.push();
        class115VarColorStack.alpha(this.openAnimator.smoothAnimation());
        float f = totalHeight();
        class699Var.bloom(x(), y(), width(), f, 20.0f, Expensive.INSTANCE.windowController().bloom().getBloomFramebuffer().getColorAttachment());
        class699Var.fillOutlinedRoundedRect(x(), y(), width(), f, 12.0f, 2.5f, class115VarColorStack.computeColor(ThemePalette.white.argb(), 0.03f), class115VarColorStack.computeColor(class764VarPalette.frameBackground().argb()));
        class699Var.texture(this.groupIcon, x() + 14.0f, (y() + (this.headerHeight / 2.0f)) - (this.groupIcon.height() / 2.0f), this.groupIcon.width(), this.groupIcon.height(), class115VarColorStack.computeColor(class764VarPalette.text().tone(400).argb()));
        float fX = x() + 14.0f + this.groupIcon.width() + 8.0f;
        float fY = (y() + (this.headerHeight / 2.0f)) - (this.font.getHeight(15.0f) / 2.0f);
        float fTextWidthPhysical = class699Var.textWidthPhysical(this.font, this.title.effective(), 15);
        this.titleBounds.withPosition(fX, fY).withSize(fTextWidthPhysical, this.font.getHeight(15.0f));
        float fX2 = this.settingsBadge.x() - 30.0f;
        boolean z = fX + fTextWidthPhysical > fX2;
        String strEffective = this.title.effective();
        float fSmoothAnimation = z ? 1.0f - this.hoverAnimator.smoothAnimation() : 1.0f;
        if (fSmoothAnimation > 0.1f && z) {
            int iMax = Math.max(0, this.title.effective().length() - ((int) Math.ceil(((fX + fTextWidthPhysical) - fX2) / (fTextWidthPhysical / this.title.effective().length()))));
            String strSubstring = this.title.effective().substring(0, iMax);
            if (iMax < this.title.effective().length()) {
                strSubstring = strSubstring + "...";
            }
            strEffective = strSubstring;
        }
        class699Var.text(this.font, strEffective, 15, fX, fY, class115VarColorStack.computeColor(class764VarPalette.text().tone(100).argb()));
        class115VarColorStack.push();
        class115VarColorStack.alpha(fSmoothAnimation);
        this.settingsBadge.render(class699Var);
        class115VarColorStack.pop();
        this.scrollArea.beginArea(class699Var, x(), (y() + this.headerHeight) - 1.0f, width(), (totalHeight() - this.headerHeight) + 2.0f);
        this.settingsColumn.render(class699Var);
        this.scrollArea.endArea(class699Var, contentHeight());
        class699Var.matrixStack().push();
        class699Var.matrixStack().translate(0.0f, -class699Var.layoutContext().toPhysical(this.scrollArea.scrollY()), 0.0f);
        this.settingsColumn.renderOverlays(class699Var);
        class699Var.matrixStack().pop();
        if (needsScrollbar()) {
            this.scrollbar.render(class699Var);
        }
        class115VarColorStack.pop();
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        this.settingsColumn.setPosition(x() + 14.0f, this.titleBounds.y() + this.titleBounds.height() + 14.0f);
        this.settingsColumn.setWidth(computeColumnWidth());
        this.settingsBadge.layout(class698Var);
        this.settingsBadge.setPosition(((x() + width()) - 14.0f) - this.settingsBadge.width(), (y() + (this.headerHeight / 2.0f)) - (this.settingsBadge.height() / 2.0f));
        this.groupIcon.setDimensions(14, 14);
        this.propertyIcon.setDimensions(10, 10);
        setSize(width(), this.headerHeight);
        this.scrollArea.layout(class698Var);
        this.scrollbar.layout(class698Var);
        this.scrollbar.setSize(4.0f, height() + contentHeight());
        this.scrollbar.setPosition(((x() + width()) - 10.0f) - this.scrollbar.width(), this.titleBounds.y() + this.titleBounds.height() + 6.0f);
        super.layout(class698Var);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        if (this.openAnimator.isZero()) {
            return false;
        }
        float fScrollY = this.scrollArea.scrollY();
        boolean zInArea = class688Var.inArea(this.titleBounds.x(), this.titleBounds.y(), this.titleBounds.width(), this.titleBounds.height());
        if (this.titleBounds.x() + this.titleBounds.width() < (x() + width()) - 14.0f) {
            this.hoverAnimator.state(zInArea && !z);
        }
        if (super.handleInput(class688Var.withMouseOffset(0.0f, fScrollY), z)) {
            return true;
        }
        if ((class688Var.inputEvent() instanceof CursorMoveInput) && !class688Var.inArea(x(), y(), width(), totalHeight())) {
            return true;
        }
        if ((class688Var.inputEvent() instanceof ScrollInput) && !class688Var.inArea(x(), y(), width(), totalHeight())) {
            closeWindow();
            return false;
        }
        InputEvent class691VarInputEvent = class688Var.inputEvent();
        if (class691VarInputEvent instanceof KeyInput) {
            KeyInput class696Var = (KeyInput) class691VarInputEvent;
            if (class696Var.keyAction().press() && class696Var.keyCode() == 256) {
                closeWindow();
                return true;
            }
        }
        InputEvent class691VarInputEvent2 = class688Var.inputEvent();
        if ((class691VarInputEvent2 instanceof MouseButtonInput) && ((MouseButtonInput) class691VarInputEvent2).action().press()) {
            if (class688Var.inArea(x(), y(), width(), totalHeight())) {
                return true;
            }
            closeWindow();
            return true;
        }
        if (needsScrollbar() && this.scrollbar.handleInput(class688Var, z)) {
            return true;
        }
        return this.scrollArea.handleInput(class688Var, z);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.openAnimator.animate(class141Var);
        this.scrollbar.animation(class141Var);
        this.hoverAnimator.animate(class141Var);
        this.scrollArea.animation(class141Var);
        this.settingsColumn.handleViewportVisibility(this.scrollArea.visibleTop(), this.scrollArea.visibleBottom(), y());
        super.animation(class141Var);
    }

    @Override
    public void collectBloomElements(RenderCommandQueue class676Var) {
        class676Var.record(class699Var -> {
            ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
            ThemePalette class764VarPalette = class699Var.theme().palette();
            class115VarColorStack.push();
            class115VarColorStack.alpha(this.openAnimator.smoothAnimation());
            class699Var.fillRoundedRect(x(), y(), width(), totalHeight(), 12.0f, class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb()));
            class115VarColorStack.pop();
        });
        super.collectBloomElements(class676Var);
    }

    public void openWindow() {
        if (isOpen()) {
            return;
        }
        this.openAnimator.state(true);
    }

    public void closeWindow() {
        if (isOpen()) {
            this.openAnimator.state(false);
            super.handleClose();
        }
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

    public boolean needsScrollbar() {
        return contentHeight() > (totalHeight() - this.headerHeight) + 0.5f;
    }

    @Override
    public boolean isOpen() {
        return this.openAnimator.state() || !this.openAnimator.isZero();
    }

    public float totalHeight() {
        return this.headerHeight + Math.min(contentHeight(), 400.0f);
    }

    public float computeColumnWidth() {
        return width() - (needsScrollbar() ? 38.0f : 28.0f);
    }

    public float contentHeight() {
        if (this.settingsColumn != null && this.settingsColumn.height() > 0.0f) {
            return 14.0f + this.settingsColumn.height();
        }
        return 0.0f;
    }
}
