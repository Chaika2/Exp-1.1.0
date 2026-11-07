package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum ItemTrackerEventType implements DisplayNamed {
    FOOD_USE(Lang.ITEM_TRACKER_EVENT_FOOD),
    TOTEM_POP(Lang.ITEM_TRACKER_EVENT_TOTEM),
    POTION_USE(Lang.ITEM_TRACKER_EVENT_POTION);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    ItemTrackerEventType(Translation class254Var) {
        this.displayName = class254Var;
    }
}
