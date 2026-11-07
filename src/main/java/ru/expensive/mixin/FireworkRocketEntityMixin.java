package ru.expensive.mixin;

import aethereal.Expensive;
import aethereal.event.TravelEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({FireworkRocketEntity.class})
public class FireworkRocketEntityMixin {

    @Shadow
    @Nullable
    private LivingEntity field_7616;

    @WrapOperation(method = {"tick"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getVelocity()Lnet/minecraft/util/math/Vec3d;", ordinal = 0)})
    public Vec3d tick(LivingEntity livingEntity, Operation<Vec3d> operation) {
        if (!(this.field_7616 instanceof ClientPlayerEntity)) {
            return (Vec3d) operation.call(new Object[]{livingEntity});
        }
        TravelEvent class324Var = new TravelEvent((Vec3d) operation.call(new Object[]{livingEntity}));
        Expensive.INSTANCE.eventDispatcher().dispatch(class324Var);
        return class324Var.vec3d();
    }
}
