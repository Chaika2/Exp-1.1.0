package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.event.BlockBreakEvent;
import aethereal.Expensive;
import aethereal.util.GrimDelayHandler;
import aethereal.type.InventoryScope;
import aethereal.net.InventoryService;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.util.PlayerActionUtil;
import aethereal.event.PlayerTickEvent;
import aethereal.model.ScopedSlot;
import aethereal.ui.setting.Setting;
import aethereal.model.SlotSearchResult2;
import aethereal.util.SwapUtil;

import java.util.Comparator;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

@Aliases(aliases = {"Auto Tool", "Tool Selector", "Automatic Tool Switch", "Tool Manager", "Quick Tool", "Smart Tool", "Auto Equipment", "Block Breaking Helper", "Mining Helper"})
public class AutoToolModule extends Module {
    public final Mc mc;
    public int swapBackTicks;
    public Integer hotbarSlot;
    public Integer inventorySlot;
    public SlotSearchResult2 currentTool;
    public long swapCooldown;

    public AutoToolModule() {
        super(ModuleTab.PLAYER, "Auto Tool");
        this.mc = Mc.INSTANCE;
        this.swapBackTicks = 0;
        this.hotbarSlot = null;
        this.inventorySlot = null;
        this.currentTool = null;
        this.swapCooldown = 0L;
        addSettings(new Setting[0]);
        registerEvents();
    }

    public void registerEvents() {
        register(PlayerTickEvent.class, this::onPlayerTick);
        register(BlockBreakEvent.class, this::onBlockBreak);
    }

    public void onPlayerTick(PlayerTickEvent class130Var) {
        if (isState() && this.mc.isWorldLoaded()) {
            tickCooldown();
            updateSwapBack();
        }
    }

    public void onBlockBreak(BlockBreakEvent class241Var) {
        if (isState() && this.mc.isWorldLoaded() && class241Var.isPre()) {
            swapForBlock(this.mc.getPlayer().getWorld().getBlockState(class241Var.getPos()));
        }
    }

    public void swapForBlock(BlockState blockState) {
        if (isState() && this.mc.isWorldLoaded() && blockState != null) {
            ClientPlayerEntity player = this.mc.getPlayer();
            InventoryService class011VarInventoryService = Expensive.INSTANCE.inventoryService();
            Optional<SlotSearchResult2> optionalFindBestTool = findBestTool(blockState);
            if (optionalFindBestTool.isPresent()) {
                SlotSearchResult2 class329Var = optionalFindBestTool.get();
                if (class329Var.found()) {
                    ScopedSlot class246VarSlotReference = class329Var.slotReference();
                    InventoryScope class305VarScope = class246VarSlotReference.scope();
                    if (shouldRestoreFirst(class329Var)) {
                        applyState();
                        return;
                    }
                    AutoEatModule class570Var = (AutoEatModule) Expensive.INSTANCE.moduleRepository().get(AutoEatModule.class);
                    if (class570Var.isState() && class570Var.isEating()) {
                        return;
                    }
                    if (class305VarScope == InventoryScope.HOTBAR) {
                        class011VarInventoryService.hotbarSlotSwapper().swapTo(this, class246VarSlotReference, 5);
                    } else if (class305VarScope == InventoryScope.INVENTORY && this.swapCooldown == 0) {
                        swapInInventory(class246VarSlotReference.slot(), player.getInventory().selectedSlot);
                    }
                    this.currentTool = class329Var;
                    this.swapBackTicks = 0;
                }
            }
        }
    }

    public void swapInInventory(int i, int i2) {
        if (GrimDelayHandler.script.isFinished()) {
            SwapUtil.swapAction(() -> {
                PlayerActionUtil.INSTANCE.windowClick(SlotActionType.SWAP, i, i2, true);
                PlayerActionUtil.INSTANCE.updateSlots(false);
                if (this.hotbarSlot == null) {
                    this.hotbarSlot = Integer.valueOf(i2);
                }
                if (this.inventorySlot == null) {
                    this.inventorySlot = Integer.valueOf(i);
                }
            });
        }
    }

    public void updateSwapBack() {
        if (this.inventorySlot == null || this.hotbarSlot == null || !GrimDelayHandler.script.isFinished()) {
            return;
        }
        int i = this.swapBackTicks;
        this.swapBackTicks = i + 1;
        if (i > 20) {
            SwapUtil.swapAction(() -> {
                PlayerActionUtil.INSTANCE.windowClick(SlotActionType.SWAP, this.inventorySlot.intValue(), this.hotbarSlot.intValue(), true);
                PlayerActionUtil.INSTANCE.updateSlots(false);
                reset();
            });
        }
    }

    public void tickCooldown() {
        if (this.swapCooldown > 0) {
            this.swapCooldown--;
        }
    }

    public void applyState() {
        if (GrimDelayHandler.script.isFinished()) {
            SwapUtil.swapAction(() -> {
                PlayerActionUtil.INSTANCE.windowClick(SlotActionType.SWAP, this.inventorySlot.intValue(), this.hotbarSlot.intValue(), true);
                PlayerActionUtil.INSTANCE.updateSlots(false);
                this.swapCooldown = 10L;
                reset();
            });
        }
    }

    public boolean shouldRestoreFirst(SlotSearchResult2 class329Var) {
        return (this.currentTool == null || ItemStack.areItemsEqual(this.currentTool.stack(), class329Var.stack()) || this.inventorySlot == null || this.hotbarSlot == null) ? false : true;
    }

    public void reset() {
        this.hotbarSlot = null;
        this.inventorySlot = null;
        this.swapBackTicks = 0;
        this.currentTool = null;
    }

    public Optional<SlotSearchResult2> findBestTool(BlockState blockState) {
        return Mc.INSTANCE.getPlayer() == null ? Optional.empty() : Expensive.INSTANCE.inventoryService().searcher().findAllItems(itemStack -> {
            return itemStack.isSuitableFor(blockState);
        }, InventoryScope.HOTBAR, InventoryScope.INVENTORY).stream().max(Comparator.comparingDouble(class329Var -> {
            return class329Var.stack().getMiningSpeedMultiplier(blockState);
        }));
    }

    @Override
    public void deactivate() {
        reset();
        super.deactivate();
    }
}
