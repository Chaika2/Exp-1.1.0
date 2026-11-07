package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum HitSoundType implements DisplayNamed {
    TYPE_1(Lang.HITSOUNDS_TYPE_1),
    TYPE_2(Lang.HITSOUNDS_TYPE_2),
    TYPE_3(Lang.HITSOUNDS_TYPE_3),
    TYPE_4(Lang.HITSOUNDS_TYPE_4),
    MOANS(Lang.HITSOUNDS_TYPE_MOANS);

    final Translation displayName;

    HitSoundType(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
