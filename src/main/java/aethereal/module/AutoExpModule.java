package aethereal.module;

import aethereal.Expensive;
import aethereal.annotation.Aliases;
import aethereal.type.ButtonAction;
import aethereal.event.KeyInputEvent;
import aethereal.type.KeyPressState;
import aethereal.ui.setting.KeybindSetting;
import aethereal.type.Mc;
import aethereal.type.SettingUnit;
import aethereal.ui.ModuleTab;
import aethereal.event.MouseButtonEvent;
import aethereal.util.PlayerActionUtil;
import aethereal.event.PlayerTickEvent;
import aethereal.math.Rotation;
import aethereal.config.RotationConfig;
import aethereal.util.RotationManager;
import aethereal.util.SilentSlotManager;
import aethereal.model.ScopedSlot;
import aethereal.model.Translation;
import aethereal.ui.setting.NumberSetting;
import aethereal.math.Stopwatch;
import aethereal.type.InventoryScope;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.Protocol1_21_2To1_21_4;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Faithful port of Celestial's {@code AutoExp} (module/player/AutoExp.java).
 * <p>
 * Aims straight down (pitch 90), waits for the server rotation to reach the target
 * (within 3.0°), then throws an experience bottle. Hotbar bottles are thrown via
 * Expensive's silent-slot system (SilentSlotManager + mixins). Bottles in the main
 * inventory (slots 9-35) are thrown exactly like Celestial: a slot-based {@code PICK_ITEM}
 * makes the server swap the bottle into the selected hotbar slot, the bottle is thrown, and
 * on release the slot is picked back to restore the inventory.
 * <p>
 * The pick is emitted through ViaFabricPlus so that the emulated server version receives a
 * native slot-based {@code PICK_ITEM} (1.16.5 = {@code 0x18}); the server performs the swap.
 */
@Aliases(aliases = {"Auto Exp", "AutoExp", "Exp Bottle", "Auto XP"})
public class AutoExpModule extends Module {
    private static final long INVENTORY_SWAP_THROTTLE_MS = 300L;
    private static final double ROTATION_TOLERANCE = 3.0d;
    private static final String VIAFABRICPLUS_MOD_ID = "viafabricplus";

    public final KeybindSetting keybind;
    public final NumberSetting delaySetting;
    public boolean throwing;
    private final Stopwatch timer;
    private long lastInventorySwap;
    private int hotbarPreviousSlot = -1;
    private int inventorySwapSlot = -1;

    public AutoExpModule() {
        super(ModuleTab.PLAYER, "Auto Exp");
        this.keybind = new KeybindSetting(Translation.clearText("Throw On"));
        this.delaySetting = new NumberSetting(Translation.clearText("Delay")).range(0.0f, 100.0f).currentValue(0.0f).step(10.0f).unit(SettingUnit.UNITS);
        this.throwing = false;
        this.timer = new Stopwatch();
        this.lastInventorySwap = 0L;
        addSettings(this.keybind, this.delaySetting);
        register(KeyInputEvent.class, class049Var -> {
            handleKey(class049Var.key(), class049Var.action() == KeyPressState.PRESS);
        });
        register(MouseButtonEvent.class, class107Var -> {
            handleKey(class107Var.button(), class107Var.action() == ButtonAction.PRESS);
        });
        register(PlayerTickEvent.class, class130Var -> {
            Mc mc = Mc.INSTANCE;
            if (!isState() || !mc.isWorldLoaded() || !class130Var.isPre()) {
                return;
            }
            ClientPlayerEntity player = mc.getPlayer();
            if (player == null || mc.getCurrentScreen() != null) {
                reset();
                return;
            }
            boolean shouldThrow = this.keybind.getKey() == -1 || this.throwing;
            if (!shouldThrow) {
                reset();
                return;
            }
            int slot = findBottleSlot(player);
            if (slot == -1) {
                reset();
                return;
            }
            // Aim straight down (pitch 90, yaw untouched) and only act once within tolerance.
            float yaw = RotationManager.INSTANCE.getServerRotation() == null ? player.getYaw() : RotationManager.INSTANCE.getServerRotation().getYaw();
            Rotation target = new Rotation(yaw, 90.0f);
            RotationManager.INSTANCE.scheduleRotation(target, RotationConfig.LINEAR, 1, this, 5);
            if (RotationManager.computeRotationDifference(target, RotationManager.INSTANCE.getCurrentRotation()) > ROTATION_TOLERANCE) {
                return;
            }
            if (this.timer.getElapsedTime(TimeUnit.MILLISECONDS) >= this.delaySetting.currentValue()) {
                throwExperience(player, slot);
                this.timer.reset();
            }
        });
    }

