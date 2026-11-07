package aethereal.module;
import aethereal.util.ActionScheduler;
import aethereal.annotation.Aliases;
import aethereal.util.BlockUtil;
import aethereal.ui.setting.BooleanSetting;
import aethereal.type.ChatMessageType;
import aethereal.event.ChatReceiveEvent;
import aethereal.util.ChatUtil;
import aethereal.resource.ClasspathResource;
import aethereal.render.ColorStack;
import aethereal.util.DrawEngine;
import aethereal.event.Event;
import aethereal.type.EventPriority;
import aethereal.ui.setting.ExpandableSetting;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.type.FtHelperItemRules;
import aethereal.model.FtTrackedBoss;
import aethereal.model.FtTrackedStructure;
import aethereal.render.GlTexture;
import aethereal.util.GrimDelayHandler;
import aethereal.util.InventoryItemFinder;
import aethereal.type.InventoryScope;
import aethereal.net.InventoryService;
import aethereal.util.InventoryTask;
import aethereal.type.ItemSearchRule;
import aethereal.ui.setting.KeybindSetting;
import aethereal.Lang;
import aethereal.util.MathUtil;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.render.MsdfFont;
import aethereal.type.NotificationType;
import aethereal.event.PacketReceiveEvent;
import aethereal.util.PlayerActionUtil;
import aethereal.event.PlayerTickEvent;
import aethereal.util.ProjectionUtil;
import aethereal.event.Render2DEvent;
import aethereal.math.Rotation;
import aethereal.math.RotationMath;
import aethereal.util.ServerUtil;
import aethereal.ui.setting.Setting;
import aethereal.model.SlotSearchResult2;
import aethereal.util.StringUtil;
import aethereal.render.ThemePalette;
import aethereal.util.WavSoundPlayer;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.StringUtils;
import org.joml.Matrix4f;
import org.joml.Vector2f;

@Aliases(aliases = {"Funtime Helper", "FT Helper", "Auto Use Items"})
public class FTHelperModule extends Module {
    public Map<BlockPos, BlockState> blockUpdates;
    public final KeybindSetting disorientationKey;
    public final KeybindSetting trapKey;
    public final KeybindSetting plastKey;
    public final KeybindSetting obviousDustKey;
    public final KeybindSetting frostSnowballKey;
    public final KeybindSetting assassinPotionKey;
    public final KeybindSetting palladinPotionKey;
    public final KeybindSetting angerPotionKey;
    public final KeybindSetting holyWaterKey;
    public final KeybindSetting petardKey;
    public final KeybindSetting radiationPotionKey;
    public final KeybindSetting sleepingPotionKey;
    public final KeybindSetting godAuraKey;
    public final KeybindSetting crossbowKey;
    public final KeybindSetting windChargeKey;
    public final KeybindSetting shulkerKey;

    public final BooleanSetting autoPoint;
    public final BooleanSetting structureTimer;
    public final BooleanSetting cooldownNotify;
    public final ExpandableSetting useByBind;

    public final BooleanSetting potionAim;
    public final Map<Item, Boolean> cooldownStates;

    public final Mc mc;
    public final FtHelperItemRules itemRules;
    public final List<FtTrackedBoss> trackedBosses;
    public final List<FtTrackedStructure> structures;
    public final ActionScheduler scheduler;
    public final Map<String, GlTexture> iconCache;
    public final Map<String, String> iconPaths;
    public final List<Runnable> pendingTasks;

