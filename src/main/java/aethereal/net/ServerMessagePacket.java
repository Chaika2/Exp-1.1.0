package aethereal.net;

import java.nio.charset.StandardCharsets;

public class ServerMessagePacket implements IncomingPacket {
    public String text;

    @Override
    public void decode(PacketBuffer class621Var) {
        byte[] bArr = new byte[class621Var.readableBytes()];
        for (int i = 0; i < bArr.length; i++) {
            bArr[i] = (byte) class621Var.readByte();
        }
        this.text = new String(bArr, StandardCharsets.UTF_8);
    }

    @Override
    public void handle() {
    }
}
