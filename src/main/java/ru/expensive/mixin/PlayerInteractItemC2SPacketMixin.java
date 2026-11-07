package ru.expensive.mixin;

import aethereal.math.Rotation;
import aethereal.util.RotationManager;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PlayerInteractItemC2SPacket.class})
public class PlayerInteractItemC2SPacketMixin {

    @Mutable
    @Shadow
    @Final
    private float field_51930;

    @Mutable
    @Shadow
    @Final
    private float field_51931;

    @Inject(method = {"<init>(Lnet/minecraft/util/Hand;IFF)V"}, at = {@At("RETURN")})
    private void modifyRotation(Hand hand, int i, float f, float f2, CallbackInfo callbackInfo) {
        Rotation currentRotation = RotationManager.INSTANCE.getCurrentRotation();
        if (currentRotation == null) {
            return;
        }
        this.field_51930 = currentRotation.getYaw();
        this.field_51931 = currentRotation.getPitch();
    }
}
