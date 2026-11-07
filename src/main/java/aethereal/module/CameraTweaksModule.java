package aethereal.module;
import aethereal.render.AnimatedFloat;
import aethereal.render.AnimationStack2;
import aethereal.type.ButtonAction;
import aethereal.event.CameraDistanceEvent;
import aethereal.type.CameraMode;
import aethereal.event.CameraPerspectiveEvent;
import aethereal.math.DeltaTimeTracker;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.event.FovEvent;
import aethereal.event.KeyInputEvent;
import aethereal.type.KeyPressState;
import aethereal.ui.setting.KeybindSetting;
import aethereal.Lang;
import aethereal.util.MathUtil;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.MouseButtonEvent;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.render.ToggleAnimator;
import aethereal.model.Translation;
import aethereal.event.VelocityEvent;
import aethereal.util.WeightedEngine;
import aethereal.event.WorldRenderEvent;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;

public class CameraTweaksModule extends Module {
    public final MultiSelectSetting<CameraMode> modeSetting;
    public final ToggleAnimator backAnimator;
    public final ToggleAnimator frontAnimator;
    public final AnimatedFloat fovAnimator;
    public final AnimatedFloat pitchAnimator;
    public final Mc mc;
    public final KeybindSetting zoomKey;
    public final DeltaTimeTracker deltaTracker;

    public final AnimationStack2 animationStack;
    public float targetFov;
    public float previousFov;
    public boolean zooming;

    public CameraTweaksModule() {
        super(ModuleTab.RENDER, "Camera Tweaks");
        this.modeSetting = new MultiSelectSetting(Lang.TWEAKS).values(CameraMode.class);
        this.backAnimator = new ToggleAnimator(300, Easings.EASE_IN_OUT_CUBIC);
        this.frontAnimator = new ToggleAnimator(300, Easings.EASE_IN_OUT_CUBIC);
        this.fovAnimator = new AnimatedFloat(150, Easings.LINEAR);
        this.pitchAnimator = new AnimatedFloat(300, Easings.LINEAR);
        this.mc = Mc.INSTANCE;
        this.zoomKey = new KeybindSetting(Translation.clearText("Zoom key"), Translation.clearText("Key to zoom in camera")).visible(() -> {
            return Boolean.valueOf(this.modeSetting.isSelected(CameraMode.ZOOM));
        });
        this.deltaTracker = new DeltaTimeTracker();
        this.animationStack = new AnimationStack2();
        this.targetFov = 110.0f;
        this.previousFov = 30.0f;
        this.zooming = false;
        addSettings(this.modeSetting, this.zoomKey);
        this.fovAnimator.set(110.0f);
        register(KeyInputEvent.class, class049Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                GameOptions gameOptions = this.mc.getGameOptions();
                if (class049Var.key() == this.zoomKey.getKey() && class049Var.action() == KeyPressState.PRESS) {
                    this.zooming = true;
                    this.targetFov = Math.min(30, ((Integer) gameOptions.getFov().getValue()).intValue() - 20);
                }
                if (class049Var.key() == this.zoomKey.getKey() && class049Var.action() == KeyPressState.RELEASE) {
                    this.zooming = false;
                    this.previousFov = this.targetFov;
                    this.targetFov = ((Integer) gameOptions.getFov().getValue()).intValue();
                }
            }
        });
        register(MouseButtonEvent.class, class107Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                GameOptions gameOptions = this.mc.getGameOptions();
                if (class107Var.button() == this.zoomKey.getKey() && class107Var.action() == ButtonAction.PRESS) {
                    this.zooming = true;
                    this.targetFov = Math.min(30, ((Integer) gameOptions.getFov().getValue()).intValue() - 20);
                }
                if (class107Var.button() == this.zoomKey.getKey() && class107Var.action() == ButtonAction.RELEASE) {
                    this.zooming = false;
                    this.previousFov = this.targetFov;
                    this.targetFov = ((Integer) gameOptions.getFov().getValue()).intValue();
                }
            }
        });
        register(WorldRenderEvent.class, class016Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                Perspective perspective = this.mc.getGameOptions().getPerspective();
                WeightedEngine class141Var = new WeightedEngine(this.deltaTracker.elapsedUnit(), this.animationStack);
                this.animationStack.begin();
                this.backAnimator.animate(class141Var);
                this.frontAnimator.animate(class141Var);
                this.pitchAnimator.animate(class141Var);
                this.fovAnimator.animate(class141Var);
                this.animationStack.end();
                this.backAnimator.state(!perspective.isFirstPerson());
                this.frontAnimator.state(perspective.isFrontView());
                this.fovAnimator.destination(this.targetFov);
            }
        });
        register(VelocityEvent.class, class238Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                GameOptions gameOptions = this.mc.getGameOptions();
                if (this.zooming && this.modeSetting.isSelected(CameraMode.ZOOM)) {
                    this.targetFov = (int) MathUtil.clamp(((double) this.targetFov) - (class238Var.getVertical() * 10.0d), 10.0d, ((Integer) gameOptions.getFov().getValue()).intValue());
                    class238Var.cancel();
                }
            }
        });
        register(FovEvent.class, class146Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                class146Var.setFov((int) MathUtil.clamp(this.fovAnimator.animatedValue(), 10.0f, ((Integer) this.mc.getGameOptions().getFov().getValue()).intValue()));
                class146Var.cancel();
            }
        });
        register(CameraDistanceEvent.class, class140Var -> {
            if (isState() && this.mc.isWorldLoaded() && !((FreeCameraModule) Expensive.INSTANCE.moduleRepository().get(FreeCameraModule.class)).isState() && this.modeSetting.isSelected(CameraMode.PERSPECTIVE)) {
                Perspective perspective = this.mc.getGameOptions().getPerspective();
                class140Var.setDistance(4.0f * this.backAnimator.smoothAnimation());
                class140Var.setFrontAnim(this.frontAnimator);
                class140Var.setBackAnim(this.backAnimator);
                if (perspective.isFirstPerson() && this.pitchAnimator.isAtDestination()) {
                    this.pitchAnimator.set(class140Var.getPitch());
                } else if (!this.frontAnimator.finished() || !this.pitchAnimator.isAtDestination()) {
                    this.pitchAnimator.destination(class140Var.getPitch());
                    class140Var.setPitch(this.pitchAnimator.animatedValue());
                }
                class140Var.cancel();
            }
        });
        register(CameraPerspectiveEvent.class, class380Var -> {
            if (isState() && this.mc.isWorldLoaded() && !((FreeCameraModule) Expensive.INSTANCE.moduleRepository().get(FreeCameraModule.class)).isState() && this.modeSetting.isSelected(CameraMode.PERSPECTIVE) && !this.pitchAnimator.isAtDestination()) {
                class380Var.cancel();
            }
        });
    }
}
