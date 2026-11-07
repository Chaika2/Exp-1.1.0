package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.ui.setting.BooleanSetting;
import aethereal.event.ClientTickEvent;
import aethereal.render.ColorStack;
import aethereal.math.DirectionalInput;
import aethereal.util.DrawEngine;
import aethereal.ui.setting.ExpandableSetting;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.util.GrimDelayHandler;
import aethereal.type.HwHelperItemRules;
import aethereal.model.HwTrackedItem;
import aethereal.util.InventoryItemFinder;
import aethereal.type.InventoryScope;
import aethereal.net.InventoryService;
import aethereal.util.InventoryTask;
import aethereal.type.ItemSearchRule;
import aethereal.ui.setting.KeybindSetting;
import aethereal.ui.setting.LabelSelectSetting;
import aethereal.Lang;
import aethereal.util.MathUtil;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.MovementInputEvent;
import aethereal.type.NotificationType;
import aethereal.event.PacketReceiveEvent;
import aethereal.net.PacketSender;
import aethereal.util.PlayerActionUtil;
import aethereal.event.PlayerTickEvent;
import aethereal.util.ProjectionUtil;
import aethereal.event.Render2DEvent;
import aethereal.math.Rotation;
import aethereal.math.RotationMath;
import aethereal.util.ServerUtil;
import aethereal.render.ShapeRenderer;
import aethereal.model.SlotSearchResult2;
import aethereal.math.Stopwatch;
import aethereal.util.StringUtil;
import aethereal.render.ThemePalette;
import aethereal.event.WorldRenderEvent;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector2f;

@Aliases(aliases = {"HolyWorld Helper", "HW Helper", "Auto Use Items"})
public class HWHelperModule extends Module {
    public LabelSelectSetting modeSelector;
    public final KeybindSetting trapKey;
    public final KeybindSetting explosiveTrapKey;
    public final KeybindSetting stunKey;
    public final KeybindSetting jackOLanternKey;
    public final KeybindSetting farewellHowlKey;
    public final KeybindSetting explosiveThingKey;
    public final KeybindSetting crossbowKey;
    public final KeybindSetting windChargeKey;

    public final BooleanSetting pickLockpickSetting;

    public final BooleanSetting pickExpBottleSetting;
    public final ExpandableSetting eventHelperSetting;
    public final ExpandableSetting useByBindSetting;

    public final BooleanSetting structureTimerSetting;

    public final BooleanSetting forceStopSetting;
    public final HwHelperItemRules itemRules;

    public final Mc mc;
    public final Stopwatch teleportTimer;
    public final List<HwTrackedItem> trackedItems;
    public final Pattern teleportPattern;
    public int teleportSeconds;

