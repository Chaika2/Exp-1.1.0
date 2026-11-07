package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.type.ButtonAction;
import aethereal.event.CameraRotationEvent;
import aethereal.event.ChangeLookDirectionEvent;
import aethereal.Expensive;
import aethereal.event.KeyInputEvent;
import aethereal.type.KeyPressState;
import aethereal.ui.setting.KeybindSetting;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.MouseButtonEvent;

import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

@Aliases(aliases = {"Free Look", "Free View", "360 Look", "Look Around", "Third-Person View", "Advanced Camera", "Free Roam", "Dynamic View", "Camera Control"})
public class FreeLookModule extends Module {
    public final KeybindSetting keybind;
    public Vec2f savedRotation;
    public boolean active;
    public double accumulatedYaw;
    public double accumulatedPitch;
    public double lastMouseX;
    public double lastMouseY;
    public Perspective previousPerspective;

    public FreeLookModule() {
        super(ModuleTab.PLAYER, "Free Look");
        this.keybind = new KeybindSetting(Lang.PLAYER_FREELOOK_KEY);
        this.active = false;
        this.accumulatedYaw = 0.0d;
        this.accumulatedPitch = 0.0d;
        this.lastMouseX = 0.0d;
        this.lastMouseY = 0.0d;
        this.previousPerspective = null;
        addSettings(this.keybind);
        register(KeyInputEvent.class, class049Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded() && class049Var.key() == this.keybind.getKey()) {
                GameOptions gameOptions = Mc.INSTANCE.getGameOptions();
                ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
                Mouse mouse = Mc.INSTANCE.getMouse();
                if (class049Var.action() == KeyPressState.PRESS) {
                    this.accumulatedPitch = 0.0d;
                    this.accumulatedYaw = 0.0d;
                    this.lastMouseX = mouse.getX();
                    this.lastMouseY = mouse.getY();
                    this.active = true;
                    this.savedRotation = new Vec2f(player.getYaw(), player.getPitch());
                    if (gameOptions.getPerspective() != Perspective.THIRD_PERSON_BACK) {
                        if (this.previousPerspective == null) {
                            this.previousPerspective = gameOptions.getPerspective();
                        }
                        gameOptions.setPerspective(Perspective.THIRD_PERSON_BACK);
                        return;
                    }
                    return;
                }
                if (class049Var.action() == KeyPressState.RELEASE) {
                    this.lastMouseY = 0.0d;
                    this.lastMouseX = 0.0d;
                    this.accumulatedPitch = 0.0d;
                    this.accumulatedYaw = 0.0d;
                    this.active = false;
                    if (this.previousPerspective == null || gameOptions.getPerspective() == this.previousPerspective) {
                        return;
                    }
                    gameOptions.setPerspective(this.previousPerspective);
                    this.previousPerspective = null;
                }
            }
        });
        register(MouseButtonEvent.class, class107Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded() && class107Var.button() == this.keybind.getKey()) {
                GameOptions gameOptions = Mc.INSTANCE.getGameOptions();
                ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
                Mouse mouse = Mc.INSTANCE.getMouse();
                if (class107Var.action() == ButtonAction.PRESS) {
                    this.accumulatedPitch = 0.0d;
                    this.accumulatedYaw = 0.0d;
                    this.lastMouseX = mouse.getX();
                    this.lastMouseY = mouse.getY();
                    this.active = true;
                    this.savedRotation = new Vec2f(player.getYaw(), player.getPitch());
                    if (gameOptions.getPerspective() != Perspective.THIRD_PERSON_BACK) {
                        if (this.previousPerspective == null) {
                            this.previousPerspective = gameOptions.getPerspective();
                        }
                        gameOptions.setPerspective(Perspective.THIRD_PERSON_BACK);
                        return;
                    }
                    return;
                }
                if (class107Var.action() == ButtonAction.RELEASE) {
                    this.lastMouseY = 0.0d;
                    this.lastMouseX = 0.0d;
                    this.accumulatedPitch = 0.0d;
                    this.accumulatedYaw = 0.0d;
                    this.active = false;
                    if (this.previousPerspective == null || gameOptions.getPerspective() == this.previousPerspective) {
                        return;
                    }
                    gameOptions.setPerspective(this.previousPerspective);
                    this.previousPerspective = null;
                }
            }
        });
        Expensive.INSTANCE.eventDispatcher().register(ChangeLookDirectionEvent.class, class026Var -> {
            if (isState() && this.active && Mc.INSTANCE.isWorldLoaded() && this.savedRotation != null) {
                class026Var.cancel();
            }
        });
        register(CameraRotationEvent.class, class152Var -> {
            if (!isState() || !this.active || !Mc.INSTANCE.isWorldLoaded()) {
                this.lastMouseY = 0.0d;
                this.lastMouseX = 0.0d;
                this.accumulatedPitch = 0.0d;
                this.accumulatedYaw = 0.0d;
                return;
            }
            Mouse mouse = Mc.INSTANCE.getMouse();
            double dPow = Math.pow((((Double) Mc.INSTANCE.getGameOptions().getMouseSensitivity().getValue()).doubleValue() * 0.6d) + 0.2d, 3.0d) * 8.0d;
            double x = mouse.getX();
            double y = mouse.getY();
            this.accumulatedYaw += (x - this.lastMouseX) * dPow;
            this.accumulatedPitch += (y - this.lastMouseY) * dPow;
            if (this.savedRotation != null) {
                class152Var.setPitch(MathHelper.clamp(this.savedRotation.y + (((float) this.accumulatedPitch) * 0.15f), -90.0f, 90.0f));
                class152Var.setYaw(this.savedRotation.x + (((float) this.accumulatedYaw) * 0.15f));
                class152Var.cancel();
            }
            this.lastMouseX = x;
            this.lastMouseY = y;
        });
    }

    @Override
    public void deactivate() {
        super.deactivate();
        if (Mc.INSTANCE.isWorldLoaded()) {
            GameOptions gameOptions = Mc.INSTANCE.getGameOptions();
            if (this.previousPerspective != null && gameOptions.getPerspective() != this.previousPerspective) {
                gameOptions.setPerspective(this.previousPerspective);
                this.previousPerspective = null;
            }
        }
        this.active = false;
        this.savedRotation = null;
        this.lastMouseY = 0.0d;
        this.lastMouseX = 0.0d;
        this.accumulatedPitch = 0.0d;
        this.accumulatedYaw = 0.0d;
    }
}
