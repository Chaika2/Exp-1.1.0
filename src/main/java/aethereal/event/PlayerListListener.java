package aethereal.event;
import aethereal.type.EventPriority;
import aethereal.Expensive;
import aethereal.type.Mc;
import aethereal.util.StaffDetector;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;

public class PlayerListListener implements ClientListener {
    public PlayerListListener() {
        Expensive.INSTANCE.eventDispatcher().register(TabListEntryEvent.class, class038Var -> {
            if (Mc.INSTANCE.isWorldLoaded()) {
                StaffDetector.onPlayerListEvent(class038Var);
            }
        }, EventPriority.LOW);
        Expensive.INSTANCE.eventDispatcher().register(PacketReceiveEvent.class, classXXX -> {
            if (classXXX.getPacket() instanceof PlayerListS2CPacket) {
                StaffDetector.onPlayerListPacket((PlayerListS2CPacket) classXXX.getPacket());
            }
        }, EventPriority.LOW);
    }
}
