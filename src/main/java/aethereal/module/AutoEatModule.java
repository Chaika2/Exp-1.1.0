package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.ui.setting.BooleanSetting;
import aethereal.Expensive;
import aethereal.util.GrimDelayHandler;
import aethereal.type.InventoryScope;
import aethereal.util.InventoryUtil;
import aethereal.util.ItemUseController;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.util.PlayerActionUtil;
import aethereal.event.PlayerTickEvent;
import aethereal.model.ScopedSlot;
import aethereal.model.SlotSearchResult2;
import aethereal.util.SwapUtil;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

@Aliases(aliases = {"Auto Eat", "Auto Food", "Automatic Eating", "Food Helper", "Auto Hunger Restore", "Eat Manager", "Auto Satiety", "Food Auto"})
public class AutoEatModule extends Module {
    public final BooleanSetting ignoreGoldenApplesSetting;
    public final BooleanSetting ignoreEnchantedGoldenApplesSetting;
    public boolean eating;
    public int savedHotbarSlot;
    public int pendingSwapSlot;
    public final Mc mc;
    public static final Set<StatusEffect> negativeEffects = Set.of((StatusEffect) StatusEffects.HUNGER.value(), (StatusEffect) StatusEffects.POISON.value(), (StatusEffect) StatusEffects.NAUSEA.value(), (StatusEffect) StatusEffects.WEAKNESS.value(), (StatusEffect) StatusEffects.BLINDNESS.value());

