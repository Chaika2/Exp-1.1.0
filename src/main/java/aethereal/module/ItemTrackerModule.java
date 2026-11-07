package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.util.ChatUtil;
import aethereal.Expensive;
import aethereal.util.FriendManager;
import aethereal.type.ItemTrackerEventType;
import aethereal.type.ItemTrackerOutput;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.event.PacketReceiveEvent;
import aethereal.event.PlayerTickEvent;
import aethereal.type.TargetPearlMode;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

@Aliases(aliases = {"Item Tracker", "Potion Tracker", "Totem Tracker", "Effect Tracker"})
public class ItemTrackerModule extends Module {
    final ModeSetting<TargetPearlMode> targetSetting;
    final MultiSelectSetting<ItemTrackerEventType> eventsSetting;
    final MultiSelectSetting<ItemTrackerOutput> outputSetting;
    final Map<Integer, ItemStack> drinkingPotions = new HashMap();
    final Mc mc;
    int lastTotemNotifyId = -1;
    long lastTotemNotifyTime = 0L;
    int lastFoodNotifyId = -1;
    long lastFoodNotifyTime = 0L;
    int lastPotionNotifyId = -1;
    long lastPotionNotifyTime = 0L;

    public ItemTrackerModule() {
        super(ModuleTab.MISC, "Item Tracker");
        this.targetSetting = new ModeSetting(Lang.TARGETPEARL_TARGET, Lang.TARGETPEARL_TARGET_DESC).values(TargetPearlMode.class);
        this.eventsSetting = new MultiSelectSetting(Lang.ITEM_TRACKER_EVENTS).values(ItemTrackerEventType.class).select(ItemTrackerEventType.FOOD_USE, ItemTrackerEventType.TOTEM_POP, ItemTrackerEventType.POTION_USE);
        this.outputSetting = new MultiSelectSetting(Lang.ITEM_TRACKER_OUTPUT).values(ItemTrackerOutput.class).select(ItemTrackerOutput.CHAT, ItemTrackerOutput.NOTIFICATIONS);
        this.mc = Mc.INSTANCE;
        addSettings(this.targetSetting, this.eventsSetting, this.outputSetting);
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && class130Var.isPre() && Mc.INSTANCE.isWorldLoaded()) {
                trackDrinkingPotions();
            }
        });
        register(PacketReceiveEvent.class, class051Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded()) {
                net.minecraft.network.packet.Packet<?> packet = class051Var.getPacket();
                Objects.requireNonNull(packet);
                if (packet instanceof EntityEquipmentUpdateS2CPacket) {
                    EntityEquipmentUpdateS2CPacket entityEquipmentUpdateS2CPacket = (EntityEquipmentUpdateS2CPacket) packet;
                    Optional<Entity> optionalOfNullable = Optional.ofNullable(this.mc.getWorld().getEntityById(entityEquipmentUpdateS2CPacket.getEntityId()));
                    Class<PlayerEntity> cls = PlayerEntity.class;
                    Objects.requireNonNull(PlayerEntity.class);
                    Optional<Entity> optionalFilter = optionalOfNullable.filter((v1) -> {
                        return cls.isInstance(v1);
                    });
                    Class<PlayerEntity> cls2 = PlayerEntity.class;
                    Objects.requireNonNull(PlayerEntity.class);
                    optionalFilter.map(cls2::cast).filter(playerEntity -> {
                        return !FriendManager.isFriend(playerEntity.getName().getString());
                    }).ifPresent(playerEntity2 -> {
                        entityEquipmentUpdateS2CPacket.getEquipmentList().stream().filter(pair -> {
                            return ((EquipmentSlot) pair.getFirst()).equals(playerEntity2.getActiveHand().equals(Hand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                        }).findFirst().map((v0) -> {
                            return v0.getSecond();
                        }).ifPresent(itemStack -> {
                            ItemStack stackInHand = playerEntity2.getStackInHand(playerEntity2.getActiveHand());
                            LivingEntity livingEntityLastTarget = ((AttackAuraModule) Expensive.INSTANCE.moduleRepository().get(AttackAuraModule.class)).lastTarget();
                            boolean zContains = stackInHand.contains(DataComponentTypes.FOOD);
                            boolean z = this.targetSetting.isSelected(TargetPearlMode.ALL) || (livingEntityLastTarget != null && livingEntityLastTarget.equals(playerEntity2));
                            if (this.eventsSetting.isSelected(ItemTrackerEventType.FOOD_USE) && playerEntity2.getItemUseTime() >= stackInHand.getMaxUseTime(playerEntity2) - 1 && z && zContains) {
                                if (this.lastFoodNotifyId == playerEntity2.getId() && System.currentTimeMillis() - this.lastFoodNotifyTime < 1000L) {
                                    return;
                                }
                                this.lastFoodNotifyId = playerEntity2.getId();
                                this.lastFoodNotifyTime = System.currentTimeMillis();
                                String strTrim = stackInHand.getName().getString().replace("[★]", "").replace("fff", "").replace("ggg", "").trim();
                                if (this.outputSetting.isSelected(ItemTrackerOutput.NOTIFICATIONS)) {
                                    Expensive.INSTANCE.notificationRepository().post(Text.of(playerEntity2.getName().getString().trim() + " использовал " + String.valueOf(Formatting.RED) + strTrim + String.valueOf(Formatting.RESET)), stackInHand.copy(), 3L, TimeUnit.SECONDS);
                                }
                                if (this.outputSetting.isSelected(ItemTrackerOutput.CHAT)) {
                                    ChatUtil.addChatMessage((Text) Text.empty().append(playerEntity2.getName().copy().formatted(Formatting.GRAY)).append(Text.literal(" использовал ").formatted(Formatting.WHITE)).append(stackInHand.getItem().getName().copy().formatted(Formatting.RED)));
                                }
                            }
                        });
                    });
                } else if (packet instanceof EntityStatusS2CPacket) {
                    EntityStatusS2CPacket entityStatusS2CPacket = (EntityStatusS2CPacket) packet;
                    if (entityStatusS2CPacket.getStatus() == 35) {
                        if ((entityStatusS2CPacket.getEntity(this.mc.getWorld())) instanceof PlayerEntity entity ) {
                            PlayerEntity playerEntity3 = entity;
                            if (playerEntity3.equals(this.mc.getPlayer()) || FriendManager.isFriend(playerEntity3.getName().getString())) {
                                return;
                            }
                            LivingEntity livingEntityLastTarget = ((AttackAuraModule) Expensive.INSTANCE.moduleRepository().get(AttackAuraModule.class)).lastTarget();
                            if ((this.targetSetting.isSelected(TargetPearlMode.ALL) || (livingEntityLastTarget != null && livingEntityLastTarget.equals(playerEntity3))) && this.eventsSetting.isSelected(ItemTrackerEventType.TOTEM_POP)) {
                                ItemStack offHandStack = playerEntity3.getOffHandStack();
                                ItemStack mainHandStack = playerEntity3.getMainHandStack();
                                boolean zHasGlint = false;
                                if (offHandStack.isOf(Items.TOTEM_OF_UNDYING)) {
                                    zHasGlint = offHandStack.hasGlint();
                                } else if (mainHandStack.isOf(Items.TOTEM_OF_UNDYING)) {
                                    zHasGlint = mainHandStack.hasGlint();
                                }
                                ItemStack itemStack = new ItemStack(Items.TOTEM_OF_UNDYING);
                                Text textAppend = Text.empty().append(playerEntity3.getName().copy().formatted(Formatting.GRAY)).append(Text.literal(" потерял тотем, зачарован: ").formatted(Formatting.GRAY)).append(Text.literal(zHasGlint ? "да" : "нет").formatted(Formatting.RED));
                                long currentTimeMillis = System.currentTimeMillis();
                                if (this.lastTotemNotifyId == playerEntity3.getId() && currentTimeMillis - this.lastTotemNotifyTime < 1000L) {
                                    return;
                                }
                                this.lastTotemNotifyId = playerEntity3.getId();
                                this.lastTotemNotifyTime = currentTimeMillis;
                                if (this.outputSetting.isSelected(ItemTrackerOutput.NOTIFICATIONS)) {
                                    Expensive.INSTANCE.notificationRepository().post(textAppend, itemStack.copy(), 3L, TimeUnit.SECONDS);
                                }
                                if (this.outputSetting.isSelected(ItemTrackerOutput.CHAT)) {
                                    ChatUtil.addChatMessage(textAppend);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public void trackDrinkingPotions() {
        var world = this.mc.getWorld();
        var clientPlayer = this.mc.getPlayer();
        if (world == null || clientPlayer == null) {
            return;
        }
        for (PlayerEntity player : world.getPlayers()) {
            if (player == clientPlayer || !player.isAlive()) {
                continue;
            }
            ItemStack activeStack = player.getActiveItem();
            if (activeStack.getItem() instanceof PotionItem) {
                this.drinkingPotions.put(Integer.valueOf(player.getId()), activeStack.copy());
            }
        }
        Iterator iterator = this.drinkingPotions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            int iId = ((Integer) entry.getKey()).intValue();
            ItemStack stack = (ItemStack) entry.getValue();
            Entity entity = world.getEntityById(iId);
            boolean blStillDrinking;
            if (entity instanceof PlayerEntity player) {
                blStillDrinking = player.isAlive() && player.getActiveItem().getItem() instanceof PotionItem;
            } else {
                blStillDrinking = false;
            }
            if (blStillDrinking) {
                continue;
            }
            iterator.remove();
            PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (contents == null) {
                continue;
            }
            PlayerEntity drinker = null;
            if (entity instanceof PlayerEntity) {
                drinker = (PlayerEntity) entity;
            }
            if (drinker == null || FriendManager.isFriend(drinker.getName().getString()) || drinker.equals(this.mc.getPlayer())) {
                continue;
            }
            LivingEntity lastTarget = ((AttackAuraModule) Expensive.INSTANCE.moduleRepository().get(AttackAuraModule.class)).lastTarget();
            if (!this.targetSetting.isSelected(TargetPearlMode.ALL) && (lastTarget == null || !lastTarget.equals(drinker))) {
                continue;
            }
            if (!this.eventsSetting.isSelected(ItemTrackerEventType.POTION_USE)) {
                continue;
            }
            Text textMessage = buildPotionMessage(drinker, contents);
            long now = System.currentTimeMillis();
            if (this.lastPotionNotifyId == drinker.getId() && now - this.lastPotionNotifyTime < 1000L) {
                continue;
            }
            this.lastPotionNotifyId = drinker.getId();
            this.lastPotionNotifyTime = now;
            if (this.outputSetting.isSelected(ItemTrackerOutput.NOTIFICATIONS)) {
                Expensive.INSTANCE.notificationRepository().post(textMessage, stack.copy(), 3L, TimeUnit.SECONDS);
            }
            if (this.outputSetting.isSelected(ItemTrackerOutput.CHAT)) {
                ChatUtil.addChatMessage(textMessage);
            }
        }
    }

    public Text buildPotionMessage(PlayerEntity player, PotionContentsComponent contents) {
        net.minecraft.text.MutableText textAppend = Text.empty().append(player.getName().copy().formatted(Formatting.GRAY)).append(Text.literal(" выпил ").formatted(Formatting.WHITE));
        Iterator iterator = contents.getEffects().iterator();
        boolean blFirst = true;
        while (iterator.hasNext()) {
            StatusEffectInstance effect = (StatusEffectInstance) iterator.next();
            if (!blFirst) {
                textAppend.append(Text.literal(", ").formatted(Formatting.GRAY));
            }
            blFirst = false;
            String strName = effect.getEffectType().value().getName().getString();
            int iAmplifier = effect.getAmplifier();
            String strLevel = "";
            if (iAmplifier > 0 && iAmplifier < POTION_ROMAN.length) {
                strLevel = " " + POTION_ROMAN[iAmplifier];
            }
            textAppend.append(Text.literal(strName + strLevel).formatted(Formatting.RED));
        }
        return textAppend;
    }

    static final String[] POTION_ROMAN = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
}
