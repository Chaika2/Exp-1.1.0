package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.model.AttributeModifierSpec;
import aethereal.type.AuctionArmorType;
import aethereal.type.AuctionAxeType;
import aethereal.type.AuctionElytraType;
import aethereal.model.AuctionItemListing;
import aethereal.type.AuctionPickaxeType;
import aethereal.type.AuctionShovelType;
import aethereal.type.AuctionSwordType;
import aethereal.ui.setting.BooleanSetting;
import aethereal.util.EventDispatcher;
import aethereal.ui.setting.ExpandableSetting;
import aethereal.Expensive;
import aethereal.event.HandledScreenRenderEvent;
import aethereal.type.ItemCategory;
import aethereal.ui.setting.KeybindSetting;
import aethereal.ui.setting.LabelSelectSetting;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.ui.setting.NumberSetting;
import aethereal.event.PacketSendEvent;
import aethereal.net.PacketSender;
import aethereal.model.PotionEffectPreset;
import aethereal.model.PotionEffectSpec;
import aethereal.type.SettingUnit;
import aethereal.ui.setting.TextFieldSetting;
import aethereal.net.TooltipPriceReader;
import aethereal.model.Translation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;

@Aliases(aliases = {"Auction Helper", "Auc Helper", "Ah Helper", "Market Helper", "Calculator", "Helper"})
public class AuctionHelperModule extends Module {
    public final Pattern multiplyPattern;
    public final TooltipPriceReader priceReader;
    public final BooleanSetting calculatorSetting;
    public final NumberSetting durabilitySetting;
    public final BooleanSetting balanceInclusionSetting;
    public final BooleanSetting enchantmentsOnlySetting;
    public final MultiSelectSetting<AuctionArmorType> armorFilterSetting;
    public final MultiSelectSetting<AuctionElytraType> elytraFilterSetting;

    public final MultiSelectSetting<AuctionSwordType> swordFilterSetting;
    public final MultiSelectSetting<AuctionPickaxeType> pickaxeFilterSetting;
    public final MultiSelectSetting<AuctionAxeType> axeFilterSetting;
    public final MultiSelectSetting<AuctionShovelType> shovelFilterSetting;
    public final MultiSelectSetting<ItemCategory> excludeCategorySetting;

    public final LabelSelectSetting countFilterSetting;

    public final NumberSetting countValueSetting;

    public final ExpandableSetting countSetting;

    public final TextFieldSetting searchCommandSetting;
    public final ExpandableSetting filtersSetting;

