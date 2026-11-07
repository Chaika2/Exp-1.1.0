package aethereal.net;
import aethereal.util.ClientChannelInitializer;
import aethereal.natives.XorCipher;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class NetworkClient {
    public static final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);

    public static final Bootstrap bootstrap = new Bootstrap().group(eventLoopGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000).handler(new ClientChannelInitializer());

    public static volatile Channel channel;

    public static void send(String str, int i, OutgoingPacket class620Var) {
        connect(str, i, channel -> {
            sendPacket(channel, class620Var);
        });
    }

    public static void connect(String str, int i, Consumer<Channel> consumer) {
        Channel channel = NetworkClient.channel;
        if (channel == null || !channel.isActive()) {
            bootstrap.connect(str, i).addListener((io.netty.channel.ChannelFuture channelFuture) -> {
                if (channelFuture.isSuccess()) {
                    NetworkClient.channel = channelFuture.channel();
                    NetworkClient.channel.closeFuture().addListener(future -> {
                        NetworkClient.channel = null;
                    });
                    consumer.accept(NetworkClient.channel);
                }
            });
        } else {
            consumer.accept(channel);
        }
    }

    public static void sendPacket(Channel channel, OutgoingPacket class620Var) {
        try {
            PacketBuffer class621VarCreate = PacketBuffer.create();
            class621VarCreate.writeByte(class620Var.id());
            class620Var.encode(class621VarCreate);
            byte[] bArrEncrypt = XorCipher.encrypt(class621VarCreate.toArray());
            ByteBuf byteBufBuffer = channel.alloc().buffer(4 + bArrEncrypt.length);
            byteBufBuffer.writeInt(bArrEncrypt.length);
            byteBufBuffer.writeBytes(bArrEncrypt);
            channel.writeAndFlush(byteBufBuffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        if (channel != null) {
            channel.close();
        }
        eventLoopGroup.shutdownGracefully(0L, 5L, TimeUnit.SECONDS);
    }
}
