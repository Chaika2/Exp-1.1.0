package aethereal.type;

public enum RenderOverlayType {
    CAMERA_HURT,
    FIRE_OVERLAY,
    GLOWING,
    LAVA_OVERLAY,
    SCOREBOARD,
    HOTBAR,
    HELD_ITEM_TOOLTIP,
    TOTEM_POP,
    WITHER_HEARTS;

    public boolean isBossBar() {
        return false;
    }

    public boolean isCameraHurt() {
        return this == CAMERA_HURT;
    }

    public boolean isFireOverlay() {
        return this == FIRE_OVERLAY;
    }

    public boolean isGlowing() {
        return this == GLOWING;
    }

    public boolean isLavaOverlay() {
        return this == LAVA_OVERLAY;
    }

    public boolean isScoreboard() {
        return this == SCOREBOARD;
    }

    public boolean isHotbar() {
        return this == HOTBAR;
    }

    public boolean isHeldItemTooltip() {
        return this == HELD_ITEM_TOOLTIP;
    }

    public boolean isTotemPop() {
        return this == TOTEM_POP;
    }

    public boolean isWitherHearts() {
        return this == WITHER_HEARTS;
    }
}
