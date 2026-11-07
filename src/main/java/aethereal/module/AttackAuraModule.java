package aethereal.module;
import aethereal.util.ActionScheduler;
import aethereal.util.AuraCooldownGuard;
import aethereal.annotation.Aliases;
import aethereal.type.AuraRotationMode;
import aethereal.type.AuraTargetType;
import aethereal.util.BlockUtil;
import aethereal.ui.setting.BooleanSetting;
import aethereal.type.CombatPauseManager;
import aethereal.math.DirectionalInput;
import aethereal.type.EntityCategory;
import aethereal.model.EntityFilter;
import aethereal.internal.EventPhaseSwitchMap;
import aethereal.type.EventPriority;
import aethereal.Expensive;
import aethereal.type.FuntimeRotationMode;
import aethereal.type.GrimRotationMode;
import aethereal.util.FriendManager;
import aethereal.util.HitPointResolver;
import aethereal.type.InventoryScope;
import aethereal.Lang;
import aethereal.util.MathUtil;
import aethereal.type.Mc;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.ModuleTab;
import aethereal.type.MoveCorrectionMode;
import aethereal.event.MovementInputEvent;
import aethereal.util.MovementInputHelper;
import aethereal.event.MovementUpdateEvent;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.type.NotificationType;
import aethereal.ui.setting.NumberSetting;
import aethereal.event.PacketReceiveEvent;
import aethereal.event.PacketSendEvent;
import aethereal.util.PlayerSnapshotManager;
import aethereal.event.PlayerTickEvent;
import aethereal.util.RaycastUtil;
import aethereal.math.Rotation;
import aethereal.config.RotationConfig;
import aethereal.util.RotationManager;
import aethereal.math.RotationMath;
import aethereal.type.RotationMode;
import aethereal.event.RotationUpdateEvent;
import aethereal.math.RotationVector;
import aethereal.util.ServerUtil;
import aethereal.type.SettingUnit;
import aethereal.math.SimulatedPlayer;
import aethereal.model.ScopedSlot;
import aethereal.model.SlotSearchResult2;
import aethereal.util.PlayerActionUtil;
import aethereal.util.SlotSyncHandler;
import aethereal.type.SlothAcRotationMode;
import aethereal.type.SmoothRotationMode;
import aethereal.type.HolyWorldRotationMode;
import aethereal.type.MatrixRotationMode;
import aethereal.type.BezierRotationMode;
import aethereal.math.Stopwatch;
import aethereal.type.TargetSortMode;
import aethereal.util.TargetSorters;
import aethereal.event.UseItemEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import net.minecraft.block.CobwebBlock;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

@Aliases(aliases = {"Attack Aura", "Kill Aura", "Hit Aura", "Aura"})
public class AttackAuraModule extends Module {
    public final ModeSetting<AuraRotationMode> rotationMode;
    public final MultiSelectSetting<AuraTargetType> targetTypes;
    public final MultiSelectSetting<TargetSortMode> sortModes;
    public final ModeSetting<MoveCorrectionMode> moveCorrectionMode;

    public final NumberSetting maxDistance;
    public final BooleanSetting onlyCriticalHit;
    public final BooleanSetting critsWithSpace;
    public final BooleanSetting ignoreWalls;
    public final BooleanSetting ignoreNaked;
    public final BooleanSetting noHitWhileEatingOffhand;
    public final BooleanSetting unpressShield;
    public final BooleanSetting breakShield;
    public final BooleanSetting tpsSync;
    public final BooleanSetting customDelay;
    public final NumberSetting attackDelayMin;
    public final NumberSetting attackDelayMax;
    public final NumberSetting cooldownThreshold;
    public final Mc mc;
    public final HitPointResolver hitPointResolver;

    public final Stopwatch attackTimer;

    public final Stopwatch lookingTimer;
    public int attackCooldown;
    public int sprintTicks;
    public int hitCounter;
    public LivingEntity target;

    public LivingEntity lastTarget;
    public final int[] funtimeAttackDelays;

    public final int[] smoothAttackDelays;
    public final int[] grimAttackDelays;

