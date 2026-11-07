package aethereal.module;
import aethereal.Expensive;
import aethereal.Lang;
import aethereal.annotation.Aliases;
import aethereal.event.PlayerTickEvent;
import aethereal.event.Render2DEvent;
import aethereal.math.Rotation;
import aethereal.math.Stopwatch;
import aethereal.model.Translation;
import aethereal.model.ScopedSlot;
import aethereal.model.SlotSearchResult2;
import aethereal.render.ColorStack;
import aethereal.render.Fonts;
import aethereal.render.ScreenResolution;
import aethereal.render.ThemePalette;
import aethereal.type.AutoPotionMode;
import aethereal.type.AutoPotionType;
import aethereal.type.InventoryScope;
import aethereal.type.Mc;
import aethereal.type.SettingUnit;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.ui.setting.NumberSetting;
import aethereal.util.AuraCooldownGuard;
import aethereal.util.DrawEngine;
import aethereal.util.GrimDelayHandler;
import aethereal.util.PvPModeDetector;
import aethereal.util.PlayerActionUtil;
import aethereal.util.RotationManager;
import aethereal.util.SilentSlotManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

@Aliases(aliases = {"Auto Potion", "AutoBuff", "Auto Buff", "Auto Splash", "Splash Helper", "Buff", "Авто Зелья"})
public class AutoPotionModule extends Module {
    private static final float THROW_PITCH = 90.0f;
    private static final long AIM_TIMEOUT_MS = 250L;

    public final Mc mc = Mc.INSTANCE;
    public final MultiSelectSetting<AutoPotionType> potions;
    public final ModeSetting<AutoPotionMode> mode;
    public final NumberSetting reThrow;
    public final NumberSetting health;
    public final NumberSetting delay;
    public final BooleanSetting onlyPvp;
    public final BooleanSetting onlyOnGround;
    public final BooleanSetting velocityAim;
    public final BooleanSetting upgradeOnly;
    public final BooleanSetting autoDisable;
    public final BooleanSetting showCounter;
    // Не брать зелье в руку: серверу уходит только UpdateSelectedSlot, зелье физически
    // не переставляется в хотбар и не занимает руку. Работает для зелий в хотбаре.
    public final BooleanSetting silentSwap;
    // Кидать все нужные зелья за один тик, а не по одному через delay.
    public final BooleanSetting batchThrow;

    public final Stopwatch throwTimer = new Stopwatch(false);
    public final Stopwatch aimTimer = new Stopwatch(false);
    public final Map<AutoPotionType, Integer> counts = new LinkedHashMap();
    public final Map<AutoPotionType, ItemStack> icons = new LinkedHashMap();

    // Бросок в два этапа: в PlayerTickEvent.PRE выставляем взгляд вниз (pitch 90),
    // в этом же тике sendMovementPackets отправляет его серверу, а в POST - бросаем.
    private Rotation pendingThrowRotation;
    private Rotation savedRotation;
    private List<PotionUse> pendingPotionUses;

