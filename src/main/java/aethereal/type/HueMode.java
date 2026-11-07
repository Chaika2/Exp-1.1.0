package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum HueMode implements DisplayNamed {
    PRIMARY(Translation.clearText("Первый")),
    SECONDARY(Translation.clearText("Второй"));

    private final Translation displayName;

    HueMode(Translation translation) {
        this.displayName = translation;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
