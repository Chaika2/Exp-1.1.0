package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum RemovedSoundCategory implements DisplayNamed {
    THROW_POTIONS(Lang.REMOVALS_THROW_POTIONS),
    DRAGON_DEATH(Lang.REMOVALS_SOUND_DRAGON_DEATH),
    WITHER_SPAWN(Lang.REMOVALS_SOUND_WITHER_SPAWN),
    WARDEN(Lang.REMOVALS_SOUND_WARDEN),
    ENDERMAN(Lang.REMOVALS_SOUND_ENDERMAN),
    LAVA_WATER(Lang.REMOVALS_SOUND_LAVA_WATER),
    LIGHTNING(Lang.REMOVALS_SOUND_LIGHTNING),
    TRIDENT(Lang.REMOVALS_SOUND_TRIDENT),
    EXPLOSIONS(Lang.REMOVALS_SOUND_EXPLOSIONS);

    final Translation displayName;

    RemovedSoundCategory(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public boolean matches(String str) throws MatchException {
        switch (ordinal()) {
            case 0:
                return str.equals("entity.experience_bottle.throw") || str.equals("entity.splash_potion.break") || str.equals("entity.splash_potion.throw");
            case 1:
                return str.startsWith("entity.ender_dragon");
            case 2:
                return str.startsWith("entity.wither");
            case 3:
                return str.startsWith("entity.warden") || str.equals("block.sculk_shrieker.shriek");
            case 4:
                return str.startsWith("entity.enderman") || str.equals("entity.endereye.launch");
            case 5:
                return str.startsWith("block.lava") || str.equals("entity.generic.splash") || str.equals("entity.generic.swim") || str.equals("entity.generic.extinguish_fire") || str.startsWith("block.water");
            case 6:
                return str.equals("entity.lightning_bolt.thunder") || str.equals("entity.lightning_bolt.impact");
            case 7:
                return str.startsWith("item.trident") || str.equals("entity.drowned.shoot");
            case 8:
                return str.equals("entity.generic.explode") || str.equals("entity.dragon_fireball.explode") || str.equals("entity.wind_charge.wind_burst") || str.equals("entity.firework_rocket.blast") || str.equals("entity.firework_rocket.large_blast") || str.equals("entity.firework_rocket.twinkle") || str.equals("entity.firework_rocket.large_blast_far") || str.equals("entity.firework_rocket.twinkle_far");
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
    }
}
