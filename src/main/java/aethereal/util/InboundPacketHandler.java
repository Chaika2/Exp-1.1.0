package aethereal.util;
import aethereal.net.IncomingPacket;
import aethereal.net.PacketBuffer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class InboundPacketHandler extends SimpleChannelInboundHandler<byte[]> {
    public void channelRead0(ChannelHandlerContext channelHandlerContext, byte[] bArr) {
        if (bArr.length == 0) {
            return;
        }
        int i = bArr[0] & 255;
        IncomingPacket class618VarCreateIncoming = PacketRegistry.createIncoming(i);
        if (class618VarCreateIncoming == null) {
            System.out.println("[Client] Unknown packet: 0x" + Integer.toHexString(i));
        } else {
            class618VarCreateIncoming.decode(PacketBuffer.wrap(bArr, 1));
            class618VarCreateIncoming.handle();
        }
    }

    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable th) {
    }
}
