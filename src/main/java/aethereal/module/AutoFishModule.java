package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.ui.setting.BooleanSetting;
import aethereal.Expensive;
import aethereal.util.GrimDelayHandler;
import aethereal.type.InventoryScope;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.type.NotificationType;
import aethereal.util.PlayerActionUtil;
import aethereal.event.PlayerTickEvent;
import aethereal.model.ScopedSlot;
import aethereal.model.SlotSearchResult2;
import aethereal.util.SwapUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

@Aliases(aliases = {"Auto Fish", "Fishing Helper", "Automatic Fishing", "Fishing Bot", "Fish Catcher", "Fishing Manager", "Auto Rod", "Fishing Automation"})
public class AutoFishModule extends Module {
    public final BooleanSetting saveRod;
    public final Mc mc;
    public boolean reeling;
    public int recastTicks;

    public AutoFishModule() {
        super(ModuleTab.PLAYER, "Auto Fish");
        this.saveRod = new BooleanSetting(Lang.AUTOFISH_SAVE_ROD);
        this.mc = Mc.INSTANCE;
        addSettings(this.saveRod);
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && this.mc.isWorldLoaded() && class130Var.isPre()) {
                ClientPlayerEntity player = this.mc.getPlayer();
                ClientPlayerInteractionManager interactionManager = this.mc.getInteractionManager();
                Hand handMethod017 = findRodHand(player);
                if (handMethod017 == null) {
                    return;
                }
                if (isRodLowDurability(player.getStackInHand(handMethod017)) && this.saveRod.isValue()) {
                    if (!swapToBestRod(player, interactionManager, handMethod017)) {
                        Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal(Lang.AUTOFISH_NO_RODS.effective()), 3L, TimeUnit.SECONDS);
                        switchState();
                        return;
                    }
                    finishRodSwap(player, interactionManager, handMethod017);
                }
                if (player.fishHook != null && player.fishHook.caughtFish && !this.reeling) {
                    useRod(interactionManager, player, handMethod017);
                    this.reeling = true;
                }
                if (this.reeling) {
                    int i = this.recastTicks + 1;
                    this.recastTicks = i;
                    if (i > 5) {
                        useRod(interactionManager, player, handMethod017);
                        reset();
                    }
                }
            }
        });
    }

    public Hand findRodHand(ClientPlayerEntity clientPlayerEntity) {
        return (Hand) Arrays.stream(Hand.values()).filter(hand -> {
            return clientPlayerEntity.getStackInHand(hand).getItem() == Items.FISHING_ROD;
        }).findFirst().orElse(null);
    }

    public boolean isRodLowDurability(ItemStack itemStack) {
        return (itemStack.getItem() instanceof FishingRodItem) && !isRodDurable(itemStack);
    }

    public void useRod(ClientPlayerInteractionManager clientPlayerInteractionManager, ClientPlayerEntity clientPlayerEntity, Hand hand) {
        ActionResult successInteractItem = clientPlayerInteractionManager.interactItem(clientPlayerEntity, hand);
        if ((successInteractItem instanceof ActionResult.Success success) && success.swingSource() == ActionResult.SwingSource.CLIENT) {
            clientPlayerEntity.swingHand(hand);
        }
    }

    public boolean swapToBestRod(ClientPlayerEntity clientPlayerEntity, ClientPlayerInteractionManager clientPlayerInteractionManager, Hand hand) {
        List<SlotSearchResult2> listMethod012 = findAvailableRods();
        if (listMethod012.isEmpty()) {
            return false;
        }
        return performRodSwap((SlotSearchResult2) listMethod012.getFirst(), clientPlayerEntity, clientPlayerInteractionManager, hand);
    }

    public List<SlotSearchResult2> findAvailableRods() {
        return Expensive.INSTANCE.inventoryService().searcher().findAllItems(this::isRodDurable, InventoryScope.HOTBAR, InventoryScope.INVENTORY).stream().sorted(rodComparator()).toList();
    }

    public Comparator<SlotSearchResult2> rodComparator() {
        return Comparator.comparingInt((SlotSearchResult2 class329Var) -> {
            return getRemainingDurability(class329Var.stack());
        }).thenComparingInt((SlotSearchResult2 class329Var2) -> {
            return getUnbreakingLevel(class329Var2.stack());
        }).reversed();
    }

    public int getRemainingDurability(ItemStack itemStack) {
        return itemStack.getMaxDamage() - itemStack.getDamage();
    }

    public int getUnbreakingLevel(ItemStack itemStack) {
        Registry orThrow = this.mc.getPlayer().getWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        return EnchantmentHelper.getLevel(orThrow.getEntry((Enchantment) orThrow.get(Enchantments.UNBREAKING)), itemStack);
    }

    public boolean performRodSwap(SlotSearchResult2 class329Var, ClientPlayerEntity clientPlayerEntity, ClientPlayerInteractionManager clientPlayerInteractionManager, Hand hand) {
        ScopedSlot class246VarSlotReference = class329Var.slotReference();
        InventoryScope class305VarScope = class246VarSlotReference.scope();
        int i = clientPlayerEntity.getInventory().selectedSlot;
        if (class305VarScope == InventoryScope.INVENTORY && GrimDelayHandler.script.isFinished()) {
            SwapUtil.swapAction(() -> {
                PlayerActionUtil.INSTANCE.windowClick(SlotActionType.SWAP, class246VarSlotReference.slot(), i, true);
            });
            return true;
        }
        if (class305VarScope != InventoryScope.HOTBAR) {
            return false;
        }
        clientPlayerEntity.getInventory().selectedSlot = class246VarSlotReference.slot();
        return true;
    }

    public void finishRodSwap(ClientPlayerEntity clientPlayerEntity, ClientPlayerInteractionManager clientPlayerInteractionManager, Hand hand) {
        if (clientPlayerEntity.fishHook != null) {
            useRod(clientPlayerInteractionManager, clientPlayerEntity, hand);
        }
        this.reeling = true;
    }

    public boolean isRodDurable(ItemStack itemStack) {
        return (itemStack.getItem() instanceof FishingRodItem) && itemStack.getMaxDamage() - itemStack.getDamage() > 10;
    }

    public void reset() {
        this.reeling = false;
        this.recastTicks = 0;
    }

    @Override
    public void deactivate() {
        reset();
        super.deactivate();
    }
}
