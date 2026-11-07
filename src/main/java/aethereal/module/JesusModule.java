package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.type.SettingUnit;
import aethereal.ui.setting.NumberSetting;
import aethereal.Lang;
import aethereal.type.EventPriority;
import aethereal.event.MovementInputEvent;
import aethereal.math.DirectionalInput;
import aethereal.event.PlayerTickEvent;

import net.minecraft.client.network.ClientPlayerEntity;

@Aliases(aliases = {"Water Walk", "Jesus Walk", "Walk on Water", "YHW", "Water Bounce"})
public class JesusModule extends Module {
    public final NumberSetting interval;
    public final Mc mc;
    public boolean shouldJump;
    public int tickCounter;

    public JesusModule() {
        super(ModuleTab.MOVEMENT, "Jesus");
        this.mc = Mc.INSTANCE;
        this.interval = new NumberSetting(Lang.JESUS_INTERVAL, Lang.JESUS_INTERVAL_DESC).currentValue(3.0f).range(1.0f, 10.0f).step(1.0f).unit(SettingUnit.TICKS);
        addSettings(this.interval);
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && this.mc.isWorldLoaded() && class130Var.isPre()) {
                this.shouldJump = false;
                this.tickCounter = 0;
                ClientPlayerEntity player = this.mc.getPlayer();
                if (player.isTouchingWater() && !player.isSubmergedInWater() && !player.hasVehicle() && !player.getAbilities().flying && DirectionalInput.fromPlayerInput(player).isMoving()) {
                    if (++this.tickCounter >= (int) this.interval.currentValue()) {
                        this.tickCounter = 0;
                        this.shouldJump = true;
                    }
                }
            }
        });
        register(MovementInputEvent.class, class040Var -> {
            if (isState() && this.shouldJump) {
                class040Var.setJumping(true);
            }
        }, EventPriority.HIGH);
    }
}