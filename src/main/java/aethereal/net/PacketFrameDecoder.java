package aethereal.net;
import aethereal.natives.XorCipher;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

public class PacketFrameDecoder extends ByteToMessageDecoder {
    public void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        if (byteBuf.readableBytes() < 4) {
            return;
        }
        byteBuf.markReaderIndex();
        int i = byteBuf.readInt();
        if (i <= 0 || i > 1048576) {
            byteBuf.resetReaderIndex();
            channelHandlerContext.close();
        } else {
            if (byteBuf.readableBytes() < i) {
                byteBuf.resetReaderIndex();
                return;
            }
            byte[] bArr = new byte[i];
            byteBuf.readBytes(bArr);
            list.add(XorCipher.decrypt(bArr));
        }
    }
}
