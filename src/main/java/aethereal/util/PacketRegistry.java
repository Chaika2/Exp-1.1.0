package aethereal.util;
import aethereal.net.ExitPacket;
import aethereal.net.IncomingPacket;
import aethereal.net.ServerMessagePacket;
import aethereal.net.TriggerRoutinePacket;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class PacketRegistry {
    public static final Map<Integer, Supplier<IncomingPacket>> incomingPackets = new HashMap();

    public PacketRegistry() {
    }

    public static void registerIncoming(int i, Supplier<IncomingPacket> supplier) {
        incomingPackets.put(Integer.valueOf(i), supplier);
    }

    public static IncomingPacket createIncoming(int i) {
        Supplier<IncomingPacket> supplier = incomingPackets.get(Integer.valueOf(i));
        if (supplier != null) {
            return supplier.get();
        }
        return null;
    }

    static {
        registerIncoming(1, ServerMessagePacket::new);
        registerIncoming(2, TriggerRoutinePacket::new);
        registerIncoming(3, ExitPacket::new);
    }
}
