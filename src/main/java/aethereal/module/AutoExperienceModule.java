package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.type.ButtonAction;
import aethereal.Expensive;
import aethereal.type.InventoryScope;
import aethereal.net.InventoryService;
import aethereal.event.KeyInputEvent;
import aethereal.type.KeyPressState;
import aethereal.ui.setting.KeybindSetting;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.MouseButtonEvent;
import aethereal.type.NotificationType;
import aethereal.util.PlayerActionUtil;
import aethereal.util.SilentSlotManager;
import aethereal.event.PlayerTickEvent;
import aethereal.math.Rotation;
import aethereal.config.RotationConfig;
import aethereal.util.RotationManager;
import aethereal.model.ScopedSlot;
import aethereal.model.SlotSearchResult2;
import aethereal.model.Translation;
import aethereal.ui.setting.NumberSetting;
import aethereal.math.Stopwatch;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.GameMode;

@Aliases(aliases = {"Auto Experience", "Auto Repair"})
public class AutoExperienceModule extends Module {
    public final KeybindSetting keybindSetting;
    public final NumberSetting delaySetting;
    public boolean throwing;
    private boolean notifiedMissing;
    private final Stopwatch timer;

    public AutoExperienceModule() {
        super(ModuleTab.MISC, "Auto Experience");
        this.keybindSetting = new KeybindSetting(Translation.clearText("Button"));
        this.delaySetting = new NumberSetting(Translation.clearText("Delay")).range(0.0f, 100.0f).currentValue(0.0f).unit(aethereal.type.SettingUnit.UNITS);
        this.delaySetting.step = 10.0f;
        this.throwing = false;
        this.notifiedMissing = false;
        this.timer = new Stopwatch();
        addSettings(this.keybindSetting, this.delaySetting);
        register(KeyInputEvent.class, class049Var -> {
            handleKey(class049Var.key(), class049Var.action() == KeyPressState.PRESS);
        });
        register(MouseButtonEvent.class, class107Var -> {
            handleKey(class107Var.button(), class107Var.action() == ButtonAction.PRESS);
        });
        register(PlayerTickEvent.class, class130Var -> {
            Mc class815Var = Mc.INSTANCE;
            if (isState() && class815Var.isWorldLoaded() && class130Var.isPre()) {
                if (class815Var.getCurrentScreen() != null) {
                    reset();
                    return;
                }
                boolean shouldThrow = (this.keybindSetting.getKey() == -1) || this.throwing;
                if (shouldThrow && timer.getElapsedTime(TimeUnit.MILLISECONDS) >= this.delaySetting.currentValue()) {
                    throwExperience();
                } else if (!shouldThrow) {
                    RotationManager.INSTANCE.cancelTasksFor(this);
                }
            }
        });
    }

    public void handleKey(int i, boolean z) {
        if (i == this.keybindSetting.getKey()) {
            this.throwing = z;
            if (!z) {
                reset();
            }
        }
    }

    public void reset() {
        RotationManager.INSTANCE.cancelTasksFor(this);
        this.throwing = false;
        this.notifiedMissing = false;
        timer.reset();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        reset();
    }

    public static boolean isExperienceBottle(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        if (itemStack.getItem() == Items.EXPERIENCE_BOTTLE) {
            return true;
        }
        String name = itemStack.getName().getString().toLowerCase();
        if (name.contains("опыт") || name.contains("experience")) {
            return true;
        }
        String id = Registries.ITEM.getId(itemStack.getItem()).getPath();
        return id.contains("experience_bottle") || id.contains("exp_bottle");
    }

