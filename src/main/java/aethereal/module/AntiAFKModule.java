package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.type.AntiAfkAction;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.MovementInputEvent;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.ui.setting.NumberSetting;
import aethereal.event.PlayerTickEvent;
import aethereal.math.Rotation;
import aethereal.config.RotationConfig;
import aethereal.util.RotationManager;
import aethereal.type.SettingUnit;
import aethereal.math.Stopwatch;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;

@Aliases(aliases = {"Anti AFK", "AFK Preventer", "No AFK", "Auto Move", "Idle Preventer", "Stay Active", "AFK Blocker", "Anti Kick"})
public class AntiAFKModule extends Module {
    public final MultiSelectSetting<AntiAfkAction> actions;
    public final NumberSetting actionInterval;
    public final Stopwatch actionTimer;
    public long lastActivityTime;
    public float lastYaw;
    public float lastPitch;
    public boolean shouldJump;

    public AntiAFKModule() {
        super(ModuleTab.PLAYER, "Anti AFK");
        this.actions = new MultiSelectSetting(Lang.ANTIAFK_ACTIONS).values(AntiAfkAction.class);
        this.actionInterval = new NumberSetting(Lang.ANTIAFK_ACTION_INTERVAL).currentValue(20.0f).range(0.0f, 30.0f).step(1.0f).unit(SettingUnit.SECONDS);
        this.actionTimer = new Stopwatch();
        this.lastActivityTime = System.currentTimeMillis();
        this.lastYaw = 0.0f;
        this.lastPitch = 0.0f;
        this.shouldJump = false;
        addSettings(this.actions, this.actionInterval);
        register(PlayerTickEvent.class, class130Var -> {
            if (class130Var.isPre() && isState() && Mc.INSTANCE.isWorldLoaded() && !this.actions.selectedValues().isEmpty()) {
                if (hasPlayerActivity()) {
                    this.lastActivityTime = System.currentTimeMillis();
                    this.shouldJump = false;
                } else if (System.currentTimeMillis() - this.lastActivityTime > 2000 && this.actionTimer.hasElapsed((long) this.actionInterval.currentValue(), TimeUnit.SECONDS)) {
                    if (this.actions.isSelected(AntiAfkAction.TURN_HEAD)) {
                        turnHead();
                    }
                    if (this.actions.isSelected(AntiAfkAction.SEND_CHAT_MESSAGE)) {
                        Mc.INSTANCE.getNetworkHandler().sendChatCommand("help123");
                    }
                    this.shouldJump = this.actions.isSelected(AntiAfkAction.JUMP);
                    this.actionTimer.reset();
                }
            }
        });
        register(MovementInputEvent.class, class040Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded() && this.shouldJump) {
                class040Var.setJumping(true);
                this.shouldJump = false;
            }
        });
    }

    public void turnHead() {
        RotationManager.INSTANCE.scheduleRotation(new Rotation(ThreadLocalRandom.current().nextInt(10, 180), ThreadLocalRandom.current().nextInt(0, 90)), RotationConfig.BLOCK_WITH_CORRECTION, 2, this, 5);
    }

    public boolean hasPlayerActivity() {
        GameOptions gameOptions = Mc.INSTANCE.getGameOptions();
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        boolean z = gameOptions.forwardKey.isPressed() || gameOptions.backKey.isPressed() || gameOptions.leftKey.isPressed() || gameOptions.rightKey.isPressed() || gameOptions.jumpKey.isPressed() || gameOptions.attackKey.isPressed() || gameOptions.useKey.isPressed();
        boolean z2 = (player.getYaw() == this.lastYaw && player.getPitch() == this.lastPitch) ? false : true;
        if (z2) {
            this.lastYaw = player.getYaw();
            this.lastPitch = player.getPitch();
        }
        return z || z2;
    }
}