    public HWHelperModule() {
        super(ModuleTab.MISC, "HW Helper");
        this.trapKey = new KeybindSetting(Lang.HWHELPER_TRAP_KEY);
        this.explosiveTrapKey = new KeybindSetting(Lang.HWHELPER_EXPLOSIVE_TRAP_KEY);
        this.stunKey = new KeybindSetting(Lang.HWHELPER_STUN_KEY);
        this.jackOLanternKey = new KeybindSetting(Lang.HWHELPER_JACKOLANTERN_KEY);
        this.farewellHowlKey = new KeybindSetting(Lang.HWHELPER_FAREWELL_HOWL_KEY);
        this.explosiveThingKey = new KeybindSetting(Lang.HWHELPER_EXPLOSIVE_THING_KEY);
        this.crossbowKey = new KeybindSetting(Lang.HWHELPER_CROSSBOW_KEY);
        this.windChargeKey = new KeybindSetting(Lang.FTHELPER_WIND_CHARGE_KEY);
        this.pickLockpickSetting = new BooleanSetting(Lang.HWHELPER_EVENTHELPER_PICKLOCKPICK, Lang.HWHELPER_EVENTHELPER_PICKLOCKPICK_DESC);
        this.pickExpBottleSetting = new BooleanSetting(Lang.HWHELPER_EVENTHELPER_PICKEXPBOTTLE, Lang.HWHELPER_EVENTHELPER_PICKEXPBOTTLE_DESC);
        this.eventHelperSetting = new ExpandableSetting(Lang.HWHELPER_EVENTHELPER, Lang.HWHELPER_EVENTHELPER_DESC).settings(this.pickLockpickSetting, this.pickExpBottleSetting);
        this.useByBindSetting = new ExpandableSetting(Lang.FTHELPER_USEBYBIND, Lang.FTHELPER_USEBYBIND_DESC);
        this.structureTimerSetting = new BooleanSetting(Lang.FTHELPER_STRUCTURE_TIMER, Lang.FTHELPER_STRUCTURE_TIMER_DESC);
        this.forceStopSetting = new BooleanSetting(Lang.HWHELPER_FORCE_STOP);
        this.itemRules = new HwHelperItemRules();
        this.mc = Mc.INSTANCE;
        this.teleportTimer = new Stopwatch();
        this.trackedItems = new ArrayList();
        this.teleportPattern = Pattern.compile("через\\s+(\\d+)\\s+секунд.*не\\s+двигайтесь");
        this.teleportSeconds = 0;
        addSettings(this.modeSelector, this.eventHelperSetting, this.useByBindSetting, this.forceStopSetting, this.structureTimerSetting);
        this.useByBindSetting.setSubSettings(List.of(this.trapKey, this.explosiveTrapKey, this.jackOLanternKey, this.farewellHowlKey, this.explosiveThingKey, this.stunKey, this.crossbowKey, this.windChargeKey));
        applyState();
        register(ClientTickEvent.class, class181Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded()) {
                ClientPlayerEntity player = this.mc.getPlayer();
                InventoryService class011VarInventoryService = Expensive.INSTANCE.inventoryService();
                InventoryItemFinder class123VarSearcher = class011VarInventoryService.searcher();
                this.itemRules.rules.forEach((class663Var, class494Var) -> {
                    class663Var.consumer(class664Var -> {
                        if (GrimDelayHandler.script.isFinished()) {
                            String strCapitalizeFirstLetter = class494Var.serverItem() ? StringUtil.capitalizeFirstLetter(class494Var.serverName()) : class494Var.boundItem().getName().getString();
                            if (player.getItemCooldownManager().isCoolingDown(class494Var.boundItem().getDefaultStack())) {
                                notifyCooldown(class494Var.boundItem(), strCapitalizeFirstLetter);
                            } else {
                                class123VarSearcher.findItem(class494Var.getSearchPredicate(), InventoryScope.ALL).ifPresentOrElse(class329Var -> {
                                    useFoundItem(class329Var, class494Var, player, class011VarInventoryService, strCapitalizeFirstLetter);
                                }, () -> {
                                    notifyItemNotFound(class494Var, strCapitalizeFirstLetter);
                                });
                            }
                        }
                    });
                });
            }
        });
        register(WorldRenderEvent.class, class016Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded()) {
                this.trackedItems.forEach(class500Var -> {
                    if (class500Var.boxSize != 0.0f && class500Var.anarchy == ServerUtil.getAnarchy() && ServerUtil.getWorldType().equals(class500Var.world)) {
                        ShapeRenderer.INSTANCE.addBox(class016Var.matrixStack().peek().getPositionMatrix(), Box.from(class500Var.vec.add(-0.5d)).expand(class500Var.boxSize), Expensive.INSTANCE.drawEngine().colorStack().computeColor(Expensive.INSTANCE.theme().palette().accent().argb(), 0.5f));
                    }
                });
            }
        });
        register(Render2DEvent.class, class311Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded() && class311Var.isPre()) {
                MatrixStack matrixStack = class311Var.matrixStack();
                Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
                DrawEngine class154VarDrawEngine = Expensive.INSTANCE.drawEngine();
                ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
                ThemePalette class764VarPalette = Expensive.INSTANCE.theme().palette();
                class154VarDrawEngine.begin();
                class115VarColorStack.push();
                this.trackedItems.forEach(class500Var -> {
                    double dCurrentTimeMillis = (class500Var.time - System.currentTimeMillis()) / 1000.0d;
                    Optional<Vector2f> optionalWorldToScreen = ProjectionUtil.worldToScreen(class500Var.vec);
                    if (optionalWorldToScreen.isPresent()) {
                        Vector2f vector2f = optionalWorldToScreen.get();
                        String str = MathUtil.round(dCurrentTimeMillis, 0.10000000149011612d) + "с";
                        float width = Fonts.INTER_BOLD.get().getWidth(str, 14.0f);
                        float height = Fonts.INTER_BOLD.get().getHeight(14.0f);
                        int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.text().tone(400).argb());
                        float f = 6.0f + 16.0f + 4.0f + width + 6.0f + 2.0f;
                        float fMax = Math.max(16.0f, height) + (6.0f * 2.0f);
                        float f2 = vector2f.x - (f / 2.0f);
                        float f3 = vector2f.y - (fMax / 2.0f);
                        if (class500Var.anarchy == ServerUtil.getAnarchy() && ServerUtil.getWorldType().equals(class500Var.world)) {
                            class154VarDrawEngine.roundedRectangle(positionMatrix, f2, f3, f, fMax, 7.0f, 2.5f, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(700).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(801).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(801).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb()));
                            float f4 = f2 + 6.0f;
                            class154VarDrawEngine.itemStack(matrixStack.peek().getPositionMatrix(), class500Var.item.getDefaultStack(), f4, f3 + ((fMax - 16.0f) / 2.0f), 0.5f, 1.0f);
                            class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), Fonts.INTER_BOLD.get(), str, f4 + 16.0f + 4.0f, f3 + ((fMax - height) / 2.0f), 14.0f, 0.0f, iComputeColor);
                        }
                    }
                });
                class115VarColorStack.pop();
                class154VarDrawEngine.end();
            }
        });
        register(PacketReceiveEvent.class, class051Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                net.minecraft.network.packet.Packet<?> packet = class051Var.getPacket();
                Objects.requireNonNull(packet);
                if (packet instanceof GameMessageS2CPacket) {
                    String lowerCase = (String) (((GameMessageS2CPacket) packet).content().getString().toLowerCase());
                    Matcher matcher = this.teleportPattern.matcher(lowerCase);
                    if (matcher.find()) {
                        this.teleportSeconds = Integer.parseInt(matcher.group(1));
                        this.teleportTimer.reset();
                    }
                    if (lowerCase.equalsIgnoreCase("Телепортирование начинается...") || lowerCase.equalsIgnoreCase("Запрос на телепортацию отменен.")) {
                        this.teleportSeconds = 0;
                    }
                } else if (packet instanceof PlaySoundS2CPacket) {
                    PlaySoundS2CPacket playSoundS2CPacket = (PlaySoundS2CPacket) packet;
                    if (playSoundS2CPacket.getCategory().equals(SoundCategory.MASTER) && this.structureTimerSetting.isValue()) {
                        Vec3d centerPos = BlockPos.ofFloored(playSoundS2CPacket.getX(), playSoundS2CPacket.getY(), playSoundS2CPacket.getZ()).toCenterPos();
                        switch (playSoundS2CPacket.getSound().getIdAsString().replace("minecraft:", "")) {
                            case "block.beacon.deactivate":
                                trackItem(Items.NETHER_STAR, centerPos, 15000L, 14.5f);
                                break;
                            case "entity.generic.explode":
                                trackItem(Items.PRISMARINE_SHARD, centerPos.add(0.0d, -1.0d, 0.0d), 11000L, 0.0f);
                                break;
                        }
                    }
                }
            }
        });
        register(MovementInputEvent.class, class040Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                if (shouldBlockMovement()) {
                    class040Var.setInput(DirectionalInput.NONE);
                    class040Var.setJumping(false);
                    class040Var.setSprinting(false);
                }
                if (this.teleportSeconds <= 0 || !this.teleportTimer.hasElapsed(this.teleportSeconds + 1, TimeUnit.SECONDS)) {
                    return;
                }
                this.teleportSeconds = 0;
            }
        });
        register(PlayerTickEvent.class, class130Var -> {
            if (!class130Var.isPost() && isState() && this.mc.isWorldLoaded()) {
                if (this.eventHelperSetting.isValue() && (this.pickLockpickSetting.isValue() || this.pickExpBottleSetting.isValue())) {
                    ClientPlayerEntity player = this.mc.getPlayer();
                    ClientWorld world = this.mc.getWorld();
                    ClientPlayerInteractionManager interactionManager = this.mc.getInteractionManager();
                    for (Entity villagerEntity : world.getEntities()) {
                        if (villagerEntity.isAlive() && !(villagerEntity instanceof PlayerEntity)) {
                            boolean z = (villagerEntity instanceof VillagerEntity) && ((VillagerEntity) villagerEntity).getOffHandStack().getItem() == Items.EXPERIENCE_BOTTLE && this.pickExpBottleSetting.isValue();
                            if (((villagerEntity instanceof WanderingTraderEntity) && ((WanderingTraderEntity) villagerEntity).getOffHandStack().getItem() == Items.TRIPWIRE_HOOK && this.pickLockpickSetting.isValue()) || z) {
                                if (villagerEntity.distanceTo(player) < 5.0f) {
                                    Rotation class007VarFromVec3d = RotationMath.INSTANCE.fromVec3d(villagerEntity.getEyePos().subtract(player.getEyePos()));
                                    PacketSender.sendPacket(new PlayerMoveC2SPacket.Full(player.getX(), player.getY(), player.getZ(), class007VarFromVec3d.getYaw(), class007VarFromVec3d.getPitch(), player.isOnGround(), player.horizontalCollision));
                                    interactionManager.interactEntity(player, villagerEntity, Hand.MAIN_HAND);
                                    PacketSender.sendPacket(new PlayerMoveC2SPacket.Full(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch(), player.isOnGround(), player.horizontalCollision));
                                }
                            }
                        }
                    }
                }
                this.trackedItems.removeIf(class500Var -> {
                    return class500Var.time - ((double) System.currentTimeMillis()) <= 0.0d;
                });
            }
        });
    }

    public void notifyCooldown(Item item, String str) {
        Expensive.INSTANCE.notificationRepository().post(NotificationType.WARNING, (Text) Text.literal(Lang.ITEM_ON_COOLDOWN.effective().replace("{item}", String.valueOf(Formatting.RED) + str + String.valueOf(Formatting.RESET)).replace("{seconds}", String.valueOf(Formatting.RED) + String.format(Locale.US, "%.1f", Float.valueOf(PlayerActionUtil.INSTANCE.getRemainingCooldownSeconds(item))) + String.valueOf(Formatting.RESET))), 2L, TimeUnit.SECONDS);
    }

    public boolean shouldBlockMovement() {
        return this.forceStopSetting.isValue() && this.teleportSeconds > 0 && !this.teleportTimer.hasElapsed((long) (this.teleportSeconds + 1), TimeUnit.SECONDS);
    }

    public void applyState() {
        this.itemRules.addByName(this.trapKey, "трапка", Items.POPPED_CHORUS_FRUIT).addByName(this.explosiveTrapKey, "взрывная трапка", Items.PRISMARINE_SHARD).addByName(this.stunKey, "стан", Items.NETHER_STAR).addByName(this.jackOLanternKey, "светильник джейка", Items.JACK_O_LANTERN).addByName(this.farewellHowlKey, "прощальный гул", Items.FIREWORK_STAR).addByName(this.explosiveThingKey, "взрывная штучка", Items.FIRE_CHARGE).addByItemFlagged(this.windChargeKey, Items.WIND_CHARGE, true).addByItem(this.crossbowKey, Items.CROSSBOW);
    }

    public Map<KeybindSetting, ItemSearchRule> getBindItems() {
        return Collections.unmodifiableMap(this.itemRules.rules);
    }

    public void notifyItemNotFound(ItemSearchRule class494Var, String str) {
        if (class494Var.boundItem() instanceof CrossbowItem) {
            Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal(Lang.NO_ITEM_FOUND.effective().replace("{item}", String.valueOf(Formatting.RED) + "Charged Crossbow" + String.valueOf(Formatting.RESET))), 2L, TimeUnit.SECONDS);
        } else {
            Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal(Lang.NO_ITEM_FOUND.effective().replace("{item}", String.valueOf(Formatting.RED) + str + String.valueOf(Formatting.RESET))), 2L, TimeUnit.SECONDS);
        }
    }

    public void useFoundItem(SlotSearchResult2 class329Var, ItemSearchRule class494Var, ClientPlayerEntity clientPlayerEntity, InventoryService class011Var, String str) {
        class011Var.addTask(InventoryTask.create(class494Var.getSearchPredicate(), class329Var, true, true, false), this);
        Expensive.INSTANCE.notificationRepository().post((Text) Text.literal(Lang.USE_ITEM.effective().replace("{item}", String.valueOf(Formatting.RED) + str + String.valueOf(Formatting.RESET))), class494Var.boundItem().getDefaultStack(), 2L, TimeUnit.SECONDS);
    }

    public Vec3d findNearestCollisionCenter(ClientPlayerEntity clientPlayerEntity, ClientWorld clientWorld) {
        return (Vec3d) StreamSupport.stream(clientWorld.getCollisions(clientPlayerEntity, clientPlayerEntity.getBoundingBox().expand(0.0d, 0.5d, 0.0d)).spliterator(), false).min(Comparator.comparingDouble(voxelShape -> {
            return voxelShape.getBoundingBox().getCenter().squaredDistanceTo(clientPlayerEntity.getPos());
        })).map(voxelShape2 -> {
            return voxelShape2.getBoundingBox().getCenter();
        }).orElse(null);
    }

    public void trackItem(Item item, Vec3d vec3d, long j, float f) {
        if (this.trackedItems.stream().noneMatch(class500Var -> {
            return class500Var.vec().equals(vec3d);
        })) {
            this.trackedItems.add(new HwTrackedItem(item, vec3d, ServerUtil.getWorldType(), ServerUtil.getAnarchy(), System.currentTimeMillis() + j, f));
        }
    }
}