    public final ActionScheduler actionScheduler;

    public Vec3d lastAttackVector;

    public AttackAuraModule() {
        super(ModuleTab.COMBAT, "Attack Aura");
        this.rotationMode = new ModeSetting(Lang.ATTACKAURA_ROTATION_MODE).values(AuraRotationMode.class);
        this.targetTypes = new MultiSelectSetting(Lang.SELECTTARGETS).values(AuraTargetType.class).select(AuraTargetType.PLAYERS);
        this.sortModes = new MultiSelectSetting(Lang.ATTACKAURA_SORT_TYPE).values(TargetSortMode.class).select(TargetSortMode.BY_FOV);
        this.moveCorrectionMode = new ModeSetting(Lang.ATTACKAURA_MOVE_CORRECTION, Lang.ATTACKAURA_MOVE_CORRECTION_DESC).values(MoveCorrectionMode.class).select(MoveCorrectionMode.FREE);
        this.maxDistance = new NumberSetting(Lang.ATTACKAURA_MAX_DISTANCE).currentValue(3.0f).range(1.0f, 6.0f).step(0.05f).unit(SettingUnit.BLOCKS);
        this.onlyCriticalHit = new BooleanSetting(Lang.TRIGGERBOT_ONLY_CRITICAL_HIT);
        BooleanSetting class665Var = new BooleanSetting(Lang.ATTACKAURA_CRITS_WITH_SPACE, Lang.ATTACKAURA_CRITS_WITH_SPACE_DESC);
        BooleanSetting class665Var2 = this.onlyCriticalHit;
        Objects.requireNonNull(class665Var2);
        this.critsWithSpace = class665Var.visible(class665Var2::isValue);
        this.ignoreWalls = new BooleanSetting(Lang.ATTACKAURA_IGNORE_WALLS, Lang.ATTACKAURA_IGNORE_WALLS_DESC);
        this.ignoreNaked = new BooleanSetting(Lang.ATTACKAURA_IGNORE_NAKED);
        this.noHitWhileEatingOffhand = new BooleanSetting(Lang.ATTACKAURA_NO_HIT_WHILE_EATING_OFFHAND);
        this.unpressShield = new BooleanSetting(Lang.ATTACKAURA_UNPRESS_SHIELD);
        this.breakShield = new BooleanSetting(Lang.ATTACKAURA_BREAK_SHIELD).setValue(true);
        this.tpsSync = new BooleanSetting(Lang.ATTACKAURA_TPS_SYNC);
        this.customDelay = new BooleanSetting(Lang.ATTACKAURA_CUSTOM_DELAY, Lang.ATTACKAURA_CUSTOM_DELAY_DESC);
        this.attackDelayMin = new NumberSetting(Lang.ATTACKAURA_DELAY_MIN, Lang.ATTACKAURA_DELAY_MIN_DESC).currentValue(10.0f).range(1.0f, 20.0f).step(1.0f).visible(this.customDelay::isValue);
        this.attackDelayMax = new NumberSetting(Lang.ATTACKAURA_DELAY_MAX, Lang.ATTACKAURA_DELAY_MAX_DESC).currentValue(10.0f).range(1.0f, 20.0f).step(1.0f).visible(this.customDelay::isValue);
        this.cooldownThreshold = new NumberSetting(Lang.ATTACKAURA_COOLDOWN_THRESHOLD, Lang.ATTACKAURA_COOLDOWN_THRESHOLD_DESC).currentValue(0.9f).range(0.1f, 0.95f).step(0.05f);
        this.mc = Mc.INSTANCE;
        this.hitPointResolver = new HitPointResolver();
        this.attackTimer = new Stopwatch();
        this.lookingTimer = new Stopwatch();
        this.attackCooldown = 0;
        this.sprintTicks = 0;
        this.target = null;
        this.lastTarget = null;
        this.funtimeAttackDelays = new int[]{10, 10, 10, 10, 12};
        this.smoothAttackDelays = new int[]{10, 10, 10, 13};
        this.grimAttackDelays = new int[]{10, 11};
        this.actionScheduler = new ActionScheduler();
        this.lastAttackVector = Vec3d.ZERO;
        addSettings(this.rotationMode, this.targetTypes, this.sortModes, this.moveCorrectionMode, this.maxDistance, this.onlyCriticalHit, this.critsWithSpace, this.ignoreWalls, this.ignoreNaked, this.noHitWhileEatingOffhand, this.unpressShield, this.breakShield, this.tpsSync, this.customDelay, this.attackDelayMin, this.attackDelayMax, this.cooldownThreshold);
        register(UseItemEvent.class, class059Var -> {
            if (isState() && this.mc.isWorldLoaded() && this.target != null && canCrit(this.mc.getPlayer(), 0)) {
                class059Var.cancel();
            }
        });
        register(PacketSendEvent.class, class037Var -> {
            if (isState() && this.mc.isWorldLoaded() && this.target != null && !AuraCooldownGuard.INSTANCE.isSuppressed()) {
                if ((class037Var.getPacket() instanceof HandSwingC2SPacket) || (class037Var.getPacket() instanceof UpdateSelectedSlotC2SPacket)) {
                    this.attackCooldown = Math.max(0, Math.round(getAttackDelay() * (this.tpsSync.isValue() ? 20.0f / MathUtil.clamp(ServerUtil.TPS, 1.0f, 20.0f) : 1.0f)));
                }
            }
        });
        register(PacketReceiveEvent.class, class051Var -> {
            if (isState() && this.mc.isWorldLoaded() && this.target != null) {
                if ((class051Var.getPacket()) instanceof EntityStatusS2CPacket packet ) {
                    EntityStatusS2CPacket entityStatusS2CPacket = packet;
                    if (entityStatusS2CPacket.getStatus() == 30 && entityStatusS2CPacket.getEntity(this.mc.getWorld()) == this.target) {
                        Expensive.INSTANCE.notificationRepository().post(NotificationType.SUCCESS, (Text) Text.literal("Сломал щит игроку " + this.target.getName().getString()), 3L, TimeUnit.SECONDS);
                    }
                }
            }
        });
        register(PlayerTickEvent.class, class130Var -> {
            if (this.mc.isWorldLoaded() && class130Var.isPre()) {
                this.actionScheduler.cleanupIfFinished().update();
            }
        });
        register(MovementUpdateEvent.class, class308Var -> {
            if (isState() && this.mc.isWorldLoaded() && class308Var.getDirectionalInput().isMoving() && this.sprintTicks > 0 && class308Var.isSprint()) {
                class308Var.setStopProgression(true);
                class308Var.setSprint(false);
            }
        }, EventPriority.HIGH);
        register(RotationUpdateEvent.class, class345Var -> {
            if (this.mc.isWorldLoaded()) {
                ClientPlayerInteractionManager interactionManager = Mc.INSTANCE.getInteractionManager();
                ClientPlayerEntity player = this.mc.getPlayer();
                RotationManager rotationManager = RotationManager.INSTANCE;
                switch (EventPhaseSwitchMap.phaseOrdinals[class345Var.stage().ordinal()]) {
                    case 1:
                        updateRotation(player, rotationManager);
                        break;
                    case 2:
                        performAttack(player, interactionManager, rotationManager);
                        break;
                }
            }
        });
        register(MovementInputEvent.class, class040Var -> {
            ClientPlayerEntity player;
            if (isState() && this.mc.isWorldLoaded() && this.moveCorrectionMode.isSelected(MoveCorrectionMode.TARGET) && (player = this.mc.getPlayer()) != null && this.target != null) {
                class040Var.setInput(MovementInputHelper.getDirectionalInputForDegrees(DirectionalInput.NONE, MovementInputHelper.getDegreesRelativeToView(this.target.getPos().subtract(player.getPos()), player.getYaw())));
                if (player.isSwimming()) {
                    class040Var.setJumping(this.target.getEyeY() > player.getEyeY() - ((double) (player.isSwimming() ? 0 : 1)));
                }
            }
        }, EventPriority.HIGH);
    }

