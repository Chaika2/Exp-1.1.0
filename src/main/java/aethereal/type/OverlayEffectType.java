package aethereal.type;

public enum OverlayEffectType {
    BLIDNESS,
    HUNGER;

    public boolean isBlidness() {
        return this == BLIDNESS;
    }

    public boolean isHunger() {
        return this == HUNGER;
    }
}
