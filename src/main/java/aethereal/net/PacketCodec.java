package aethereal.net;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class PacketCodec {
    public static final Map<Byte, Class<? extends SocketPacket>> packetTypes = new HashMap();

    public static SocketPacket fromBytes(byte[] bArr) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        ByteBuffer byteBufferMethod001 = decrypt(ByteBuffer.wrap(bArr));
        byte b = byteBufferMethod001.get();
        Class<? extends SocketPacket> cls = packetTypes.get(Byte.valueOf(b));
        if (cls == null) {
            throw new IllegalArgumentException("Unknown packet ID: " + b);
        }
        SocketPacket class716VarNewInstance = cls.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        class716VarNewInstance.read(byteBufferMethod001);
        return class716VarNewInstance;
    }

    public static byte[] sendData(SocketPacket class716Var) {
        return encrypt(class716Var.write());
    }

    public static ByteBuffer decrypt(ByteBuffer byteBuffer) {
        for (int i = 0; i < byteBuffer.limit(); i++) {
            byteBuffer.put(i, (byte) ((byteBuffer.get(i) ^ (byteBuffer.limit() % 66)) ^ 35));
        }
        return byteBuffer;
    }

    public static byte[] encrypt(ByteBuffer byteBuffer) {
        for (int i = 0; i < byteBuffer.limit(); i++) {
            byteBuffer.put(i, (byte) ((byteBuffer.get(i) ^ (byteBuffer.limit() % 66)) ^ 35));
        }
        return byteBuffer.array();
    }

    static {
        packetTypes.put((byte) 1, AuthHandshakePacket.class);
        packetTypes.put((byte) 2, AuthResponsePacket.class);
    }
}
