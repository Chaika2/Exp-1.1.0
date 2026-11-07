package aethereal.render;

import com.google.common.base.Suppliers;
import java.util.function.Supplier;

public class DestraFonts {
    private static final String ROOT = "assets/destra/fonts/";

    private static Supplier<MsdfFont> load(String name) {
        return Suppliers.memoize(() -> MsdfFont.builder()
                .atlasPath(ROOT + name)
                .dataPath(ROOT + name)
                .build());
    }

    public static final Supplier<MsdfFont> SUISSE_SB = load("suissesb");
    public static final Supplier<MsdfFont> SUISSE_BOLD = load("suissebold");
    public static final Supplier<MsdfFont> SUISSE_MEDIUM = load("suissemedium");
    public static final Supplier<MsdfFont> SUISSE_BOOK = load("suissebook");
    public static final Supplier<MsdfFont> DESTRA_ICONS = load("destra");
    public static final Supplier<MsdfFont> ICO2 = load("ico2");
    public static final Supplier<MsdfFont> ICONS = load("icons");
    public static final Supplier<MsdfFont> MAIN = load("main");
    public static final Supplier<MsdfFont> CLICKGUI = load("clickgui");
    public static final Supplier<MsdfFont> MNTSB = load("mntsb");
    public static final Supplier<MsdfFont> ACCOUNTS = load("accounts");
    public static final Supplier<MsdfFont> PARTY = load("party");
    public static final Supplier<MsdfFont> MEDIA = load("media");
    public static final Supplier<MsdfFont> VOICE = load("voice");
}
