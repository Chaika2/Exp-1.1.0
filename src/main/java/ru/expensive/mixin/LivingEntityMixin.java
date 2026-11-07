package ru.expensive.mixin;

import aethereal.math.Rotation;
import aethereal.event.StatusEffectEvent;
import aethereal.event.HandSwingEvent;
import aethereal.Expensive;
import aethereal.event.GlideStateEvent;
import aethereal.event.PushEvent;
import aethereal.type.PushType;
import aethereal.event.JumpEvent;
import aethereal.module.WaterFixModule;
import aethereal.util.RotationManager;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class})
public abstract class LivingEntityMixin {

    @Unique
    private JumpEvent lastJumpEvent;

    @Shadow
    public float field_6283;

    @Shadow
    public abstract void method_5728(boolean z);

    @Shadow
    protected abstract float method_53964();

    @Inject(method = {"jump"}, at = {@At("HEAD")})
    private void jump(CallbackInfo callbackInfo) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (livingEntity instanceof ClientPlayerEntity) {
            JumpEvent class237Var = new JumpEvent(livingEntity.getYaw());
            Expensive.INSTANCE.eventDispatcher().dispatch(class237Var);
            this.lastJumpEvent = class237Var;
        }
    }

    @Inject(method = {"getHandSwingDuration"}, at = {@At("HEAD")}, cancellable = true)
    private void getHandSwingDuration(CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        HandSwingEvent class087Var = new HandSwingEvent();
        Expensive.INSTANCE.eventDispatcher().dispatch(class087Var);
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (class087Var.isCancelled() && (livingEntity instanceof ClientPlayerEntity)) {
            callbackInfoReturnable.setReturnValue(Integer.valueOf((int) class087Var.swingSpeed()));
        }
    }

    @Inject(method = {"isGliding"}, at = {@At("RETURN")})
    public void onTickGliding(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (((LivingEntity) (Object) this) instanceof ClientPlayerEntity) {
            Expensive.INSTANCE.eventDispatcher().dispatch(new GlideStateEvent(((Boolean) callbackInfoReturnable.getReturnValue()).booleanValue()));
        }
    }

    @Inject(method = {"isPushable"}, at = {@At("HEAD")}, cancellable = true)
    public void isPushable(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        PushEvent class231Var = new PushEvent(PushType.PLAYERS);
        Expensive.INSTANCE.eventDispatcher().dispatch(class231Var);
        if ((((LivingEntity) (Object) this) instanceof PlayerEntity) && class231Var.isCancelled()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    @ModifyExpressionValue(method = {"calcGlidingVelocity"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getPitch()F")})
    private float hookModifyFallFlyingPitch(float f) {
        if (!(((LivingEntity) (Object) this) instanceof ClientPlayerEntity)) {
            return f;
        }
        Rotation currentRotation = RotationManager.INSTANCE.getCurrentRotation();
        return (currentRotation == null || RotationManager.INSTANCE.getCurrentStrategy() == null || !RotationManager.INSTANCE.getCurrentStrategy().moveCorrection()) ? f : currentRotation.getPitch();
    }

    @ModifyExpressionValue(method = {"calcGlidingVelocity"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;")})
    private Vec3d hookModifyFallFlyingRotationVector(Vec3d vec3d) {
        if (!(((LivingEntity) (Object) this) instanceof ClientPlayerEntity)) {
            return vec3d;
        }
        Rotation currentRotation = RotationManager.INSTANCE.getCurrentRotation();
        return (currentRotation == null || RotationManager.INSTANCE.getCurrentStrategy() == null || !RotationManager.INSTANCE.getCurrentStrategy().moveCorrection()) ? vec3d : currentRotation.getDirectionVector();
    }

    @ModifyExpressionValue(method = {"jump"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F")})
    private float getYaw(float f) {
        return (this.lastJumpEvent == null || !(((LivingEntity) (Object) this) instanceof ClientPlayerEntity)) ? f : this.lastJumpEvent.getYaw();
    }

    @Inject(method = {"hasStatusEffect"}, at = {@At("HEAD")}, cancellable = true)
    public void hasStatusEffect(RegistryEntry<StatusEffect> registryEntry, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        StatusEffectEvent class046Var = new StatusEffectEvent((StatusEffect) registryEntry.value());
        Expensive.INSTANCE.eventDispatcher().dispatch(class046Var);
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (class046Var.isCancelled() && (livingEntity instanceof ClientPlayerEntity)) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    @Inject(method = {"turnHead"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F")}, cancellable = true)
    private void turnHeadHook(float f, float f2, CallbackInfoReturnable<Float> callbackInfoReturnable) {
        if ((Object) this != MinecraftClient.getInstance().player) {
            return;
        }
        this.field_6283 += MathHelper.wrapDegrees(f - this.field_6283) * 0.3f;
        float fWrapDegrees = MathHelper.wrapDegrees(RotationManager.INSTANCE.getCurrentRotation().getYaw() - this.field_6283);
        float fMethod_53964 = method_53964();
        if (Math.abs(fWrapDegrees) > fMethod_53964) {
            this.field_6283 += fWrapDegrees - (MathHelper.sign(fWrapDegrees) * fMethod_53964);
        }
        if (fWrapDegrees < -90.0f || fWrapDegrees >= 90.0f) {
            f2 *= -1.0f;
        }
        callbackInfoReturnable.setReturnValue(Float.valueOf(f2));
    }

    /**
     * Подмена WATER_MOVEMENT_EFFICIENCY только внутри расчёта движения в воде.
     *
     * travelInFluid читает этот атрибут один раз в водной ветке:
     *     h = WATER_MOVEMENT_EFFICIENCY; if (!onGround) h *= 0.5;
     * Depth Strider даёт до 1.0, и в воде это почти двукратная скорость.
     * Серверный античит mlegacy.ru её не моделирует и пишет
     * "FastMath ... неверное движение" - проверено: без ботинок с Depth Strider
     * предупреждения нет, со щитом (замедление) тоже нет.
     *
     * Возвращая 0 локальному игроку, мы сохраняем полностью ванильную физику
     * (скорость как без ботинок), но остаёмся внутри серверного предсказания.
     * На сушу и воздух подмена не влияет: атрибут читается только здесь.
     */
    @ModifyExpressionValue(method = {"travelInFluid"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getAttributeValue(Lnet/minecraft/registry/entry/RegistryEntry;)D")})
    private double waterFix$ignoreDepthStrider(double original) {
        if ((Object) this != MinecraftClient.getInstance().player) {
            return original;
        }
        // Всегда обнуляем WATER_MOVEMENT_EFFICIENCY для локального игрока в воде.
        // mlegacy.ru не учитывает Depth Strider при расчёте скорости в воде —
        // при DS II чаще всего ловится "FastMath ... неверное движение".
        // DS III почему-то проходит, но лучше обнулять всегда.
        WaterFixModule.lastWaterEfficiency = 0.0d;
        return 0.0d;
    }
}
