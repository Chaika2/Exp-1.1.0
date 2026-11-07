package aethereal.ui;
import aethereal.type.BindMode;
import aethereal.module.Module;
import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.ButtonSetting;
import aethereal.ui.setting.ButtonSettingElement;
import aethereal.ui.setting.CheckboxSettingElement;
import aethereal.ui.setting.ColorSetting;
import aethereal.ui.setting.ColorSettingElement;
import aethereal.ui.setting.EnumSetting;
import aethereal.ui.setting.ExpandableSetting;
import aethereal.ui.setting.GroupSettingElement;
import aethereal.ui.setting.KeybindSetting;
import aethereal.ui.setting.KeybindSettingElement;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.setting.ModeSettingElement;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.ui.setting.MultiSelectSettingElement;
import aethereal.ui.setting.NumberSetting;
import aethereal.ui.setting.OrderedEnumSetting;
import aethereal.ui.setting.OrderedListSettingElement;
import aethereal.ui.setting.SeparatorSetting;
import aethereal.ui.setting.Setting;
import aethereal.ui.setting.SliderSettingElement;
import aethereal.ui.setting.SoundModeSettingElement;
import aethereal.ui.setting.TextFieldSetting;
import aethereal.ui.setting.TextFieldSettingElement;
import aethereal.model.Translation;
import aethereal.util.ConfigAutoSaveScheduler;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ModuleSettingsBuilder {
    private ModuleSettingsBuilder() {
    }

    public static void populate(Module class605Var, FrameElementColumn class816Var) {
        class605Var.getSettings().forEach(class661Var -> {
            if (class661Var instanceof BooleanSetting) {
                BooleanSetting class665Var = (BooleanSetting) class661Var;
                Translation name = class665Var.getName();
                Translation description = class665Var.getDescription();
                Objects.requireNonNull(class665Var);
                CheckboxSettingElement class822Var = new CheckboxSettingElement(name, description, class665Var::getKeyBind);
                Objects.requireNonNull(class665Var);
                class822Var.toggledSupplier(class665Var::isValue);
                Objects.requireNonNull(class665Var);
                class822Var.changeRunnable(wrapWithAutoSave(class665Var::switchValue));
                Objects.requireNonNull(class665Var);
                class822Var.onChange(wrapConsumerWithAutoSave(class665Var::setKey));
                Objects.requireNonNull(class665Var);
                Supplier<BindMode> supplier = class665Var::getType;
                Objects.requireNonNull(class665Var);
                class822Var.setupBindType(supplier, class665Var::setType);
                class822Var.visibleSupplier(class665Var.getVisible());
                class816Var.addFrameElement(class822Var);
                return;
            }
            if (class661Var instanceof NumberSetting) {
                NumberSetting class613Var = (NumberSetting) class661Var;
                SliderSettingElement class830Var = new SliderSettingElement(class613Var.getName(), class613Var.getDescription(), class613Var.valueUnit(), class613Var.max(), class613Var.min(), class613Var.step());
                Objects.requireNonNull(class613Var);
                class830Var.currentValueSupplier(class613Var::currentValue);
                Objects.requireNonNull(class613Var);
                class830Var.onValueChanged(wrapConsumerWithAutoSave(class613Var::setCurrentValue));
                class830Var.visibleSupplier(class613Var.getVisible());
                class816Var.addFrameElement(class830Var);
                return;
            }
            if (class661Var instanceof ModeSetting) {
                addModeElement((ModeSetting) class661Var, class816Var);
                return;
            }
            if (class661Var instanceof EnumSetting) {
                addSoundModeElement((EnumSetting) class661Var, class816Var);
                return;
            }
            if (class661Var instanceof MultiSelectSetting) {
                addMultiSelectElement((MultiSelectSetting) class661Var, class816Var);
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
                class816Var.addFrameElement(class835Var);
                return;
            }
            if (class661Var instanceof ButtonSetting) {
                ButtonSetting class666Var = (ButtonSetting) class661Var;
                ButtonSettingElement class821Var = new ButtonSettingElement(class666Var.getName(), class666Var.getDescription(), class666Var.getButtonName(), wrapWithAutoSave(class666Var.getRunnable()));
                class821Var.visibleSupplier(class666Var.getVisible());
                class816Var.addFrameElement(class821Var);
                return;
            }
            if (class661Var instanceof OrderedEnumSetting) {
                addOrderedListElement((OrderedEnumSetting) class661Var, class816Var);
                return;
            }
            if (class661Var instanceof ColorSetting) {
                ColorSetting class667Var = (ColorSetting) class661Var;
                ColorSettingElement class823Var = new ColorSettingElement(class667Var.getName(), class667Var.getDescription(), class667Var::getColorWithoutAlpha, class667Var::getAlpha);
                class823Var.visibleSupplier(class667Var.getVisible());
                Objects.requireNonNull(class667Var);
                class823Var.colorConsumer(class667Var::setColor);
                Objects.requireNonNull(class667Var);
                class823Var.alphaConsumer(class667Var::setAlpha);
                class823Var.commitRunnable(ConfigAutoSaveScheduler::scheduleAutoSave);
                class816Var.addFrameElement(class823Var);
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
                class816Var.addFrameElement(class827Var);
                return;
            }
            if (class661Var instanceof KeybindSetting) {
                KeybindSetting class663Var = (KeybindSetting) class661Var;
                KeybindSettingElement class828Var = new KeybindSettingElement(class663Var.getName(), class663Var.getDescription(), class663Var::getKeyBind);
                class828Var.visibleSupplier(class663Var.getVisible());
                Objects.requireNonNull(class663Var);
                class828Var.onChange(wrapConsumerWithAutoSave(class663Var::setKey));
                class816Var.addFrameElement(class828Var);
                return;
            }
            if (class661Var instanceof SeparatorSetting) {
                class816Var.addFrameElement(new SeparatorElement());
            }
        });
    }

    public static <E extends Enum<E>> void addModeElement(ModeSetting<E> class669Var, FrameElementColumn class816Var) {
        ModeSettingElement class825Var = new ModeSettingElement(class669Var.getName(), class669Var.getDescription(), class669Var.options());
        Objects.requireNonNull(class669Var);
        class825Var.onChangeCallback(wrapConsumerWithAutoSave(class669Var::setCurrentValue));
        Objects.requireNonNull(class669Var);
        class825Var.currentValueSupplier(class669Var::currentValue);
        class825Var.visibleSupplier(class669Var.getVisible());
        class816Var.addFrameElement(class825Var);
    }

    public static <E extends Enum<E>> void addSoundModeElement(EnumSetting<E> class610Var, FrameElementColumn class816Var) {
        SoundModeSettingElement class833Var = new SoundModeSettingElement(class610Var.getName(), class610Var.getDescription(), class610Var.options(), class610Var.soundAction());
        Objects.requireNonNull(class610Var);
        class833Var.onChangeCallback(wrapConsumerWithAutoSave(class610Var::setCurrentValue));
        Objects.requireNonNull(class610Var);
        class833Var.currentValueSupplier(class610Var::currentValue);
        class833Var.visibleSupplier(class610Var.getVisible());
        class816Var.addFrameElement(class833Var);
    }

    public static <E extends Enum<E>> void addMultiSelectElement(MultiSelectSetting<E> class671Var, FrameElementColumn class816Var) {
        MultiSelectSettingElement class836Var = new MultiSelectSettingElement(class671Var.getName(), class671Var.getDescription(), class671Var.options(), class671Var::selectedValues, wrapConsumerWithAutoSave(class671Var::toggle));
        class836Var.visibleSupplier(class671Var.getVisible());
        class816Var.addFrameElement(class836Var);
    }

    public static <E extends Enum<E>> void addOrderedListElement(OrderedEnumSetting<E> class609Var, FrameElementColumn class816Var) {
        OrderedListSettingElement<E> class831Var = new OrderedListSettingElement<>(class609Var::orderedValues, class609Var::getSelected, class609Var::optionName, wrapConsumerWithAutoSave(e -> {
            if (class609Var.isSelected(e)) {
                class609Var.deselect(e);
            } else {
                class609Var.select(e);
            }
        }), (num, num2) -> {
            class609Var.move(num.intValue(), num2.intValue());
            ConfigAutoSaveScheduler.scheduleAutoSave();
        });
        class831Var.visibleSupplier(class609Var.getVisible());
        class816Var.addFrameElement(class831Var);
    }

    private static Runnable wrapWithAutoSave(Runnable runnable) {
        return () -> {
            if (runnable != null) {
                runnable.run();
            }
            ConfigAutoSaveScheduler.scheduleAutoSave();
        };
    }

    private static <T> Consumer<T> wrapConsumerWithAutoSave(Consumer<T> consumer) {
        return obj -> {
            consumer.accept(obj);
            ConfigAutoSaveScheduler.scheduleAutoSave();
        };
    }
}
