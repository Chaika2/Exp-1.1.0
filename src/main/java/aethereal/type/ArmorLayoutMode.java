package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum ArmorLayoutMode implements DisplayNamed {
    VERTICAL(Translation.clearText("Vertical")),
    HORIZONTAL(Translation.clearText("Horizontal"));

    public final Translation displayName;

    ArmorLayoutMode(Translation displayName) {
        this.displayName = displayName;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }
}
