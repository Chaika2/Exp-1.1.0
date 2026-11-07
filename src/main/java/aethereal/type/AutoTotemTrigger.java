package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum AutoTotemTrigger implements DisplayNamed {
    CRYSTAL(Translation.clearText("Кристалл")),
    OBSIDIAN(Translation.clearText("Обсидиан")),
    ANCHOR(Translation.clearText("Якорь")),
    DYNAMITE(Translation.clearText("Динамит")),
    FALL(Translation.clearText("Падение"));

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }

    AutoTotemTrigger(Translation class254Var) {
        this.displayName = class254Var;
    }
}
