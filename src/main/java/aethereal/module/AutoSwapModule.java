package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.type.AutoSwapItemType;
import aethereal.type.AutoSwapMode;
import aethereal.type.CombatPauseManager;
import aethereal.Expensive;
import aethereal.util.GrimDelayHandler;
import aethereal.event.PlayerTickEvent;
import aethereal.type.InventoryScope;
import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.KeybindSetting;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.setting.NumberSetting;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.type.NotificationType;
import aethereal.type.SettingUnit;
import aethereal.model.SlotSearchResult2;
import aethereal.model.Translation;
import aethereal.util.SwapUtil;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;

@Aliases(aliases = {"Swap", "Auto Swap", "Fast Swap", "Quick Swap", "Hotkey Swap", "Inventory Swap", "Offhand Swap", "Auto Item Switch", "Switch Items", "Auto Offhand", "Quick Switch", "Item Swap", "Swap Items", "Auto Hand Swap", "Key Swap"})
public class AutoSwapModule extends Module {
    public final ModeSetting<AutoSwapMode> mode;
    public final ModeSetting<AutoSwapItemType> swapFrom;
    public final ModeSetting<AutoSwapItemType> swapTo;
    public final KeybindSetting swapKey;
    public final NumberSetting healthThreshold;
    public final BooleanSetting checkCrystal;
    public final Mc mc;

    /** Найденный предмет, ожидающий клика: спринт гасится, клик - на следующем тике. */
    private SlotSearchResult2 pendingSwap;
    private boolean pendingSaveBack;
    private boolean pendingRestore;

    /** Предмет в оффхенде, который нужно вернуть после окончания угрозы. */
    private ItemStack swapBackStack;
    private boolean swapBackActive;

    // Короткое и мягкое окно: спринт гасится лишь на тик клика, а ввод почти не
    // режется, поэтому свап не выглядит как полная остановка бега.
    private static final int SWAP_SLOWDOWN_TICKS = 2;
    private static final float[] SWAP_SLOWDOWN_FACTORS = {0.95f, 0.85f};

    public AutoSwapModule() {
        super(ModuleTab.COMBAT, "Auto Swap");
        this.mode = new ModeSetting(Translation.clearText("Режим")).values(AutoSwapMode.class);
        this.swapFrom = new ModeSetting(Lang.COMBAT_AUTOSWAP_SWAPFROM).values(AutoSwapItemType.class).select(AutoSwapItemType.SHIELD);
        this.swapTo = new ModeSetting(Lang.COMBAT_AUTOSWAP_SWAPTO).values(AutoSwapItemType.class).select(AutoSwapItemType.GAPPLE);
        this.swapKey = new KeybindSetting(Lang.COMBAT_AUTOSWAP_SWAPKEY).visible(() -> Boolean.valueOf(this.mode.isSelected(AutoSwapMode.BIND)));
        this.healthThreshold = new NumberSetting(Translation.clearText("Здоровье")).currentValue(16.5f).range(1.0f, 19.5f).step(0.5f).unit(SettingUnit.HITPOINTS).visible(() -> Boolean.valueOf(this.mode.isSelected(AutoSwapMode.SMART)));
        this.checkCrystal = new BooleanSetting(Translation.clearText("Проверять кристалл")).setValue(true).visible(() -> Boolean.valueOf(this.mode.isSelected(AutoSwapMode.SMART)));
        this.mc = Mc.INSTANCE;
        this.swapBackStack = ItemStack.EMPTY;
        addSettings(this.mode, this.swapFrom, this.swapTo, this.swapKey, this.healthThreshold, this.checkCrystal);
        this.swapKey.consumer(class664Var -> {
            swap();
        });
        // Клик по оффхенду всегда делается из PlayerTickEvent.PRE: если игрок бежит,
        // спринт гасится в этом же тике (STOP_SPRINTING уходит штатно), а клик - на
        // следующем. Без этого сервер отбрасывает SWAP-клик, отправленный на бегу.
        register(PlayerTickEvent.class, class130Var -> {
            if (class130Var.isPre()) {
                tickSmart();
                tickPendingSwap();
            }
        });
    }

    /** Режим Бинд: по нажатию клавиши меняет оффхенд между "Менять с" и "Менять на". */
    public void swap() {
        if (isState() && this.mc.isWorldLoaded() && !CombatPauseManager.INSTANCE.shouldPauseAutoSwap()) {
            ClientPlayerEntity player = this.mc.getPlayer();
            if (player == null) {
                return;
            }
            AutoSwapItemType class855Var = (AutoSwapItemType) this.swapFrom.currentValue();
            AutoSwapItemType class855Var2 = (AutoSwapItemType) this.swapTo.currentValue();
            if (class855Var2.filter().test(player.getOffHandStack())) {
                scheduleSwap(class855Var, false);
            } else {
                scheduleSwap(class855Var2, true);
            }
        }
    }

