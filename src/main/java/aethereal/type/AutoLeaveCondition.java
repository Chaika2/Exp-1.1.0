package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum AutoLeaveCondition implements DisplayNamed {
    LOW_HEALTH(Lang.AUTOLEAVE_CONDITION_LOW_HEALTH),
    MODERATOR(Lang.AUTOLEAVE_CONDITION_MODERATOR),
    DISTANCE(Lang.AUTOLEAVE_CONDITION_DISTANCE);

    final Translation displayName;

    AutoLeaveCondition(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