    public List<AttributeModifierSpec> parseAttributeModifiers(NbtList nbtList) {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound compound = nbtList.getCompound(i);
            arrayList.add(new AttributeModifierSpec(compound.getString("AttributeName"), compound.getDouble("Amount"), compound.getString("Slot")));
        }
        return arrayList;
    }

    public PotionEffectPreset parsePotionEffects(NbtList nbtList) {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound compound = nbtList.getCompound(i);
            arrayList.add(new PotionEffectSpec(compound.getString("id"), compound.getByte("amplifier") & 255, compound.getInt("duration")));
        }
        return new PotionEffectPreset(arrayList);
    }

    public int getCustomEnchantmentLevel(ItemStack itemStack, String str) {
        NbtList list = getCustomData(itemStack).getList("custom-enchantments", 10);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound compound = list.getCompound(i);
            if (compound.getString("type").equals(str)) {
                return compound.getInt("level");
            }
        }
        return -1;
    }

    public List<AttributeModifierSpec> getAttributeModifiers(ItemStack itemStack) {
        return parseAttributeModifiers(getCustomData(itemStack).getList("AttributeModifiers", 10));
    }

    public PotionEffectPreset getPotionEffects(ItemStack itemStack) {
        return parsePotionEffects(getCustomData(itemStack).getList("custom_potion_effects", 10));
    }

    public boolean isMatchingItem(ItemStack itemStack) {
        if (itemStack.isOf(Items.GRAY_DYE) && itemStack.getName().getString().contains("Товар не актуален")) {
            return false;
        }
        if (itemStack.isDamageable() && (itemStack.getMaxDamage() - itemStack.getDamage()) / itemStack.getMaxDamage() < this.durabilitySetting.currentValue() / 100.0f) {
            return false;
        }
        if (this.enchantmentsOnlySetting.isValue() && !hasEnchantments(itemStack)) {
            return false;
        }
        if (this.countSetting.isValue()) {
            Translation selected = this.countFilterSetting.getSelected();
            int count = itemStack.getCount();
            int iFloor = (int) Math.floor(this.countValueSetting.currentValue());
            if (selected == Lang.AUCTION_HELPER_COUNT_FILTER_EXACT) {
                if (count != iFloor) {
                    return false;
                }
            } else if (selected == Lang.AUCTION_HELPER_COUNT_FILTER_MINIMAL && count < iFloor) {
                return false;
            }
        }
        if (!this.filtersSetting.isValue()) {
            return true;
        }
        Item item = itemStack.getItem();
        getAttributeModifiers(itemStack);
        getCustomData(itemStack);
        if (this.excludeCategorySetting.isSelected(ItemCategory.ARROWS) && (item instanceof ArrowItem)) {
            return false;
        }
        if (this.excludeCategorySetting.isSelected(ItemCategory.GLASS_BOTTLES) && itemStack.isOf(Items.GLASS_BOTTLE)) {
            return false;
        }
        if (this.excludeCategorySetting.isSelected(ItemCategory.DRAGON_BREATH) && itemStack.isOf(Items.DRAGON_BREATH)) {
            return false;
        }
        if (item instanceof ArmorItem) {
            if (this.excludeCategorySetting.isSelected(ItemCategory.ARMOR)) {
                return false;
            }
            if (this.armorFilterSetting.isSelected(AuctionArmorType.WITHOUT_THORNS) && getEnchantmentLevel(itemStack, "minecraft:thorns") != -1) {
                return false;
            }
            if (this.armorFilterSetting.isSelected(AuctionArmorType.PROTECTION5) && getEnchantmentLevel(itemStack, "minecraft:protection") < 5) {
                return false;
            }
            if (this.armorFilterSetting.isSelected(AuctionArmorType.UNBREAKING5) && getEnchantmentLevel(itemStack, "minecraft:unbreaking") < 5) {
                return false;
            }
            if (this.armorFilterSetting.isSelected(AuctionArmorType.MENDING) && getEnchantmentLevel(itemStack, "minecraft:mending") == -1) {
                return false;
            }
        }
        if (item == Items.ELYTRA) {
            if (this.excludeCategorySetting.isSelected(ItemCategory.ELYTRA)) {
                return false;
            }
            if (this.elytraFilterSetting.isSelected(AuctionElytraType.UNBREAKING5) && getEnchantmentLevel(itemStack, "minecraft:unbreaking") < 5) {
                return false;
            }
            if (this.elytraFilterSetting.isSelected(AuctionElytraType.MENDING) && getEnchantmentLevel(itemStack, "minecraft:mending") == -1) {
                return false;
            }
        }
        if (itemStack.isOf(Items.TOTEM_OF_UNDYING) && this.excludeCategorySetting.isSelected(ItemCategory.TOTEMS)) {
            return false;
        }
        if (itemStack.isOf(Items.PLAYER_HEAD) && this.excludeCategorySetting.isSelected(ItemCategory.PLAYER_HEADS)) {
            return false;
        }
        if ((item instanceof TridentItem) && this.excludeCategorySetting.isSelected(ItemCategory.TRIDENT)) {
            return false;
        }
        if ((item instanceof BowItem) && this.excludeCategorySetting.isSelected(ItemCategory.BOWS)) {
            return false;
        }
        if ((item instanceof CrossbowItem) && this.excludeCategorySetting.isSelected(ItemCategory.CROSSBOWS)) {
            return false;
        }
        if ((item instanceof FlintAndSteelItem) && this.excludeCategorySetting.isSelected(ItemCategory.FLINT_AND_STEEL)) {
            return false;
        }
        Identifier id = Registries.ITEM.getId(item);
        if (this.excludeCategorySetting.isSelected(ItemCategory.HORSE_ARMOR) && "minecraft".equals(id.getNamespace()) && id.getPath().endsWith("_horse_armor")) {
            return false;
        }
        if (item instanceof SwordItem) {
            if (this.excludeCategorySetting.isSelected(ItemCategory.SWORDS)) {
                return false;
            }
            if (this.swordFilterSetting.isSelected(AuctionSwordType.WITHOUT_KNOCKBACK) && getEnchantmentLevel(itemStack, "minecraft:knockback") != -1) {
                return false;
            }
            if (this.swordFilterSetting.isSelected(AuctionSwordType.SHARPNESS) && getEnchantmentLevel(itemStack, "minecraft:sharpness") == -1) {
                return false;
            }
            if (this.swordFilterSetting.isSelected(AuctionSwordType.UNBREAKING) && getEnchantmentLevel(itemStack, "minecraft:unbreaking") == -1) {
                return false;
            }
            if (this.swordFilterSetting.isSelected(AuctionSwordType.POISON) && getCustomEnchantmentLevel(itemStack, "poison") == -1) {
                return false;
            }
            if (this.swordFilterSetting.isSelected(AuctionSwordType.DETECTION) && getCustomEnchantmentLevel(itemStack, "detection") == -1) {
                return false;
            }
            if (this.swordFilterSetting.isSelected(AuctionSwordType.OXIDATION) && getCustomEnchantmentLevel(itemStack, "oxidation") == -1) {
                return false;
            }
            if (this.swordFilterSetting.isSelected(AuctionSwordType.VAMPIRISM) && getCustomEnchantmentLevel(itemStack, "vampirism") == -1) {
                return false;
            }
        }
        if (item instanceof PickaxeItem) {
            if (this.excludeCategorySetting.isSelected(ItemCategory.PICKAXE)) {
                return false;
            }
            if (this.pickaxeFilterSetting.isSelected(AuctionPickaxeType.EFFICIENCY) && getEnchantmentLevel(itemStack, "minecraft:efficiency") == -1) {
                return false;
            }
            if (this.pickaxeFilterSetting.isSelected(AuctionPickaxeType.MENDING) && getEnchantmentLevel(itemStack, "minecraft:mending") == -1) {
                return false;
            }
            if (this.pickaxeFilterSetting.isSelected(AuctionPickaxeType.FORTUNE) && getEnchantmentLevel(itemStack, "minecraft:fortune") == -1) {
                return false;
            }
            if (this.pickaxeFilterSetting.isSelected(AuctionPickaxeType.UNBREAKING) && getEnchantmentLevel(itemStack, "minecraft:unbreaking") == -1) {
                return false;
            }
            if (this.pickaxeFilterSetting.isSelected(AuctionPickaxeType.SILK_TOUCH) && getEnchantmentLevel(itemStack, "minecraft:silk_touch") == -1) {
                return false;
            }
            if (this.pickaxeFilterSetting.isSelected(AuctionPickaxeType.SMELTING) && getCustomEnchantmentLevel(itemStack, "smelting") == -1) {
                return false;
            }
            if (this.pickaxeFilterSetting.isSelected(AuctionPickaxeType.MAGNET) && getCustomEnchantmentLevel(itemStack, "magnet") == -1) {
                return false;
            }
            if (this.pickaxeFilterSetting.isSelected(AuctionPickaxeType.BULLDOZING)) {
                int iMethod013 = getCustomEnchantmentLevel(itemStack, "buldozing");
                int iMethod014 = getCustomEnchantmentLevel(itemStack, "megabuldozing");
                if (iMethod013 == -1 && iMethod014 == -1) {
                    return false;
                }
            }
            if (this.pickaxeFilterSetting.isSelected(AuctionPickaxeType.WITHOUT_HEAVY) && getCustomEnchantmentLevel(itemStack, "heavy") != -1) {
                return false;
            }
        }
        if (item instanceof AxeItem) {
            if (this.excludeCategorySetting.isSelected(ItemCategory.AXE)) {
                return false;
            }
            if (this.axeFilterSetting.isSelected(AuctionAxeType.EFFICIENCY) && getEnchantmentLevel(itemStack, "minecraft:efficiency") == -1) {
                return false;
            }
            if (this.axeFilterSetting.isSelected(AuctionAxeType.MENDING) && getEnchantmentLevel(itemStack, "minecraft:mending") == -1) {
                return false;
            }
            if (this.axeFilterSetting.isSelected(AuctionAxeType.FORTUNE) && getEnchantmentLevel(itemStack, "minecraft:fortune") == -1) {
                return false;
            }
            if (this.axeFilterSetting.isSelected(AuctionAxeType.UNBREAKING) && getEnchantmentLevel(itemStack, "minecraft:unbreaking") == -1) {
                return false;
            }
            if (this.axeFilterSetting.isSelected(AuctionAxeType.MAGNET) && getCustomEnchantmentLevel(itemStack, "magnet") == -1) {
                return false;
            }
            if (this.axeFilterSetting.isSelected(AuctionAxeType.LUMBERJACK) && getCustomEnchantmentLevel(itemStack, "lumberjack") == -1) {
                return false;
            }
            if (this.axeFilterSetting.isSelected(AuctionAxeType.PINGER) && getCustomEnchantmentLevel(itemStack, "pinger") == -1) {
                return false;
            }
            if (this.axeFilterSetting.isSelected(AuctionAxeType.BULLDOZING)) {
                int iMethod015 = getCustomEnchantmentLevel(itemStack, "buldozing");
                int iMethod016 = getCustomEnchantmentLevel(itemStack, "megabuldozing");
                if (iMethod015 == -1 && iMethod016 == -1) {
                    return false;
                }
            }
        }
        if (item instanceof ShovelItem) {
            if (this.excludeCategorySetting.isSelected(ItemCategory.SHOVEL)) {
                return false;
            }
            if (this.shovelFilterSetting.isSelected(AuctionShovelType.EFFICIENCY) && getEnchantmentLevel(itemStack, "minecraft:efficiency") == -1) {
                return false;
            }
            if (this.shovelFilterSetting.isSelected(AuctionShovelType.MENDING) && getEnchantmentLevel(itemStack, "minecraft:mending") == -1) {
                return false;
            }
            if (this.shovelFilterSetting.isSelected(AuctionShovelType.FORTUNE) && getEnchantmentLevel(itemStack, "minecraft:fortune") == -1) {
                return false;
            }
            if (this.shovelFilterSetting.isSelected(AuctionShovelType.UNBREAKING) && getEnchantmentLevel(itemStack, "minecraft:unbreaking") == -1) {
                return false;
            }
            if (this.shovelFilterSetting.isSelected(AuctionShovelType.MAGNET) && getCustomEnchantmentLevel(itemStack, "magnet") == -1) {
                return false;
            }
            if (this.shovelFilterSetting.isSelected(AuctionShovelType.BULLDOZING)) {
                int iMethod017 = getCustomEnchantmentLevel(itemStack, "buldozing");
                int iMethod018 = getCustomEnchantmentLevel(itemStack, "megabuldozing");
                if (iMethod017 == -1 && iMethod018 == -1) {
                    return false;
                }
            }
        }
        return ((item instanceof HoeItem) && this.excludeCategorySetting.isSelected(ItemCategory.HOE)) ? false : true;
    }

    public static boolean hasEnchantments(ItemStack itemStack) {
        ItemEnchantmentsComponent itemEnchantmentsComponent = (ItemEnchantmentsComponent) itemStack.get(DataComponentTypes.ENCHANTMENTS);
        return (itemEnchantmentsComponent == null || itemEnchantmentsComponent.isEmpty()) ? false : true;
    }

    public NbtCompound getCustomData(ItemStack itemStack) {
        NbtComponent nbtComponent = (NbtComponent) itemStack.get(DataComponentTypes.CUSTOM_DATA);
        return nbtComponent != null ? nbtComponent.copyNbt() : new NbtCompound();
    }

    public static int getEnchantmentLevel(ItemStack itemStack, String str) {
        int level;
        Identifier identifierOf = Identifier.of(str);
        Registry orThrow = Mc.INSTANCE.getPlayer().getWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        Enchantment enchantment = (Enchantment) orThrow.get(identifierOf);
        if (enchantment != null && (level = EnchantmentHelper.getLevel(orThrow.getEntry(enchantment), itemStack)) > 0) {
            return level;
        }
        return -1;
    }

    public AuctionHelperModule() {
        super(ModuleTab.MISC, "Auction Helper");
        this.multiplyPattern = Pattern.compile("(\\d+)\\*(\\d+)");
        this.priceReader = new TooltipPriceReader();
        this.calculatorSetting = new BooleanSetting(Lang.AUCTION_HELPER_CALCULATOR, Lang.AUCTION_HELPER_CALCULATOR_DESC).setValue(true);
        this.durabilitySetting = new NumberSetting(Lang.AUCTION_HELPER_DURABILITY, Lang.AUCTION_HELPER_DURABILITY_DESC).range(0.0f, 100.0f).currentValue(95.0f).unit(SettingUnit.PERCENTS);
        this.balanceInclusionSetting = new BooleanSetting(Lang.AUCTION_HELPER_BALANCE_INCLUSION, Lang.AUCTION_HELPER_BALANCE_INCLUSION_DESC).setValue(true);
        this.enchantmentsOnlySetting = new BooleanSetting(Lang.AUCTION_HELPER_ENCHANTMENTS_ONLY).setValue(false);
        this.armorFilterSetting = new MultiSelectSetting(Lang.AUCTION_HELPER_ARMOR, Lang.AUCTION_HELPER_ARMOR_DESC).values(AuctionArmorType.class);
        this.elytraFilterSetting = new MultiSelectSetting(Lang.AUCTION_HELPER_ELYTRA, Lang.AUCTION_HELPER_ELYTRA_DESC).values(AuctionElytraType.class);
        this.swordFilterSetting = new MultiSelectSetting(Lang.AUCTION_HELPER_SWORDS, Lang.AUCTION_HELPER_SWORDS_DESC).values(AuctionSwordType.class);
        this.pickaxeFilterSetting = new MultiSelectSetting(Lang.AUCTION_HELPER_PICKAXES, Lang.AUCTION_HELPER_PICKAXES_DESC).values(AuctionPickaxeType.class);
        this.axeFilterSetting = new MultiSelectSetting(Lang.AUCTION_HELPER_AXES, Lang.AUCTION_HELPER_AXES_DESC).values(AuctionAxeType.class);
        this.shovelFilterSetting = new MultiSelectSetting(Lang.AUCTION_HELPER_SHOVELS, Lang.AUCTION_HELPER_SHOVELS_DESC).values(AuctionShovelType.class);
        this.excludeCategorySetting = new MultiSelectSetting(Lang.AUCTION_HELPER_EXCLUDES, Lang.AUCTION_HELPER_EXCLUDES_DESC).values(ItemCategory.class);
        this.countFilterSetting = new LabelSelectSetting(Lang.AUCTION_HELPER_COUNT_FILTER, Lang.AUCTION_HELPER_COUNT_FILTER_DESC).value(Lang.AUCTION_HELPER_COUNT_FILTER_EXACT, Lang.AUCTION_HELPER_COUNT_FILTER_MINIMAL);
        this.countValueSetting = new NumberSetting(Lang.AUCTION_HELPER_COUNT_VALUE, Lang.AUCTION_HELPER_COUNT_VALUE_DESC).range(1.0f, 64.0f).currentValue(1.0f).step(1.0f);
        this.countSetting = new ExpandableSetting(Lang.AUCTION_HELPER_COUNT, Lang.AUCTION_HELPER_COUNT_DESC).settings(this.countFilterSetting, this.countValueSetting);
        this.searchCommandSetting = new TextFieldSetting(Lang.AUCTION_HELPER_SEARCH_COMMAND, Lang.AUCTION_HELPER_SEARCH_COMMAND_DESC).setText("ah search").setPlaceholder(Lang.AUCTION_HELPER_SEARCH_COMMAND_PLACEHOLDER).setMax(50);
        this.filtersSetting = new ExpandableSetting(Lang.AUCTION_HELPER_FILTERS).settings(this.armorFilterSetting, this.elytraFilterSetting, this.swordFilterSetting, this.pickaxeFilterSetting, this.axeFilterSetting, this.shovelFilterSetting, this.excludeCategorySetting);
        EventDispatcher class275VarEventDispatcher = Expensive.INSTANCE.eventDispatcher();
        KeybindSetting class663Var = new KeybindSetting(Lang.AUCTION_HELPER_SEARCH_BIND, Lang.AUCTION_HELPER_SEARCH_BIND_DESC);
        addSettings(this.countSetting, this.calculatorSetting, this.durabilitySetting, this.balanceInclusionSetting, this.enchantmentsOnlySetting, this.filtersSetting, class663Var, this.searchCommandSetting);
        class663Var.consumer(class664Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded()) {
                ItemStack mainHandStack = Mc.INSTANCE.getPlayer().getMainHandStack();
                if (mainHandStack.isEmpty()) {
                    return;
                }
                String strTrim = mainHandStack.getName().getString().trim();
                if (strTrim.isEmpty()) {
                    return;
                }
                String strTrim2 = StringHelper.stripTextFormat(strTrim.replace("\"", "\\\"")).replace("[★]", "").replace("fff", "").replace("ggg", "").replace("xxx", "").trim();
                String strStripLeading = (this.searchCommandSetting.getText() == null || this.searchCommandSetting.getText().isBlank()) ? "ah search" : this.searchCommandSetting.getText().stripLeading();
                if (strStripLeading.startsWith("/")) {
                    strStripLeading = strStripLeading.substring(1);
                }
                PacketSender.sendPacket(new CommandExecutionC2SPacket(strStripLeading + " " + strTrim2));
            }
        });
        class275VarEventDispatcher.register(PacketSendEvent.class, class037Var -> {
            if (isState() && this.calculatorSetting.isValue()) {
                if ((class037Var.getPacket()) instanceof CommandExecutionC2SPacket packet ) {
                    PacketSender.sendPacket(new CommandExecutionC2SPacket(expandMultiplication(packet.command())));
                    class037Var.cancel();
                }
            }
        });
        class275VarEventDispatcher.register(HandledScreenRenderEvent.class, class015Var -> {
            if (isState()) {
                Scoreboard scoreboard = MinecraftClient.getInstance().player.getScoreboard();
                ScoreboardObjective objectiveForSlot = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
                Optional optionalFindFirst = objectiveForSlot != null ? scoreboard.getScoreboardEntries(objectiveForSlot).stream().filter(scoreboardEntry -> {
                    return !scoreboardEntry.hidden();
                }).map(scoreboardEntry2 -> {
                    return (Integer) Team.decorateName(scoreboard.getScoreHolderTeam(scoreboardEntry2.owner()), scoreboardEntry2.name()).getSiblings().stream().map((v0) -> {
                        return v0.getString();
                    }).filter(str -> {
                        return str.toLowerCase(Locale.ROOT).replace('o', (char) 1086).replace('e', (char) 1077).contains("монет") || str.toLowerCase(Locale.ROOT).replace('a', (char) 1072).replace('c', (char) 1089).contains("баланс");
                    }).map(this::parseFirstNumber).flatMap((v0) -> {
                        return v0.stream();
                    }).findFirst().orElse(null);
                }).filter((v0) -> {
                    return Objects.nonNull(v0);
                }).findFirst() : Optional.empty();
                if ((Mc.INSTANCE.getCurrentScreen()) instanceof GenericContainerScreen currentScreen ) {
                    GenericContainerScreen genericContainerScreen = currentScreen;
                    if (genericContainerScreen.getTitle().getString().contains("1A0ꀄ") || genericContainerScreen.getTitle().getString().toLowerCase().contains("漢:") || genericContainerScreen.getTitle().getString().contains("ꈁꀀꈂꌲꈂꀁ§0ꈃꄙ") || genericContainerScreen.getTitle().getString().toLowerCase().contains("аукцион") || genericContainerScreen.getTitle().getString().toLowerCase().contains("поиск:")) {
                        List<AuctionItemListing> list = genericContainerScreen.getScreenHandler().slots.stream().filter(slot -> {
                            return slot.hasStack() && isMatchingItem(slot.getStack());
                        }).map(slot2 -> {
                            return new AuctionItemListing(this.priceReader.getPrice(slot2.getStack()), slot2, slot2.getStack());
                        }).filter(class425Var -> {
                            return class425Var.price() >= 0 && (!this.balanceInclusionSetting.isValue() || optionalFindFirst.isEmpty() || class425Var.price() <= ((Integer) optionalFindFirst.get()).intValue());
                        }).toList();
                        AuctionItemListing class425VarMethod007 = findCheapest(list);
                        AuctionItemListing class425VarMethod020 = findBestPerUnit(list);
                        DrawContext drawContext = class015Var.drawContext();
                        MatrixStack matrices = drawContext.getMatrices();
                        matrices.push();
                        matrices.translate(genericContainerScreen.x, genericContainerScreen.y, 0.0d);
                        highlightSlot(drawContext, class425VarMethod007, -16711936);
                        highlightSlot(drawContext, class425VarMethod020, -16711681);
                        matrices.pop();
                    }
                }
            }
        });
    }

    public Optional<Integer> parseFirstNumber(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(c);
            }
        }
        return !sb.isEmpty() ? Optional.of(Integer.valueOf(Integer.parseInt(sb.toString()))) : Optional.empty();
    }

    public String expandMultiplication(CharSequence charSequence) {
        Matcher matcher = this.multiplyPattern.matcher(charSequence);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            try {
                matcher.appendReplacement(sb, String.valueOf(Integer.parseInt(matcher.group(1)) * Integer.parseInt(matcher.group(2))));
            } catch (NumberFormatException e) {
                matcher.appendReplacement(sb, matcher.group(0));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public AuctionItemListing findCheapest(List<AuctionItemListing> list) {
        return list.stream().min(Comparator.comparingInt((v0) -> {
            return v0.price();
        })).orElse(null);
    }

    public AuctionItemListing findBestPerUnit(List<AuctionItemListing> list) {
        return list.stream().filter(this::hasMultipleItems).min(Comparator.comparingDouble(this::getPricePerItem)).orElse(null);
    }

    public double getPricePerItem(AuctionItemListing class425Var) {
        return ((double) class425Var.price()) / ((double) class425Var.itemStack().getCount());
    }

    public boolean hasMultipleItems(AuctionItemListing class425Var) {
        return class425Var.itemStack().getCount() > 1;
    }

    public void highlightSlot(DrawContext drawContext, AuctionItemListing class425Var, int i) {
        if (class425Var != null) {
            int i2 = class425Var.slot().x;
            int i3 = class425Var.slot().y;
            drawContext.fill(i2, i3, i2 + 16, i3 + 16, i);
        }
    }
}
