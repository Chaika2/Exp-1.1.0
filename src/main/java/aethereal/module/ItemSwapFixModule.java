package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.PacketReceiveEvent;

import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;

@Aliases(aliases = {"Item Swap Fix", "Swap Correction", "Slot Fix", "Item Slot Manager", "Swap Bug Fix", "Item Switch Fix", "Fix Slot Swap", "No Swap Fix", "No Item Swap", "Hotbar Fix", "Hotbar Sync"})
public class ItemSwapFixModule extends Module {
    public ItemSwapFixModule() {
        super(ModuleTab.MISC, "Item Swap Fix");
        register(PacketReceiveEvent.class, class051Var -> {
            Mc class815Var = Mc.INSTANCE;
            if (isState() && class815Var.isWorldLoaded()) {
                if ((class051Var.getPacket()) instanceof UpdateSelectedSlotS2CPacket packet ) {
                    try {
                        if (packet.slot() != class815Var.getPlayer().getInventory().selectedSlot) {
                            class051Var.cancel();
                        }
                    } catch (Throwable th) {
                        throw new MatchException(th.toString(), th);
                    }
                }
            }
        });
    }
}
