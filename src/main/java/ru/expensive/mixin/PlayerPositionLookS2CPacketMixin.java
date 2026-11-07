package ru.expensive.mixin;

import aethereal.math.Rotation;
import aethereal.event.ServerRotationEvent;
import aethereal.Expensive;
import aethereal.type.Mc;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({PlayerPositionLookS2CPacket.class})
public class PlayerPositionLookS2CPacketMixin {
    @WrapOperation(method = {"apply(Lnet/minecraft/network/listener/ClientPlayPacketListener;)V"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/network/listener/ClientPlayPacketListener;onPlayerPositionLook(Lnet/minecraft/network/packet/s2c/play/PlayerPositionLookS2CPacket;)V")})
    public void applyHook(ClientPlayPacketListener clientPlayPacketListener, PlayerPositionLookS2CPacket playerPositionLookS2CPacket, Operation<Void> operation) {
        Rotation class007VarPlayerRotation = Rotation.playerRotation();
        operation.call(new Object[]{clientPlayPacketListener, playerPositionLookS2CPacket});
        ServerRotationEvent class079Var = new ServerRotationEvent();
        Expensive.INSTANCE.eventDispatcher().dispatch(class079Var);
        if (class079Var.isCancelled() && Mc.INSTANCE.isWorldLoaded()) {
            Rotation class007VarRandom = class007VarPlayerRotation.random(0.001f);
            Mc.INSTANCE.getPlayer().setYaw(class007VarRandom.getYaw());
            Mc.INSTANCE.getPlayer().setPitch(class007VarRandom.getPitch());
        }
    }
}
