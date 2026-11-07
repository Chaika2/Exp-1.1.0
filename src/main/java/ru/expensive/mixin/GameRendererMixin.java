package ru.expensive.mixin;

import aethereal.math.Rotation;
import aethereal.event.AspectRatioEvent;
import aethereal.module.AspectRatioModule;
import aethereal.type.Mc;
import aethereal.event.FovEvent;
import aethereal.event.PerspectiveEvent;
import aethereal.Expensive;
import aethereal.util.RaycastUtil;
import aethereal.event.RenderOverlayEvent;
import aethereal.type.RenderOverlayType;
import aethereal.event.VisualEffectEvent;
import aethereal.type.VisualEffectType;
import aethereal.event.EntityTraceEvent;
import aethereal.util.RotationManager;
import aethereal.module.FreeCameraModule;
import aethereal.module.ShaderHandModule;
import aethereal.module.ThemesModule;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({GameRenderer.class})
public abstract class GameRendererMixin {

    @Shadow
    @Final
    MinecraftClient field_4015;

    @Shadow
    private float field_4005;

    @Shadow
    private float field_3988;

    @Shadow
    private float field_4004;

    @Shadow
    public abstract float method_32796();

    @ModifyExpressionValue(method = {"renderWorld"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getPerspective()Lnet/minecraft/client/option/Perspective;")})
    private Perspective hookPerspectiveEventOnCamera(Perspective perspective) {
        PerspectiveEvent class160Var = new PerspectiveEvent(perspective);
        Expensive.INSTANCE.eventDispatcher().dispatch(class160Var);
        return class160Var.perspective();
    }

    @ModifyExpressionValue(method = {"renderHand"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getPerspective()Lnet/minecraft/client/option/Perspective;")})
    private Perspective hookPerspectiveEventOnHand(Perspective perspective) {
        PerspectiveEvent class160Var = new PerspectiveEvent(perspective);
        Expensive.INSTANCE.eventDispatcher().dispatch(class160Var);
        return class160Var.perspective();
    }

    @Inject(method = {"renderHand"}, at = {@At("HEAD")})
    private void hookShaderHandBegin(Camera camera, float f, Matrix4f matrix4f, CallbackInfo callbackInfo) {
        if (Expensive.INSTANCE.moduleRepository() == null) {
            return;
        }
        ShaderHandModule class634Var = (ShaderHandModule) Expensive.INSTANCE.moduleRepository().find(ShaderHandModule.class).orElse(null);
        if (class634Var != null && class634Var.isState() && class634Var.shouldCapture()) {
            class634Var.beginHandCapture();
        }
    }

    @Inject(method = {"renderHand"}, at = {@At("RETURN")})
    private void hookShaderHandEnd(Camera camera, float f, Matrix4f matrix4f, CallbackInfo callbackInfo) {
        if (Expensive.INSTANCE.moduleRepository() == null) {
            return;
        }
        ShaderHandModule class634Var = (ShaderHandModule) Expensive.INSTANCE.moduleRepository().find(ShaderHandModule.class).orElse(null);
        if (class634Var != null && class634Var.isState() && class634Var.shouldCapture()) {
            class634Var.endHandCaptureAndComposite();
        }
    }

    @Inject(method = {"tiltViewWhenHurt"}, at = {@At("HEAD")}, cancellable = true)
    public void tiltViewWhenHurt(MatrixStack matrixStack, float f, CallbackInfo callbackInfo) {
        RenderOverlayEvent class252Var = new RenderOverlayEvent(RenderOverlayType.CAMERA_HURT);
        Expensive.INSTANCE.eventDispatcher().dispatch(class252Var);
        if (class252Var.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @ModifyExpressionValue(method = {"renderWorld"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F")})
    private float hookNausea(float f) {
        VisualEffectEvent class258Var = new VisualEffectEvent(VisualEffectType.NAUSEA);
        Expensive.INSTANCE.eventDispatcher().dispatch(class258Var);
        if (class258Var.isCancelled()) {
            return 0.0f;
        }
        return f;
    }

    @ModifyExpressionValue(method = {"getFov"}, at = {@At(value = "INVOKE", target = "Ljava/lang/Integer;intValue()I", remap = false)})
    private int hookGetFov(int i) {
        FovEvent class146Var = new FovEvent();
        Expensive.INSTANCE.eventDispatcher().dispatch(class146Var);
        return class146Var.isCancelled() ? class146Var.getFov() : i;
    }

    @ModifyExpressionValue(method = {"findCrosshairTarget(Lnet/minecraft/entity/Entity;DDF)Lnet/minecraft/util/hit/HitResult;"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;")})
    @Nullable
    private EntityHitResult hookEntityHitResult(@Nullable EntityHitResult entityHitResult) {
        EntityTraceEvent class342Var = new EntityTraceEvent();
        Expensive.INSTANCE.eventDispatcher().dispatch(class342Var);
        if (class342Var.isCancelled()) {
            return null;
        }
        return entityHitResult;
    }

    @ModifyExpressionValue(method = {"findCrosshairTarget(Lnet/minecraft/entity/Entity;DDF)Lnet/minecraft/util/hit/HitResult;"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;raycast(DFZ)Lnet/minecraft/util/hit/HitResult;")})
    private HitResult hookRaycast(HitResult hitResult, Entity entity, double d, double d2, float f) {
        Rotation serverRotation;
        if (entity != MinecraftClient.getInstance().player) {
            return hitResult;
        }
        Rotation class007Var = new Rotation(entity.getYaw(f), entity.getPitch(f));
        RotationManager class398Var = RotationManager.INSTANCE;
        if (class398Var.getCurrentRotation() != null) {
            serverRotation = class398Var.getCurrentRotation();
        } else {
            serverRotation = (Expensive.INSTANCE.moduleRepository() == null || !((FreeCameraModule) Expensive.INSTANCE.moduleRepository().get(FreeCameraModule.class)).isState()) ? class007Var : RotationManager.INSTANCE.getServerRotation();
        }
        return RaycastUtil.raycast(Math.max(d, d2), serverRotation, false);
    }

    @ModifyExpressionValue(method = {"findCrosshairTarget(Lnet/minecraft/entity/Entity;DDF)Lnet/minecraft/util/hit/HitResult;"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getRotationVec(F)Lnet/minecraft/util/math/Vec3d;")})
    private Vec3d hookRotationVector(Vec3d vec3d, Entity entity, double d, double d2, float f) {
        Rotation currentRotation;
        if (entity == MinecraftClient.getInstance().player && (currentRotation = RotationManager.INSTANCE.getCurrentRotation()) != null) {
            return currentRotation.getDirectionVector();
        }
        return vec3d;
    }

    @Inject(method = {"renderBlur"}, at = {@At("HEAD")}, cancellable = true)
    public void renderBlur(CallbackInfo callbackInfo) {
        if (Expensive.INSTANCE.moduleRepository() == null) {
            return;
        }
        ThemesModule themesModule = (ThemesModule) Expensive.INSTANCE.moduleRepository().find(ThemesModule.class).orElse(null);
        if (themesModule != null && !themesModule.vanillaBackgroundBlur.isValue()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = {"getBasicProjectionMatrix"}, at = {@At("HEAD")}, cancellable = true)
    public void getBasicProjectionMatrix(float f, CallbackInfoReturnable<Matrix4f> callbackInfoReturnable) {
        if (!((AspectRatioModule) Expensive.INSTANCE.moduleRepository().get(AspectRatioModule.class)).isState() || !Mc.INSTANCE.isWorldLoaded()) {
            return;
        }
        MatrixStack matrixStack = new MatrixStack();
        Window window = this.field_4015.getWindow();
        AspectRatioEvent class109Var = new AspectRatioEvent((float) window.getFramebufferWidth() / (float) window.getFramebufferHeight());
        Expensive.INSTANCE.eventDispatcher().dispatch(class109Var);
        matrixStack.peek().getPositionMatrix().identity();
        if (this.field_4005 != 1.0f) {
            matrixStack.translate(this.field_3988, -this.field_4004, 0.0f);
            matrixStack.scale(this.field_4005, this.field_4005, 1.0f);
        }
        matrixStack.peek().getPositionMatrix().mul(new Matrix4f().setPerspective((float) (((double) f) * 0.01745329238474369d), class109Var.getAspectRatio(), 0.05f, method_32796()));
        callbackInfoReturnable.setReturnValue(matrixStack.peek().getPositionMatrix());
    }
}
