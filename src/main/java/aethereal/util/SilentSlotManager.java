package aethereal.util;
import aethereal.type.Mc;
import aethereal.model.PendingSlotSwap;
import aethereal.model.ScopedSlot;

public class SilentSlotManager {
    public PendingSlotSwap pendingSwap;
    public final Mc mc = Mc.INSTANCE;
    public int tickCounter = 0;

    public int getServersideSlot() {
        if (this.pendingSwap != null) {
            return this.pendingSwap.pendingSlot.slot();
        }
        if (this.mc.getPlayer() != null) {
            return this.mc.getPlayer().getInventory().selectedSlot;
        }
        return 0;
    }

    public int getClientsideSlot() {
        if (this.pendingSwap != null) {
            return this.pendingSwap.clientsideSlot();
        }
        if (this.mc.getPlayer() != null) {
            return this.mc.getPlayer().getInventory().selectedSlot;
        }
        return 0;
    }

    public void update() {
        if (this.pendingSwap != null) {
            int i = this.tickCounter;
            this.tickCounter = i + 1;
            if (i >= this.pendingSwap.ticksUntilReset()) {
                this.pendingSwap = null;
            }
        }
    }

    public void swapTo(Object obj, ScopedSlot class246Var, int i) {
        this.pendingSwap = new PendingSlotSwap(obj, class246Var, getClientsideSlot(), i);
        this.tickCounter = 0;
    }
}
