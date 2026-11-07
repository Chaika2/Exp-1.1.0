package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.event.ClientTickEvent;
import aethereal.model.Translation;
import aethereal.render.PanelStyle;
import aethereal.render.ThemeManager;
import aethereal.type.HueMode;
import aethereal.type.ModuleCategory;
import aethereal.type.SettingUnit;
import aethereal.type.ThemeSelection;
import aethereal.type.UiColorMode;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.ColorSetting;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.setting.NumberSetting;

@Aliases(aliases = {"Theme", "Appearance", "Colors", "Темы", "Тема", "Цвета"})
public class ThemesModule extends Module {
    public static ThemesModule INSTANCE;

    public final ModeSetting<ThemeSelection> themeMode;
    public final ModeSetting<UiColorMode> uiColorMode;
    public final ColorSetting customColor;
    public final ColorSetting moduleColorFirst;
    public final ColorSetting moduleColorSecond;
    public final BooleanSetting colorGradientEnabled;
    public final ModeSetting<HueMode> hueMode;
    public final ModeSetting<PanelStyle> panelStyle;
    public final BooleanSetting hudBlurEnabled;
    public final NumberSetting hudBlurStrength;
    public final BooleanSetting hudGlowEnabled;
    public final NumberSetting hudGlowStrength;
    public final NumberSetting hudGlowOpacity;
    public final BooleanSetting vanillaBackgroundBlur;

    public ThemesModule() {
        super(ModuleTab.RENDER, ModuleCategory.INTERFACE, "Themes");
        INSTANCE = this;
        this.themeMode = new ModeSetting(Translation.clearText("Тема"), Translation.clearText("Тема интерфейса клиента")).values(ThemeSelection.class).select(ThemeSelection.DARK);
        this.uiColorMode = new ModeSetting(Translation.clearText("Цвет интерфейса"), Translation.clearText("Акцентный цвет интерфейса")).values(UiColorMode.class).select(UiColorMode.DEFAULT);
        this.customColor = new ColorSetting(Translation.clearText("Цвет"), Translation.clearText("Свой акцентный цвет")).visible(() -> this.uiColorMode.currentValue() == UiColorMode.CUSTOM).value(0xFF6E74E3);
        this.moduleColorFirst = new ColorSetting(Translation.clearText("Первый цвет для модулей"), Translation.clearText("Основной цвет модулей")).visible(() -> this.themeMode.currentValue() == ThemeSelection.CUSTOM).value(0xFF6E74E3);
        this.moduleColorSecond = new ColorSetting(Translation.clearText("Второй цвет для модулей"), Translation.clearText("Дополнительный цвет модулей")).visible(() -> this.themeMode.currentValue() == ThemeSelection.CUSTOM).value(0xFF8186EA);
        this.colorGradientEnabled = new BooleanSetting(Translation.clearText("Градиент цветов"), Translation.clearText("Плавный переход между цветами")).setValue(false);
        this.hueMode = new ModeSetting(Translation.clearText("Оттенок"), Translation.clearText("Какой оттенок использовать")).values(HueMode.class).select(HueMode.PRIMARY).visible(() -> !this.colorGradientEnabled.isValue());
        this.panelStyle = new ModeSetting(Translation.clearText("Стиль панелей"), Translation.clearText("Внешний вид карточек модулей")).values(PanelStyle.class).select(PanelStyle.DEFAULT);
        this.hudBlurEnabled = new BooleanSetting(Translation.clearText("Блюр HUD-элементов"), Translation.clearText("Стеклянный блюр за HUD-элементами")).setValue(true);
        this.hudBlurStrength = new NumberSetting(Translation.clearText("Сила блюра HUD")).range(0.0f, 100.0f).step(5.0f).currentValue(100.0f).unit(SettingUnit.PERCENTS).visible(() -> this.hudBlurEnabled.isValue());
        this.hudGlowEnabled = new BooleanSetting(Translation.clearText("Свечение HUD-элементов"), Translation.clearText("Свечение вокруг HUD-элементов")).setValue(false);
        this.hudGlowStrength = new NumberSetting(Translation.clearText("Сила свечения HUD")).range(0.0f, 100.0f).step(5.0f).currentValue(30.0f).unit(SettingUnit.PERCENTS).visible(() -> this.hudGlowEnabled.isValue());
        this.hudGlowOpacity = new NumberSetting(Translation.clearText("Прозрачность свечения HUD")).range(0.0f, 100.0f).step(5.0f).currentValue(55.0f).unit(SettingUnit.PERCENTS).visible(() -> this.hudGlowEnabled.isValue());
        this.vanillaBackgroundBlur = new BooleanSetting(Translation.clearText("Ванильный блюр фона"), Translation.clearText("Блюр мира за меню")).setValue(true);
        addSettings(this.themeMode, this.uiColorMode, this.customColor, this.moduleColorFirst, this.moduleColorSecond, this.colorGradientEnabled, this.hueMode, this.panelStyle, this.hudBlurEnabled, this.hudBlurStrength, this.hudGlowEnabled, this.hudGlowStrength, this.hudGlowOpacity, this.vanillaBackgroundBlur);
        register(ClientTickEvent.class, event -> ThemeManager.INSTANCE.sync());
    }

    public static ThemesModule instance() {
        return INSTANCE;
    }

    public static boolean hudBlurActive() {
        return INSTANCE != null && INSTANCE.state && INSTANCE.hudBlurEnabled.isValue();
    }

    public static boolean hudGlowActive() {
        return INSTANCE != null && INSTANCE.state && INSTANCE.hudGlowEnabled.isValue();
    }

    public static int hudBlurPasses() {
        if (INSTANCE == null) {
            return 0;
        }
        return Math.max(1, Math.round(INSTANCE.hudBlurStrength.currentValue() / 100.0f * 40.0f));
    }

    public static boolean isGlassPanels() {
        return INSTANCE != null && INSTANCE.panelStyle.currentValue() == PanelStyle.GLASS;
    }
}