    public void throwExperience() {
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        if (player == null || !Mc.INSTANCE.isWorldLoaded()) {
            return;
        }
        InventoryService inventoryService = Expensive.INSTANCE.inventoryService();
        Predicate<ItemStack> predicate = AutoExperienceModule::isExperienceBottle;
        Optional<SlotSearchResult2> found = inventoryService.searcher().findItem(predicate, InventoryScope.HOTBAR, InventoryScope.INVENTORY, InventoryScope.OFFHAND);
        if (found.isEmpty() || !found.get().found()) {
            notifyMissing();
            return;
        }
        if (player.getItemCooldownManager().isCoolingDown(found.get().stack())) {
            return;
        }

        Rotation throwRotation = new Rotation(player.getYaw(), 90.0f);
        RotationManager.INSTANCE.scheduleRotation(throwRotation, RotationConfig.LINEAR, 1, this, 5);
        if (RotationManager.INSTANCE.getCurrentRotation().getPitch() < 85.0f) {
            return;
        }

        ScopedSlot slot = found.get().slotReference();
        Rotation rotation = RotationManager.INSTANCE.getCurrentRotation();
        if (slot.slot() < 9) {
            // Bottle is already in the hotbar: silently select its slot for one use, then restore.
            SilentSlotManager swapper = inventoryService.hotbarSlotSwapper();
            int previousSlot = player.getInventory().selectedSlot;
            swapper.swapTo(this, slot, 1);
            PlayerActionUtil.INSTANCE.interactItem(Hand.MAIN_HAND, rotation, true);
            swapper.pendingSwap = null;
            player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
        } else if (slot.slot() == InventoryScope.OFFHAND.start()) {
            // Bottle is in the offhand: use it directly, no swap at all.
            PlayerActionUtil.INSTANCE.interactItem(Hand.OFF_HAND, rotation, true);
        } else {
            // Bottle is in the main inventory: silent swap-in-hand, throw, swap-back in the same tick.
            silentSwapThrow(player, found.get().stack(), slot, rotation);
        }
        this.notifiedMissing = false;
        timer.reset();
    }

    /**
     * Throws an experience bottle that sits in the main inventory without ever letting the bottle
     * visibly leave its slot and without the move-key/sprint pauses of the shared {@code SwapUtil}
     * scripts (which is what Celestial's Auto Exp effectively does):
     * <ol>
     *   <li>a SWAP click moves the bottle into the selected hotbar slot on the server only
     *       (client prediction disabled) - the client keeps showing the original item;</li>
     *   <li>the interaction packet is sent with the throw rotation;</li>
     *   <li>a second SWAP click puts everything back on the server.</li>
     * </ol>
     * Because the bottle is never used client-side, the item held in the hand is never touched.
     */
    private void silentSwapThrow(ClientPlayerEntity player, ItemStack stack, ScopedSlot slot, Rotation rotation) {
        ClientPlayerInteractionManager interactionManager = Mc.INSTANCE.getInteractionManager();
        if (interactionManager == null || interactionManager.getCurrentGameMode() == GameMode.SPECTATOR) {
            return;
        }
        int syncId = player.currentScreenHandler.syncId;
        int hotbar = player.getInventory().selectedSlot;
        int inventorySlot = slot.increasedSlot();
        PlayerActionUtil.INSTANCE.clickSlot(syncId, inventorySlot, hotbar, SlotActionType.SWAP, true);
        interactionManager.sendSequencedPacket(Mc.INSTANCE.getWorld(), sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, rotation.getYaw(), rotation.getPitch()));
        PlayerActionUtil.INSTANCE.clickSlot(syncId, inventorySlot, hotbar, SlotActionType.SWAP, true);
        // Mirror the vanilla item cooldown so repeated throws stay throttled like the hotbar path.
        player.getItemCooldownManager().set(stack, 10);
    }

    private void notifyMissing() {
        if (this.notifiedMissing) {
            return;
        }
        this.notifiedMissing = true;
        Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal(Lang.NO_ITEM_FOUND.effective().replace("{item}", String.valueOf(Formatting.RED) + Items.EXPERIENCE_BOTTLE.getName().getString() + String.valueOf(Formatting.RESET))), 3L, TimeUnit.SECONDS);
    }
}
