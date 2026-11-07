package aethereal.module;
import aethereal.event.CrosshairRenderEvent;
import aethereal.Lang;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.NumberSetting;

public class OpenWallsModule extends Module {
    NumberSetting maxDistance;

    public OpenWallsModule() {
        super(ModuleTab.MISC, "Open Walls");
        this.maxDistance = new NumberSetting(Lang.ATTACKAURA_MAX_DISTANCE).currentValue(4.5f).range(4.5f, 9.0f);
        addSettings(this.maxDistance);
        register(CrosshairRenderEvent.class, class247Var -> {
        });
    }
}
