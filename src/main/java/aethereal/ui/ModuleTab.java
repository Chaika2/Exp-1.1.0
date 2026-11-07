package aethereal.ui;
import aethereal.Lang;
import aethereal.model.Translation;

public enum ModuleTab {
    COMBAT(Lang.COMBAT, "combat"),
    MOVEMENT(Lang.MOVEMENT, "movement"),
    PLAYER(Lang.PLAYER, "player"),
    RENDER(Lang.RENDER, "render"),
    MISC(Lang.MISC, "misc"),
    CONFIGS(Lang.CONFIGS, "cloud");

    public final Translation name;
    public final String iconName;

    public Translation getName() {
        return this.name;
    }

    public String getIconName() {
        return this.iconName;
    }

    ModuleTab(Translation class254Var, String str) {
        this.name = class254Var;
        this.iconName = str;
    }
}