    public AutoPotionModule() {
        super(ModuleTab.PLAYER, "Auto Potion");
        this.potions = new MultiSelectSetting(Lang.AUTOPOTION_POTIONS, Lang.AUTOPOTION_POTIONS_DESC).values(AutoPotionType.class).select(AutoPotionType.FIRE_RESISTANCE, AutoPotionType.STRENGTH, AutoPotionType.SPEED);
        this.mode = new ModeSetting(Lang.AUTOPOTION_MODE, Lang.AUTOPOTION_MODE_DESC).values(AutoPotionMode.class).select(AutoPotionMode.PACKET);
        this.reThrow = new NumberSetting(Lang.AUTOPOTION_PROTECTION4, Lang.AUTOPOTION_PROTECTION4_DESC).currentValue(4.0f).range(0.0f, 30.0f).step(1.0f).unit(SettingUnit.SECONDS);
        this.health = new NumberSetting(Lang.AUTOPOTION_HEALTH, Lang.AUTOPOTION_HEALTH_DESC).currentValue(8.0f).range(1.0f, 20.0f).step(0.5f).unit(SettingUnit.HITPOINTS).visible(() -> this.potions.isSelected(AutoPotionType.INSTANT_HEALTH));
        this.delay = new NumberSetting(Lang.AUTOPOTION_DELAY, Lang.AUTOPOTION_DELAY_DESC).currentValue(450.0f).range(50.0f, 2000.0f).step(25.0f).unit(SettingUnit.MILLISECONDS);
        this.onlyPvp = new BooleanSetting(Lang.AUTOPOTION_ONLY_PVP, Lang.AUTOPOTION_ONLY_PVP_DESC).setValue(true);
        this.onlyOnGround = new BooleanSetting(Lang.AUTOPOTION_ONLY_ON_GROUND, Lang.AUTOPOTION_ONLY_ON_GROUND_DESC).setValue(true);
        this.velocityAim = new BooleanSetting(Lang.AUTOPOTION_VELOCITY_AIM, Lang.AUTOPOTION_VELOCITY_AIM_DESC).setValue(false);
        this.upgradeOnly = new BooleanSetting(Lang.AUTOPOTION_ONLY_UPGRADE, Lang.AUTOPOTION_ONLY_UPGRADE_DESC).setValue(true);
        this.autoDisable = new BooleanSetting(Lang.AUTOPOTION_AUTO_DISABLE, Lang.AUTOPOTION_AUTO_DISABLE_DESC).setValue(false);
        this.showCounter = new BooleanSetting(Lang.AUTOPOTION_COUNTER, Lang.AUTOPOTION_COUNTER_DESC).setValue(true);
        this.silentSwap = new BooleanSetting(Translation.clearText("Silent"), Translation.clearText("Не брать зелье в руку: серверу уходит только UpdateSelectedSlot, предмет физически не переставляется")).setValue(true);
        this.batchThrow = new BooleanSetting(Translation.clearText("Batch"), Translation.clearText("Кидать все нужные зелья за один тик, а не по одному через задержку")).setValue(true);
        addSettings(this.potions, this.mode, this.reThrow, this.health, this.delay, this.onlyPvp, this.onlyOnGround, this.velocityAim, this.upgradeOnly, this.autoDisable, this.silentSwap, this.batchThrow, this.showCounter);
        register(PlayerTickEvent.class, class130Var -> {
            if (!this.mc.isWorldLoaded()) {
                return;
            }
            if (class130Var.isPre()) {
                if (isState()) {
                    tick();
                }
            } else if (class130Var.isPost()) {
                if (isState()) {
                    finishThrow();
                } else {
                    abortThrow();
                }
            }
        });
        register(Render2DEvent.class, class311Var -> {
            if (isState() && class311Var.isPost() && this.mc.isWorldLoaded() && this.showCounter.isValue() && !this.icons.isEmpty()) {
                renderCounter(class311Var);
            }
        });
    }

    public void tick() {
        ClientPlayerEntity player = this.mc.getPlayer();
        if (player == null || this.mc.getCurrentScreen() != null) {
            abortThrow();
            return;
        }
        refreshCounter(player);
        if (!player.isAlive() || player.isUsingItem()) {
            abortThrow();
            return;
        }
        if (this.onlyPvp.isValue() && !PvPModeDetector.isPvPMode()) {
            abortThrow();
            return;
        }
        if (this.onlyOnGround.isValue() && !player.isOnGround()) {
            abortThrow();
            return;
        }
        // Уже целимся вниз: на этом тике sendMovementPackets отправит серверу pitch 90,
        // а сам бросок произойдёт в PlayerTickEvent.POST - после того, как сервер
        // увидел поворот. Бросок одним interact-пакетом с pitch 90 на 1.16.5
        // (ViaFabricPlus) не работает: там у пакета использования нет углов.
        if (this.pendingPotionUses != null) {
            applyThrowRotation();
            return;
        }
        if (this.throwTimer.getElapsedTime(TimeUnit.MILLISECONDS) < this.delay.currentValue()) {
            return;
        }
        if (!GrimDelayHandler.script.isFinished()) {
            return;
        }
        List<PotionUse> list;
        if (this.batchThrow.isValue()) {
            list = findPotionUses(player);
        } else {
            Optional<PotionUse> optional = findPotionUse(player);
            list = optional.isEmpty() ? List.of() : List.of(optional.get());
        }
        if (list.isEmpty()) {
            return;
        }
        // Запоминаем ротацию ауры/камеры, чтобы вернуть её сразу после броска.
        // Пока целимся, аура успевает отработать свой тик: этот код выполняется
        // уже после RotationUpdateEvent (там аура бьёт), поэтому её рейтрейс
        // не видит pitch 90 и удары не срываются.
        this.savedRotation = RotationManager.INSTANCE.currentRotationOriginal;
        this.pendingThrowRotation = computeThrowRotation(player);
        this.pendingPotionUses = list;
        this.aimTimer.reset();
        applyThrowRotation();
    }