    public void updateRotation(ClientPlayerEntity clientPlayerEntity, RotationManager rotationManager) {
        int i;
        int i2;
        if (isState()) {
            this.target = findTarget(clientPlayerEntity, true);
            if (this.target == null) {
                return;
            }
            float fCurrentValue = this.maxDistance.currentValue();
            if (clientPlayerEntity.isCreative()) {
                fCurrentValue = (float) Math.max(fCurrentValue, clientPlayerEntity.getEntityInteractionRange());
            }
            Vec3d vec3dSubtract = ((Vec3d) this.hitPointResolver.computeVector(this.target, fCurrentValue, rotationManager.getCurrentRotation(), getRotationMode().randomValue(), false).getLeft()).subtract(clientPlayerEntity.getEyePos());
            if (vec3dSubtract == null) {
                return;
            }
            Rotation targetRotation = RotationMath.INSTANCE.fromVec3d(vec3dSubtract);
            if (this.rotationMode.isSelected(AuraRotationMode.GRIM)) {
                i = 2;
            } else {
                i = this.rotationMode.isSelected(AuraRotationMode.FUNTIME) ? 20 : 5;
            }
            int i3 = i;
            if (this.rotationMode.isSelected(AuraRotationMode.GRIM)) {
                i2 = 4;
            } else {
                i2 = this.rotationMode.isSelected(AuraRotationMode.FUNTIME) ? 5 : 0;
            }
            int i4 = i2;
            if (i4 > 0 && !canAttack(clientPlayerEntity, i4)) {
                this.lastTarget = this.target;
            } else {
                rotationManager.scheduleRotation(new RotationVector(targetRotation, vec3dSubtract), this.target, new RotationConfig(getRotationMode(), false, !this.moveCorrectionMode.isSelected(MoveCorrectionMode.NONE), this.moveCorrectionMode.isSelected(MoveCorrectionMode.FOCUS)), 1, this, i3);
                this.lastTarget = this.target;
            }
        }
    }