    /** Режим Умный: сам меняет оффхенд на "Менять на" при низком здоровье и меняет обратно, когда угроза ушла. */
    private void tickSmart() {
        if (!isState() || !this.mc.isWorldLoaded() || !this.mode.isSelected(AutoSwapMode.SMART)) {
            return;
        }
        if (CombatPauseManager.INSTANCE.shouldPauseAutoSwap()) {
            return;
        }
        ClientPlayerEntity player = this.mc.getPlayer();
        if (player == null) {
            return;
        }
        AutoSwapItemType class855Var = (AutoSwapItemType) this.swapTo.currentValue();
        boolean z = (player.getHealth() + player.getAbsorptionAmount()) <= this.healthThreshold.currentValue() && (!this.checkCrystal.isValue() || !crystalNear(player));
        if (z) {
            if (!class855Var.filter().test(player.getOffHandStack()) && this.pendingSwap == null) {
                scheduleSwap(class855Var, true);
            }
        } else if (this.swapBackActive) {
            scheduleRestore();
        }
    }

    private void scheduleSwap(AutoSwapItemType class855Var, boolean z) {
        Optional<SlotSearchResult2> optionalFindItem = Expensive.INSTANCE.inventoryService().searcher().findItem(class855Var.filter(), InventoryScope.HOTBAR, InventoryScope.INVENTORY);
        if (optionalFindItem.isEmpty()) {
            notifyItemNotFound(class855Var.displayItem());
            return;
        }
        this.pendingSwap = optionalFindItem.get();
        this.pendingSaveBack = z;
        this.pendingRestore = false;
    }

    private void scheduleRestore() {
        if (!this.swapBackActive || this.swapBackStack.isEmpty()) {
            this.swapBackActive = false;
            return;
        }
        Optional<SlotSearchResult2> optionalFindItem = Expensive.INSTANCE.inventoryService().searcher().findItem(itemStack -> {
            return !itemStack.isEmpty() && ItemStack.areItemsAndComponentsEqual(itemStack, this.swapBackStack);
        }, InventoryScope.HOTBAR, InventoryScope.INVENTORY);
        if (optionalFindItem.isEmpty()) {
            this.swapBackActive = false;
            this.swapBackStack = ItemStack.EMPTY;
            return;
        }
        this.pendingSwap = optionalFindItem.get();
        this.pendingSaveBack = false;
        this.pendingRestore = true;
    }

    private void tickPendingSwap() {
        SlotSearchResult2 class329Var = this.pendingSwap;
        if (class329Var == null) {
            return;
        }
        if (!isState() || !this.mc.isWorldLoaded() || CombatPauseManager.INSTANCE.shouldPauseAutoSwap()) {
            clearPending();
            return;
        }
        if (!GrimDelayHandler.script.isFinished()) {
            return;
        }
        ClientPlayerEntity player = this.mc.getPlayer();
        if (player == null) {
            clearPending();
            return;
        }
        int iIncreasedSlot = class329Var.slotReference().increasedSlot();
        if (!AutoTotemModule.requestSprintPausedClick(player, iIncreasedSlot, SWAP_SLOWDOWN_TICKS, SWAP_SLOWDOWN_FACTORS)) {
            return;
        }
        boolean z = this.pendingSaveBack;
        boolean z2 = this.pendingRestore;
        clearPending();
        if (z) {
            this.swapBackStack = player.getOffHandStack().copy();
            this.swapBackActive = !this.swapBackStack.isEmpty();
        }
        SwapUtil.swapToOffhand(iIncreasedSlot);
        if (z2) {
            this.swapBackActive = false;
            this.swapBackStack = ItemStack.EMPTY;
        }
        Expensive.INSTANCE.notificationRepository().post(Text.empty().append(Lang.ELYTRAHELPER_SWAPPED_TO.effective().replace("{item}", String.valueOf(Formatting.RED) + StringHelper.stripTextFormat(class329Var.stack().getName().getString()).replace("[★]", "").replace("fff", "").replace("ggg", "").replace("xxx", "").trim() + String.valueOf(Formatting.RESET))), class329Var.stack(), 3L, TimeUnit.SECONDS);
    }

    private void clearPending() {
        this.pendingSwap = null;
        this.pendingSaveBack = false;
        this.pendingRestore = false;
    }

    private boolean crystalNear(ClientPlayerEntity clientPlayerEntity) {
        for (Entity entity : this.mc.getWorld().getEntities()) {
            if ((entity instanceof EndCrystalEntity) && entity.squaredDistanceTo(clientPlayerEntity) <= 36.0d) {
                return true;
            }
        }
        return false;
    }

    public static void notifyItemNotFound(Item item) {
        Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal("В Вашем инвентаре не найден предмет %s.".formatted(String.valueOf(Formatting.RED) + item.getName().getString())), 3L, TimeUnit.SECONDS);
    }

    @Override
    public void deactivate() {
        clearPending();
        this.swapBackActive = false;
        this.swapBackStack = ItemStack.EMPTY;
        super.deactivate();
    }
}
