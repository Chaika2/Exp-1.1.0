package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.util.KeyboardUtil;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.NumberSetting;
import aethereal.type.SettingUnit;
import aethereal.model.SlotReference;
import aethereal.event.SlotScrollEvent;
import aethereal.math.Stopwatch;

import java.util.LinkedList;
import java.util.Queue;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

@Aliases(aliases = {"Item Scrolling", "Item Scroller", "Item Scroll", "Fast Item Move"})
public class ItemScrollerModule extends Module {
    public final Queue<SlotReference> slotQueue;
    public final NumberSetting delaySetting;
    public final Stopwatch delayTimer;

    public ItemScrollerModule() {
        super(ModuleTab.PLAYER, "ItemScroller");
        this.slotQueue = new LinkedList();
        this.delaySetting = new NumberSetting(Lang.CHESTSTEALER_DELAY).currentValue(0.0f).range(0.0f, 1000.0f).unit(SettingUnit.MILLISECONDS).step(5.0f);
        this.delayTimer = new Stopwatch();
        addSettings(this.delaySetting);
        register(SlotScrollEvent.class, class384Var -> {
            if (Mc.INSTANCE.isWorldLoaded() && isState()) {
                if (isActivationHeld()) {
                    Slot slot = class384Var.slot();
                    int iSlotId = class384Var.slotId();
                    if (slot != null && slot.getStack().getItem() != Items.AIR && canMoveSlot(slot)) {
                        this.slotQueue.add(new SlotReference(slot, iSlotId));
                    }
                }
                while (!this.slotQueue.isEmpty()) {
                    SlotReference class586VarPoll = this.slotQueue.poll();
                    if (class586VarPoll != null && canMoveSlot(class586VarPoll.slot)) {
                        if (!this.delayTimer.hasElapsed((long) this.delaySetting.currentValue())) {
                            return;
                        }
                        moveSlot(class586VarPoll.slot, class586VarPoll.slotId);
                        this.delayTimer.reset();
                    }
                }
            }
        });
    }

    public boolean canMoveSlot(Slot slot) {
        if (slot == null || !slot.hasStack() || slot.getStack().isEmpty()) {
            return false;
        }
        int i = slot.id;
        if (i < 0 || i >= 9) {
            return (i < 9 || i >= 36) ? hasSpaceInRange(0, 36) : hasSpaceInRange(0, 9);
        }
        return hasSpaceInRange(9, 36);
    }

    public boolean hasSpaceInRange(int i, int i2) {
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        if (player == null) {
            return false;
        }
        for (int i3 = i; i3 < i2; i3++) {
            ItemStack itemStack = (ItemStack) player.getInventory().main.get(i3);
            if (itemStack.isEmpty()) {
                return true;
            }
            if (itemStack.isStackable() && itemStack.getCount() < itemStack.getMaxCount()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void deactivate() {
        this.slotQueue.clear();
        super.deactivate();
    }

    public boolean isActivationHeld() {
        return KeyboardUtil.isKeyPressed(340) && KeyboardUtil.isMouseKeyPressed(0);
    }

    public void moveSlot(Slot slot, int i) {
        if (slot != null) {
            i = slot.id;
            if (slot.getStack().getItem() == Items.AIR) {
                return;
            }
        }
        Mc.INSTANCE.getInteractionManager().clickSlot(Mc.INSTANCE.getPlayer().currentScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, Mc.INSTANCE.getPlayer());
    }
}