    /**
     * Вторая половина броска. PlayerTickEvent.POST приходит уже после
     * sendMovementPackets, поэтому сервер получил pitch 90. Если ротация ещё не
     * дошла (лаг, отменённые пакеты движения), ждём следующий тик; по таймауту
     * бросаем принудительно, как раньше, - чтобы модуль не завис в прицеливании.
     */
    public void finishThrow() {
        if (this.pendingPotionUses == null) {
            return;
        }
        ClientPlayerEntity player = this.mc.getPlayer();
        Rotation target = this.pendingThrowRotation;
        if (player == null || target == null) {
            abortThrow();
            return;
        }
        boolean aligned = RotationManager.INSTANCE.getServerRotation().angleTo(target) <= 0.05f;
        if (!aligned && this.aimTimer.getElapsedTime(TimeUnit.MILLISECONDS) < AIM_TIMEOUT_MS) {
            return;
        }
        List<PotionUse> list;
        if (this.batchThrow.isValue()) {
            list = findPotionUses(player);
        } else {
            Optional<PotionUse> optional = findPotionUse(player);
            list = optional.isEmpty() ? List.of() : List.of(optional.get());
        }
        // Пока целились, инвентарь/эффекты могли измениться - перепроверяем слоты,
        // чтобы interact не применился к уже не тому предмету.
        if (list.isEmpty()) {
            rotateBack();
            clearPendingThrow();
            return;
        }
        AuraCooldownGuard.INSTANCE.run(() -> {
            for (PotionUse potionUse : list) {
                throwPotion(potionUse, target);
            }
        });
        rotateBack();
        this.throwTimer.reset();
        clearPendingThrow();
        if (this.autoDisable.isValue() && !needsAnyPotion(player)) {
            setState(false);
        }
    }

    private void applyThrowRotation() {
        if (this.pendingThrowRotation != null) {
            RotationManager.INSTANCE.currentRotationOriginal = this.pendingThrowRotation;
        }
    }

    private void rotateBack() {
        RotationManager.INSTANCE.currentRotationOriginal = this.savedRotation;
        Rotation target = this.savedRotation != null ? this.savedRotation : Rotation.playerRotation();
        restoreServerRotation(target);
    }

    private void abortThrow() {
        if (this.pendingPotionUses == null) {
            return;
        }
        rotateBack();
        clearPendingThrow();
    }

    private void clearPendingThrow() {
        this.pendingPotionUses = null;
        this.pendingThrowRotation = null;
        this.savedRotation = null;
    }

    // Все зелья, которым сейчас надо быть выпитыми/брошенными. Раньше модуль брал только
    // первое подходящее и ждал следующего throwTimer, то есть в бою зелья вылетали
    // по одному через 450 мс - успевали сгореть все эффекты.
    public List<PotionUse> findPotionUses(ClientPlayerEntity player) {
        ArrayList<PotionUse> list = new ArrayList<>();
        for (AutoPotionType type : AutoPotionType.values()) {
            if (!this.potions.isSelected(type)) {
                continue;
            }
            Optional<SlotSearchResult2> optional = findBestSlot(player, type);
            if (optional.isEmpty()) {
                continue;
            }
            if (shouldUse(type, player, potionAmplifier(optional.get().stack(), type))) {
                list.add(new PotionUse(type, optional.get()));
            }
        }
        return list;
    }

    public Optional<PotionUse> findPotionUse(ClientPlayerEntity player) {
        for (AutoPotionType type : AutoPotionType.values()) {
            if (!this.potions.isSelected(type)) {
                continue;
            }
            Optional<SlotSearchResult2> optional = findBestSlot(player, type);
            if (optional.isEmpty()) {
                continue;
            }
            int iAmplifier = potionAmplifier(optional.get().stack(), type);
            if (shouldUse(type, player, iAmplifier)) {
                return Optional.of(new PotionUse(type, optional.get()));
            }
        }
        return Optional.empty();
    }

