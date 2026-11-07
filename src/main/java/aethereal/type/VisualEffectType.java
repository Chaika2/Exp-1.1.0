package aethereal.type;

public enum VisualEffectType {
    BLINDNESS,
    DARKNESS,
    NAUSEA;

    public boolean isBlindness() {
        return this == BLINDNESS;
    }

    public boolean isDarkness() {
        return this == DARKNESS;
    }

    public boolean isNausea() {
        return this == NAUSEA;
    }
}
