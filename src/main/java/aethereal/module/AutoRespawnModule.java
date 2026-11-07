package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.event.DeathTickEvent;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.NumberSetting;
import aethereal.type.SettingUnit;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;

@Aliases(aliases = {"Auto Respawn", "Automatic Respawn", "Fast Respawn", "Quick Respawn", "Auto Revive"})
public class AutoRespawnModule extends Module {
    public final NumberSetting respawnDelay;

    public AutoRespawnModule() {
        super(ModuleTab.PLAYER, "AutoRespawn");
        this.respawnDelay = new NumberSetting(Lang.AUTORESPAWN_DELAY).currentValue(20.0f).range(0.0f, 70.0f).step(1.0f).unit(SettingUnit.TICKS);
        addSettings(this.respawnDelay);
        register(DeathTickEvent.class, class276Var -> {
            Mc class815Var;
            ClientPlayerEntity player;
            if (!isState() || class276Var.ticksSinceDeath() <= Math.round(this.respawnDelay.currentValue()) || (player = (class815Var = Mc.INSTANCE).getPlayer()) == null) {
                return;
            }
            player.requestRespawn();
            class815Var.getMinecraft().setScreen((Screen) null);
        });
    }
}
