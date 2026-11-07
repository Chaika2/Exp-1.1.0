package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum NoPushTarget implements DisplayNamed {
    PLAYERS(Lang.PLAYER_NOPUSH_TARGET_PLAYERS),
    WATER(Lang.PLAYER_NOPUSH_TARGET_WATER),
    BLOCKS(Lang.PLAYER_NOPUSH_TARGET_BLOCKS);

    final Translation displayName;

    NoPushTarget(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
