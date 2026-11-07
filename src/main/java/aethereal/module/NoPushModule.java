package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.type.NoPushTarget;
import aethereal.event.PushEvent;
import aethereal.type.PushType;

@Aliases(aliases = {"No Push", "Anti Push", "No Collision", "Anti Collision"})
public class NoPushModule extends Module {
    public final MultiSelectSetting<NoPushTarget> targetSetting;

    public NoPushModule() {
        super(ModuleTab.PLAYER, "No Push");
        this.targetSetting = new MultiSelectSetting(Lang.PLAYER_NOPUSH_TARGET).values(NoPushTarget.class);
        addSettings(this.targetSetting);
        register(PushEvent.class, class231Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded()) {
                PushType type = class231Var.getType();
                if ((type.isPlayers() && this.targetSetting.isSelected(NoPushTarget.PLAYERS)) || ((type.isWater() && this.targetSetting.isSelected(NoPushTarget.WATER)) || (type.isBlocks() && this.targetSetting.isSelected(NoPushTarget.BLOCKS)))) {
                    class231Var.cancel();
                }
            }
        });
    }
}
