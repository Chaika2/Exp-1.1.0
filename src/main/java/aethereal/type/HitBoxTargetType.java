package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum HitBoxTargetType implements DisplayNamed {
    PLAYERS(Lang.HITBOX_TARGET_PLAYERS),
    MOBS(Lang.HITBOX_TARGET_MOBS),
    ANIMALS(Lang.HITBOX_TARGET_ANIMALS);

    final Translation displayName;

    HitBoxTargetType(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