    public boolean shouldUse(AutoPotionType type, ClientPlayerEntity player, int i) {
        if (type == AutoPotionType.INSTANT_HEALTH) {
            return (player.getHealth() + player.getAbsorptionAmount()) <= this.health.currentValue();
        }
        StatusEffectInstance effect = player.getStatusEffect(type.effect);
        if (effect == null) {
            return true;
        }
        if (effect.isInfinite()) {
            return false;
        }
        int iRemaining = effect.getDuration();
        int iThreshold = (int) (this.reThrow.currentValue() * 20.0f);
        if (this.upgradeOnly.isValue()) {
            return iRemaining <= 0 || i > effect.getAmplifier();
        }
        return iRemaining <= iThreshold;
    }

    public Optional<SlotSearchResult2> findBestSlot(ClientPlayerEntity player, AutoPotionType type) {
        boolean zPacket = this.mode.isSelected(AutoPotionMode.PACKET);
        // Тихий свап возможен только для хотбара (слот < 9): зелье из основного инвентаря
        // нельзя сделать "выбранным", там нужен настоящий SWAP-клик.
        if (this.silentSwap.isValue()) {
            Optional<SlotSearchResult2> zHotbar = findPotions(player, type, InventoryScope.HOTBAR).stream().filter(class329Var -> {
                return !player.getItemCooldownManager().isCoolingDown(class329Var.stack());
            }).max(Comparator.comparingInt(class329Var2 -> potionAmplifier(class329Var2.stack(), type)));
            if (zHotbar.isPresent()) {
                return zHotbar;
            }
        }
        InventoryScope[] scopes = zPacket ? new InventoryScope[]{InventoryScope.HOTBAR, InventoryScope.INVENTORY} : new InventoryScope[]{InventoryScope.HOTBAR};
        return findPotions(player, type, scopes).stream().filter(class329Var -> {
            return !player.getItemCooldownManager().isCoolingDown(class329Var.stack());
        }).max(Comparator.<SlotSearchResult2>comparingInt(class329Var2 -> {
            return potionAmplifier(class329Var2.stack(), type);
        }).thenComparingInt(class329Var3 -> {
            return potionDuration(class329Var3.stack(), type);
        }).thenComparingInt(class329Var4 -> {
            return class329Var4.slotReference().slot() < 9 ? 1 : 0;
        }));
    }

    public List<SlotSearchResult2> findPotions(ClientPlayerEntity player, AutoPotionType type, InventoryScope... class305VarArr) {
        return Expensive.INSTANCE.inventoryService().searcher().findAllItems(class329Var -> {
            return isPotionOfType(class329Var, type);
        }, class305VarArr);
    }

    public boolean isPotionOfType(ItemStack itemStack, AutoPotionType type) {
        return getPotionEffect(itemStack, type).isPresent();
    }

    public Optional<StatusEffectInstance> getPotionEffect(ItemStack itemStack, AutoPotionType type) {
        if (itemStack.getItem() != Items.SPLASH_POTION) {
            return Optional.empty();
        }
        PotionContentsComponent component = itemStack.get(DataComponentTypes.POTION_CONTENTS);
        if (component == null) {
            return Optional.empty();
        }
        for (StatusEffectInstance effect : component.getEffects()) {
            if (matching(type, effect)) {
                return Optional.of(effect);
            }
        }
        return Optional.empty();
    }

    public boolean matching(AutoPotionType type, StatusEffectInstance effect) {
        return effect.getEffectType().value() == type.effect.value();
    }

    public int potionAmplifier(ItemStack itemStack, AutoPotionType type) {
        return getPotionEffect(itemStack, type).map(StatusEffectInstance::getAmplifier).orElse(0);
    }

    public int potionDuration(ItemStack itemStack, AutoPotionType type) {
        return getPotionEffect(itemStack, type).map(StatusEffectInstance::getDuration).orElse(0);
    }

