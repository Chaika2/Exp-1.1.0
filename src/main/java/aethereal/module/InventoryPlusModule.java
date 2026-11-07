package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.PacketSendEvent;

import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

@Aliases(aliases = {"Inventory Tweaks", "Inventory Manager", "Inventory Expansion", "Keep Inventory Open", "Advanced Inventory", "InventoryPlus", "X Carry", "No Close Inventory", "Persistent Inventory"})
public class InventoryPlusModule extends Module {
    public InventoryPlusModule() {
        super(ModuleTab.MISC, "Inventory Plus");
        register(PacketSendEvent.class, class037Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded() && (class037Var.getPacket() instanceof CloseHandledScreenC2SPacket)) {
                class037Var.cancel();
            }
        });
    }
}
