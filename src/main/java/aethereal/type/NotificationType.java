package aethereal.type;
import aethereal.resource.ClasspathResource;
import aethereal.render.GlTexture;

public enum NotificationType {
    INFO("/icons/menu/new/info.png"),
    WARNING("/icons/menu/new/warning.png"),
    ERROR("/icons/menu/new/debug.png"),
    EVENT("/icons/menu/new/falling_star.png"),
    SUCCESS("/icons/menu/new/success.png"),
    MODULE_ENABLED("/icons/menu/new/frame.png"),
    MODULE_DISABLED("/icons/menu/new/frame.png");

    public final String iconPath;
    public final GlTexture texture;

    NotificationType(String str) {
        this.iconPath = str;
        this.texture = new GlTexture(new ClasspathResource(str));
    }

    public String getIconPath() {
        return this.iconPath;
    }

    public GlTexture getTexture() {
        return this.texture;
    }
}
