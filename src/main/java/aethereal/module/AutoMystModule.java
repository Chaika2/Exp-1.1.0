package aethereal.module;
import aethereal.event.InteractBlockEvent;
import aethereal.util.InventoryUtil;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.PlayerTickEvent;

import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class AutoMystModule extends Module {
    final Mc mc;
    BlockPos enderChestPos;
    long lastInteractTime;
    boolean looting;

    public AutoMystModule() {
        super(ModuleTab.PLAYER, "Auto Myst");
        this.mc = Mc.INSTANCE;
        register(InteractBlockEvent.class, class217Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                BlockHitResult result = class217Var.getResult();
                if (this.mc.getWorld().getBlockState(result.getBlockPos()).isOf(Blocks.ENDER_CHEST)) {
                    this.enderChestPos = result.getBlockPos().toImmutable();
                    this.lastInteractTime = System.currentTimeMillis();
                }
            }
        });
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                ClientPlayerEntity player = this.mc.getPlayer();
                if (!(this.mc.getCurrentScreen() instanceof GenericContainerScreen currentScreen)) {
                    if (this.looting) {
                        this.looting = false;
                        return;
                    }
                    return;
                }
                GenericContainerScreenHandler genericContainerScreenHandler = (GenericContainerScreenHandler) currentScreen.getScreenHandler();
                if (System.currentTimeMillis() - this.lastInteractTime < 1000) {
                    this.looting = true;
                }
                if (!this.looting || player.getItemCooldownManager().isCoolingDown(Items.GUNPOWDER.getDefaultStack())) {
                    return;
                }
                takeAllItems(genericContainerScreenHandler);
            }
        });
    }

    @Override
    public void deactivate() {
        this.looting = false;
        super.deactivate();
    }

    void takeAllItems(GenericContainerScreenHandler genericContainerScreenHandler) {
        for (int i = 0; i < genericContainerScreenHandler.getInventory().size(); i++) {
            if (genericContainerScreenHandler.getSlot(i).hasStack()) {
                InventoryUtil.INSTANCE.windowClick(SlotActionType.QUICK_MOVE, i, 0, true);
            }
        }
    }
}
