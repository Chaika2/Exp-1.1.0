package aethereal.render;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum PanelStyle implements DisplayNamed {
    DEFAULT(Translation.clearText("Обычные")),
    GLASS(Translation.clearText("Стекло"));

    public final Translation displayName;

    PanelStyle(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
