package aethereal.net;

import java.nio.ByteBuffer;

public class AuthResponsePacket extends SocketPacket {
    public String hwid;
    public String json;
    public long value;

    public AuthResponsePacket(String str) {
        this.hwid = str;
    }

    public AuthResponsePacket() {
    }

    @Override
    public void read(ByteBuffer byteBuffer) {
        if (byteBuffer.capacity() == 1) {
            return;
        }
        this.value = byteBuffer.getLong();
        if (byteBuffer.remaining() > 0) {
            byte[] bArr = new byte[byteBuffer.remaining()];
            byteBuffer.get(bArr);
            this.json = new String(bArr);
        }
    }

    @Override
    public ByteBuffer write() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte packetID() {
        return (byte) 2;
    }
}
