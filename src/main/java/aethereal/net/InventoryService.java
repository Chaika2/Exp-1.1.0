package aethereal.net;
import aethereal.type.CombatPauseManager;
import aethereal.Expensive;
import aethereal.util.InventoryItemFinder;
import aethereal.util.InventoryTask;
import aethereal.util.ItemInteractionHelper;
import aethereal.type.Mc;
import aethereal.module.Module;
import aethereal.util.PlayerActionUtil;
import aethereal.event.PlayerTickEvent;
import aethereal.util.RotationManager;
import aethereal.util.SilentSlotManager;
import aethereal.model.SlotSearchResult2;
import aethereal.util.SwapUtil;

import net.minecraft.util.Hand;

public class InventoryService {
    public final ItemInteractionHelper itemInteractor = new ItemInteractionHelper();

    public final SilentSlotManager hotbarSlotSwapper = new SilentSlotManager();

    public final InventoryItemFinder searcher = new InventoryItemFinder();

    public InventoryService() {
        Expensive.INSTANCE.eventDispatcher().register(PlayerTickEvent.class, class130Var -> {
            if (Mc.INSTANCE.isWorldLoaded() && class130Var.isPre() && !CombatPauseManager.INSTANCE.shouldPauseSwaps()) {
                this.hotbarSlotSwapper.update();
            }
        });
    }

    public void addTask(InventoryTask class012Var, Module class605Var) {
        if (Mc.INSTANCE.getPlayer() == null) {
            return;
        }
        SlotSearchResult2 class329VarResult = class012Var.result();
        if (class329VarResult.found()) {
            SwapUtil.swapAndExecute(class329VarResult.slotReference().increasedSlot(), class012Var.rotation(), class012Var.needStop(), () -> {
                PlayerActionUtil.INSTANCE.interactItem(Hand.MAIN_HAND, RotationManager.INSTANCE.getCurrentRotation(), false);
            });
        }
    }

    public ItemInteractionHelper itemInteractor() {
        return this.itemInteractor;
    }

    public InventoryItemFinder searcher() {
        return this.searcher;
    }

    public SilentSlotManager hotbarSlotSwapper() {
        return this.hotbarSlotSwapper;
    }
}
