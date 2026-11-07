package aethereal.render;
import aethereal.Expensive;
import aethereal.model.ThemeData;
import aethereal.module.Module;
import aethereal.module.ThemesModule;
import aethereal.type.ConfigOrigin;
import aethereal.type.HueMode;
import aethereal.type.ThemeMode;
import aethereal.type.ThemeSelection;
import aethereal.type.UiColorMode;
import aethereal.util.ColorUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ThemeManager {
    public static final ThemeManager INSTANCE = new ThemeManager();

    public final List<Theme> themes = new ArrayList<>();
    public Theme activeTheme;
    public String appliedSignature = "";

    private ThemeManager() {
        rebuildBuiltIns();
        this.activeTheme = findById("expensive-dark");
        if (Expensive.INSTANCE != null) {
            Expensive.INSTANCE.theme = this.activeTheme;
        }
    }

    public void rebuildBuiltIns() {
        this.themes.clear();
        Theme dark = ThemeData.defaultDark();
        this.themes.add(dark);
        this.themes.add(create("expensive-light", "Expensive Light", ThemeMode.LIGHT, lightPalette()));
        this.themes.add(createAccent(dark, "expensive-azure", "Azure", "4C8DFF", "6FA6FF"));
        this.themes.add(createAccent(dark, "expensive-rose", "Rose", "F0508A", "FF74A6"));
        this.themes.add(createAccent(dark, "expensive-emerald", "Emerald", "3FBF6F", "5FD98C"));
        this.themes.add(createAccent(dark, "expensive-amber", "Amber", "F0A93F", "FFBE5F"));
        this.themes.add(createAccent(dark, "expensive-violet", "Violet", "8A6CF0", "A98CFF"));
    }

    private Theme create(String id, String name, ThemeMode mode, ThemePalette palette) {
        Instant now = Instant.now();
        return Theme.of(id, name, "Expensive", ConfigOrigin.OFFICIAL, mode, palette, now, now);
    }

    private Theme createAccent(Theme base, String id, String name, String accent, String accentBright) {
        return create(id, name, base.type(), paletteWithAccent(base.palette(), ColorValue.fromHex(accent), ColorValue.fromHex(accentBright)));
    }

    private static ThemePalette paletteWithAccent(ThemePalette base, ColorValue accent, ColorValue accentBright) {
        return new ThemePalette(accent, accentBright, base.favorite(), base.frameBackground(), base.text(), base.error(), base.surfaceOutline(), base.surfaceBackground());
    }

    public List<Theme> themes() {
        return this.themes;
    }

    public Theme findById(String id) {
        for (Theme theme : this.themes) {
            if (theme.id().equals(id)) {
                return theme;
            }
        }
        return this.themes.get(0);
    }

    public static String idFor(ThemeSelection selection) {
        switch (selection) {
            case LIGHT: return "expensive-light";
            case AZURE: return "expensive-azure";
            case ROSE: return "expensive-rose";
            case EMERALD: return "expensive-emerald";
            case AMBER: return "expensive-amber";
            case VIOLET: return "expensive-violet";
            default: return "expensive-dark";
        }
    }

    public Theme active() {
        if (this.activeTheme == null) {
            this.activeTheme = this.themes.get(0);
        }
        return this.activeTheme;
    }

    public void setTheme(Theme theme) {
        if (theme == null) {
            return;
        }
        this.activeTheme = theme;
        if (Expensive.INSTANCE != null) {
            Expensive.INSTANCE.theme = theme;
        }
    }

    public ThemesModule module() {
        if (Expensive.INSTANCE == null || Expensive.INSTANCE.moduleRepository() == null) {
            return null;
        }
        try {
            Module module = Expensive.INSTANCE.moduleRepository().get(ThemesModule.class);
            return module instanceof ThemesModule ? (ThemesModule) module : null;
        } catch (Throwable throwable) {
            return null;
        }
    }

    public void sync() {
        ThemesModule module = module();
        if (module == null) {
            return;
        }
        ThemeSelection selection = module.themeMode.currentValue();
        Theme base = findById(idFor(selection));
        ColorValue accent;
        ColorValue accentBright;
        if (selection == ThemeSelection.CUSTOM) {
            accent = new ColorValue(module.moduleColorFirst.getColor());
            accentBright = new ColorValue(module.moduleColorSecond.getColor());
        } else {
            accent = base.palette().accent();
            accentBright = base.palette().accentBright();
        }
        UiColorMode colorMode = module.uiColorMode.currentValue();
        if (colorMode == UiColorMode.CUSTOM) {
            accent = new ColorValue(module.customColor.getColor());
            accentBright = brighten(accent, 1.25f);
        } else if (colorMode != UiColorMode.DEFAULT) {
            accent = namedColor(colorMode);
            accentBright = brighten(accent, 1.25f);
        }
        String signature = base.id() + ":" + accent.argb() + ":" + accentBright.argb();
        if (signature.equals(this.appliedSignature)) {
            return;
        }
        this.appliedSignature = signature;
        setTheme(Theme.of(base.id(), base.name(), base.owner(), base.kind(), base.type(), paletteWithAccent(base.palette(), accent, accentBright), base.createdAt(), Instant.now()));
    }

    public int globalColor(float phase) {
        ThemesModule module = module();
        ThemePalette palette = active().palette();
        if (module != null && module.colorGradientEnabled.isValue()) {
            return ColorUtil.interpolateColor(palette.accent().argb(), palette.accentBright().argb(), pingPong(phase));
        }
        if (module != null && module.hueMode.currentValue() == HueMode.SECONDARY) {
            return palette.accentBright().argb();
        }
        return palette.accent().argb();
    }

    private static float pingPong(float phase) {
        float value = (phase * 0.03f) % 2.0f;
        if (value < 0.0f) {
            value += 2.0f;
        }
        return value > 1.0f ? 2.0f - value : value;
    }

    private static ColorValue brighten(ColorValue color, float factor) {
        int argb = color.argb();
        int alpha = ColorUtil.alpha(argb);
        int red = Math.min(255, Math.round(ColorUtil.red(argb) * factor));
        int green = Math.min(255, Math.round(ColorUtil.green(argb) * factor));
        int blue = Math.min(255, Math.round(ColorUtil.blue(argb) * factor));
        return ColorValue.fromRGBA(red, green, blue, alpha);
    }

    private static ColorValue namedColor(UiColorMode mode) {
        switch (mode) {
            case WHITE: return ColorValue.fromHex("FFFFFF");
            case GOLD: return ColorValue.fromHex("FDC95A");
            case RED: return ColorValue.fromHex("FF4C4F");
            case GREEN: return ColorValue.fromHex("4CA765");
            case BLUE: return ColorValue.fromHex("5E84FF");
            case PURPLE: return ColorValue.fromHex("7267FF");
            case PINK: return ColorValue.fromHex("F0508A");
            case CYAN: return ColorValue.fromHex("4CD0E0");
            default: return ColorValue.fromHex("6E74E3");
        }
    }

    private static ThemePalette lightPalette() {
        ColorToneScale text = new ColorToneScale(Map.ofEntries(
                Map.entry(50, ColorValue.fromHex("E3E4E7")),
                Map.entry(100, ColorValue.fromHex("C5C6C8")),
                Map.entry(200, ColorValue.fromHex("9A9BA3")),
                Map.entry(300, ColorValue.fromHex("76777E")),
                Map.entry(400, ColorValue.fromHex("606166")),
                Map.entry(500, ColorValue.fromHex("4A4B50")),
                Map.entry(600, ColorValue.fromHex("33343A")),
                Map.entry(700, ColorValue.fromHex("2A2B2E")),
                Map.entry(800, ColorValue.fromHex("1F2023")),
                Map.entry(900, ColorValue.fromHex("151617"))));
        ColorToneScale error = new ColorToneScale(Map.ofEntries(
                Map.entry(300, ColorValue.fromHex("F2A0AD")),
                Map.entry(500, ColorValue.fromHex("E24D66")),
                Map.entry(900, ColorValue.fromHex("C2364B"))));
        ColorToneScale surfaceOutline = new ColorToneScale(Map.ofEntries(
                Map.entry(50, ColorValue.fromHex("8A8B90")),
                Map.entry(300, ColorValue.fromHex("DADCE2")),
                Map.entry(400, ColorValue.fromHex("C5C6C8")),
                Map.entry(500, ColorValue.fromHex("B4B5BA")),
                Map.entry(600, ColorValue.fromHex("A0A1A8")),
                Map.entry(700, ColorValue.fromHex("8A8B90"))));
        ColorToneScale surfaceBackground = new ColorToneScale(Map.ofEntries(
                Map.entry(200, ColorValue.fromHex("9A9BA3")),
                Map.entry(300, ColorValue.fromHex("C5C6C8")),
                Map.entry(400, ColorValue.fromHex("DADCE2")),
                Map.entry(500, ColorValue.fromHex("E3E4E7")),
                Map.entry(600, ColorValue.fromHex("EAEBEE")),
                Map.entry(700, ColorValue.fromHex("EFF0F3")),
                Map.entry(800, ColorValue.fromHex("F4F5F7")),
                Map.entry(801, ColorValue.fromHex("F7F8FA")),
                Map.entry(900, ColorValue.fromHex("FBFBFC"))));
        return new ThemePalette(ColorValue.fromHex("6E74E3"), ColorValue.fromHex("8186EA"), ColorValue.fromHex("E9B444"), ColorValue.fromHex("F4F5F7"), text, error, surfaceOutline, surfaceBackground);
    }
}
