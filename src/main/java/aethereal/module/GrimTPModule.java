package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.event.PlayerTickEvent;
import aethereal.model.Translation;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.NumberSetting;
import aethereal.type.NotificationType;
import aethereal.Expensive;

import java.util.concurrent.TimeUnit;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;

@Aliases(aliases = {"Grim TP", "GrimTp", "Grim Teleport", "HitBox Expand", "Hitbox Clip"})
public class GrimTPModule extends Module {
    private final NumberSetting updateSpeedSetting;
    private final NumberSetting heightUpSetting;
    private final NumberSetting heightDownSetting;
    private final Mc mc;
    private Box originalHitbox;
    private boolean warningShown;
    private int tickCounter;

    public GrimTPModule() {
        super(ModuleTab.MOVEMENT, "Grim TP");
        this.updateSpeedSetting = new NumberSetting(Translation.clearText("Update Speed")).currentValue(5.0f).range(1.0f, 20.0f).step(1.0f);
        this.heightUpSetting = new NumberSetting(Translation.clearText("Height Up")).currentValue(0.5f).range(0.1f, 2.0f).step(0.1f);
        this.heightDownSetting = new NumberSetting(Translation.clearText("Height Down")).currentValue(0.5f).range(0.1f, 2.0f).step(0.1f);
        this.mc = Mc.INSTANCE;
        addSettings(this.updateSpeedSetting, this.heightUpSetting, this.heightDownSetting);
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && this.mc.isWorldLoaded() && class130Var.isPre()) {
                this.tickCounter++;
                if (this.tickCounter >= (int) this.updateSpeedSetting.currentValue()) {
                    this.tickCounter = 0;
                    handleGrimTP();
                }
            }
        });
    }

    @Override
    public void activate() {
        super.activate();
        this.warningShown = false;
        this.tickCounter = 0;
        if (this.mc.isWorldLoaded() && this.mc.getPlayer() != null) {
            this.originalHitbox = this.mc.getPlayer().getBoundingBox();
            if (!this.warningShown) {
                sendWarningMessage();
                this.warningShown = true;
            }
        }
    }

    @Override
    public void deactivate() {
        if (this.mc.isWorldLoaded() && this.mc.getPlayer() != null && this.originalHitbox != null) {
            this.mc.getPlayer().setBoundingBox(this.originalHitbox);
        }
        super.deactivate();
    }

    public void sendWarningMessage() {
        Expensive.INSTANCE.notificationRepository().post(NotificationType.WARNING, (Text) Text.literal(String.valueOf(Formatting.RED) + "Внимание! " + String.valueOf(Formatting.YELLOW) + "Для правильной работы этой функции нужен " + String.valueOf(Formatting.AQUA) + "FLY!"), 4L, TimeUnit.SECONDS);
    }

    public void handleGrimTP() {
        ClientPlayerEntity player = this.mc.getPlayer();
        if (player == null) {
            return;
        }
        expandSelfHitBox(this.heightUpSetting.currentValue(), this.heightDownSetting.currentValue());
    }

    public void expandSelfHitBox(float f, float f2) {
        ClientPlayerEntity player = this.mc.getPlayer();
        if (player == null) {
            return;
        }
        Box boxCurrent = player.getBoundingBox();
        Box boxExpanded = new Box(boxCurrent.minX, boxCurrent.minY - ((double) f2), boxCurrent.minZ, boxCurrent.maxX, boxCurrent.maxY + ((double) f), boxCurrent.maxZ);
        player.setBoundingBox(boxExpanded);
    }
}