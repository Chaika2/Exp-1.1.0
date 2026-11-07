package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.event.BlockBreakEvent2;
import aethereal.util.BlockUtil;
import aethereal.render.ColorStack;
import aethereal.type.CombatPauseManager;
import aethereal.util.DrawEngine;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.util.GrimDelayHandler;
import aethereal.util.InventoryItemFinder;
import aethereal.type.InventoryScope;
import aethereal.util.IteratorUtil;
import aethereal.Lang;
import aethereal.util.MathUtil;
import aethereal.type.Mc;
import aethereal.type.MineHelperAction;
import aethereal.util.MineHelperCountdown;
import aethereal.ui.ModuleTab;
import aethereal.render.MsdfFont;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.type.NotificationType;
import aethereal.util.PlayerActionUtil;
import aethereal.event.PlayerTickEvent;
import aethereal.event.Render2DEvent;
import aethereal.math.Rotation;
import aethereal.render.ScreenResolution;
import aethereal.model.SlotSearchResult2;
import aethereal.util.StencilBufferUtil;
import aethereal.math.Stopwatch;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

@Aliases(aliases = {"Fun Time", "Mine Helper"})
public class MineHelperModule extends Module {
    public final MultiSelectSetting<MineHelperAction> helpActions;
    public final Mc mc;
    public MineHelperCountdown countdown;
    public boolean cleaningInProgress;
    public final Stopwatch notificationTimer;
    public static final Pattern timePattern = Pattern.compile("\\d{2}:\\d{2}");
    public static final Set<Item> valuableItems = Set.of(new Item[]{Items.DIAMOND, Items.EMERALD, Items.GOLD_INGOT, Items.IRON_INGOT, Items.NETHERITE_INGOT, Items.NETHERITE_SCRAP, Items.ANCIENT_DEBRIS, Items.LAPIS_LAZULI, Items.REDSTONE, Items.COAL, Items.RAW_IRON, Items.RAW_GOLD, Items.RAW_COPPER, Items.AMETHYST_SHARD, Items.EXPERIENCE_BOTTLE, Items.ENDER_PEARL, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE});
    public static final Set<Item> junkItems = Set.of(new Item[]{Items.COBBLESTONE, Items.COBBLED_DEEPSLATE, Items.DIRT, Items.GRAVEL, Items.FLINT, Items.ANDESITE, Items.DIORITE, Items.GRANITE, Items.TUFF, Items.NETHERRACK, Items.STONE, Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.BONE, Items.GUNPOWDER, Items.STRING});
    public static final Set<Item> toolItems = Set.of(Items.DIAMOND_SWORD, Items.NETHERITE_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_AXE, Items.DIAMOND_HOE, Items.NETHERITE_PICKAXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_AXE, Items.NETHERITE_HOE);
    public static final Set<Item> armorItems = Set.of(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);

