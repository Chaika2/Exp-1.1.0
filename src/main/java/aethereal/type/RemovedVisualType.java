package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum RemovedVisualType implements DisplayNamed {
    CAMERA_CLIP(Lang.REMOVALS_CAMERA_CLIP),
    CAMERA_HURT(Lang.REMOVALS_CAMERA_HURT),
    FIRE_OVERLAY(Lang.REMOVALS_FIRE_OVERLAY),
    LAVA_OVERLAY(Lang.REMOVALS_LAVA_OVERLAY),
    SCOREBOARD(Lang.REMOVALS_SCOREBOARD),
    BOSS_BAR(Lang.REMOVALS_BOSS_BAR),
    TOTEM_POP(Lang.REMOVALS_TOTEM_POP),
    GLOWING(Lang.REMOVALS_GLOWING),
    WITHER_HEARTS(Lang.REMOVALS_WITHER_HEARTS);

    final Translation displayName;

    RemovedVisualType(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
