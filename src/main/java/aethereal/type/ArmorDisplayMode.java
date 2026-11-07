package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum ArmorDisplayMode implements DisplayNamed {
    PERCENT(Translation.clearText("Percent")),
    VALUE(Translation.clearText("Value"));

    public final Translation displayName;

    ArmorDisplayMode(Translation displayName) {
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
