package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.util.BlockUtil;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.util.MovementInputHelper;
import aethereal.ui.setting.NumberSetting;
import aethereal.event.PlayerTickEvent;
import aethereal.type.SettingUnit;
import aethereal.math.SimulatedPlayer;

import net.minecraft.block.CobwebBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;

@Aliases(aliases = {"No Web", "Anti Web"})
public class NoWebModule extends Module {
    public final NumberSetting horizontalSpeed;
    public final NumberSetting verticalSpeed;

    public NoWebModule() {
        super(ModuleTab.MOVEMENT, "No Web");
        this.horizontalSpeed = new NumberSetting(Lang.FLIGHT_VALUE_HORIZONTAL_SPEED).currentValue(0.45f).range(0.1f, 5.0f).step(0.05f).unit(SettingUnit.BLOCKS);
        this.verticalSpeed = new NumberSetting(Lang.FLIGHT_VALUE_VERTICAL_SPEED).currentValue(0.7f).range(0.1f, 5.0f).step(0.05f).unit(SettingUnit.BLOCKS);
        addSettings(this.horizontalSpeed, this.verticalSpeed);
        register(PlayerTickEvent.class, class130Var -> {
            double dCurrentValue;
            if (class130Var.isPre() && isState() && Mc.INSTANCE.isWorldLoaded() && BlockUtil.checkBlockIntersection(Mc.INSTANCE.getPlayer().getBoundingBox().contract(0.01d), block -> {
                return block instanceof CobwebBlock;
            })) {
                SimulatedPlayer class136VarSimulateLocalPlayer = SimulatedPlayer.simulateLocalPlayer(1);
                GameOptions gameOptions = Mc.INSTANCE.getGameOptions();
                double[] dArrDirection = MovementInputHelper.direction(class136VarSimulateLocalPlayer.onGround && gameOptions.sneakKey.isPressed() ? 0.0d : this.horizontalSpeed.currentValue());
                ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
                double d = dArrDirection[0];
                if (gameOptions.jumpKey.isPressed()) {
                    dCurrentValue = this.verticalSpeed.currentValue();
                } else {
                    dCurrentValue = gameOptions.sneakKey.isPressed() ? -this.verticalSpeed.currentValue() : 0.0d;
                }
                player.setVelocity(d, dCurrentValue, dArrDirection[1]);
            }
        });
    }
}
