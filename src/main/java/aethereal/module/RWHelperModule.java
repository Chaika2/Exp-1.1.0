package aethereal.module;
import aethereal.util.ActionScheduler;
import aethereal.annotation.Aliases;
import aethereal.event.ClientTickEvent;
import aethereal.ui.setting.ExpandableSetting;
import aethereal.Expensive;
import aethereal.util.GrimDelayHandler;
import aethereal.util.InventoryItemFinder;
import aethereal.type.InventoryScope;
import aethereal.net.InventoryService;
import aethereal.util.InventoryTask;
import aethereal.util.InventoryUtil;
import aethereal.ui.setting.KeybindSetting;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.type.NotificationType;
import aethereal.event.PacketReceiveEvent;
import aethereal.event.PacketSendEvent;
import aethereal.util.PlayerActionUtil;
import aethereal.type.RwHelperAction;
import aethereal.model.RwHelperItemBind;
import aethereal.ui.setting.Setting;
import aethereal.util.StringUtil;
import aethereal.model.Translation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;

@Aliases(aliases = {"ReallyWorld Helper", "RW Helper", "Auto Use Items"})
public class RWHelperModule extends Module {
    public final MultiSelectSetting<RwHelperAction> actions;
    public final KeybindSetting antiFlyKey;
    public final KeybindSetting expScrollKey;
    public final KeybindSetting darkPulseKey;
    public final KeybindSetting crossbowKey;
    public final Mc mc;
    public final List<Setting> keybindSettings;
    public final List<RwHelperItemBind> itemBinds;
    public final Pattern bannedWordsPattern;
    public final ActionScheduler swapScheduler;
    public final ActionScheduler closeScheduler;

