package aethereal.net;

import net.minecraft.network.packet.Packet;

public final class TimestampedPacket {
    public final Packet<?> packet;
    public final long timestamp;

    public TimestampedPacket(Packet<?> packet, long j) {
        this.packet = packet;
        this.timestamp = j;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "timestamp=" + this.timestamp + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.timestamp);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TimestampedPacket)) return false;
        TimestampedPacket o = (TimestampedPacket) obj;
        return java.util.Objects.equals(this.timestamp, o.timestamp);
    }
public Packet<?> packet() {
        return this.packet;
    }

    public long timestamp() {
        return this.timestamp;
    }
}
