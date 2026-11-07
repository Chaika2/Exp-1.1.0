package aethereal.net;

import java.nio.ByteBuffer;

public abstract class SocketPacket {
    public abstract void read(ByteBuffer byteBuffer);

    public abstract ByteBuffer write();

    public abstract byte packetID();
}