    public RWHelperModule() {
        super(ModuleTab.MISC, "RW Helper");
        this.actions = new MultiSelectSetting(Lang.RWHELPER_ACTIONS, Lang.RWHELPER_ACTIONS_DESC).values(RwHelperAction.class);
        this.mc = Mc.INSTANCE;
        KeybindSetting class663Var = new KeybindSetting(Translation.clearText("Анти полёт"));
        this.antiFlyKey = class663Var;
        KeybindSetting class663Var2 = new KeybindSetting(Translation.clearText("Свиток опыта"));
        this.expScrollKey = class663Var2;
        KeybindSetting class663Var3 = new KeybindSetting(Translation.clearText("Тёмный пульс"));
        this.darkPulseKey = class663Var3;
        KeybindSetting class663Var4 = new KeybindSetting(Lang.FTHELPER_CROSSBOW_KEY);
        this.crossbowKey = class663Var4;
        this.keybindSettings = Arrays.asList(class663Var, class663Var2, class663Var3, class663Var4);
        this.itemBinds = List.of(new RwHelperItemBind(this.antiFlyKey, Items.FIREWORK_STAR, "анти полет", true), new RwHelperItemBind(this.expScrollKey, Items.FLOWER_BANNER_PATTERN, "свиток опыта", true), new RwHelperItemBind(this.darkPulseKey, Items.FIREWORK_STAR, "тёмный пульс", true), new RwHelperItemBind(this.crossbowKey, Items.CROSSBOW, "1", false));
        this.swapScheduler = new ActionScheduler();
        this.closeScheduler = new ActionScheduler();
        this.bannedWordsPattern = Pattern.compile((String) new HashSet<String>(Arrays.asList("акриен(а|у|ом|е|чик)?", "рич(а|у|ом|ей|е)?", "ньюкод(ом|а|у|ами|ик|е)?", "экспенсив(ом|а|у|ами|е)?", "импакт(ом|а|у|ами|ик|е)?", "экселлент(ом|а|у|ами|ик|е)?", "экселент(ом|а|у|ами|ик)?", "катлаван(ом|а|у|ами|чик)?", "катлован(ом|а|у|ами|чик)?", "целестиал(ом|а|у|ами|е)?", "целк(ой|а|у|ами|очка|и|е)?", "матикс(ом|а|у|ами|е)?", "инерти(я|ей|ю|ями|е)?", "эксп(а|ой|ою|у|уличка|е)?", "флюгер(ом|а|у|ами)?", "рикер(а|у|ом|очек)?", "фанпе(й|ю|я|ем|е|йчик)?", "вексайд(ом|а|у|ами|ик|е)?", "нурсултан(а|у|е|ом|чик)?", "нурик(а|у|ом|е)?", "нурлан(а|у|ом|чик|е)?", "векс(ом|у|а|ами|ик|е)?", "релейк(ом|у|а|ами|е)?", "арбуз(ом|а|у|ами|ик|е|иком)?", "вилд(ом|у|а|ами|ик|е)?", "фантайм(е|а|у)?", "холик(е|а|у)?", "холиворлд(а|у|е)?", "рокстар(ом|а|у|ами|чик|е)?", "рогалик(а|у|ом|е)?", "тандерхак(ом|у|и|ами|а|е)?", "ликвидбаунс(а|у|ами|е)?", "expensive", "celestial", "newcode", "arbuz", "akrien", "nursultan", "relake", "wild", "wurst", "catlovan", "excellent", "rockstar", "catlavan", "impact", "matix", "inertia", "wex", "wexside", "nurik", "nurlan", "rich", "funpay", "fluger", "riker", "funtime", "holyworld", "wwe", "hvh", "rogalik", "thunderhack", "liquidbounce")).stream().map(str -> {
            if (str.contains(" ")) {
                return "(?i)" + str.replace(" ", "\\s+");
            }
            return str.contains("(") ? "(?i)(?<![\\p{L}])" + str + "(?![\\p{L}])" : "(?i)(?<![\\p{L}])" + Pattern.quote(str) + "(а|у|ом|ями|е|ей|ю|иком|очка|йчик|ою|чик|очек|ик|уличка|ой|и)?(?![\\p{L}])";
        }).collect(Collectors.joining("|")), 64);
        ExpandableSetting class670Var = new ExpandableSetting(Lang.FTHELPER_USEBYBIND, Lang.FTHELPER_USEBYBIND_DESC);
        class670Var.setSubSettings(this.keybindSettings);
        addSettings(this.actions, class670Var);
        register(PacketSendEvent.class, class037Var -> {
            if (isState() && this.mc.isWorldLoaded() && this.actions.isSelected(RwHelperAction.FILTER_BANNED_WORDS)) {
                if ((class037Var.getPacket()) instanceof ChatMessageC2SPacket packet ) {
                    String strChatMessage = packet.chatMessage();
                    if (strChatMessage.startsWith("/") || strChatMessage.startsWith(".") || !this.bannedWordsPattern.matcher(strChatMessage).find()) {
                        return;
                    }
                    Expensive.INSTANCE.notificationRepository().post(NotificationType.WARNING, (Text) Text.literal(String.valueOf(Formatting.GRAY) + Lang.RWHELPER_BANNED_MESSAGE.effective()), 3L, TimeUnit.SECONDS);
                    class037Var.cancel();
                }
            }
        });
        register(PacketReceiveEvent.class, class051Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                ClientPlayerEntity player = this.mc.getPlayer();
                if (this.actions.isSelected(RwHelperAction.CLOSE_SERVER_MENU) && (class051Var.getPacket() instanceof OpenScreenS2CPacket packet) && StringHelper.stripTextFormat(packet.getName().getString()).contains("ꈁꀀꈂꌁꈂꀁꈃꄀ") && player.age < 100) {
                    ActionScheduler class265Var = this.closeScheduler;
                    Objects.requireNonNull(player);
                    class265Var.addTickStep(1, player::closeHandledScreen);
                    class051Var.cancel();
                }
            }
        });
        register(ClientTickEvent.class, class181Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                this.itemBinds.forEach(class516Var -> {
                    class516Var.bindSetting.consumer(class664Var -> {
                        ClientPlayerEntity player = this.mc.getPlayer();
                        Item item = class516Var.item;
                        String strCapitalizeFirstLetter = class516Var.serverItem() ? StringUtil.capitalizeFirstLetter(class516Var.serverItemName()) : class516Var.item().getName().getString();
                        if (player.getItemCooldownManager().isCoolingDown(item.getDefaultStack())) {
                            notifyCooldown(item, strCapitalizeFirstLetter);
                            return;
                        }
                        InventoryService class011VarInventoryService = Expensive.INSTANCE.inventoryService();
                        InventoryItemFinder class123VarSearcher = class011VarInventoryService.searcher();
                        String str2 = class516Var.serverItemName;
                        class123VarSearcher.findItem(class516Var.getSearchPredicate(), InventoryScope.ALL).ifPresentOrElse(class329Var -> {
                            if (str2.equalsIgnoreCase("тёмный пульс") || str2.equalsIgnoreCase("анти полет")) {
                                int iIncreasedSlot = class329Var.slotReference().increasedSlot();
                                if (iIncreasedSlot != -1 && this.swapScheduler.isFinished()) {
                                    ItemStack offHandStack = player.getOffHandStack();
                                    if (offHandStack.getItem() == item && (!class516Var.serverItem() || offHandStack.getName().getString().toLowerCase().trim().contains(str2))) {
                                        this.swapScheduler.addTickStep(0, () -> {
                                            this.mc.getGameOptions().sneakKey.setPressed(true);
                                        }).addTickStep(1, () -> {
                                            this.mc.getGameOptions().sneakKey.setPressed(false);
                                        });
                                    } else {
                                        this.swapScheduler.addTickStep(0, () -> {
                                            InventoryUtil.INSTANCE.windowClick(SlotActionType.SWAP, iIncreasedSlot, 40, true);
                                            this.mc.getGameOptions().sneakKey.setPressed(true);
                                        }).addTickStep(1, () -> {
                                            this.mc.getGameOptions().sneakKey.setPressed(false);
                                        }).addTickStep(10, () -> {
                                            InventoryUtil.INSTANCE.windowClick(SlotActionType.SWAP, iIncreasedSlot, 40, true);
                                            PlayerActionUtil.INSTANCE.updateSlots(true);
                                        });
                                    }
                                }
                            } else if (!GrimDelayHandler.script.isFinished()) {
                                return;
                            } else {
                                class011VarInventoryService.addTask(InventoryTask.create(class516Var.getSearchPredicate(), class329Var, true, true, false), this);
                            }
                            Expensive.INSTANCE.notificationRepository().post(NotificationType.INFO, (Text) Text.literal(Lang.USE_ITEM.effective().replace("{item}", String.valueOf(Formatting.RED) + strCapitalizeFirstLetter + String.valueOf(Formatting.RESET))), 2L, TimeUnit.SECONDS);
                        }, () -> {
                            notifyItemNotFound(item, strCapitalizeFirstLetter);
                        });
                    });
                });
            }
            this.swapScheduler.update().cleanupIfFinished();
            this.closeScheduler.update().cleanupIfFinished();
        });
    }

    public void notifyCooldown(Item item, String str) {
        Expensive.INSTANCE.notificationRepository().post(NotificationType.WARNING, (Text) Text.literal(Lang.ITEM_ON_COOLDOWN.effective().replace("{item}", String.valueOf(Formatting.RED) + str + String.valueOf(Formatting.RESET)).replace("{seconds}", String.valueOf(Formatting.RED) + String.format(Locale.US, "%.1f", Float.valueOf(PlayerActionUtil.INSTANCE.getRemainingCooldownSeconds(item))) + String.valueOf(Formatting.RESET))), 2L, TimeUnit.SECONDS);
    }

    public void notifyItemNotFound(Item item, String str) {
        if (item instanceof CrossbowItem) {
            Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal(Lang.NO_ITEM_FOUND.effective().replace("{item}", String.valueOf(Formatting.RED) + "Charged Crossbow" + String.valueOf(Formatting.RESET))), 2L, TimeUnit.SECONDS);
        } else {
            Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal(Lang.NO_ITEM_FOUND.effective().replace("{item}", String.valueOf(Formatting.RED) + str + String.valueOf(Formatting.RESET))), 2L, TimeUnit.SECONDS);
        }
    }
}
