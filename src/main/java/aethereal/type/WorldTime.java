package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum WorldTime implements DisplayNamed {
    DAY(Lang.WORLD_TWEAKS_DAY, 1000),
    NIGHT(Lang.WORLD_TWEAKS_NIGHT, 18000),
    SUNSET(Lang.WORLD_TWEAKS_SUNSET, 12000),
    MIDNIGHT(Lang.WORLD_TWEAKS_MIDNIGHT, 13000),
    SUNRISE(Lang.WORLD_TWEAKS_SUNRISE, 23000);

    public final Translation displayName;
    public final int ticks;

    WorldTime(Translation class254Var, int i) {
        this.displayName = class254Var;
        this.ticks = i;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public int ticks() {
        return this.ticks;
    }
}
