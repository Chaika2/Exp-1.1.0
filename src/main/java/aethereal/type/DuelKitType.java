package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum DuelKitType implements DisplayNamed {
    SHIELD(Translation.clearText("Щит")),
    THORNS(Translation.clearText("Шипы 3")),
    BOW(Translation.clearText("Лук")),
    TOTEMS(Translation.clearText("Тотемы")),
    NO_DEBUFF(Translation.clearText("nodebaff")),
    BALLS(Translation.clearText("shari")),
    CLASSIC(Translation.clearText("classic")),
    CHEATER_PARADISE(Translation.clearText("chiterskii rai")),
    NETHERITE(Translation.clearText("nezerka"));

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }

    DuelKitType(Translation class254Var) {
        this.displayName = class254Var;
    }
}