    public AutoEatModule() {
        super(ModuleTab.PLAYER, "Auto Eat");
        this.ignoreGoldenApplesSetting = new BooleanSetting(Lang.AUTOEAT_IGNORE_GOLDEN_APPLES);
        this.ignoreEnchantedGoldenApplesSetting = new BooleanSetting(Lang.AUTOEAT_IGNORE_ENCHANTED_GOLDEN_APPLES);
        this.savedHotbarSlot = -1;
        this.pendingSwapSlot = -1;
        this.mc = Mc.INSTANCE;
        addSettings(this.ignoreGoldenApplesSetting, this.ignoreEnchantedGoldenApplesSetting);
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && this.mc.isWorldLoaded() && class130Var.isPre()) {
                ClientPlayerEntity player = this.mc.getPlayer();
                GameOptions gameOptions = this.mc.getGameOptions();
                ItemStack offHandStack = player.getOffHandStack();
                ItemStack mainHandStack = player.getMainHandStack();
                boolean zIsNotFull = player.getHungerManager().isNotFull();
                boolean z = isEdible(offHandStack) || isEdible(mainHandStack);
                if (zIsNotFull && z) {
                    Hand handMethod013 = getFoodHand(player);
                    if (handMethod013 != null) {
                        ItemUseController.INSTANCE.useHand(handMethod013);
                        this.eating = true;
                        return;
                    }
                    return;
                }
                if (this.eating) {
                    stopEating(gameOptions, player);
                } else if (zIsNotFull) {
                    selectFood(player);
                }
            }
        });
    }

    public Hand getFoodHand(ClientPlayerEntity clientPlayerEntity) {
        ItemStack mainHandStack = clientPlayerEntity.getMainHandStack();
        ItemStack offHandStack = clientPlayerEntity.getOffHandStack();
        boolean zMethod015 = isEdible(mainHandStack);
        if (isEdible(offHandStack)) {
            return Hand.OFF_HAND;
        }
        if (zMethod015) {
            return Hand.MAIN_HAND;
        }
        return null;
    }

    public boolean canUseOffhandForFood(ClientPlayerEntity clientPlayerEntity) {
        AutoTotemModule autoTotemModule = Expensive.INSTANCE.moduleRepository().get(AutoTotemModule.class);
        return !autoTotemModule.isState() || autoTotemModule.findTotems(clientPlayerEntity).isEmpty();
    }

    public boolean isEating() {
        return this.eating;
    }

    public void stopEating(GameOptions gameOptions, ClientPlayerEntity clientPlayerEntity) {
        if (this.pendingSwapSlot == -1 && this.savedHotbarSlot == -1) {
            this.eating = false;
        } else {
            restoreSlots(clientPlayerEntity);
        }
    }

    public void restoreSlots(ClientPlayerEntity clientPlayerEntity) {
        if (this.savedHotbarSlot != -1) {
            clientPlayerEntity.getInventory().selectedSlot = this.savedHotbarSlot;
            this.eating = false;
            this.savedHotbarSlot = -1;
        }
        if (this.pendingSwapSlot == -1 || !GrimDelayHandler.script.isFinished()) {
            return;
        }
        SwapUtil.swapAction(() -> {
            PlayerActionUtil.INSTANCE.windowClick(SlotActionType.SWAP, this.pendingSwapSlot, clientPlayerEntity.getInventory().selectedSlot, true);
            this.pendingSwapSlot = -1;
            this.eating = false;
        });
    }

    public void selectFood(ClientPlayerEntity clientPlayerEntity) {
        List<SlotSearchResult2> list = Expensive.INSTANCE.inventoryService().searcher().findAllItems(itemStack -> {
            return itemStack.getItem().getComponents().get(DataComponentTypes.FOOD) != null;
        }, InventoryScope.HOTBAR, InventoryScope.INVENTORY).stream().filter(class329Var -> {
            return !shouldIgnore(class329Var.stack().getItem());
        }).sorted(Comparator.comparingInt((SlotSearchResult2 class329Var2) -> {
            return getFoodPriority(class329Var2.stack());
        }).thenComparingDouble((SlotSearchResult2 class329Var3) -> {
            return -getFoodValue(class329Var3.stack());
        })).toList();
        if (list.isEmpty()) {
            return;
        }
        SlotSearchResult2 class329Var4 = (SlotSearchResult2) list.getFirst();
        if (class329Var4.found()) {
            ScopedSlot class246VarSlotReference = class329Var4.slotReference();
            int iSlot = class246VarSlotReference.slot();
            if (clientPlayerEntity.getOffHandStack().isEmpty() && GrimDelayHandler.script.isFinished() && canUseOffhandForFood(clientPlayerEntity)) {
                SwapUtil.swapToOffhand(class246VarSlotReference.increasedSlot());
                return;
            }
            if (class246VarSlotReference.scope() == InventoryScope.INVENTORY && GrimDelayHandler.script.isFinished()) {
                this.pendingSwapSlot = iSlot;
                SwapUtil.swapAction(() -> {
                    InventoryUtil.INSTANCE.windowClick(SlotActionType.SWAP, iSlot, clientPlayerEntity.getInventory().selectedSlot, true);
                });
            } else if (class246VarSlotReference.scope() == InventoryScope.HOTBAR) {
                this.savedHotbarSlot = clientPlayerEntity.getInventory().selectedSlot;
                clientPlayerEntity.getInventory().selectedSlot = iSlot;
            }
        }
    }

    public int getFoodPriority(ItemStack itemStack) {
        boolean zMethod007 = isGoldenApple(itemStack.getItem());
        boolean zMethod001 = isSafeFood(itemStack);
        boolean zMethod012 = isEnchantedGoldenApple(itemStack.getItem());
        if (!zMethod001 || zMethod007 || zMethod012) {
            return (zMethod007 || zMethod012) ? 2 : 3;
        }
        return 1;
    }

    public boolean isSafeFood(ItemStack itemStack) {
        ConsumableComponent consumableComponent = (ConsumableComponent) itemStack.getComponents().get(DataComponentTypes.CONSUMABLE);
        if (consumableComponent == null) {
            return false;
        }
        for (net.minecraft.item.consume.ConsumeEffect consumeEffect : consumableComponent.onConsumeEffects()) {
            if (consumeEffect instanceof ApplyEffectsConsumeEffect) {
                ApplyEffectsConsumeEffect applyEffectsConsumeEffect = (ApplyEffectsConsumeEffect) consumeEffect;
                Iterator it = applyEffectsConsumeEffect.effects().iterator();
                while (it.hasNext()) {
                    if (negativeEffects.contains(((StatusEffectInstance) it.next()).getEffectType().value())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public float getFoodValue(ItemStack itemStack) {
        FoodComponent foodComponent = (FoodComponent) itemStack.getComponents().get(DataComponentTypes.FOOD);
        if (foodComponent == null) {
            return 0.0f;
        }
        return foodComponent.nutrition() * foodComponent.saturation();
    }

    public boolean isGoldenApple(Item item) {
        return item == Items.GOLDEN_APPLE;
    }

    public boolean isEnchantedGoldenApple(Item item) {
        return item == Items.ENCHANTED_GOLDEN_APPLE;
    }

    public boolean shouldIgnore(Item item) {
        if (this.ignoreGoldenApplesSetting.isValue() && isGoldenApple(item)) {
            return true;
        }
        return this.ignoreEnchantedGoldenApplesSetting.isValue() && isEnchantedGoldenApple(item);
    }

    public boolean isEdible(ItemStack itemStack) {
        return (((FoodComponent) itemStack.get(DataComponentTypes.FOOD)) == null || shouldIgnore(itemStack.getItem())) ? false : true;
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.pendingSwapSlot = -1;
        this.savedHotbarSlot = -1;
    }
}