    public void performAttack(ClientPlayerEntity clientPlayerEntity, ClientPlayerInteractionManager clientPlayerInteractionManager, RotationManager rotationManager) {
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
        if (this.sprintTicks > 0) {
            this.sprintTicks--;
        }
        if (!isState() || this.target == null) {
            return;
        }
        float fCurrentValue = this.maxDistance.currentValue();
        if (clientPlayerEntity.isCreative()) {
            fCurrentValue = (float) Math.max(fCurrentValue, clientPlayerEntity.getEntityInteractionRange());
        }
        boolean zRayTrace = RaycastUtil.rayTrace(this.hitPointResolver, this.target, clientPlayerEntity.getEyePos(), RotationMath.INSTANCE.rotationToVector(rotationManager.getCurrentRotation()), fCurrentValue, this.target.getBoundingBox(), this.ignoreWalls.isValue());
        if (canAttack(clientPlayerEntity, 1)) {
            this.sprintTicks = 1;
            clientPlayerEntity.setSprinting(false);
            if (this.unpressShield.isValue() && clientPlayerEntity.isUsingItem() && clientPlayerEntity.getActiveItem().getItem() == Items.SHIELD && this.mc.getGameOptions().useKey.isPressed() && this.actionScheduler.isFinished()) {
                clientPlayerInteractionManager.stopUsingItem(clientPlayerEntity);
                this.mc.getGameOptions().useKey.setPressed(false);
                this.actionScheduler.cleanup().addTickStep(1, () -> {
                    this.mc.getGameOptions().useKey.setPressed(true);
                });
            }
        }
        if (zRayTrace && !SlotSyncHandler.lastSprinting && !CombatPauseManager.INSTANCE.shouldPauseCombat()) {
            // Обход слома щита: удар топором уходит ПЕРВЫМ и отдельным пакетом.
            // Сервер 1.16.5 (через ViaVersion) принимает только один удар в окне
            // кулдауна: при стандартной схеме "меч, потом топор" топор сервер
            // отбрасывает, и щит остаётся целым. После удара топором обычный
            // удар мечом уйдёт уже на следующем кулдауне.
            if (breakShieldWithAxe(clientPlayerEntity, clientPlayerInteractionManager)) {
                this.hitCounter++;
                this.attackTimer.reset();
                return;
            }
            if (canAttack(clientPlayerEntity, 0)) {
                clientPlayerInteractionManager.attackEntity(clientPlayerEntity, this.target);
                clientPlayerEntity.swingHand(Hand.MAIN_HAND);
                this.hitCounter++;
                this.attackTimer.reset();
            }
        }
    }

