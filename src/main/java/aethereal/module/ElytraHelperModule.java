package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.accessor.ArmorItemAccessor;
import aethereal.ui.setting.BooleanSetting;
import aethereal.Expensive;
import aethereal.util.GrimDelayHandler;
import aethereal.type.InventoryScope;
import aethereal.net.InventoryService;
import aethereal.util.InventoryTask;
import aethereal.util.InventoryUtil;
import aethereal.event.JumpEvent;
import aethereal.ui.setting.KeybindSetting;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.MovementInputEvent;
import aethereal.util.MovementInputHelper;
import aethereal.type.NotificationType;
import aethereal.event.PacketSendEvent;
import aethereal.util.PlayerActionUtil;
import aethereal.model.ScopedSlot;
import aethereal.util.ServerUtil;
import aethereal.model.SlotSearchResult2;
import aethereal.math.Stopwatch;
import aethereal.util.SwapUtil;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

@Aliases(aliases = {"Elytra Helper", "Elytra Manager", "Auto Elytra", "Flight Helper", "Elytra Control", "Elytra Swap"})
public class ElytraHelperModule extends Module {
    public final KeybindSetting swapKey;
    public final KeybindSetting fireworkKey;
    public final BooleanSetting autoTakeoff;
    public final BooleanSetting autoFirework;
    public final Mc mc;
    public final Stopwatch swapTimer;
    public final Stopwatch jumpTimer;

    public ElytraHelperModule() {
        super(ModuleTab.MISC, "Elytra Helper");
        this.swapKey = new KeybindSetting(Lang.ELYTRAHELPER_SWAPKEY);
        this.fireworkKey = new KeybindSetting(Lang.ELYTRAHELPER_FIREWORKKEY);
        this.autoTakeoff = new BooleanSetting(Lang.ELYTRAHELPER_AUTOTAKEOFF);
        BooleanSetting class665Var = new BooleanSetting(Lang.ELYTRAHELPER_AUTO_FIREWORK, Lang.ELYTRAHELPER_AUTO_FIREWORK_DESC);
        BooleanSetting class665Var2 = this.autoTakeoff;
        Objects.requireNonNull(class665Var2);
        this.autoFirework = class665Var.visible(class665Var2::isValue);
        this.mc = Mc.INSTANCE;
        this.swapTimer = new Stopwatch();
        this.jumpTimer = new Stopwatch();
        addSettings(this.swapKey, this.fireworkKey, this.autoTakeoff, this.autoFirework);
        this.swapKey.consumer(class664Var -> {
            trySwap();
        });
        this.fireworkKey.consumer(class664Var2 -> {
            useFirework(true);
        });
        register(PacketSendEvent.class, class037Var -> {
            if (isState() && this.mc.isWorldLoaded() && this.autoTakeoff.isValue() && this.autoFirework.isValue()) {
                if ((class037Var.getPacket() instanceof ClientCommandC2SPacket packet) && packet.getMode() == ClientCommandC2SPacket.Mode.START_FALL_FLYING && this.swapTimer.hasElapsed(150L)) {
                    useFirework(false);
                    this.swapTimer.reset();
                }
            }
        });
        register(JumpEvent.class, class237Var -> {
            if (isState() && this.mc.isWorldLoaded() && this.autoTakeoff.isValue()) {
                this.jumpTimer.reset();
            }
        });
        register(MovementInputEvent.class, class040Var -> {
            if (isState() && this.mc.isWorldLoaded() && this.autoTakeoff.isValue()) {
                if (!this.jumpTimer.hasElapsed(50L)) {
                    class040Var.setJumping(false);
                } else if (canStart()) {
                    class040Var.setJumping(true);
                }
            }
        });
    }

    public boolean canStart() {
        ClientPlayerEntity player = this.mc.getPlayer();
        boolean z = (!isState() || !this.autoTakeoff.isValue() || !MovementInputHelper.hasPlayerMovement() || !this.jumpTimer.hasElapsed(50L) || player.getAbilities().flying || player.hasVehicle() || player.isClimbing() || player.isTouchingWater() || player.getEquippedStack(EquipmentSlot.CHEST).willBreakNextUse() || player.hasStatusEffect(StatusEffects.LEVITATION) || !LivingEntity.canGlideWith(player.getEquippedStack(EquipmentSlot.CHEST), EquipmentSlot.CHEST)) ? false : true;
        if (z) {
            this.jumpTimer.reset();
        }
        return z;
    }

    public boolean isFlyingWithElytra(ClientPlayerEntity clientPlayerEntity) {
        ItemStack equippedStack = clientPlayerEntity.getEquippedStack(EquipmentSlot.CHEST);
        return (clientPlayerEntity.getAbilities().flying || clientPlayerEntity.hasVehicle() || clientPlayerEntity.isOnGround() || !clientPlayerEntity.isGliding() || clientPlayerEntity.isClimbing() || clientPlayerEntity.isTouchingWater() || clientPlayerEntity.hasStatusEffect(StatusEffects.LEVITATION) || !equippedStack.isOf(Items.ELYTRA) || equippedStack.willBreakNextUse()) ? false : true;
    }

