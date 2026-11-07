package aethereal.model;
import aethereal.render.ColorToneScale;
import aethereal.render.ColorValue;
import aethereal.type.ConfigOrigin;
import aethereal.Expensive;
import aethereal.render.Theme;
import aethereal.type.ThemeMode;
import aethereal.render.ThemePalette;

import java.time.Instant;
import java.util.Map;

public class ThemeData {
    public static ThemePalette createDarkPalette() {
        return new ThemePalette(ColorValue.fromHex("6E74E3"), ColorValue.fromHex("8186EA"), ColorValue.fromHex("FDC95A"), ColorValue.fromHex("151617"), new ColorToneScale(Map.ofEntries(Map.entry(900, ColorValue.fromHex("313133")), Map.entry(800, ColorValue.fromHex("505155")), Map.entry(700, ColorValue.fromHex("606166")), Map.entry(600, ColorValue.fromHex("76777E")), Map.entry(500, ColorValue.fromHex("868791")), Map.entry(400, ColorValue.fromHex("B4B5BA")), Map.entry(300, ColorValue.fromHex("C5C6C8")), Map.entry(200, ColorValue.fromHex("DADCE2")), Map.entry(100, ColorValue.fromHex("E3E4E7")), Map.entry(50, ColorValue.fromHex("F0F1F4")))), new ColorToneScale(Map.ofEntries(Map.entry(900, ColorValue.fromHex("ED4561")), Map.entry(500, ColorValue.fromHex("EE5871")), Map.entry(300, ColorValue.fromHex("EF6179")))), new ColorToneScale(Map.ofEntries(Map.entry(700, ColorValue.fromHex("17181A")), Map.entry(600, ColorValue.fromHex("1A1B1E")), Map.entry(500, ColorValue.fromHex("202123")), Map.entry(400, ColorValue.fromHex("222325")), Map.entry(300, ColorValue.fromHex("282A2E")), Map.entry(50, ColorValue.fromHex("6E7279")))), new ColorToneScale(Map.ofEntries(Map.entry(900, ColorValue.fromHex("0F1011")), Map.entry(801, ColorValue.fromHex("121315")), Map.entry(800, ColorValue.fromHex("17181A")), Map.entry(700, ColorValue.fromHex("18191A")), Map.entry(600, ColorValue.fromHex("1A1B1D")), Map.entry(500, ColorValue.fromHex("1E1F22")), Map.entry(400, ColorValue.fromHex("26272A")), Map.entry(300, ColorValue.fromHex("2D2E31")), Map.entry(200, ColorValue.fromHex("565659")))));
    }

    public static Theme defaultDark() {
        ThemePalette class764VarMethod001 = createDarkPalette();
        Instant instantNow = Instant.now();
        return Theme.of("expensive-dark", "Expensive Dark", "Expensive", ConfigOrigin.OFFICIAL, ThemeMode.DARK, class764VarMethod001, instantNow, instantNow);
    }
}
