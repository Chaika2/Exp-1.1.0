package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum RwHelperAction implements DisplayNamed {
    AUTO_WAYPOINT(Lang.RWHELPER_ACTION_AUTO_WAYPOINT),
    FILTER_BANNED_WORDS(Lang.RWHELPER_ACTION_FILTER_BANNED_WORDS),
    CLOSE_SERVER_MENU(Lang.RWHELPER_ACTION_CLOSE_SERVER_MENU);

    final Translation displayName;

    RwHelperAction(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
