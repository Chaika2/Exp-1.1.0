package aethereal.event;

import net.minecraft.screen.slot.SlotActionType;

public class SlotClickEvent extends CancellableEvent {
    public int windowId;
    public int slotId;
    public int button;
    public SlotActionType actionType;

    public int getWindowId() {
        return this.windowId;
    }

    public int getSlotId() {
        return this.slotId;
    }

    public int getButton() {
        return this.button;
    }

    public SlotActionType getActionType() {
        return this.actionType;
    }

    public void setWindowId(int i) {
        this.windowId = i;
    }

    public void setSlotId(int i) {
        this.slotId = i;
    }

    public void setButton(int i) {
        this.button = i;
    }

    public void setActionType(SlotActionType slotActionType) {
        this.actionType = slotActionType;
    }

    public SlotClickEvent(int i, int i2, int i3, SlotActionType slotActionType) {
        this.windowId = i;
        this.slotId = i2;
        this.button = i3;
        this.actionType = slotActionType;
    }
}