    public FTHelperModule() {
        super(ModuleTab.MISC, "FT Helper");
        this.blockUpdates = new HashMap();
        this.disorientationKey = new KeybindSetting(Lang.FTHELPER_DISORIENTATION_KEY);
        this.trapKey = new KeybindSetting(Lang.FTHELPER_TRAP_KEY);
        this.plastKey = new KeybindSetting(Lang.FTHELPER_PLAST_KEY);
        this.obviousDustKey = new KeybindSetting(Lang.FTHELPER_OBVIOUS_DUST_KEY);
        this.frostSnowballKey = new KeybindSetting(Lang.FTHELPER_FROST_SNOWBALL_KEY);
        this.assassinPotionKey = new KeybindSetting(Lang.FTHELPER_ASSASIN_POTION_KEY);
        this.palladinPotionKey = new KeybindSetting(Lang.FTHELPER_PALLADIN_POTION_KEY);
        this.angerPotionKey = new KeybindSetting(Lang.FTHELPER_ANGER_POTION_KEY);
        this.holyWaterKey = new KeybindSetting(Lang.FTHELPER_HOLY_WATER_POTION_KEY);
        this.petardKey = new KeybindSetting(Lang.FTHELPER_PETARD_POTION_KEY);
        this.radiationPotionKey = new KeybindSetting(Lang.FTHELPER_RADIATION_POTION_KEY);
        this.sleepingPotionKey = new KeybindSetting(Lang.FTHELPER_SLEEPING_POTION_KEY);
        this.godAuraKey = new KeybindSetting(Lang.FTHELPER_GOD_AURA_KEY);
        this.crossbowKey = new KeybindSetting(Lang.FTHELPER_CROSSBOW_KEY);
        this.windChargeKey = new KeybindSetting(Lang.FTHELPER_WIND_CHARGE_KEY);
        this.shulkerKey = new KeybindSetting(Lang.FTHELPER_SHULKER_KEY);
        this.autoPoint = new BooleanSetting(Lang.FTHELPER_AUTO_POINT, Lang.FTHELPER_AUTO_POINT_DESC);
        this.structureTimer = new BooleanSetting(Lang.FTHELPER_STRUCTURE_TIMER, Lang.FTHELPER_STRUCTURE_TIMER_DESC);
        this.cooldownNotify = new BooleanSetting(Lang.FTHELPER_COOLDOWN_NOTIFY, Lang.FTHELPER_COOLDOWN_NOTIFY_DESC);
        this.useByBind = new ExpandableSetting(Lang.FTHELPER_USEBYBIND, Lang.FTHELPER_USEBYBIND_DESC);
        BooleanSetting value = new BooleanSetting(Lang.FTHELPER_POTION_AIM, Lang.FTHELPER_POTION_AIM_DESC).setValue(true);
        ExpandableSetting class670Var = this.useByBind;
        Objects.requireNonNull(class670Var);
        this.potionAim = value.visible(class670Var::isValue);
        this.cooldownStates = new HashMap();
        this.mc = Mc.INSTANCE;
        this.itemRules = new FtHelperItemRules();
        this.trackedBosses = new ArrayList();
        this.structures = new ArrayList();
        this.scheduler = new ActionScheduler();
        this.iconCache = new HashMap();
        this.iconPaths = Map.ofEntries(Map.entry("Мистический сундук", "/icons/fthelper/mystic.png"), Map.entry("Вулкан", "/icons/fthelper/volcano.png"), Map.entry("Метеоритный дождь", "/icons/fthelper/shower.png"), Map.entry("Маяк убийца", "/icons/fthelper/lighthouse.png"), Map.entry("Мистический Алтарь", "/icons/fthelper/altar.png"), Map.entry("Загадочный маяк", "/icons/fthelper/lighthouse.png"), Map.entry("Сундук смерти", "/icons/fthelper/deadchest.png"), Map.entry("Адская резня", "/icons/fthelper/infernal.png"));
        this.pendingTasks = new ArrayList();
        this.useByBind.setSubSettings(List.of(new Setting[]{this.disorientationKey, this.trapKey, this.plastKey, this.obviousDustKey, this.frostSnowballKey, this.assassinPotionKey, this.palladinPotionKey, this.angerPotionKey, this.holyWaterKey, this.petardKey, this.radiationPotionKey, this.sleepingPotionKey, this.godAuraKey, this.crossbowKey, this.windChargeKey, this.shulkerKey}));
        addSettings(this.useByBind, this.potionAim, this.autoPoint, this.structureTimer, this.cooldownNotify);
        applyState();
        register(Render2DEvent.class, class311Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded() && class311Var.isPre()) {
                MatrixStack matrixStack = class311Var.matrixStack();
                Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
                DrawEngine class154VarDrawEngine = Expensive.INSTANCE.drawEngine();
                ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
                ThemePalette class764VarPalette = Expensive.INSTANCE.theme().palette();
                class154VarDrawEngine.begin();
                class115VarColorStack.push();
                this.structures.forEach(class493Var -> {
                    double dCurrentTimeMillis = (class493Var.time - System.currentTimeMillis()) / 1000.0d;
                    Optional<Vector2f> optionalWorldToScreen = ProjectionUtil.worldToScreen(class493Var.vec);
                    if (optionalWorldToScreen.isPresent()) {
                        Vector2f vector2f = optionalWorldToScreen.get();
                        String str = MathUtil.round(dCurrentTimeMillis, 0.10000000149011612d) + "с";
                        float width = Fonts.INTER_BOLD.get().getWidth(str, 14.0f);
                        float height = Fonts.INTER_BOLD.get().getHeight(14.0f);
                        float f = 6.0f + 16.0f + 4.0f + width + 6.0f + 2.0f;
                        float fMax = Math.max(16.0f, height) + (6.0f * 2.0f);
                        float f2 = vector2f.x - (f / 2.0f);
                        float f3 = vector2f.y - (fMax / 2.0f);
                        if (class493Var.anarchy == ServerUtil.getAnarchy() && ServerUtil.getWorldType().equals(class493Var.world)) {
                            class154VarDrawEngine.roundedRectangle(positionMatrix, f2, f3, f, fMax, 7.0f, 2.5f, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(700).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(801).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(801).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb()));
                            float f4 = f2 + 6.0f;
                            class154VarDrawEngine.itemStack(matrixStack.peek().getPositionMatrix(), class493Var.item.getDefaultStack(), f4, f3 + ((fMax - 16.0f) / 2.0f), 0.5f, 1.0f);
                            class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), Fonts.INTER_BOLD.get(), str, f4 + 16.0f + 4.0f, f3 + ((fMax - height) / 2.0f), 14.0f, 0.0f, class115VarColorStack.computeColor(class764VarPalette.text().tone(400).argb()));
                        }
                    }
                });
                this.trackedBosses.forEach(class496Var -> {
                    String strReplace;
                    Optional<Vector2f> optionalWorldToScreen = ProjectionUtil.worldToScreen(class496Var.vec);
                    if (optionalWorldToScreen.isPresent()) {
                        Vector2f vector2f = optionalWorldToScreen.get();
                        double dCurrentTimeMillis = (class496Var.timeOpen - System.currentTimeMillis()) / 1000.0d;
                        double dCurrentTimeMillis2 = (class496Var.timeEnd - System.currentTimeMillis()) / 1000.0d;
                        if (dCurrentTimeMillis > 0.0d) {
                            strReplace = ("До начала: " + MathUtil.round(dCurrentTimeMillis, dCurrentTimeMillis < 30.0d ? 0.10000000149011612d : 1.0d) + "с").replace(".0", "");
                        } else if (dCurrentTimeMillis2 > 0.0d) {
                            strReplace = ("До конца: " + MathUtil.round(dCurrentTimeMillis2, dCurrentTimeMillis2 < 30.0d ? 0.10000000149011612d : 1.0d) + "с").replace(".0", "");
                        } else {
                            strReplace = "Конец ивента!";
                        }
                        String str = strReplace;
                        String str2 = " [" + MathUtil.round(Mc.INSTANCE.getEntityRenderDispatcher().camera.getPos().distanceTo(class496Var.vec), 0.1d) + "m]";
                        if (class496Var.anarchy == ServerUtil.getAnarchy() && ServerUtil.getWorldType().equals(class496Var.world)) {
                            ArrayList arrayList = new ArrayList(Collections.singletonList(class496Var.name + str2));
                            if (class496Var.owner != null) {
                                arrayList.add("Призван: " + String.valueOf(Formatting.GOLD) + class496Var.owner);
                            }
                            arrayList.add(str);
                            if (class496Var.lvl != null) {
                                arrayList.add(class496Var.lvl);
                            }
                            class154VarDrawEngine.texture(matrixStack.peek().getPositionMatrix(), vector2f.x - 8.0f, vector2f.y, 16.0f, 16.0f, class154VarDrawEngine.bindTexture(this.iconCache.computeIfAbsent(this.iconPaths.get(class496Var.name), str3 -> {
                                return new GlTexture(new ClasspathResource(str3));
                            }).textureWithSTB()), class115VarColorStack.white());
                            drawTooltip(matrixStack, class154VarDrawEngine, arrayList, vector2f);
                        }
                    }
                });
                class115VarColorStack.pop();
                class154VarDrawEngine.end();
            }
        });
        register(ChatReceiveEvent.class, class066Var -> {
            String strSubstringBetween;
            if (isState() && Mc.INSTANCE.isWorldLoaded() && class066Var.type() == ChatMessageType.GAME_MESSAGE && class066Var.message().contains("Координаты:") && (strSubstringBetween = StringUtils.substringBetween(class066Var.textData().getString(), "Координаты: [", "]")) != null) {
                class066Var.cancel();
                ChatUtil.addMessage(Mc.INSTANCE.getInGameHud().getChatHud(), class066Var.textData().copy().append(Text.literal(" ")).append(Text.literal("[Поставить WayPoint]").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ".way add Event " + strSubstringBetween)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Нажмите чтобы добавить вейпоинт"))))), null);
            }
        }, EventPriority.HIGHEST);
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded() && class130Var.isPre()) {
                this.scheduler.cleanupIfFinished().update();
                if (this.useByBind.isValue()) {
                    ClientPlayerEntity player = this.mc.getPlayer();
                    InventoryService class011VarInventoryService = Expensive.INSTANCE.inventoryService();
                    InventoryItemFinder class123VarSearcher = class011VarInventoryService.searcher();
                    this.itemRules.rules.forEach((class663Var, class494Var) -> {
                        class663Var.consumer(class664Var -> {
                            String strCapitalizeFirstLetter = class494Var.serverItem() ? StringUtil.capitalizeFirstLetter(class494Var.serverName()) : class494Var.boundItem().getName().getString();
                            if (player.getItemCooldownManager().isCoolingDown(class494Var.boundItem().getDefaultStack())) {
                                notifyCooldown(class494Var.boundItem(), strCapitalizeFirstLetter);
                            } else if (GrimDelayHandler.script.isFinished()) {
                                class123VarSearcher.findItem(class494Var.getSearchPredicate(), InventoryScope.ALL).ifPresentOrElse(class329Var -> {
                                    useItem(class329Var, class494Var, player, class011VarInventoryService, strCapitalizeFirstLetter);
                                }, () -> {
                                    notifyNotFound(class494Var, strCapitalizeFirstLetter);
                                });
                            }
                        });
                    });
                }
                if (!this.pendingTasks.isEmpty()) {
                    ArrayList<Runnable> arrayList = new ArrayList<Runnable>(this.pendingTasks);
                    this.pendingTasks.clear();
                    arrayList.forEach((v0) -> {
                        v0.run();
                    });
                }
                this.blockUpdates.clear();
                this.structures.removeIf(class493Var -> {
                    return class493Var.time - ((double) System.currentTimeMillis()) <= 0.0d;
                });
                this.trackedBosses.removeIf(class496Var -> {
                    return (class496Var.timeEnd + 90000.0d) - ((double) System.currentTimeMillis()) <= 0.0d;
                });
                if (!this.cooldownNotify.isValue()) {
                    this.cooldownStates.clear();
                    return;
                }
                ClientPlayerEntity player2 = this.mc.getPlayer();
                if (player2 != null) {
                    this.itemRules.rules.values().forEach(class494Var2 -> {
                        Item itemBoundItem = class494Var2.boundItem();
                        boolean zIsCoolingDown = player2.getItemCooldownManager().isCoolingDown(itemBoundItem.getDefaultStack());
                        if (this.cooldownStates.getOrDefault(itemBoundItem, false).booleanValue() && !zIsCoolingDown) {
                            Expensive.INSTANCE.notificationRepository().post((Text) Text.literal(Lang.FTHELPER_COOLDOWN_READY.effective().replace("{item}", String.valueOf(Formatting.RED) + (class494Var2.serverItem() ? StringUtil.capitalizeFirstLetter(class494Var2.serverName()) : itemBoundItem.getName().getString()) + String.valueOf(Formatting.RESET))), itemBoundItem.getDefaultStack(), 4L, TimeUnit.SECONDS);
                        }
                        this.cooldownStates.put(itemBoundItem, Boolean.valueOf(zIsCoolingDown));
                    });
                }
            }
        });
        register(PacketReceiveEvent.class, class051Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded()) {
                net.minecraft.network.packet.Packet<?> packet = class051Var.getPacket();
                Objects.requireNonNull(packet);
                if (packet instanceof ChunkDeltaUpdateS2CPacket) {
                    ChunkDeltaUpdateS2CPacket chunkDeltaUpdateS2CPacket = (ChunkDeltaUpdateS2CPacket) packet;
                    if (this.structureTimer.isValue()) {
                        chunkDeltaUpdateS2CPacket.visitUpdates((blockPos, blockState) -> {
                            this.blockUpdates.put(blockPos.toImmutable(), blockState);
                        });
                        this.pendingTasks.add(() -> {
                            chunkDeltaUpdateS2CPacket.visitUpdates((blockPos2, blockState2) -> {
                                Vec3d centerPos = blockPos2.toImmutable().toCenterPos();
                                if (this.blockUpdates.size() <= 50 || this.blockUpdates.size() >= 600) {
                                    return;
                                }
                                if (isSmallStructure(blockPos2.up(2))) {
                                    addTrackedStructure(Items.NETHERITE_SCRAP, centerPos, System.currentTimeMillis() + 15000);
                                } else if (isLargeStructure(blockPos2.up(3))) {
                                    addTrackedStructure(Items.NETHERITE_SCRAP, centerPos, System.currentTimeMillis() + 30000);
                                }
                            });
                        });
                    }
                } else if (packet instanceof GameMessageS2CPacket) {
                    GameMessageS2CPacket gameMessageS2CPacket = (GameMessageS2CPacket) packet;
                    if (this.autoPoint.isValue()) {
                        Text textContent = gameMessageS2CPacket.content();
                        String string = textContent.toString();
                        String string2 = textContent.getString();
                        String strSubstringBetween = StringUtils.substringBetween(string2, "|||   [", "]   ");
                        if (strSubstringBetween != null) {
                            String strSubstringBetween2 = StringUtils.substringBetween(string, "value='/gps ", "'");
                            String strSubstringBetween3 = StringUtils.substringBetween(string2, "Уровень лута: ", "\n ║");
                            String strSubstringBetween4 = StringUtils.substringBetween(string2, "Призван игроком: ", "\n ║");
                            if (strSubstringBetween2 == null) {
                                switch (strSubstringBetween) {
                                    case "Сундук смерти":
                                        addTrackedBoss(strSubstringBetween, strSubstringBetween3, strSubstringBetween4, BlockPos.ofFloored(-155.0d, 64.0d, 205.0d).toCenterPos(), "lobby", 300, 0);
                                        break;
                                    case "Адская резня":
                                        addTrackedBoss(strSubstringBetween, strSubstringBetween3, strSubstringBetween4, BlockPos.ofFloored(48.0d, 87.0d, 73.0d).toCenterPos(), "lobby", 180, 120);
                                        break;
                                }
                                return;
                            }
                            String[] strArrSplit = strSubstringBetween2.split(" ");
                            Vec3d centerPos = BlockPos.ofFloored(Integer.parseInt(strArrSplit[0]), Integer.parseInt(strArrSplit[1]), Integer.parseInt(strArrSplit[2])).toCenterPos();
                            switch (strSubstringBetween) {
                                case "Мистический сундук":
                                    addTrackedBoss(strSubstringBetween, strSubstringBetween3, strSubstringBetween4, centerPos, "overworld", 300, 0);
                                    break;
                                case "Вулкан":
                                    addTrackedBoss(strSubstringBetween, strSubstringBetween3, strSubstringBetween4, centerPos, "overworld", 300, 120);
                                    break;
                                case "Метеоритный дождь":
                                case "Маяк убийца":
                                case "Мистический Алтарь":
                                    addTrackedBoss(strSubstringBetween, strSubstringBetween3, strSubstringBetween4, centerPos, "overworld", 360, 0);
                                    break;
                                case "Загадочный маяк":
                                    addTrackedBoss(strSubstringBetween, strSubstringBetween3, strSubstringBetween4, centerPos, "overworld", 60, 180);
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    public void notifyCooldown(Item item, String str) {
        Expensive.INSTANCE.notificationRepository().post(NotificationType.WARNING, (Text) Text.literal(Lang.ITEM_ON_COOLDOWN.effective().replace("{item}", String.valueOf(Formatting.RED) + str + String.valueOf(Formatting.RESET)).replace("{seconds}", String.valueOf(Formatting.RED) + String.format(Locale.US, "%.1f", Float.valueOf(PlayerActionUtil.INSTANCE.getRemainingCooldownSeconds(item))) + String.valueOf(Formatting.RESET))), 2L, TimeUnit.SECONDS);
    }

    public void drawTooltip(MatrixStack matrixStack, DrawEngine class154Var, List<String> list, Vector2f vector2f) {
        ColorStack class115VarColorStack = class154Var.colorStack();
        float f = 0.0f;
        for (String str : list) {
            MsdfFont class161Var = Fonts.INTER_MEDIUM.get();
            class154Var.msdfFont(matrixStack.peek().getPositionMatrix(), class161Var, str, (int) (vector2f.x - (class161Var.getWidth(str, 10.0f) / 2.0f)), (int) (vector2f.y + 17.0f + f), 10.0f, 0.0f, class115VarColorStack.computeColor(13948641));
            f += 10.0f;
        }
    }

    public void addTrackedBoss(String str, String str2, String str3, Vec3d vec3d, String str4, int i, int i2) {
        if (this.trackedBosses.stream().noneMatch(class496Var -> {
            return class496Var.vec.equals(vec3d);
        })) {
            long jCurrentTimeMillis = System.currentTimeMillis() + (((long) i) * 1000);
            this.trackedBosses.add(new FtTrackedBoss(str, str2, str3, vec3d, str4, ServerUtil.getAnarchy(), jCurrentTimeMillis, jCurrentTimeMillis + (((long) i2) * 1000)));
            Expensive.INSTANCE.notificationRepository().post(NotificationType.EVENT, (Text) Text.literal("Начался ивент: " + str), 10L, TimeUnit.SECONDS);
            WavSoundPlayer.INSTANCE.playSound("apple_pay", 80.0f, false);
        }
    }

    public void addTrackedStructure(Item item, Vec3d vec3d, double d) {
        if (this.structures.stream().noneMatch(class493Var -> {
            return class493Var.vec.equals(vec3d);
        })) {
            this.structures.add(new FtTrackedStructure(item, vec3d, ServerUtil.getWorldType(), ServerUtil.getAnarchy(), d));
        }
    }

    public void applyState() {
        this.itemRules.addByName(this.disorientationKey, "дезориентация", Items.ENDER_EYE).addByName(this.trapKey, "трапка", Items.NETHERITE_SCRAP).addByName(this.plastKey, "пласт", Items.DRIED_KELP).addByName(this.obviousDustKey, "явная пыль", Items.SUGAR).addByName(this.godAuraKey, "божья аура", Items.PHANTOM_MEMBRANE).addByName(this.frostSnowballKey, "снежок заморозка", Items.SNOWBALL).addPotionByName(this.assassinPotionKey, "зелье ассасина", true).addPotionByName(this.palladinPotionKey, "зелье палладина", true).addPotionByName(this.angerPotionKey, "зелье гнева", true).addPotionByName(this.holyWaterKey, "святая вода", true).addPotionByName(this.petardKey, "хлопушка", false).addPotionByName(this.radiationPotionKey, "зелье радиации", false).addPotionByName(this.sleepingPotionKey, "снотворное", false).addByItem(this.crossbowKey, Items.CROSSBOW).addByItemFlagged(this.windChargeKey, Items.WIND_CHARGE, true).addByItem(this.shulkerKey, Items.SHULKER_BOX);
    }

    public Map<KeybindSetting, ItemSearchRule> getBindItems() {
        return Collections.unmodifiableMap(this.itemRules.rules);
    }

    public void notifyNotFound(ItemSearchRule class494Var, String str) {
        if (class494Var.boundItem() instanceof CrossbowItem) {
            Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal(Lang.NO_ITEM_FOUND.effective().replace("{item}", String.valueOf(Formatting.RED) + "Charged Crossbow" + String.valueOf(Formatting.RESET))), 2L, TimeUnit.SECONDS);
        } else {
            Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal(Lang.NO_ITEM_FOUND.effective().replace("{item}", String.valueOf(Formatting.RED) + str + String.valueOf(Formatting.RESET))), 2L, TimeUnit.SECONDS);
        }
    }

    public void useItem(SlotSearchResult2 class329Var, ItemSearchRule class494Var, ClientPlayerEntity clientPlayerEntity, InventoryService class011Var, String str) {
        boolean z = class494Var.needRotation && this.potionAim.isValue();
        InventoryTask class012VarCreate = InventoryTask.create(class494Var.getSearchPredicate(), class329Var, true, true, z);
        if (z) {
            Vec3d vec3dMethod022 = findAimTarget(clientPlayerEntity, this.mc.getWorld());
            class012VarCreate = class012VarCreate.withRotation(vec3dMethod022 != null ? RotationMath.INSTANCE.fromVec3d(vec3dMethod022.subtract(clientPlayerEntity.getEyePos())) : new Rotation(clientPlayerEntity.getYaw(), 90.0f));
        }
        class011Var.addTask(class012VarCreate, this);
        Expensive.INSTANCE.notificationRepository().post(NotificationType.INFO, (Text) Text.literal(Lang.USE_ITEM.effective().replace("{item}", String.valueOf(Formatting.RED) + str + String.valueOf(Formatting.RESET))), 2L, TimeUnit.SECONDS);
    }

    public Vec3d findAimTarget(ClientPlayerEntity clientPlayerEntity, ClientWorld clientWorld) {
        return (Vec3d) StreamSupport.stream(clientWorld.getCollisions(clientPlayerEntity, clientPlayerEntity.getBoundingBox().expand(0.0d, 0.5d, 0.0d)).spliterator(), false).min(Comparator.comparingDouble(voxelShape -> {
            return voxelShape.getBoundingBox().getCenter().squaredDistanceTo(clientPlayerEntity.getPos());
        })).map(voxelShape2 -> {
            return voxelShape2.getBoundingBox().getCenter();
        }).orElse(null);
    }

    public boolean isSmallStructure(BlockPos blockPos) {
        BlockState blockState;
        int i = 0;
        for (BlockPos blockPos2 : BlockUtil.getCube(blockPos, 2.0f)) {
            if (blockPos2.toCenterPos().distanceTo(blockPos.toCenterPos()) < 2.0d) {
                BlockState blockState2 = this.blockUpdates.get(blockPos2);
                if (blockState2 != null && !blockState2.isAir()) {
                    i++;
                }
            } else if (!blockPos2.equals(blockPos.up(2).north().east()) && !blockPos2.equals(blockPos.up(2).north().west()) && !blockPos2.equals(blockPos.up(2).south().east()) && !blockPos2.equals(blockPos.up(2).south().west()) && ((blockState = this.blockUpdates.get(blockPos2)) == null || blockState.isAir())) {
                i++;
            }
            if (i > 5) {
                return false;
            }
        }
        return true;
    }

    public boolean isLargeStructure(BlockPos blockPos) {
        BlockState blockState;
        int i = 0;
        for (BlockPos blockPos2 : BlockUtil.getCube(blockPos, 3.0f)) {
            if (Math.abs(blockPos2.getX() - blockPos.getX()) <= 2 && Math.abs(blockPos2.getY() - blockPos.getY()) <= 2 && Math.abs(blockPos2.getZ() - blockPos.getZ()) <= 2) {
                BlockState blockState2 = this.blockUpdates.get(blockPos2);
                if (blockState2 != null && !blockState2.isAir()) {
                    i++;
                }
            } else if (!blockPos2.equals(blockPos.up(3)) && ((blockState = this.blockUpdates.get(blockPos2)) == null || blockState.isAir())) {
                i++;
            }
            if (i > 5) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void deactivate() {
        this.trackedBosses.clear();
        this.structures.clear();
        super.deactivate();
    }

    public List<FtTrackedStructure> getStructures() {
        return this.structures;
    }
}
