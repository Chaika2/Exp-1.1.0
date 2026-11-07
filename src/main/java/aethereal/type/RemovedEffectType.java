package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum RemovedEffectType implements DisplayNamed {
    JUMP_BOOST(Lang.REMOVEEFFECTS_POTIONS_JUMP_BOOST),
    BLINDNESS(Lang.REMOVEEFFECTS_POTIONS_BLINDNESS),
    NAUSEA(Lang.REMOVEEFFECTS_POTIONS_NAUSEA),
    DARKNESS(Lang.REMOVEEFFECTS_POTIONS_DARKNESS);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    RemovedEffectType(Translation class254Var) {
        this.displayName = class254Var;
    }
}
