package aethereal.module;
import aethereal.ui.setting.BooleanSetting;
import aethereal.event.ClientTickEvent;
import aethereal.render.ColorStack;
import aethereal.math.DirectionalInput;
import aethereal.util.DrawEngine;
import aethereal.event.EntityInterpolationEvent;
import aethereal.event.EntityRenderEvent2;
import aethereal.event.EntityRenderEvent3;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.math.InterpolatedPosition;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.MovementInputEvent;
import aethereal.util.MovementInputHelper;
import aethereal.render.MsdfFont;
import aethereal.ui.setting.NumberSetting;
import aethereal.event.PerspectiveEvent;
import aethereal.event.PlayerInitEvent;
import aethereal.event.PlayerTickEvent;
import aethereal.event.Render2DEvent;
import aethereal.math.Rotation;
import aethereal.config.RotationConfig;
import aethereal.util.RotationManager;
import aethereal.render.ScreenResolution;
import aethereal.type.SettingUnit;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class FreeCameraModule extends Module {
    public NumberSetting speedSetting;
    public final BooleanSetting airStuck;
    public final BooleanSetting positionDelta;
    public InterpolatedPosition cameraPosition;
    public Rotation lockedRotation;
    public final Mc mc;
    public int tickCounter;

    public FreeCameraModule() {
        super(ModuleTab.PLAYER, "Free Camera");
        this.speedSetting = new NumberSetting(Lang.FREECAMERA_SPEED).currentValue(1.0f).range(0.1f, 10.0f).step(0.1f).unit(SettingUnit.BLOCKS);
        this.airStuck = new BooleanSetting(Lang.FREECAMERA_AIR_STUCK, Lang.FREECAMERA_AIR_STUCK_DESC);
        this.positionDelta = new BooleanSetting(Lang.FREECAMERA_POSITION_DELTA, Lang.FREECAMERA_POSITION_DELTA_DESC);
        this.cameraPosition = null;
        this.lockedRotation = null;
        this.mc = Mc.INSTANCE;
        this.tickCounter = 0;
        addSettings(this.speedSetting, this.airStuck, this.positionDelta);
        register(MovementInputEvent.class, class040Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                class040Var.setInput(DirectionalInput.NONE);
                class040Var.setJumping(false);
                class040Var.setSneaking(false);
            }
        });
        register(PlayerInitEvent.class, class125Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                setState(false);
            }
        });
        register(EntityRenderEvent3.class, class386Var -> {
            Camera camera;
            if (isState() && this.mc.isWorldLoaded()) {
                Entity player = this.mc.getPlayer();
                GameRenderer gameRenderer = this.mc.getGameRenderer();
                if (class386Var.entity() != player || this.cameraPosition == null || gameRenderer == null || (camera = gameRenderer.getCamera()) == null) {
                    return;
                }
                camera.setPos(this.cameraPosition.interpolate(class386Var.tickDelta()));
            }
        });
        register(Render2DEvent.class, class311Var -> {
            ClientPlayerEntity player;
            if (isState() && this.mc.isWorldLoaded() && this.positionDelta.isValue() && this.cameraPosition != null && class311Var.isPost() && (player = this.mc.getPlayer()) != null) {
                Vec3d vec3dSubtract = this.cameraPosition.interpolate(class311Var.tickCounter().getTickDelta(false)).subtract(player.getEyePos());
                double length = vec3dSubtract.length();
                String str = String.format("X %.1f | Y %.1f | Z %.1f", Double.valueOf(vec3dSubtract.x), Double.valueOf(vec3dSubtract.y), Double.valueOf(vec3dSubtract.z));
                String str2 = String.format("delta hypot: %.1f", Double.valueOf(length));
                float fScreenWidth = ScreenResolution.resolution().screenWidth() / 2.0f;
                float fScreenHeight = ScreenResolution.resolution().screenHeight() / 2.0f;
                MsdfFont class161Var = Fonts.INTER_SEMIBOLD.get();
                DrawEngine class154VarDrawEngine = Expensive.INSTANCE.drawEngine();
                ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
                class154VarDrawEngine.begin();
                class154VarDrawEngine.msdfFont(class311Var.matrixStack().peek().getPositionMatrix(), class161Var, str, fScreenWidth - (class161Var.getWidth(str, 12.0f) / 2.0f), (fScreenHeight - class161Var.getHeight(12.0f)) + 40.0f, 12.0f, 0.05f, class115VarColorStack.computeColor(Expensive.INSTANCE.theme().palette().text().tone(400).argb()));
                class154VarDrawEngine.msdfFont(class311Var.matrixStack().peek().getPositionMatrix(), class161Var, str2, fScreenWidth - (class161Var.getWidth(str2, 12.0f) / 2.0f), fScreenHeight + 2.0f + 40.0f, 12.0f, 0.05f, class115VarColorStack.computeColor(Expensive.INSTANCE.theme().palette().text().tone(400).argb()));
                class154VarDrawEngine.end();
            }
        });
        register(PerspectiveEvent.class, class160Var -> {
            Mc class815Var = Mc.INSTANCE;
            if (isState() && class815Var.isWorldLoaded()) {
                class160Var.perspective(Perspective.FIRST_PERSON);
            }
        });
        register(EntityRenderEvent2.class, class377Var -> {
            Camera camera;
            if (isState() && this.mc.isWorldLoaded()) {
                Entity player = this.mc.getPlayer();
                GameRenderer gameRenderer = this.mc.getGameRenderer();
                if (gameRenderer == null || (camera = gameRenderer.getCamera()) == null || player != class377Var.entity() || camera.isThirdPerson()) {
                    return;
                }
                class377Var.cancel();
            }
        });
        register(ClientTickEvent.class, class181Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                ClientPlayerEntity player = this.mc.getPlayer();
                GameOptions gameOptions = this.mc.getGameOptions();
                float fCurrentValue = this.speedSetting.currentValue();
                float f = 0.0f;
                if (gameOptions.jumpKey.isPressed()) {
                    f = 1.0f;
                } else if (gameOptions.sneakKey.isPressed()) {
                    f = -1.0f;
                }
                moveCamera(MovementInputHelper.withStrafe(new Vec3d(0.0d, 0.0d, 0.0d).withAxis(Direction.Axis.Y, f * fCurrentValue), DirectionalInput.fromGameOptions(gameOptions), fCurrentValue, player.getYaw()), player);
            }
        });
        register(PlayerTickEvent.class, class130Var -> {
            if (this.mc.isWorldLoaded() && isState() && class130Var.isPre()) {
                RotationManager.INSTANCE.scheduleRotation(this.lockedRotation, RotationConfig.LINEAR, 0, this, 5);
                if (this.airStuck.isValue()) {
                    if (this.tickCounter % (600 + 1) < 600) {
                        class130Var.cancel();
                    }
                    this.tickCounter++;
                }
            }
        });
        register(EntityInterpolationEvent.class, class018Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                if (class018Var.target() != this.mc.getPlayer() || this.cameraPosition == null) {
                    return;
                }
                class018Var.changedVector(this.cameraPosition.interpolate(class018Var.tickDelta()));
                class018Var.cancel();
            }
        });
    }

    @Override
    public void activate() {
        if (Mc.INSTANCE.isWorldLoaded()) {
            ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
            this.lockedRotation = new Rotation(player.getYaw(), player.getPitch());
            moveCamera(Vec3d.ZERO, player);
        }
        super.activate();
    }

    @Override
    public void deactivate() {
        this.cameraPosition = null;
        this.lockedRotation = null;
        this.tickCounter = 0;
        Mc class815Var = Mc.INSTANCE;
        if (class815Var.isWorldLoaded()) {
            ClientPlayerEntity player = class815Var.getPlayer();
            Rotation currentRotation = RotationManager.INSTANCE.getCurrentRotation();
            player.setYaw(currentRotation.getYaw());
            player.setPitch(currentRotation.getPitch());
        }
        super.deactivate();
    }

    public void moveCamera(Vec3d vec3d, ClientPlayerEntity clientPlayerEntity) {
        if (clientPlayerEntity == null) {
            return;
        }
        if (this.cameraPosition == null) {
            Vec3d eyePos = clientPlayerEntity.getEyePos();
            this.cameraPosition = new InterpolatedPosition(eyePos, eyePos);
        }
        this.cameraPosition.plusAssign(vec3d);
    }
}
