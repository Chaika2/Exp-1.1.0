package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum ClickPearlMode implements DisplayNamed {
    VANILLA(Translation.clearText("Vanilla")),
    HOLYWORLD(Translation.clearText("HolyWorld"));

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    ClickPearlMode(Translation class254Var) {
        this.displayName = class254Var;
    }
}
