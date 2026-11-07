package aethereal.module;
import aethereal.util.ActionScheduler;
import aethereal.annotation.Aliases;
import aethereal.Expensive;
import aethereal.type.ClickPearlMode;
import aethereal.type.InventoryScope;
import aethereal.model.Translation;
import aethereal.ui.setting.KeybindSetting;
import aethereal.ui.setting.ModeSetting;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.type.NotificationType;
import aethereal.util.PlayerActionUtil;
import aethereal.util.InventoryTask;
import aethereal.util.SwapUtil;
import aethereal.util.RotationManager;
import aethereal.util.SilentSlotManager;
import aethereal.model.ScopedSlot;
import aethereal.model.SlotSearchResult2;
import aethereal.event.PlayerTickEvent;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

@Aliases(aliases = {"Click Pearl", "Middle Click Pearl", "MC Pearl", "HotKey Pearl"})
public class ClickPearlModule extends Module {
    public final KeybindSetting keybind;
    public final ModeSetting<ClickPearlMode> mode;
    public final Mc mc;
    public final ActionScheduler actionScheduler;

    public ClickPearlModule() {
        super(ModuleTab.PLAYER, "Click Pearl");
        this.keybind = new KeybindSetting(Lang.CLICKPEARL_KEY, Lang.CLICKPEARL_KEY_DESC);
        this.mode = new ModeSetting(Translation.clearText("Mode"), Translation.clearText("Restore the hotbar slot immediately or on the next tick")).values(ClickPearlMode.class).select(ClickPearlMode.VANILLA);
        this.mc = Mc.INSTANCE;
        this.actionScheduler = new ActionScheduler();
        addSettings(this.keybind, this.mode);
        this.keybind.consumer(class664Var -> {
            if (isState() && this.mc.isWorldLoaded() && this.mc.getPlayer() != null) {
                throwPearl();
            }
        });
        register(PlayerTickEvent.class, class130Var -> {
            if (this.mc.isWorldLoaded() && class130Var.isPre()) {
                this.actionScheduler.cleanupIfFinished().update();
            }
        });
    }

    public void notifyCooldown(Item item) {
        Expensive.INSTANCE.notificationRepository().post((Text) Text.literal(Lang.ITEM_ON_COOLDOWN.effective().replace("{item}", String.valueOf(Formatting.RED) + item.getName().getString() + String.valueOf(Formatting.RESET)).replace("{seconds}", String.valueOf(Formatting.RED) + String.format(Locale.US, "%.1f", Float.valueOf(PlayerActionUtil.INSTANCE.getRemainingCooldownSeconds(item))) + String.valueOf(Formatting.RESET))), item.getDefaultStack(), 2L, TimeUnit.SECONDS);
    }

    // Celestial Click Pearl (rryy / rryC) translated: find the ender pearl, temporarily select its
    // hotbar slot (silent swap) or SWAP it into the hand from the inventory, right-click to throw,
    // then restore the original slot. Vanilla restores immediately, HolyWorld one tick later.
    public void throwPearl() {
        ClientPlayerEntity player = this.mc.getPlayer();
        if (player == null || !this.mc.isWorldLoaded()) {
            return;
        }
        if (player.getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL.getDefaultStack())) {
            notifyCooldown(Items.ENDER_PEARL);
            return;
        }
        Predicate<ItemStack> predicate = itemStack -> {
            return itemStack.getItem() == Items.ENDER_PEARL;
        };
        Optional<SlotSearchResult2> found = Expensive.INSTANCE.inventoryService().searcher().findItem(predicate, InventoryScope.ALL);
        if (found.isEmpty() || !found.get().found()) {
            Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal(Lang.NO_ITEM_FOUND.effective().replace("{item}", String.valueOf(Formatting.RED) + Items.ENDER_PEARL.getName().getString() + String.valueOf(Formatting.RESET))), 3L, TimeUnit.SECONDS);
            return;
        }

        ScopedSlot pearlSlot = found.get().slotReference();
        int previousSlot = player.getInventory().selectedSlot;

        if (pearlSlot.slot() < 9) {
            // Pearl already in the hotbar: silent slot swap, the interact packet carries the throw rotation.
            SilentSlotManager swapper = Expensive.INSTANCE.inventoryService().hotbarSlotSwapper();
            swapper.swapTo(this, pearlSlot, 1);
            PlayerActionUtil.INSTANCE.interactItem(Hand.MAIN_HAND, RotationManager.INSTANCE.getCurrentRotation(), true);
            if (this.mode.isSelected(ClickPearlMode.HOLYWORLD)) {
                this.actionScheduler.cleanup().addTickStep(1, () -> {
                    Expensive.INSTANCE.inventoryService().hotbarSlotSwapper().pendingSwap = null;
                    player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
                });
            } else {
                swapper.pendingSwap = null;
                player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
            }
        } else {
            // Pearl in the main inventory: use the shared inventory-task pipeline (swap -> use -> restore,
            // sequenced across ticks by GrimDelayHandler). Doing all three SWAP clicks in the same tick
            // (as the port did) gets rejected/desynced by servers, so throwing from the inventory failed.
            Expensive.INSTANCE.inventoryService().addTask(
                    InventoryTask.create(predicate, found.get(), SwapUtil.needsStop(), false, true), this);
        }
    }
}
