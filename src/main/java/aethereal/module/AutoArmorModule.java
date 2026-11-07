package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.accessor.ArmorItemAccessor;
import aethereal.Expensive;
import aethereal.util.GrimDelayHandler;
import aethereal.util.InventoryItemFinder;
import aethereal.type.InventoryScope;
import aethereal.util.InventoryUtil;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.util.PlayerActionUtil;
import aethereal.event.PlayerTickEvent;
import aethereal.model.ScopedSlot;
import aethereal.ui.setting.Setting;
import aethereal.model.SlotSearchResult2;
import aethereal.math.Stopwatch;
import aethereal.util.SwapUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.SlotActionType;

@Aliases(aliases = {"Auto Armor", "Auto Equip", "Equip Armor", "Armor Manager", "Armor Swap"})
public class AutoArmorModule extends Module {
    public final Mc mc;
    public final Stopwatch swapStopwatch;

    public AutoArmorModule() {
        super(ModuleTab.PLAYER, "Auto Armor");
        this.mc = Mc.INSTANCE;
        this.swapStopwatch = new Stopwatch();
        addSettings(new Setting[0]);
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                ClientPlayerEntity player = this.mc.getPlayer();
                InventoryItemFinder class123VarSearcher = Expensive.INSTANCE.inventoryService().searcher();
                ArrayList<Runnable> arrayList = new ArrayList<>();
                if (this.mc.getCurrentScreen() == null || (this.mc.getCurrentScreen() instanceof InventoryScreen)) {
                    for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                        if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                            ItemStack armorStack = player.getInventory().getArmorStack(equipmentSlot.getEntitySlotId());
                            Optional<SlotSearchResult2> optionalMax = class123VarSearcher.findAllItems(itemStack -> {
                                return (itemStack.getItem() instanceof ArmorItem) && ((ArmorItemAccessor) itemStack.getItem()).expensive_ru$getType().getEquipmentSlot() == equipmentSlot && !hasBindingCurse(itemStack);
                            }, InventoryScope.ALL).stream().max(Comparator.comparingDouble(class329Var -> {
                                return getArmorScore(class329Var.stack(), (ArmorItemAccessor) class329Var.stack().getItem());
                            }));
                            if (optionalMax.isPresent()) {
                                SlotSearchResult2 class329Var2 = optionalMax.get();
                                InventoryUtil class103Var = InventoryUtil.INSTANCE;
                                if (class329Var2.found() && isBetterArmor(class329Var2.stack(), armorStack)) {
                                    ScopedSlot class246VarSlotReference = class329Var2.slotReference();
                                    int iSlot = class246VarSlotReference.slot();
                                    int armorSlotInventoryIndex = class103Var.getArmorSlotInventoryIndex(equipmentSlot);
                                    if (class246VarSlotReference.scope() == InventoryScope.INVENTORY) {
                                        arrayList.add(() -> {
                                            class103Var.swapTo(iSlot, armorSlotInventoryIndex);
                                        });
                                    } else if (class246VarSlotReference.scope() == InventoryScope.HOTBAR) {
                                        arrayList.add(() -> {
                                            class103Var.windowClick(SlotActionType.SWAP, armorSlotInventoryIndex, iSlot, true);
                                        });
                                    }
                                    this.swapStopwatch.reset();
                                }
                            }
                        }
                    }
                    if (arrayList.isEmpty() || !GrimDelayHandler.script.isFinished()) {
                        return;
                    }
                    SwapUtil.swapAction(() -> {
                        arrayList.forEach((v0) -> {
                            v0.run();
                        });
                        PlayerActionUtil.INSTANCE.updateSlots(false);
                    });
                }
            }
        });
    }

    public float getArmorScore(ItemStack itemStack, ArmorItemAccessor class327Var) {
        Registry orThrow = this.mc.getWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        ArmorMaterial armorMaterialExpensive_ru$getMaterial = class327Var.expensive_ru$getMaterial();
        return ((Integer) armorMaterialExpensive_ru$getMaterial.defense().getOrDefault(class327Var.expensive_ru$getType(), 0)).intValue() + armorMaterialExpensive_ru$getMaterial.toughness() + EnchantmentHelper.getLevel((RegistryEntry) orThrow.getEntry(Enchantments.PROTECTION.getValue()).orElseThrow(), itemStack) + (EnchantmentHelper.getLevel((RegistryEntry) orThrow.getEntry(Enchantments.UNBREAKING.getValue()).orElseThrow(), itemStack) * 0.1f) + (EnchantmentHelper.getLevel((RegistryEntry) orThrow.getEntry(Enchantments.MENDING.getValue()).orElseThrow(), itemStack) * 0.2f);
    }

    public boolean hasBindingCurse(ItemStack itemStack) {
        Registry orThrow = this.mc.getPlayer().getWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        RegistryEntry entry = orThrow.getEntry((Enchantment) orThrow.get(Enchantments.BINDING_CURSE));
        return entry != null && EnchantmentHelper.getLevel(entry, itemStack) > 0;
    }

    public boolean isBetterArmor(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack2.isEmpty()) {
            return true;
        }
        if (!(itemStack.getItem() instanceof ArmorItem armorItem)) {
            return false;
        }
        if ((itemStack2.getItem()) instanceof ArmorItem item2 ) {
            return getArmorScore(itemStack, (ArmorItemAccessor) armorItem) > getArmorScore(itemStack2, (ArmorItemAccessor) item2);
        }
        return false;
    }
}
