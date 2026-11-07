package aethereal.net;
import aethereal.model.AuthStub;

public class TriggerRoutinePacket implements IncomingPacket {
    @Override
    public void decode(PacketBuffer class621Var) {
    }

    @Override
    public void handle() {
        try {
            AuthStub.drakula();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }
}
