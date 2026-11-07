package aethereal.util;
import aethereal.type.ContainerDropStrategy;
import aethereal.Expensive;
import aethereal.event.FocusedSlotEvent;
import aethereal.type.Mc;

import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class DropAllHandler implements ClientHandler {
    public ItemDropper dropper;

    public DropAllHandler() {
        Expensive.INSTANCE.eventDispatcher().register(FocusedSlotEvent.class, class183Var -> {
            ScreenHandler screenHandler;
            Slot slotFocusedSlot = class183Var.focusedSlot();
            if (!isHotkeyPressed() || slotFocusedSlot == null || slotFocusedSlot.getStack() == null || (screenHandler = Mc.INSTANCE.getPlayer().currentScreenHandler) == null) {
                return;
            }
            selectDropper(screenHandler);
            if (this.dropper != null) {
                this.dropper.dropItems(screenHandler, slotFocusedSlot.id, slotFocusedSlot.getStack().getItem());
            }
        });
    }

    public void selectDropper(ScreenHandler screenHandler) {
        if (screenHandler instanceof GenericContainerScreenHandler) {
            if (!(this.dropper instanceof ContainerDropStrategy)) {
                this.dropper = new ContainerDropStrategy();
            }
        } else if (screenHandler instanceof PlayerScreenHandler) {
            if (!(this.dropper instanceof PlayerInventoryDropper)) {
                this.dropper = new PlayerInventoryDropper();
            }
        } else {
            this.dropper = null;
        }
    }

    public boolean isHotkeyPressed() {
        return KeyboardUtil.isKeyPressed(341) && KeyboardUtil.isKeyPressed(340) && KeyboardUtil.isKeyPressed(81);
    }
}