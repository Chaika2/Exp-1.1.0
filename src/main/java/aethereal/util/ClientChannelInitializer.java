package aethereal.util;
import aethereal.net.PacketFrameDecoder;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    public void initChannel(SocketChannel socketChannel) {
        socketChannel.pipeline().addLast(new ChannelHandler[]{new PacketFrameDecoder(), new InboundPacketHandler()});
    }
}
