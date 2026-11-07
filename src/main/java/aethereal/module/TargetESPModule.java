package aethereal.module;
import aethereal.render.AnimationStack2;
import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.ColorSetting;
import aethereal.render.ColorStack;
import aethereal.util.ColorUtil;
import aethereal.math.DeltaTimeTracker;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.Lang;
import aethereal.util.MathUtil;
import aethereal.type.Mc;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.NumberSetting;
import aethereal.event.PlayerTickEvent;
import aethereal.render.ShapeRenderer;
import aethereal.type.TargetEspMode;
import aethereal.render.ToggleAnimator;
import aethereal.util.WeightedEngine;
import aethereal.event.WorldRenderEvent;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class TargetESPModule extends Module {
    public static final Identifier targetTexture = Identifier.of("expensive", "textures/target.png");
    public static final Identifier bloomTexture = Identifier.of("expensive", "textures/bloom.png");
    public final NumberSetting rotationSpeed;
    public ModeSetting<TargetEspMode> mode;
    public final BooleanSetting blending;
    public final ColorSetting color;
    public final DeltaTimeTracker deltaTimeTracker;
    public final AnimationStack2 animationStack;
    public final ToggleAnimator toggleAnimator;
    public LivingEntity target;
    public float rotation;
    public float prevRotation;
    public float spinVelocity;
    public boolean spinDecreasing;

    public TargetESPModule() {
        super(ModuleTab.RENDER, "Target ESP");
        this.rotationSpeed = new NumberSetting(Lang.TARGETESP_ROTATION_SPEED).range(0.4f, 2.0f).currentValue(1.0f).step(0.1f);
        this.mode = new ModeSetting(Lang.TARGETESP_MODE).values(TargetEspMode.class);
        this.blending = new BooleanSetting(Lang.TARGETESP_BLENDING).setValue(true);
        this.color = new ColorSetting(Lang.TARGETESP_COLOR);
        this.deltaTimeTracker = new DeltaTimeTracker();
        this.animationStack = new AnimationStack2();
        this.toggleAnimator = new ToggleAnimator(700, Easings.EASE_IN_OUT_CUBIC);
        this.rotation = 1.0f;
        this.spinVelocity = 1.0f;
        addSettings(this.mode, this.blending, this.color, this.rotationSpeed);
        register(PlayerTickEvent.class, class130Var -> {
            if (Mc.INSTANCE.isWorldLoaded() && class130Var.isPre()) {
                LivingEntity livingEntityTarget = ((AttackAuraModule) Expensive.INSTANCE.moduleRepository().get(AttackAuraModule.class)).target();
                if (livingEntityTarget != null && livingEntityTarget.getHealth() > 0.0f) {
                    this.target = livingEntityTarget;
                }
                if (this.toggleAnimator.smoothAnimation() <= 0.0d && (livingEntityTarget == null || livingEntityTarget.getHealth() <= 0.0f)) {
                    this.target = null;
                }
                this.toggleAnimator.state(isState() && livingEntityTarget != null && livingEntityTarget.getHealth() > 0.0f);
                WeightedEngine class141Var = new WeightedEngine(this.deltaTimeTracker.elapsedUnit(), this.animationStack);
                this.animationStack.begin();
                this.toggleAnimator.animate(class141Var);
                this.animationStack.end();
                this.prevRotation = this.rotation;
                this.rotation += this.spinVelocity * this.rotationSpeed.currentValue();
                if (this.spinVelocity > 25.0f) {
                    this.spinDecreasing = true;
                } else if (this.spinVelocity < -25.0f) {
                    this.spinDecreasing = false;
                }
                this.spinVelocity = this.spinDecreasing ? this.spinVelocity - 0.5f : this.spinVelocity + 0.5f;
            }
        });
        register(WorldRenderEvent.class, class016Var -> {
            if (Mc.INSTANCE.isWorldLoaded() && this.target != null) {
                Mc class815Var = Mc.INSTANCE;
                ColorStack class115VarColorStack = Expensive.INSTANCE.drawEngine().colorStack();
                Camera camera = class815Var.getCamera();
                Vec3d pos = camera.getPos();
                MatrixStack matrixStack = class016Var.matrixStack();
                float tickDelta = class815Var.getTickDelta();
                double dInterpolate = MathUtil.interpolate(this.target.prevX, this.target.getX(), tickDelta) - pos.getX();
                double dInterpolate2 = MathUtil.interpolate(this.target.prevY, this.target.getY(), tickDelta) - pos.getY();
                double dInterpolate3 = MathUtil.interpolate(this.target.prevZ, this.target.getZ(), tickDelta) - pos.getZ();
                int color = this.color.getColor();
                int iAlpha = (int) (ColorUtil.alpha(color) * this.toggleAnimator.smoothAnimation());
                float f = this.target.hurtTime / 10.0f;
                if (f > 0.0f) {
                    color = class115VarColorStack.interpolate(color, -65536, f);
                }
                int iComputeColor = class115VarColorStack.computeColor(color, iAlpha);
                double dCurrentTimeMillis = (System.currentTimeMillis() / 50.0d) * ((double) this.rotationSpeed.currentValue());
                float height = (this.target.getHeight() / 2.0f) + 0.1f;
                float width = this.target.getWidth();
                switch ((TargetEspMode) this.mode.currentValue()) {
                    case CUBE:
                        float fInterpolate = MathUtil.interpolate(this.prevRotation, this.rotation, tickDelta);
                        RenderSystem.disableDepthTest();
                        RenderSystem.disableCull();
                        matrixStack.push();
                        matrixStack.translate(dInterpolate, dInterpolate2 + ((double) (this.target.getEyeHeight(this.target.getPose()) / 2.0f)) + 0.1d, dInterpolate3);
                        matrixStack.multiply(camera.getRotation());
                        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(fInterpolate));
                        matrixStack.scale(1.25f, 1.25f, 1.25f);
                        ShapeRenderer.INSTANCE.addTexture(matrixStack.peek().getPositionMatrix(), -0.5f, -0.5f, 1.0f, 1.0f, targetTexture, iComputeColor, true, this.blending.isValue());
                        matrixStack.pop();
                        RenderSystem.enableCull();
                        RenderSystem.enableDepthTest();
                        break;
                    case GHOSTS:
                        RenderSystem.disableDepthTest();
                        RenderSystem.disableCull();
                        for (int i = 0; i < 3; i++) {
                            for (int i2 = 0; i2 <= 10; i2++) {
                                double radians = Math.toRadians((((((double) (i2 / 2.0f)) + (dCurrentTimeMillis * 3.0d)) * ((double) 10)) + ((double) (i * 120))) % ((double) (10 * 360)));
                                double dSin = Math.sin(Math.toRadians((dCurrentTimeMillis * 5.0d) + ((double) (i2 * (i + height)))) * 2.0d) / 2.0d;
                                double dCos = dInterpolate + (Math.cos(radians) * ((double) width));
                                double d = dInterpolate2 + ((double) height) + dSin;
                                double dSin2 = dInterpolate3 + (Math.sin(radians) * ((double) width));
                                matrixStack.push();
                                matrixStack.translate(dCos, d, dSin2);
                                matrixStack.multiply(camera.getRotation());
                                float f2 = 0.5f * ((i2 + 10) / (10 + 10));
                                ShapeRenderer.INSTANCE.addTexture(matrixStack.peek().getPositionMatrix(), (-f2) / 2.0f, (-f2) / 2.0f, f2, f2, bloomTexture, iComputeColor, true, true);
                                matrixStack.pop();
                            }
                        }
                        RenderSystem.enableCull();
                        RenderSystem.enableDepthTest();
                        break;
                }
            }
        });
    }
}