    public void trySwap() {
        if (isState() && this.mc.isWorldLoaded()) {
            boolean z = this.mc.getPlayer().getInventory().getArmorStack(2).getItem() == Items.ELYTRA;
            if (this.swapTimer.hasElapsed(400L, TimeUnit.MILLISECONDS)) {
                performSwap(z);
                this.swapTimer.reset();
            }
        }
    }

    public Optional<SlotSearchResult2> findSwapItem(boolean z) {
        return Expensive.INSTANCE.inventoryService().searcher().findItem(z ? this::isChestplate : this::isElytra, InventoryScope.HOTBAR, InventoryScope.INVENTORY);
    }

    public boolean isChestplate(ItemStack itemStack) {
        return (itemStack.getItem() instanceof ArmorItem) && ((ArmorItemAccessor) itemStack.getItem()).expensive_ru$getType().getEquipmentSlot() == EquipmentSlot.CHEST && itemStack.getItem() != Items.ELYTRA;
    }

    public boolean isElytra(ItemStack itemStack) {
        return itemStack.getItem() == Items.ELYTRA;
    }

    public void useFirework(boolean z) {
        if (isState() && this.mc.isWorldLoaded() && this.mc.getPlayer().isGliding()) {
            InventoryService class011VarInventoryService = Expensive.INSTANCE.inventoryService();
            Predicate<ItemStack> predicate = itemStack -> {
                return itemStack.getItem() == Items.FIREWORK_ROCKET;
            };
            if (GrimDelayHandler.script.isFinished()) {
                class011VarInventoryService.searcher().findItem(predicate, InventoryScope.ALL).ifPresentOrElse(class329Var -> {
                    class011VarInventoryService.addTask(InventoryTask.create(predicate, class329Var, SwapUtil.needsStop(), false, false), this);
                }, () -> {
                    if (z) {
                        Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal(Lang.NO_ITEM_FOUND.effective().replace("{item}", String.valueOf(Formatting.RED) + Items.FIREWORK_ROCKET.getName().getString() + String.valueOf(Formatting.RESET))), 2L, TimeUnit.SECONDS);
                    }
                });
            }
        }
    }

    public void performSwap(boolean z) {
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        findSwapItem(z).ifPresentOrElse(class329Var -> {
            if (GrimDelayHandler.script.isFinished()) {
                ScopedSlot class246VarSlotReference = class329Var.slotReference();
                int iSlot = class246VarSlotReference.slot();
                if (class246VarSlotReference.scope() == InventoryScope.HOTBAR) {
                    SwapUtil.swapAction(() -> {
                        InventoryUtil.INSTANCE.windowClick(SlotActionType.SWAP, 6, iSlot, true);
                    });
                } else if (class246VarSlotReference.scope() == InventoryScope.INVENTORY) {
                    int i = player.getInventory().selectedSlot;
                    if (ServerUtil.isConnectedToServer("holyworld")) {
                        holySwap(iSlot, i);
                    } else {
                        SwapUtil.swapAction(() -> {
                            PlayerActionUtil.INSTANCE.swapHand(iSlot, Hand.MAIN_HAND, false);
                            PlayerActionUtil.INSTANCE.swapHand(6, Hand.MAIN_HAND, false);
                            PlayerActionUtil.INSTANCE.swapHand(iSlot, Hand.MAIN_HAND, true, true);
                        });
                    }
                }
                Expensive.INSTANCE.notificationRepository().post((Text) Text.literal(Lang.ELYTRAHELPER_SWAPPED_TO.effective().replace("{item}", String.valueOf(Formatting.RED) + (z ? "Chestplate" : Items.ELYTRA.getName().getString()) + String.valueOf(Formatting.RESET))), (z ? Items.NETHERITE_CHESTPLATE : Items.ELYTRA).getDefaultStack(), 3L, TimeUnit.SECONDS);
            }
        }, () -> {
            Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal(Lang.NO_ITEM_FOUND.effective().replace("{item}", String.valueOf(Formatting.RED) + (z ? "Chestplate" : Items.ELYTRA.getName().getString()) + String.valueOf(Formatting.RESET))), 3L, TimeUnit.SECONDS);
        });
    }

    public void holySwap(int i, int i2) {
        GrimDelayHandler.script.addTickStep(0, () -> {
            PlayerActionUtil.INSTANCE.windowClick(SlotActionType.SWAP, i, i2, true);
        }).addTickStep(1, () -> {
            PlayerActionUtil.INSTANCE.windowClick(SlotActionType.SWAP, 6, i2, true);
        }).addTickStep(2, () -> {
            PlayerActionUtil.INSTANCE.windowClick(SlotActionType.SWAP, i, i2, false);
            PlayerActionUtil.INSTANCE.updateSlots(true);
        });
    }
}
