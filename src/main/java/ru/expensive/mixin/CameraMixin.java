package ru.expensive.mixin;

import aethereal.math.Rotation;
import aethereal.event.CameraDistanceEvent;
import aethereal.event.CameraRotationEvent;
import aethereal.Expensive;
import aethereal.event.CameraClipEvent;
import aethereal.event.EntityRenderEvent3;
import aethereal.util.RotationManager;
import aethereal.math.ScheduledRotation;
import aethereal.type.Mc;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Camera.class})
public abstract class CameraMixin {

    @Shadow
    @Final
    private BlockPos.Mutable field_18713;

    @Shadow
    private float field_18718;

    @Shadow
    private float field_18717;

    @Shadow
    private boolean field_18719;

    @Unique
    private float pitchAnim;

    @Shadow
    public void method_19325(float f, float f2) {
    }

    @Shadow
    protected void method_19324(float f, float f2, float f3) {
    }

    @Shadow
    protected abstract float method_19318(float f);

    @Inject(method = {"update"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V", shift = At.Shift.AFTER)}, cancellable = true)
    private void updateHook(BlockView blockView, Entity entity, boolean z, boolean z2, float f, CallbackInfo callbackInfo) {
        CameraDistanceEvent class140Var = new CameraDistanceEvent(z2 ? -this.field_18717 : this.field_18717);
        Expensive.INSTANCE.eventDispatcher().dispatch(class140Var);
        if (class140Var.isCancelled() && (entity instanceof ClientPlayerEntity)) {
            method_19325(this.field_18718 + (180.0f * class140Var.getFrontAnim().smoothAnimation()), class140Var.getPitch());
            method_19324(-method_19318(class140Var.getDistance()), 0.0f, 0.0f);
            if (((LivingEntity) entity).isSleeping() && !z) {
                Direction sleepingDirection = ((LivingEntity) entity).getSleepingDirection();
                method_19325(sleepingDirection != null ? sleepingDirection.getPositiveHorizontalDegrees() - 180.0f : 0.0f, 0.0f);
                method_19324(0.0f, 0.3f, 0.0f);
            }
            callbackInfo.cancel();
        }
    }

    @Redirect(method = {"update"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V", ordinal = 1))
    private void update(Camera camera, float f, float f2) {
        CameraRotationEvent class152Var = new CameraRotationEvent(f, f2);
        Expensive.INSTANCE.eventDispatcher().dispatch(class152Var);
        RotationManager class398Var = RotationManager.INSTANCE;
        ScheduledRotation currentStrategy = class398Var.getCurrentStrategy();
        if (currentStrategy == null || !currentStrategy.clientRotation()) {
            if (class152Var.isCancelled()) {
                camera.setRotation(class152Var.getYaw(), class152Var.getPitch());
                return;
            } else {
                camera.setRotation(f, f2);
                return;
            }
        }
        Rotation previousRotation = class398Var.getPreviousRotation();
        Rotation currentRotation = class398Var.getCurrentRotation();
        if (previousRotation != null && currentRotation != null) {
            float tickDelta = Mc.INSTANCE.getTickDelta();
            camera.setRotation(MathHelper.lerp(tickDelta, previousRotation.getYaw(), currentRotation.getYaw()), MathHelper.lerp(tickDelta, previousRotation.getPitch(), currentRotation.getPitch()));
        } else if (class152Var.isCancelled()) {
            camera.setRotation(class152Var.getYaw(), class152Var.getPitch());
        } else {
            camera.setRotation(f, f2);
        }
    }

    @Inject(method = {"update"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V", shift = At.Shift.AFTER)})
    private void update(BlockView blockView, Entity entity, boolean z, boolean z2, float f, CallbackInfo callbackInfo) {
        Expensive.INSTANCE.eventDispatcher().dispatch(new EntityRenderEvent3(entity, f));
    }

    @Inject(method = {"clipToSpace"}, at = {@At("HEAD")}, cancellable = true)
    private void clipToSpace(float f, CallbackInfoReturnable<Float> callbackInfoReturnable) {
        CameraClipEvent class209Var = new CameraClipEvent();
        Expensive.INSTANCE.eventDispatcher().dispatch(class209Var);
        if (class209Var.isCancelled()) {
            callbackInfoReturnable.setReturnValue(Float.valueOf(f));
            callbackInfoReturnable.cancel();
        }
    }
}
