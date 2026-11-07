package aethereal.render;

import aethereal.resource.ClasspathResource;
import java.util.HashMap;
import java.util.Map;

public final class DestraAssets {
    private static final String IMAGES = "assets/destra/images/";
    private static final String MENU = IMAGES + "menu/";

    private static final Map<String, GlTexture> CACHE = new HashMap<>();

    private DestraAssets() {
    }

    public static GlTexture menuLogo() {
        return texture(MENU + "main-logo.png");
    }

    public static GlTexture menuIcon(int index) {
        return texture(MENU + "menu" + index + ".png");
    }

    public static GlTexture stopmenu() {
        return texture(MENU + "stopmenu.png");
    }

    public static GlTexture texture(String path) {
        return CACHE.computeIfAbsent(path, key -> new GlTexture(new ClasspathResource(key)));
    }
}
