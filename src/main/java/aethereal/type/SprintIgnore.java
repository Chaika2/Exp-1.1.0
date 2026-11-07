package aethereal.type;
import aethereal.Lang;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum SprintIgnore implements DisplayNamed {
    HUNGER(Lang.SPRINT_IGNORE_OPTION_HUNGER),
    BLINDNESS(Lang.SPRINT_IGNORE_OPTION_BLINDNESS),
    SNEAK(Lang.SPRINT_IGNORE_OPTION_SNEAK),
    CRAWL(Lang.SPRINT_IGNORE_OPTION_CRAWL),
    ITEM_USE(Lang.SPRINT_IGNORE_OPTION_ITEM_USE),
    HORIZONTAL_COLLISION(Lang.SPRINT_IGNORE_OPTION_HORIZONTAL_COLLISION);

    public final Translation displayName;

    SprintIgnore(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
