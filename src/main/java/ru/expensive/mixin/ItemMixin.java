package ru.expensive.mixin;

import aethereal.math.Rotation;
import aethereal.util.RotationManager;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({Item.class})
public abstract class ItemMixin {
    @ModifyExpressionValue(method = {"raycast"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getRotationVector(FF)Lnet/minecraft/util/math/Vec3d;")})
    private static Vec3d hookFixRotation(Vec3d vec3d, World world, PlayerEntity playerEntity, RaycastContext.FluidHandling fluidHandling) {
        Rotation currentRotation;
        return (playerEntity != MinecraftClient.getInstance().player || (currentRotation = RotationManager.INSTANCE.getCurrentRotation()) == null) ? vec3d : currentRotation.getDirectionVector();
    }
}