    public MineHelperModule() {
        super(ModuleTab.MISC, "Mine Helper");
        this.helpActions = new MultiSelectSetting(Lang.MINE_HELPER_HELP_TYPE, Lang.MINE_HELPER_HELP_TYPE_DESC).values(MineHelperAction.class);
        this.mc = Mc.INSTANCE;
        this.countdown = null;
        this.notificationTimer = new Stopwatch();
        addSettings(this.helpActions);
        register(Render2DEvent.class, class311Var -> {
            if (isState() && this.mc.isWorldLoaded() && this.helpActions.isSelected(MineHelperAction.NEXT_MINE) && this.countdown != null) {
                MatrixStack matrixStack = class311Var.matrixStack();
                DrawEngine class154VarDrawEngine = Expensive.INSTANCE.drawEngine();
                ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
                int iScreenWidth = ScreenResolution.resolution().screenWidth();
                MsdfFont class161Var = Fonts.INTER_MEDIUM.get();
                float height = class161Var.getHeight(14.0f);
                float size = this.mc.getInGameHud().getBossBarHud().bossBars.size() * 45;
                class154VarDrawEngine.begin();
                class154VarDrawEngine.drawLine(matrixStack, class161Var, 14, iScreenWidth, size, Lang.MINE_HELPER_NEXT_MINE_LABEL.effective(), this.countdown.nextType(), class115VarColorStack.computeColor(230, 230, 230, StencilBufferUtil.STENCIL_MASK), class115VarColorStack.computeColor(getRarityColor(this.countdown.nextType())));
                class154VarDrawEngine.drawLine(matrixStack, class161Var, 14, iScreenWidth, size + height, Lang.MINE_HELPER_TIME_LEFT_LABEL.effective(), this.countdown.formattedTime(), class115VarColorStack.computeColor(230, 230, 230, StencilBufferUtil.STENCIL_MASK), class115VarColorStack.computeColor(getTimeLeftColor(this.countdown.getSecondsLeft())));
                class154VarDrawEngine.end();
            }
        });
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && this.mc.isWorldLoaded() && class130Var.isPre()) {
                ClientPlayerEntity player = this.mc.getPlayer();
                ClientWorld world = this.mc.getWorld();
                if (this.helpActions.isSelected(MineHelperAction.NEXT_MINE)) {
                    if (this.countdown == null) {
                        parseCountdown(IteratorUtil.toList(world.getEntities().iterator()).stream().filter(entity -> {
                            return entity instanceof ArmorStandEntity;
                        }).map(entity2 -> {
                            return (ArmorStandEntity) entity2;
                        }).filter(armorStandEntity -> {
                            String strMethod009 = getArmorStandName(armorStandEntity);
                            return strMethod009.contains("Авто-Шахта") || strMethod009.contains("Следующая:") || strMethod009.contains("Обновление через:") || timePattern.matcher(strMethod009).matches();
                        }).sorted(Comparator.comparingDouble(armorStandEntity2 -> {
                            return -armorStandEntity2.getY();
                        })).toList()).ifPresent(class510Var -> {
                            this.countdown = class510Var;
                        });
                    } else if (this.countdown.isExpired() && this.countdown != null) {
                        this.countdown = null;
                    }
                }
                if (this.helpActions.isSelected(MineHelperAction.CLEAN_INVENTORY)) {
                    int emptySlots = PlayerActionUtil.INSTANCE.getEmptySlots(player.getInventory());
                    InventoryItemFinder class123VarSearcher = Expensive.INSTANCE.inventoryService().searcher();
                    if (shouldCleanInventory(emptySlots) && BlockUtil.isWithInMine(player.getBlockPos())) {
                        List<SlotSearchResult2> listFindAllItems = class123VarSearcher.findAllItems(this::isTrashItem, InventoryScope.HOTBAR, InventoryScope.INVENTORY);
                        if (!listFindAllItems.isEmpty() && emptySlots <= 2 && GrimDelayHandler.script.isFinished()) {
                            GrimDelayHandler.addTask(new Rotation(MathUtil.oppositeYaw(player), player.getPitch()), () -> {
                                listFindAllItems.forEach(class329Var -> {
                                    if (class329Var.found()) {
                                        PlayerActionUtil.INSTANCE.windowClick(SlotActionType.THROW, class329Var.slotReference().increasedSlot(), 1, true);
                                    }
                                });
                                this.cleaningInProgress = false;
                            });
                            this.cleaningInProgress = false;
                        }
                    } else if (this.cleaningInProgress) {
                        this.cleaningInProgress = false;
                    }
                }
                if (this.helpActions.isSelected(MineHelperAction.SAVE_PICKAXE)) {
                    ItemStack mainHandStack = this.mc.getPlayer().getInventory().getMainHandStack();
                    if (mainHandStack.isEmpty() || !(mainHandStack.getItem() instanceof PickaxeItem) || !mainHandStack.isDamageable() || 1.0f - (mainHandStack.getDamage() / mainHandStack.getMaxDamage()) > 0.15f) {
                        return;
                    }
                    CombatPauseManager.INSTANCE.pauseBreakingForAtLeast(40);
                }
            }
        });
        register(BlockBreakEvent2.class, class393Var -> {
            if (isState() && this.mc.isWorldLoaded() && this.helpActions.isSelected(MineHelperAction.SAVE_PICKAXE)) {
                ItemStack mainHandStack = this.mc.getPlayer().getInventory().getMainHandStack();
                if (!mainHandStack.isEmpty() && (mainHandStack.getItem() instanceof PickaxeItem) && mainHandStack.isDamageable()) {
                    if (1.0f - (mainHandStack.getDamage() / mainHandStack.getMaxDamage()) <= 0.15f) {
                        if (this.notificationTimer.hasElapsed(5L, TimeUnit.SECONDS)) {
                            Expensive.INSTANCE.notificationRepository().post(NotificationType.WARNING, (Text) Text.literal(Lang.MINE_HELPER_PICKAXE_LOW_DURABILITY.effective()), 2L, TimeUnit.SECONDS);
                            this.notificationTimer.reset();
                        }
                        class393Var.cancel();
                    }
                }
            }
        });
    }

    public boolean shouldCleanInventory(int i) {
        return !this.cleaningInProgress && i <= 2;
    }

    public boolean isTrashItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        Item item = itemStack.getItem();
        String path = Registries.ITEM.getId(item).getPath();
        if (((item instanceof BlockItem blockItem) && (blockItem.getBlock() instanceof ShulkerBoxBlock)) || (item instanceof PlayerHeadItem) || item == Items.TOTEM_OF_UNDYING || toolItems.contains(item) || armorItems.contains(item) || valuableItems.contains(item) || path.endsWith("_ore") || path.contains("raw_") || path.endsWith("_wire") || item.getComponents().contains(DataComponentTypes.FOOD) || itemStack.hasEnchantments()) {
            return false;
        }
        return junkItems.contains(item) || isJunkBlockPath(path);
    }

    public boolean isJunkBlockPath(String str) {
        return str.equals("cobblestone") || str.equals("cobbled_deepslate") || str.equals("dirt") || str.equals("gravel") || str.equals("andesite") || str.equals("diorite") || str.equals("granite") || str.equals("tuff") || str.equals("netherrack") || str.equals("stone");
    }

    public Optional<MineHelperCountdown> parseCountdown(List<ArmorStandEntity> list) {
        if (list.size() < 4) {
            return Optional.empty();
        }
        String strMethod009 = getArmorStandName(list.get(0));
        String strMethod0010 = getArmorStandName(list.get(1));
        String strMethod0011 = getArmorStandName(list.get(2));
        String strMethod0012 = getArmorStandName(list.get(3));
        if (!strMethod009.contains("Авто-Шахта") || !strMethod0010.contains("Следующая:") || !strMethod0011.contains("Обновление через:") || !timePattern.matcher(strMethod0012).matches()) {
            return Optional.empty();
        }
        String[] strArrSplit = strMethod0012.split(":");
        if (strArrSplit.length != 2) {
            return Optional.empty();
        }
        return Optional.of(new MineHelperCountdown(strMethod0010.replace("Следующая: ", "").trim(), (Integer.parseInt(strArrSplit[0]) * 60) + Integer.parseInt(strArrSplit[1]), System.currentTimeMillis()));
    }

    public String getArmorStandName(ArmorStandEntity armorStandEntity) {
        return armorStandEntity.getCustomName() != null ? armorStandEntity.getCustomName().getString() : "";
    }

    public int getTimeLeftColor(int i) {
        if (i <= 10) {
            return -43691;
        }
        return i <= 60 ? -171 : -11141291;
    }

    public int getRarityColor(String str) {
        String lowerCase = str.toLowerCase(Locale.ROOT);
        if (lowerCase.contains("мифическ")) {
            return -5614081;
        }
        return lowerCase.contains("легендарн") ? -22016 : -5592406;
    }
}
