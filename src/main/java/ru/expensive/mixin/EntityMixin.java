package ru.expensive.mixin;

import aethereal.event.EntityInterpolationEvent;
import aethereal.event.EntityHitboxEvent;
import aethereal.event.RotationVectorEvent;
import aethereal.Expensive;
import aethereal.event.EntityInvisibilityEvent;
import aethereal.event.RenderOverlayEvent;
import aethereal.type.RenderOverlayType;
import aethereal.event.MovementYawEvent;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin({Entity.class})
public abstract class EntityMixin {

    @Shadow
    private Box field_6005;

    @ModifyExpressionValue(method = {"move"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isControlledByPlayer()Z")})
    public boolean isControlledByPlayerHook(boolean z) {
        if ((Object) this == MinecraftClient.getInstance().player) {
            return false;
        }
        return z;
    }

    @Shadow
    protected abstract Vec3d method_5631(float f, float f2);

    @ModifyReturnValue(method = {"getCameraPosVec"}, at = {@At("RETURN")})
    private Vec3d getCameraPosVec(Vec3d vec3d, float f) {
        EntityInterpolationEvent class018Var = new EntityInterpolationEvent(vec3d, (Entity) (Object) this, f);
        Expensive.INSTANCE.eventDispatcher().dispatch(class018Var);
        return class018Var.isCancelled() ? class018Var.changedVector() : vec3d;
    }

    @Inject(method = {"isGlowing"}, at = {@At("HEAD")}, cancellable = true)
    private void onIsGlowing(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        RenderOverlayEvent class252Var = new RenderOverlayEvent(RenderOverlayType.GLOWING);
        Expensive.INSTANCE.eventDispatcher().dispatch(class252Var);
        if (class252Var.isCancelled()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    @Inject(method = {"getRotationVector()Lnet/minecraft/util/math/Vec3d;"}, at = {@At("HEAD")}, cancellable = true)
    protected void getRotationVector(CallbackInfoReturnable<Vec3d> callbackInfoReturnable) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof ClientPlayerEntity) {
            RotationVectorEvent class150Var = new RotationVectorEvent(entity.getYaw(), entity.getPitch());
            Expensive.INSTANCE.eventDispatcher().dispatch(class150Var);
            callbackInfoReturnable.setReturnValue(method_5631(class150Var.getPitch(), class150Var.getYaw()));
        }
    }

    @Inject(method = {"getRotationVec"}, at = {@At("HEAD")}, cancellable = true)
    protected void getRotationVec(float f, CallbackInfoReturnable<Vec3d> callbackInfoReturnable) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof ClientPlayerEntity) {
            RotationVectorEvent class150Var = new RotationVectorEvent(entity.getYaw(), entity.getPitch());
            Expensive.INSTANCE.eventDispatcher().dispatch(class150Var);
            callbackInfoReturnable.setReturnValue(method_5631(class150Var.getPitch(), class150Var.getYaw()));
        }
    }

    @ModifyArgs(method = {"updateVelocity"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;movementInputToVelocity(Lnet/minecraft/util/math/Vec3d;FF)Lnet/minecraft/util/math/Vec3d;"))
    public void updateVelocityArgs(Args args) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof ClientPlayerEntity) {
            MovementYawEvent class257Var = new MovementYawEvent(entity.getYaw());
            Expensive.INSTANCE.eventDispatcher().dispatch(class257Var);
            args.set(2, Float.valueOf(class257Var.getYaw()));
        }
    }

    @Inject(at = {@At("RETURN")}, method = {"isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z"}, cancellable = true)
    private void onIsInvisibleTo(PlayerEntity playerEntity, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        Entity entity;
        if (callbackInfoReturnable.getReturnValueZ() && (entity = (Entity) (Object) this) != null) {
            EntityInvisibilityEvent class198Var = new EntityInvisibilityEvent(entity);
            Expensive.INSTANCE.eventDispatcher().dispatch(class198Var);
            if (class198Var.isCancelled()) {
                callbackInfoReturnable.setReturnValue(false);
            }
        }
    }

    @Inject(method = {"getBoundingBox"}, at = {@At("HEAD")}, cancellable = true)
    public final void getBoundingBox(CallbackInfoReturnable<Box> callbackInfoReturnable) {
        EntityHitboxEvent class099Var = new EntityHitboxEvent(this.field_6005, (Entity) (Object) this);
        Expensive.INSTANCE.eventDispatcher().dispatch(class099Var);
        if (class099Var.isCancelled()) {
            callbackInfoReturnable.setReturnValue(class099Var.getChangedBox());
        }
    }
}
