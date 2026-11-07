package aethereal.net;
import aethereal.type.Mc;
import aethereal.util.SlotSyncHandler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public final class PacketSender {
    public static void sendPacket(Packet<?> packet) {
        if (packet instanceof UpdateSelectedSlotC2SPacket) {
            UpdateSelectedSlotC2SPacket updateSelectedSlotC2SPacket = (UpdateSelectedSlotC2SPacket) packet;
            if (MinecraftClient.getInstance().getCurrentServerEntry() != null) {
                if (updateSelectedSlotC2SPacket.getSelectedSlot() == SlotSyncHandler.selectedSlot) {
                    return;
                } else {
                    SlotSyncHandler.selectedSlot = updateSelectedSlotC2SPacket.getSelectedSlot();
                }
            }
        }
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        if (player == null) {
            return;
        }
        player.networkHandler.getConnection().send(packet, (PacketCallbacks) null);
    }

    public static void sendSequencedNotSilentPacket(SequencedPacketCreator sequencedPacketCreator) {
        Mc.INSTANCE.getInteractionManager().sendSequencedPacket(Mc.INSTANCE.getWorld(), sequencedPacketCreator);
    }

    public static void sendSequencedPacket(SequencedPacketCreator sequencedPacketCreator) {
        Mc class815Var = Mc.INSTANCE;
        ClientWorld world = class815Var.getWorld();
        ClientPlayerEntity player = class815Var.getPlayer();
        if (world == null || player == null) {
            return;
        }
        try (PendingUpdateManager pendingUpdateManagerIncrementSequence = world.getPendingUpdateManager().incrementSequence()) {
            player.networkHandler.getConnection().send(sequencedPacketCreator.predict(pendingUpdateManagerIncrementSequence.getSequence()), (PacketCallbacks) null);
        }
    }

    public PacketSender() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
