package aethereal.module;
import aethereal.event.AttackEntityEvent;
import aethereal.Expensive;
import aethereal.type.GrimAdvancedMode;
import aethereal.Lang;
import aethereal.util.MathUtil;
import aethereal.type.Mc;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.ModuleTab;
import aethereal.type.NotificationType;
import aethereal.util.PlayerActionUtil;
import aethereal.math.Rotation;
import aethereal.util.RotationManager;
import aethereal.util.ServerUtil;

import java.util.concurrent.TimeUnit;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CriticalsModule extends Module {
    ModeSetting<GrimAdvancedMode> modeSetting;

    public CriticalsModule() {
        super(ModuleTab.COMBAT, "Criticals");
        this.modeSetting = new ModeSetting(Lang.MODE).values(GrimAdvancedMode.class);
        addSettings(this.modeSetting);
        register(AttackEntityEvent.class, class144Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded()) {
                ServerUtil.valid1_17().ifPresentOrElse(num -> {
                    Rotation class007VarRandom = RotationManager.INSTANCE.getCurrentRotation().random(0.001f);
                    PlayerActionUtil.INSTANCE.moveBypass$$$(MathUtil.getRandom(0.003d, 0.004d), class007VarRandom.random(1.0f), false);
                    PlayerActionUtil.INSTANCE.moveBypass$$$(-MathUtil.getRandom(0.001d, 0.002d), class007VarRandom.random(0.001f), false);
                    Mc.INSTANCE.getPlayer().fallDistance = 1.0E-4f;
                }, () -> {
                    Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, Text.of("[Criticals] Нужна версия " + String.valueOf(Formatting.RED) + "1.17-" + (ServerUtil.isConnectedToServer("holyworld") ? "1.18.2" : "1.20.6")), 3L, TimeUnit.SECONDS);
                    switchState();
                });
            }
        });
    }

    @Override
    public void activate() {
        if (ServerUtil.valid1_17().isEmpty()) {
            Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, Text.of("[Criticals] Нужна версия " + String.valueOf(Formatting.RED) + "1.17-" + (ServerUtil.isConnectedToServer("holyworld") ? "1.18.2" : "1.20.6")), 3L, TimeUnit.SECONDS);
            switchState();
        }
        super.activate();
    }
}