    public boolean breakShieldWithAxe(ClientPlayerEntity clientPlayerEntity, ClientPlayerInteractionManager clientPlayerInteractionManager) {
        if (!this.breakShield.isValue() || this.target == null || !this.target.isAlive()) {
            return false;
        }
        if (!this.target.isUsingItem() || !(this.target.getActiveItem().getItem() instanceof ShieldItem)) {
            return false;
        }
        if (!isActiveItemStackBlocking(this.target, 2) || !isBlockingTowards(this.target, clientPlayerEntity)) {
            return false;
        }
        if (!attackReady(clientPlayerEntity)) {
            return false;
        }
        Optional<SlotSearchResult2> optionalFindItem = Expensive.INSTANCE.inventoryService().searcher().findItem(itemStack -> {
            return itemStack.getItem() instanceof AxeItem;
        }, InventoryScope.HOTBAR, InventoryScope.INVENTORY);
        if (optionalFindItem.isEmpty() || !optionalFindItem.get().found()) {
            return false;
        }
        ScopedSlot axeSlot = optionalFindItem.get().slotReference();
        int selectedSlot = clientPlayerEntity.getInventory().selectedSlot;
        releaseShield(clientPlayerEntity, clientPlayerInteractionManager);

        if (axeSlot.scope() == InventoryScope.HOTBAR) {
            // Топор уже в хотбаре: меняем только "серверный" слот одним пакетом,
            // без ClickSlot - сервер не видит никаких манипуляций с инвентарём.
            int hotbarSlot = axeSlot.increasedSlot() - 36;
            if (hotbarSlot != selectedSlot) {
                clientPlayerEntity.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(hotbarSlot));
            }
            clientPlayerInteractionManager.attackEntity(clientPlayerEntity, this.target);
            clientPlayerEntity.swingHand(Hand.MAIN_HAND);
            if (hotbarSlot != selectedSlot) {
                clientPlayerEntity.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot));
            }
            return true;
        }

        // Топор в инвентаре: один клик SWAP в текущий слот хотбара, удар и возврат
        // на следующем тике (два клика в одном тике сервер может отбить).
        int slotId = axeSlot.increasedSlot();
        PlayerActionUtil.INSTANCE.swapHand(slotId, selectedSlot, false);
        clientPlayerInteractionManager.attackEntity(clientPlayerEntity, this.target);
        clientPlayerEntity.swingHand(Hand.MAIN_HAND);
        this.actionScheduler.addTickStep(1, () -> {
            PlayerActionUtil.INSTANCE.swapHand(slotId, selectedSlot, false);
        });
        return true;
    }

    /**
     * Готов ли удар по кулдауну. Крит здесь не проверяется: слом щита топором
     * в ванили от крита не зависит.
     */
    public boolean attackReady(ClientPlayerEntity clientPlayerEntity) {
        return clientPlayerEntity.getAttackCooldownProgress(0.5f) > this.cooldownThreshold.currentValue() && this.attackCooldown <= 0;
    }

    /**
     * Блокирует ли цель щитом в нашу сторону (такой удар ваниль засчитывает
     * как попадание в щит и отключает его топором).
     */
    public boolean isBlockingTowards(LivingEntity livingEntity, ClientPlayerEntity clientPlayerEntity) {
        if (!livingEntity.isBlocking() || !(livingEntity.getActiveItem().getItem() instanceof ShieldItem)) {
            return false;
        }
        double dDotProduct = MathUtil.clamp(livingEntity.getRotationVec(1.0f).normalize().dotProduct(clientPlayerEntity.getPos().subtract(livingEntity.getPos()).normalize()), -1.0d, 1.0d);
        return Math.toDegrees(Math.acos(dDotProduct)) <= 110.0d;
    }

    /**
     * Отпускает свой щит перед ударом (пакет RELEASE_USE_ITEM отправляет
     * interactionManager). С зажатым щитом сервер видит удар из блокировки
     * и может отклонить его.
     */
    public void releaseShield(ClientPlayerEntity clientPlayerEntity, ClientPlayerInteractionManager clientPlayerInteractionManager) {
        if (!this.unpressShield.isValue() || clientPlayerInteractionManager == null) {
            return;
        }
        if (!clientPlayerEntity.isUsingItem() || clientPlayerEntity.getActiveItem().getItem() != Items.SHIELD) {
            return;
        }
        clientPlayerInteractionManager.stopUsingItem(clientPlayerEntity);
        if (!this.mc.getGameOptions().useKey.isPressed()) {
            return;
        }
        this.mc.getGameOptions().useKey.setPressed(false);
        this.actionScheduler.addTickStep(1, () -> {
            this.mc.getGameOptions().useKey.setPressed(true);
        });
    }

    @Override
    public void deactivate() {
        this.target = null;
        super.deactivate();
    }

    public boolean isActiveItemStackBlocking(LivingEntity livingEntity, int i) {
        if (!livingEntity.isUsingItem() || livingEntity.activeItemStack.isEmpty()) {
            return false;
        }
        Item item = livingEntity.activeItemStack.getItem();
        return item.getUseAction(livingEntity.activeItemStack) == UseAction.BLOCK && item.getMaxUseTime(livingEntity.activeItemStack, livingEntity) - livingEntity.getItemUseTimeLeft() >= i;
    }

    public RotationMode getRotationMode() throws MatchException {
        switch (((AuraRotationMode) this.rotationMode.currentValue()).ordinal()) {
            case 0:
                return new FuntimeRotationMode();
            case 1:
                return new SmoothRotationMode();
            case 2:
                return new GrimRotationMode();
            case 3:
                return new SlothAcRotationMode();
            case 4:
                return new HolyWorldRotationMode();
            case 5:
                return new BezierRotationMode();
            case 6:
                return new MatrixRotationMode();
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
    }

    public LivingEntity findTarget(ClientPlayerEntity clientPlayerEntity, boolean z) {
        float fCurrentValue = this.maxDistance.currentValue();
        if (clientPlayerEntity.isCreative()) {
            fCurrentValue = (float) Math.max(fCurrentValue, clientPlayerEntity.getEntityInteractionRange());
        }
        Vec3d vec3dNormalize = RotationMath.INSTANCE.rotationToVector(Rotation.playerRotation()).normalize();
        if (z && isValidTarget(this.target, clientPlayerEntity, fCurrentValue)) {
            double dDotProduct = vec3dNormalize.dotProduct(this.target.getPos().subtract(clientPlayerEntity.getPos()).normalize());
            double dCos = Math.cos(Math.toRadians(180.0d));
            if (!this.hitPointResolver.hasValidPoint(this.target, fCurrentValue, this.ignoreWalls.isValue()) || dDotProduct <= dCos) {
                return null;
            }
            return this.target;
        }
        EntityFilter class095Var = new EntityFilter();
        if (this.targetTypes.isSelected(AuraTargetType.PLAYERS)) {
            class095Var.add(EntityCategory.PLAYER);
        }
        if (this.targetTypes.isSelected(AuraTargetType.FRIENDS)) {
            class095Var.add(EntityCategory.FRIEND);
        }
        if (this.targetTypes.isSelected(AuraTargetType.MOBS)) {
            class095Var.add(EntityCategory.MOB);
        }
        if (this.targetTypes.isSelected(AuraTargetType.ANIMALS)) {
            class095Var.add(EntityCategory.ANIMAL);
        }
        ArrayList arrayList = new ArrayList();
        for (Entity livingEntity : this.mc.getWorld().getEntities()) {
            if (livingEntity instanceof LivingEntity) {
                LivingEntity livingEntity2 = (LivingEntity) livingEntity;
                if (class095Var.matches(livingEntity2) && isValidTarget(livingEntity2, clientPlayerEntity, fCurrentValue) && vec3dNormalize.dotProduct(livingEntity2.getPos().subtract(clientPlayerEntity.getPos()).normalize()) > Math.cos(Math.toRadians(180.0d))) {
                    arrayList.add(livingEntity2);
                }
            }
        }
        List<LivingEntity> listSort = TargetSorters.of(this.sortModes.selectedValues()).sort(arrayList, clientPlayerEntity);
        this.target = listSort.isEmpty() ? null : (LivingEntity) listSort.getFirst();
        return this.target;
    }

    public boolean isValidTarget(LivingEntity livingEntity, ClientPlayerEntity clientPlayerEntity, float f) {
        if (livingEntity == null || livingEntity == clientPlayerEntity || !livingEntity.isAlive() || livingEntity.getHealth() <= 0.0f || AntiBotModule.isBot(livingEntity)) {
            return false;
        }
        // Друзей не трогаем никогда. EntityCategory.PLAYER матчит ВСЕХ игроков,
        // включая друзей, поэтому одного Select Targets недостаточно. Проверка
        // намеренно безусловная: даже если FRIENDS остался в сохранённом
        // конфиге, аура не должна бить своих.
        if (FriendManager.isFriend(livingEntity.getName().getString())) {
            return false;
        }
        if (this.ignoreNaked.isValue() && (livingEntity instanceof PlayerEntity) && StreamSupport.stream(((PlayerEntity) livingEntity).getArmorItems().spliterator(), false).allMatch((v0) -> {
            return v0.isEmpty();
        })) {
            return false;
        }
        return this.hitPointResolver.hasValidPoint(livingEntity, f, this.ignoreWalls.isValue());
    }

    public boolean canAttack(ClientPlayerEntity clientPlayerEntity, int i) {
        if (this.target == null || !this.target.isAlive() || (this.mc.getCurrentScreen() instanceof GenericContainerScreen)) {
            return false;
        }
        UseAction useAction = clientPlayerEntity.getActiveItem().getUseAction();
        boolean z = useAction == UseAction.EAT || useAction == UseAction.DRINK || useAction == UseAction.CROSSBOW || useAction == UseAction.SPEAR || useAction == UseAction.BOW;
        if (clientPlayerEntity.isUsingItem() && z) {
            if (clientPlayerEntity.getActiveHand() == Hand.MAIN_HAND) {
                return false;
            }
            if (clientPlayerEntity.getActiveHand() == Hand.OFF_HAND && this.noHitWhileEatingOffhand.isValue()) {
                return false;
            }
        }
        float fCurrentValue = this.maxDistance.currentValue();
        if (clientPlayerEntity.isCreative()) {
            fCurrentValue = (float) Math.max(fCurrentValue, clientPlayerEntity.getEntityInteractionRange());
        }
        if (this.hitPointResolver.hasValidPoint(this.target, fCurrentValue, this.ignoreWalls.isValue())) {
            return IntStream.rangeClosed(0, i).anyMatch(i2 -> {
                return canCrit(clientPlayerEntity, i2);
            });
        }
        return false;
    }

    public boolean canCrit(ClientPlayerEntity clientPlayerEntity, int i) {
        if (!(clientPlayerEntity.getAttackCooldownProgress(((float) i) + 0.5f) > this.cooldownThreshold.currentValue() && this.attackCooldown - i <= 0)) {
            return false;
        }
        if (!this.onlyCriticalHit.isValue()) {
            return true;
        }
        SimulatedPlayer class136VarSimulateLocalPlayer = SimulatedPlayer.simulateLocalPlayer(i);
        boolean z = class136VarSimulateLocalPlayer.input.playerInput.jump() || ((AutoJumpModule) Expensive.INSTANCE.moduleRepository().get(AutoJumpModule.class)).isState();
        if (this.critsWithSpace.isValue() && class136VarSimulateLocalPlayer.onGround && !z) {
            return true;
        }
        return shouldCrit(clientPlayerEntity, i);
    }

    public boolean shouldCrit(ClientPlayerEntity clientPlayerEntity, int i) {
        SimulatedPlayer class136VarSimulateLocalPlayer = SimulatedPlayer.simulateLocalPlayer(i);
        if (class136VarSimulateLocalPlayer.hasStatusEffect(StatusEffects.LEVITATION) || class136VarSimulateLocalPlayer.hasStatusEffect(StatusEffects.BLINDNESS) || class136VarSimulateLocalPlayer.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            return true;
        }
        if (BlockUtil.checkBlockIntersection(class136VarSimulateLocalPlayer.boundingBox, block -> {
            return block instanceof CobwebBlock;
        }) || class136VarSimulateLocalPlayer.isSubmergedInWater() || class136VarSimulateLocalPlayer.isInLava() || class136VarSimulateLocalPlayer.isClimbing() || this.mc.getPlayer().getAbilities().flying) {
            return true;
        }
        if (clientPlayerEntity.getMainHandStack().getItem() instanceof MaceItem) {
            return MaceItem.shouldDealAdditionalDamage(clientPlayerEntity);
        }
        boolean z = SimulatedPlayer.simulateLocalPlayer(i + 1).onGround;
        boolean z2 = SimulatedPlayer.simulateLocalPlayer(i + 2).onGround;
        boolean z3 = class136VarSimulateLocalPlayer.fallDistance > 1.0f && z;
        return (class136VarSimulateLocalPlayer.onGround || class136VarSimulateLocalPlayer.fallDistance <= 0.0f || ((class136VarSimulateLocalPlayer.velocity.y > (-0.3d) ? 1 : (class136VarSimulateLocalPlayer.velocity.y == (-0.3d) ? 0 : -1)) < 0 && z2 && (verticalCollisionSnapshot(4) || verticalCollisionSnapshot(5) || verticalCollisionSnapshot(6) || verticalCollisionSnapshot(7))) || ((class136VarSimulateLocalPlayer.fallDistance > ((float) class136VarSimulateLocalPlayer.velocity.y) ? 1 : (class136VarSimulateLocalPlayer.fallDistance == ((float) class136VarSimulateLocalPlayer.velocity.y) ? 0 : -1)) == 0 && z) || z3) ? false : true;
    }

    public boolean verticalCollisionSnapshot(int i) {
        LivingEntity player = Mc.INSTANCE.getPlayer();
        return ((Boolean) PlayerSnapshotManager.INSTANCE.getSnapshot(player, i).map(class373Var -> {
            return Boolean.valueOf(class373Var.verticalCollision);
        }).orElse(Boolean.valueOf(((ClientPlayerEntity) player).verticalCollision))).booleanValue();
    }

    int getAttackDelay() {
        if (this.customDelay.isValue()) {
            int min = Math.max(1, Math.round(Math.min(this.attackDelayMin.currentValue(), this.attackDelayMax.currentValue())));
            int max = Math.max(min, Math.round(Math.max(this.attackDelayMin.currentValue(), this.attackDelayMax.currentValue())));
            return min + ThreadLocalRandom.current().nextInt(max - min + 1);
        }
        int i = this.hitCounter;
        switch (((AuraRotationMode) this.rotationMode.currentValue()).ordinal()) {
            case 0:
                return this.funtimeAttackDelays[i % this.funtimeAttackDelays.length];
            case 1:
                return this.smoothAttackDelays[i % this.smoothAttackDelays.length];
            case 3:
                return this.funtimeAttackDelays[i % this.funtimeAttackDelays.length];
            case 4:
            case 5:
                return 10;
            default:
                return 10;
        }
    }

    public Stopwatch attackTimer() {
        return this.attackTimer;
    }

    public Stopwatch lookingTimer() {
        return this.lookingTimer;
    }

    public int attackCooldown() {
        return this.attackCooldown;
    }

    public int sprintTicks() {
        return this.sprintTicks;
    }

    public AttackAuraModule hitCounter(int i) {
        this.hitCounter = i;
        return this;
    }

    public int hitCounter() {
        return this.hitCounter;
    }

    public LivingEntity target() {
        return this.target;
    }

    public LivingEntity lastTarget() {
        return this.lastTarget;
    }
}
