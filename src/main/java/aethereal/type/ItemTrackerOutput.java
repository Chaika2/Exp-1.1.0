package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum ItemTrackerOutput implements DisplayNamed {
    CHAT(Lang.CHAT),
    NOTIFICATIONS(Lang.ITEM_TRACKER_OUTPUT_NOTIFICATIONS);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    ItemTrackerOutput(Translation class254Var) {
        this.displayName = class254Var;
    }
}