    public void handleKey(int i, boolean z) {
        if (i == this.keybind.getKey()) {
            this.throwing = z;
            if (!z) {
                reset();
            }
        }
    }

    public void reset() {
        RotationManager.INSTANCE.cancelTasksFor(this);
        if (this.inventorySwapSlot != -1 && Mc.INSTANCE.getPlayer() != null) {
            // Pick the inventory slot back so the server restores the hotbar/inventory state.
            sendPickItem(this.inventorySwapSlot);
            this.lastInventorySwap = System.currentTimeMillis();
            this.inventorySwapSlot = -1;
        }
        if (this.hotbarPreviousSlot != -1 && Mc.INSTANCE.getPlayer() != null) {
            Mc.INSTANCE.getPlayer().networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.hotbarPreviousSlot));
            this.hotbarPreviousSlot = -1;
        }
        this.throwing = false;
        this.timer.reset();
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

    // -2 main hand, -3 off hand, 0-8 hotbar, 9-35 main inventory, -1 none
    private int findBottleSlot(ClientPlayerEntity player) {
        if (isExperienceBottle(player.getStackInHand(Hand.MAIN_HAND))) {
            return -2;
        }
        if (isExperienceBottle(player.getStackInHand(Hand.OFF_HAND))) {
            return -3;
        }
        for (int i = 0; i < 9; i++) {
            if (isExperienceBottle(player.getInventory().getStack(i))) {
                return i;
            }
        }
        for (int i = 9; i < 36; i++) {
            if (isExperienceBottle(player.getInventory().getStack(i))) {
                return i;
            }
        }
        return -1;
    }

    public void throwExperience(ClientPlayerEntity player, int slot) {
        Rotation rotation = RotationManager.INSTANCE.getCurrentRotation();
        if (slot == -2) {
            PlayerActionUtil.INSTANCE.interactItem(Hand.MAIN_HAND, rotation, false);
            return;
        }
        if (slot == -3) {
            PlayerActionUtil.INSTANCE.interactItem(Hand.OFF_HAND, rotation, false);
            return;
        }
        if (slot < 9) {
            AutoTotemModule.pauseSprintForClick(player);
            SilentSlotManager swapper = Expensive.INSTANCE.inventoryService().hotbarSlotSwapper();
            int previousSlot = player.getInventory().selectedSlot;
            if (this.hotbarPreviousSlot == -1) {
                this.hotbarPreviousSlot = previousSlot;
            }
            // Silent-select the bottle's hotbar slot (server sees it, client keeps view)
            swapper.swapTo(this, new ScopedSlot(slot, InventoryScope.HOTBAR), 1);
            PlayerActionUtil.INSTANCE.interactItem(Hand.MAIN_HAND, rotation, false);
            swapper.pendingSwap = null;
            // Restore to the original selected slot (as Celestial does on release; keep if holding)
            player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
            return;
        }
        long now = System.currentTimeMillis();
        if (now - this.lastInventorySwap <= INVENTORY_SWAP_THROTTLE_MS) {
            return;
        }
        this.inventorySwapSlot = slot;
        sendPickItem(slot);
        PlayerActionUtil.INSTANCE.interactItem(Hand.MAIN_HAND, rotation, false);
        this.lastInventorySwap = now;
    }

    /**
     * Emits a slot-based {@code PICK_ITEM} through ViaFabricPlus, skipping the modern
     * {@code Protocol1_21_2To1_21_4} (which removed the slot form) so the emulated server
     * receives a native pick. Mirrors Celestial's {@code Ax_1.id_class(int)}.
     */
    private void sendPickItem(int slot) {
        if (!FabricLoader.getInstance().isModLoaded(VIAFABRICPLUS_MOD_ID)) {
            return;
        }
        try {
            UserConnection connection = ProtocolTranslator.getPlayNetworkUserConnection();
            if (connection == null || !connection.getProtocolInfo().getPipeline().contains(Protocol1_21_2To1_21_4.class)) {
                return;
            }
            PacketWrapper wrapper = PacketWrapper.create(ServerboundPackets1_21_2.PICK_ITEM, connection);
            wrapper.write(Types.VAR_INT, slot);
            wrapper.scheduleSendToServer(Protocol1_21_2To1_21_4.class);
        } catch (Throwable ignored) {
        }
    }
}