    public Rotation computeThrowRotation(ClientPlayerEntity player) {
        // Бросок строго под себя: pitch 90, yaw оставляем текущим (аура/камера).
        // Именно этот угол уйдёт серверу обычным movement-пакетом в этом же тике,
        // поэтому работает и на 1.16.5 через ViaFabricPlus, где у пакета
        // использования предмета нет собственных yaw/pitch.
        return new Rotation(RotationManager.INSTANCE.getCurrentRotation().getYaw(), THROW_PITCH);
    }

    // Возвращаем серверу ту ротацию, которая должна быть у него после броска.
    //
    // PlayerInteractItemC2SPacket переписывает серверные углы на углы броска:
    // ServerPlayNetworkHandler.onPlayerInteractItem вызывает player.setAngles(yaw, pitch)
    // из пакета. Без отката сервер остался бы смотреть в землю (pitch 90) до следующего
    // пакета движения с изменённым взглядом - а если игрок камеру не двигал, такой пакет
    // не уходит вообще, и удар киллауры в этом тике проверялся бы лучом в пол ("аура не
    // бьёт, пока AutoPotion бафает"). Цель - getCurrentRotation() до броска: ровно то,
    // что следующий пакет движения отправил бы серверу.
    public void restoreServerRotation(Rotation targetRotation) {
        ClientPlayerEntity player = this.mc.getPlayer();
        if (player == null || targetRotation == null) {
            return;
        }
        if (RotationManager.INSTANCE.getServerRotation().angleTo(targetRotation) < 0.5f) {
            return;
        }
        player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(player.getX(), player.getY(), player.getZ(),
                targetRotation.getYaw(), targetRotation.getPitch(), player.isOnGround(), player.horizontalCollision));
    }

    public void throwPotion(PotionUse potionUse, Rotation rotation) {
        ClientPlayerEntity player = this.mc.getPlayer();
        SlotSearchResult2 class329Var = potionUse.slot;
        ScopedSlot potionSlot = class329Var.slotReference();
        int iIncremented = potionSlot.increasedSlot();
        int iSelected = player.getInventory().selectedSlot;
        boolean zPacket = this.mode.isSelected(AutoPotionMode.PACKET);
        // Обход спринта ровно как в Auto Totem: спринт локально замирает на тик
        // броска, пакет смены состояния бега в этом тике не уходит, на следующем
        // тике бег возвращается сам. Без этого сервер иногда флагует связку
        // "бег + смена слота/бросок" (UpdateSelectedSlot и SWAP-клики считаются
        // инвентарным действием на бегу). Ставим ДО первого пакета, иначе тик
        // успеет проскочить.
        AutoTotemModule.pauseSprintForClick(player);

        // Тихий бросок: зелье остаётся в хотбаре и не занимает руку. Обычный путь делал
        // два ClickSlot(SWAP) - они переставляют предмет в руку и возвращают обратно, а
        // потом updateSlots() слал поддельный клик с revision=0 и CloseHandledScreen.
        // Второй клик в том же тике (тотем, слом щита ауры) после закрытия контейнера
        // сервер откатывал - отсюда и "тотемы не берутся". Сейчас updateSlots() из
        // пути броска вообще убран, клик идёт один и с настоящим revision.
        if (this.silentSwap.isValue() && potionSlot.slot() < 9) {
            SilentSlotManager swapper = Expensive.INSTANCE.inventoryService().hotbarSlotSwapper();
            swapper.swapTo(this, potionSlot, 1);
            // interactItem -> syncSelectedSlot() сам отправит UpdateSelectedSlot(слот зелья)
            interactItemWithThrowRotation(rotation);
            swapper.pendingSwap = null;
            player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(iSelected));
            return;
        }

        // Зелье в основном инвентаре: нужен настоящий SWAP-клик. Раньше здесь стоял
        // SwapUtil.swapAction -> GrimDelayHandler.addTask, и вот он и ломал киллауру:
        // rotateToAngle() делал RotationManager.clear() (стирал задачу наведения ауры)
        // и планировал ротацию броска на 3 тика, а disableMoveKeys() ещё и гасил
        // ввод/спринт. Аура в эти 1-2 тика брала луч по "взгляду в землю" - zRayTrace
        // false, поэтому ударов не было вообще ("аура не бьёт, пока AutoPotion
        // бафает"). Теперь бросок строго вниз, а угол броска interactItem учитывает сам
        // через временную подмену RotationManager - поэтому клики делаем сразу, без
        // скрипта GRIM - как в тихом пути выше.
        player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(iSelected));
        PlayerActionUtil.INSTANCE.clickSlot(player.currentScreenHandler.syncId, iIncremented, iSelected, SlotActionType.SWAP, zPacket);
        interactItemWithThrowRotation(rotation);
        PlayerActionUtil.INSTANCE.clickSlot(player.currentScreenHandler.syncId, iIncremented, iSelected, SlotActionType.SWAP, zPacket);
    }

    // PlayerInteractItemC2SPacketMixin переписывает yaw/pitch интеракт-пакета на
    // RotationManager.getCurrentRotation() - углы, переданные в interactItem, серверу
    // не уходят. Поэтому на время построения пакета подменяем currentRotationOriginal
    // на угол броска (pitch 90), чтобы ServerPlayNetworkHandler.onPlayerInteractItem
    // сделал player.setAngles(наш pitch) и зелье вылетело строго в пол.
    //
    // previousRotation не трогаем (не через setRotation) - иначе поехала бы интерполяция
    // рендера. Значение возвращаем сразу после interactItem, до следующих пакетов, чтобы
    // аура/камера не мигнули на один тик.
    private void interactItemWithThrowRotation(Rotation rotation) {
        Rotation previous = RotationManager.INSTANCE.currentRotationOriginal;
        RotationManager.INSTANCE.currentRotationOriginal = rotation;
        try {
            PlayerActionUtil.INSTANCE.interactItem(Hand.MAIN_HAND, rotation, false);
        } finally {
            RotationManager.INSTANCE.currentRotationOriginal = previous;
        }
    }

    public boolean needsAnyPotion(ClientPlayerEntity player) {
        return findPotionUse(player).isPresent();
    }

    public void refreshCounter(ClientPlayerEntity player) {
        this.icons.clear();
        this.counts.clear();
        if (!this.showCounter.isValue()) {
            return;
        }
        for (AutoPotionType type : AutoPotionType.values()) {
            if (!this.potions.isSelected(type)) {
                continue;
            }
            List<SlotSearchResult2> list = findPotions(player, type, InventoryScope.HOTBAR, InventoryScope.INVENTORY);
            if (list.isEmpty()) {
                continue;
            }
            int iCount = 0;
            for (SlotSearchResult2 class329Var : list) {
                iCount += class329Var.stack().getCount();
            }
            this.counts.put(type, Integer.valueOf(iCount));
            this.icons.put(type, list.get(0).stack());
        }
    }

    public void renderCounter(Render2DEvent class311Var) {
        ClientPlayerEntity player = this.mc.getPlayer();
        if (player == null) {
            return;
        }
        DrawEngine drawEngine = Expensive.INSTANCE.drawEngine();
        ColorStack colorStack = drawEngine.colorStack();
        MatrixStack matrixStack = class311Var.matrixStack();
        ThemePalette palette = Expensive.INSTANCE.theme().palette();
        float fCenterX = ScreenResolution.resolution().screenWidth() / 2.0f;
        float fCenterY = ScreenResolution.resolution().screenHeight() / 2.0f;
        float fX = fCenterX + 42.0f;
        float fY = fCenterY - (this.icons.size() * 18.0f) / 2.0f;
        drawEngine.begin();
        for (Map.Entry<AutoPotionType, ItemStack> entry : this.icons.entrySet()) {
            drawEngine.itemStack(matrixStack.peek().getPositionMatrix(), entry.getValue(), fX, fY, 0.5f, 1.0f);
            int iCount = this.counts.getOrDefault(entry.getKey(), Integer.valueOf(0)).intValue();
            float fTextY = fY + (16.0f - Fonts.INTER_SEMIBOLD.get().getHeight(12)) / 2.0f;
            drawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), Fonts.INTER_SEMIBOLD.get(), "x" + iCount, fX + 18.0f, fTextY, 12, 0.0f, colorStack.computeColor(palette.text().tone(400).argb()));
            fY += 18.0f;
        }
        drawEngine.end();
    }

    @Override
    public void deactivate() {
        abortThrow();
        this.icons.clear();
        this.counts.clear();
        this.throwTimer.reset();
        super.deactivate();
    }

    public record PotionUse(AutoPotionType type, SlotSearchResult2 slot) {
    }
}
