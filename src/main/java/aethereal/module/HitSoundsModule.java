package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.event.AttackEntityEvent;
import aethereal.ui.setting.EnumSetting;
import aethereal.type.HitSoundType;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.NumberSetting;
import aethereal.type.SettingUnit;
import aethereal.util.WavSoundPlayer;

import java.util.concurrent.ThreadLocalRandom;

@Aliases(aliases = {"Hit Sounds", "Custom Hit Sounds", "Attack Sounds", "Damage Sounds", "Hit Markers"})
public class HitSoundsModule extends Module {
    EnumSetting<HitSoundType> soundTypeSetting;
    NumberSetting volumeSetting;

    public HitSoundsModule() {
        super(ModuleTab.MISC, "Hit Sounds");
        this.soundTypeSetting = new EnumSetting(Lang.HITSOUNDS_TYPE).values(HitSoundType.class);
        this.volumeSetting = new NumberSetting(Lang.HITSOUNDS_VOLUME).currentValue(70.0f).range(1.0f, 100.0f).step(1.0f).unit(SettingUnit.PERCENTS);
        addSettings(this.soundTypeSetting, this.volumeSetting);
        this.soundTypeSetting.soundAction(this::previewSound);
        register(AttackEntityEvent.class, class144Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded()) {
                playHitSound();
            }
        });
    }

    public void previewSound(HitSoundType class502Var) {
        WavSoundPlayer.INSTANCE.playSound("/" + getSoundPath(class502Var), this.volumeSetting.currentValue(), false);
    }

    public void playHitSound() {
        WavSoundPlayer.INSTANCE.playSound("/" + getSoundPath((HitSoundType) this.soundTypeSetting.currentValue()), this.volumeSetting.currentValue(), false);
    }

    public String getSoundPath(HitSoundType class502Var) {
        if (class502Var == HitSoundType.MOANS) {
            return "hitsounds/moans/moan" + ThreadLocalRandom.current().nextInt(1, 4);
        }
        int i = 0;
        HitSoundType[] class502VarArrValues = HitSoundType.values();
        int length = class502VarArrValues.length;
        for (int i2 = 0; i2 < length && class502VarArrValues[i2] != class502Var; i2++) {
            i++;
        }
        return "hitsounds/hit" + (i + 1);
    }
}
