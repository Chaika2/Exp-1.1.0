package ru.expensive.mixin;

import aethereal.event.PacketSendEvent;
import aethereal.event.PacketReceiveEvent;
import aethereal.Expensive;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientConnection.class})
public class ClientConnectionMixin {
    @Inject(method = {"handlePacket"}, at = {@At("HEAD")}, cancellable = true)
    private static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener packetListener, CallbackInfo callbackInfo) {
        PacketReceiveEvent class051Var = new PacketReceiveEvent(packet);
        Expensive.INSTANCE.eventDispatcher().dispatch(class051Var);
        if (class051Var.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = {"send(Lnet/minecraft/network/packet/Packet;)V"}, at = {@At("HEAD")}, cancellable = true)
    private void send(Packet<?> packet, CallbackInfo callbackInfo) {
        PacketSendEvent class037Var = new PacketSendEvent(packet);
        Expensive.INSTANCE.eventDispatcher().dispatch(class037Var);
        if (class037Var.isCancelled()) {
            callbackInfo.cancel();
        }
    }
}
