package aethereal.event;

import net.minecraft.network.packet.Packet;

public class PacketReceiveEvent extends CancellableEvent {
    public final Packet<?> packet;

    public Packet<?> getPacket() {
        return this.packet;
    }

    public PacketReceiveEvent(Packet<?> packet) {
        this.packet = packet;
    }
}
