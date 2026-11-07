package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum NotificationDirection implements DisplayNamed {
    AUTO(Lang.WIDGET_NOTIFICATIONS_DIRECTION_AUTO),
    UPWARD(Lang.WIDGET_NOTIFICATIONS_DIRECTION_UPWARD),
    DOWNWARD(Lang.WIDGET_NOTIFICATIONS_DIRECTION_DOWNWARD);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    NotificationDirection(Translation class254Var) {
        this.displayName = class254Var;
    }
}
