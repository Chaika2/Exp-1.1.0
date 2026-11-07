package aethereal.module;
import aethereal.Expensive;
import aethereal.event.MovementInputEvent;
import aethereal.event.PlayerTickEvent;
import aethereal.event.WorldRenderEvent;
import aethereal.math.DirectionalInput;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;
import aethereal.render.ShapeRenderer;
import aethereal.type.EventPriority;
import aethereal.type.Mc;
import aethereal.type.SettingUnit;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.setting.NumberSetting;
import aethereal.util.FriendManager;
import aethereal.util.MathUtil;
import aethereal.util.MovementInputHelper;

import java.util.Comparator;
import org.joml.Matrix4f;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class TargetStrafeModule extends Module {
    public enum StrafeDirection implements DisplayNamed {
        RIGHT(Translation.clearText("Право")),
        LEFT(Translation.clearText("Лево")),
        AUTO(Translation.clearText("Авто"));

        public final Translation displayName;

        @Override
        public Translation getDisplayName() {
            return this.displayName;
        }

        public Translation displayName() {
            return this.displayName;
        }

        StrafeDirection(Translation class254Var) {
            this.displayName = class254Var;
        }
    }

    public enum StrafeDrive implements DisplayNamed {
        KEYS(Translation.clearText("Клавиши (Легит)")),
        VELOCITY(Translation.clearText("Скорость (HVH)"));

        public final Translation displayName;

        @Override
        public Translation getDisplayName() {
            return this.displayName;
        }

        public Translation displayName() {
            return this.displayName;
        }

        StrafeDrive(Translation class254Var) {
            this.displayName = class254Var;
        }
    }

    public final ModeSetting directionSetting;
    public final ModeSetting<StrafeDrive> driveSetting;
    public final NumberSetting radiusSetting;
    public final NumberSetting speedSetting;
    public final BooleanSetting autoJump;
    public final BooleanSetting useAuraTarget;
    public final BooleanSetting renderCircle;
    public final Mc mc;
    public int autoSign;
    public LivingEntity currentTarget;
    public float lastBearing;

    public TargetStrafeModule() {
        super(ModuleTab.MOVEMENT, "Target Strafe");
        this.directionSetting = new ModeSetting(Translation.clearText("Направление")).values(StrafeDirection.class).select(StrafeDirection.AUTO);
        this.driveSetting = new ModeSetting(Translation.clearText("Привод")).values(StrafeDrive.class).select(StrafeDrive.KEYS);
        this.radiusSetting = new NumberSetting(Translation.clearText("Радиус")).currentValue(2.7f).range(1.0f, 5.0f).step(0.1f).unit(SettingUnit.BLOCKS);
        this.speedSetting = new NumberSetting(Translation.clearText("Скорость")).currentValue(0.32f).range(0.1f, 0.6f).step(0.01f).unit(SettingUnit.BLOCKS);
        this.autoJump = new BooleanSetting(Translation.clearText("Авто-прыжок"));
        this.useAuraTarget = new BooleanSetting(Translation.clearText("Цель Kill Aura"));
        this.renderCircle = new BooleanSetting(Translation.clearText("Рисовать круг"));
        this.mc = Mc.INSTANCE;
        this.autoSign = 1;
        this.currentTarget = null;
        this.lastBearing = 0.0f;
        addSettings(this.directionSetting, this.driveSetting, this.radiusSetting, this.speedSetting, this.autoJump, this.useAuraTarget, this.renderCircle);
        register(MovementInputEvent.class, class040Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                ClientPlayerEntity player = this.mc.getPlayer();
                LivingEntity target = findTarget();
                if (player == null || target == null) {
                    return;
                }
                Vec3d vec3dSubtract = target.getPos().subtract(player.getPos());
                double dDist = Math.hypot(vec3dSubtract.getX(), vec3dSubtract.getZ());
                if (dDist <= 0.01d) {
                    return;
                }
                this.lastBearing = computeBearing(player, vec3dSubtract, dDist);
                Vec3d vec3dTangent = new Vec3d(-Math.sin(Math.toRadians(this.lastBearing)), 0.0d, Math.cos(Math.toRadians(this.lastBearing)));
                class040Var.setInput(MovementInputHelper.getDirectionalInputForDegrees(DirectionalInput.NONE, MovementInputHelper.getDegreesRelativeToView(vec3dTangent, player.getYaw())));
            }
        }, EventPriority.HIGH);
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && this.mc.isWorldLoaded() && class130Var.isPre()) {
                ClientPlayerEntity player = this.mc.getPlayer();
                LivingEntity target = findTarget();
                if (player == null || target == null) {
                    this.currentTarget = null;
                    return;
                }
                this.currentTarget = target;
                Vec3d vec3dSubtract = target.getPos().subtract(player.getPos());
                double dDist = Math.hypot(vec3dSubtract.getX(), vec3dSubtract.getZ());
                if (dDist <= 0.01d) {
                    return;
                }
                float fBearing = computeBearing(player, vec3dSubtract, dDist);
                if (this.driveSetting.isSelected(StrafeDrive.VELOCITY)) {
                    double dRadians = Math.toRadians(fBearing);
                    double dSpeed = this.speedSetting.currentValue();
                    player.setVelocity(new Vec3d((-Math.sin(dRadians)) * dSpeed, player.getVelocity().getY(), Math.cos(dRadians) * dSpeed));
                    if (this.autoJump.isValue() && player.isOnGround()) {
                        player.jump();
                    }
                }
            }
        }, EventPriority.HIGH);
        register(WorldRenderEvent.class, class016Var -> {
            if (isState() && this.renderCircle.isValue() && this.currentTarget != null) {
                Matrix4f positionMatrix = class016Var.matrixStack().peek().getPositionMatrix();
                Vec3d vec3dCenter = MathUtil.interpolate(this.currentTarget).add(0.0d, 0.1d, 0.0d);
                float fRadius = this.radiusSetting.currentValue() + 0.3f;
                int iColor = 210588671;
                for (int i = 0; i < 32; i++) {
                    float f1 = (i / 32.0f) * 6.2831855f;
                    float f2 = ((i + 1) / 32.0f) * 6.2831855f;
                    Vec3d vec3dFirst = vec3dCenter.add(Math.cos(f1) * fRadius, 0.0d, Math.sin(f1) * fRadius);
                    Vec3d vec3dSecond = vec3dCenter.add(Math.cos(f2) * fRadius, 0.0d, Math.sin(f2) * fRadius);
                    ShapeRenderer.INSTANCE.addLine(positionMatrix, vec3dFirst, vec3dSecond, iColor, 1.6f);
                }
            }
        });
    }

    public LivingEntity findTarget() {
        ClientPlayerEntity player = this.mc.getPlayer();
        if (player == null || this.mc.getWorld() == null) {
            return null;
        }
        if (this.useAuraTarget.isValue()) {
            AttackAuraModule class878Var = Expensive.INSTANCE.moduleRepository().get(AttackAuraModule.class);
            if (class878Var.isState() && class878Var.target() != null && class878Var.target().isAlive()) {
                return class878Var.target();
            }
        }
        double dRange = this.radiusSetting.currentValue() + 3.0d;
        return this.mc.getWorld().getNonSpectatingEntities(PlayerEntity.class, player.getBoundingBox().expand(dRange)).stream().filter(p -> p != player && p.isAlive() && !FriendManager.isFriend(p.getName().getString())).min(Comparator.comparingDouble(p -> p.squaredDistanceTo(player))).orElse(null);
    }

    public float computeBearing(ClientPlayerEntity player, Vec3d delta, double dist) {
        float fSign = this.directionSetting.isSelected(StrafeDirection.AUTO) ? (float) this.autoSign : (this.directionSetting.isSelected(StrafeDirection.RIGHT) ? 1.0f : -1.0f);
        float fBearing = (float) Math.toDegrees(Math.atan2(-delta.getX(), delta.getZ()));
        float fRadius = this.radiusSetting.currentValue();
        float fYaw;
        if (dist > fRadius + 0.5f) {
            fYaw = fBearing;
        } else {
            fYaw = fBearing + 90.0f * fSign;
        }
        if (this.directionSetting.isSelected(StrafeDirection.AUTO) && dist <= fRadius + 0.5f) {
            Vec3d vec3dFrom = player.getEyePos();
            double dRadians = Math.toRadians(fYaw);
            Vec3d vec3dTangent = new Vec3d(-Math.sin(dRadians), 0.0d, Math.cos(dRadians));
            HitResult class304Var = this.mc.getWorld().raycast(new RaycastContext(vec3dFrom, vec3dFrom.add(vec3dTangent.multiply(0.8d)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
            if (class304Var.getType() != HitResult.Type.MISS) {
                this.autoSign = -this.autoSign;
                fYaw = fBearing + 90.0f * this.autoSign;
            }
        }
        return fYaw;
    }

    @Override
    public void deactivate() {
        this.currentTarget = null;
        super.deactivate();
    }
}