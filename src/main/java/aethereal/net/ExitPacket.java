package aethereal.net;

public class ExitPacket implements IncomingPacket {
    @Override
    public void decode(PacketBuffer class621Var) {
    }

    @Override
    public void handle() {
        System.exit(-1);
    }
}
