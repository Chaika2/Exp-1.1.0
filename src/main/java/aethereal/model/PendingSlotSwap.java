package aethereal.model;

public class PendingSlotSwap {
    public Object owner;
    public ScopedSlot pendingSlot;
    public int clientsideSlot;
    public int ticksUntilReset;

    public PendingSlotSwap() {
    }

    public PendingSlotSwap(Object obj, ScopedSlot class246Var, int i, int i2) {
        this.owner = obj;
        this.pendingSlot = class246Var;
        this.clientsideSlot = i;
        this.ticksUntilReset = i2;
    }

    public int clientsideSlot() {
        return this.clientsideSlot;
    }

    public int ticksUntilReset() {
        return this.ticksUntilReset;
    }
}
