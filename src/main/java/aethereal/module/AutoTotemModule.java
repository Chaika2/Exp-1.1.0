package aethereal.module;

import aethereal.annotation.Aliases;
import aethereal.type.AutoTotemMode;
import aethereal.type.AutoTotemTrigger;
import aethereal.ui.setting.BooleanSetting;
import aethereal.render.ColorStack;
import aethereal.type.CombatPauseManager;
import aethereal.util.DrawEngine;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.util.GrimDelayHandler;
import aethereal.type.InventoryScope;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.ui.setting.NumberSetting;
import aethereal.event.PlayerTickEvent;
import aethereal.event.Render2DEvent;
import aethereal.render.ScreenResolution;
import aethereal.type.SettingUnit;
import aethereal.model.SlotSearchResult2;
import aethereal.math.Stopwatch;
import aethereal.net.PacketSender;
import aethereal.util.SwapUtil;
import aethereal.render.ThemePalette;
import aethereal.model.Translation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.item.consume.UseAction;
import org.jetbrains.annotations.Nullable;

@Aliases(aliases = {"Auto Totem", "Totem Swap", "Totem", "Automatic Totem", "Auto Offhand Totem", "Totem Switch"})
public class AutoTotemModule extends Module {
    public final ModeSetting<AutoTotemMode> mode;
    public final NumberSetting healthThreshold;
    public final MultiSelectSetting<AutoTotemTrigger> triggers;
    public final NumberSetting crystalRadius;
    public final NumberSetting dynamiteRadius;
    public final NumberSetting obsidianRadius;
    public final NumberSetting anchorRadius;
    public final NumberSetting fallDistance;
    public final BooleanSetting saveEnchanted;
    public final BooleanSetting dontSwapSphere;
    public final BooleanSetting cancelEating;
    public final BooleanSetting showCounter;
    public final BooleanSetting countEnchanted;
    public final Mc mc;

    public ItemStack savedOffhand;
    public Item savedOffhandItem;
    public int totemCount;
    public int enchantedTotemCount;
    public final Stopwatch swapTimer;

    public int advancedRestoreSelected = -1;
    public long advancedRestoreAt = 0L;

    public AutoTotemModule() {
        super(ModuleTab.COMBAT, "Auto Totem");
        this.mode = new ModeSetting(Translation.clearText("Режим")).values(AutoTotemMode.class);
        this.healthThreshold = new NumberSetting(Lang.COMBAT_AUTOTOTEM_HEALTH, Lang.COMBAT_AUTOTOTEM_HEALTH_DESC).currentValue(5.0f).range(1.0f, 20.0f).step(0.5f).unit(SettingUnit.HITPOINTS);
        this.triggers = new MultiSelectSetting(Translation.clearText("Срабатывать на")).values(AutoTotemTrigger.class).select(AutoTotemTrigger.CRYSTAL);
        this.crystalRadius = new NumberSetting(Translation.clearText("Радиус до кристалла")).currentValue(6.0f).range(0.0f, 16.0f).step(0.5f).visible(() -> Boolean.valueOf(this.triggers.isSelected(AutoTotemTrigger.CRYSTAL)));
        this.dynamiteRadius = new NumberSetting(Translation.clearText("Радиус до динамита")).currentValue(6.0f).range(0.0f, 16.0f).step(0.5f).visible(() -> Boolean.valueOf(this.triggers.isSelected(AutoTotemTrigger.DYNAMITE)));
        this.obsidianRadius = new NumberSetting(Translation.clearText("Радиус до обсидиана")).currentValue(6.0f).range(0.0f, 16.0f).step(0.5f).visible(() -> Boolean.valueOf(this.triggers.isSelected(AutoTotemTrigger.OBSIDIAN)));
        this.anchorRadius = new NumberSetting(Translation.clearText("Радиус до якоря")).currentValue(6.0f).range(0.0f, 16.0f).step(0.5f).visible(() -> Boolean.valueOf(this.triggers.isSelected(AutoTotemTrigger.ANCHOR)));
        this.fallDistance = new NumberSetting(Translation.clearText("Дальность падения")).currentValue(5.0f).range(0.0f, 32.0f).step(1.0f).visible(() -> Boolean.valueOf(this.triggers.isSelected(AutoTotemTrigger.FALL)));
        this.saveEnchanted = new BooleanSetting(Translation.clearText("Сохранять зачарованные")).setValue(true);
        this.dontSwapSphere = new BooleanSetting(Translation.clearText("Не менять шар")).setValue(true);
        this.cancelEating = new BooleanSetting(Translation.clearText("Отменять еду"), Translation.clearText("Перед свапом тотема прерывает жевание/питье: иначе SWAP-клик по оффхенду иногда отбрасывается и тотем не встаёт.")).setValue(true);
        this.showCounter = new BooleanSetting(Lang.COMBAT_AUTOTOTEM_COUNTER);
        this.countEnchanted = new BooleanSetting(Lang.COMBAT_AUTOTOTEM_COUNT_ENCHANTED).visible(() -> Boolean.valueOf(this.showCounter.isValue()));
        this.mc = Mc.INSTANCE;
        this.savedOffhand = ItemStack.EMPTY;
        this.savedOffhandItem = Items.AIR;
        this.totemCount = 0;
        this.enchantedTotemCount = 0;
        this.swapTimer = new Stopwatch(false);
        addSettings(this.mode, this.healthThreshold, this.triggers, this.crystalRadius, this.dynamiteRadius, this.obsidianRadius, this.anchorRadius, this.fallDistance, this.saveEnchanted, this.dontSwapSphere, this.cancelEating, this.showCounter, this.countEnchanted);
        register(Render2DEvent.class, class311Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded() && this.showCounter.isValue()) {
                if ((this.totemCount > 0 || (this.enchantedTotemCount > 0 && this.countEnchanted.isValue())) && class311Var.isPost()) {
                    DrawEngine class154VarDrawEngine = Expensive.INSTANCE.drawEngine();
                    ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
                    MatrixStack matrixStack = class311Var.matrixStack();
                    ThemePalette class764VarPalette = Expensive.INSTANCE.theme().palette();
                    float fScreenWidth = (ScreenResolution.resolution().screenWidth() / 2.0f) - (16 / 2.0f);
                    float fScreenHeight = (ScreenResolution.resolution().screenHeight() / 2.0f) - (16 / 2.0f);
                    class154VarDrawEngine.begin();
                    class154VarDrawEngine.itemStack(matrixStack.peek().getPositionMatrix(), Items.TOTEM_OF_UNDYING.getDefaultStack(), fScreenWidth + 35.0f, fScreenHeight, 16 / 32.0f, 1.0f);
                    class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), Fonts.INTER_SEMIBOLD.get(), (this.totemCount + (this.countEnchanted.isValue() ? this.enchantedTotemCount : 0)) + "x", fScreenWidth + 35.0f + 16 + 3.0f, (fScreenHeight + (16 / 2.0f)) - (Fonts.INTER_SEMIBOLD.get().getHeight(12) / 2.0f), 12, 0.0f, class115VarColorStack.computeColor(class764VarPalette.text().tone(400).argb()));
                    class154VarDrawEngine.end();
                }
            }
        });
        register(PlayerTickEvent.class, class130Var -> {
            if (!class130Var.isPre() || !isState() || !this.mc.isWorldLoaded()) {
                return;
            }
            ClientPlayerEntity player = this.mc.getPlayer();
            ClientWorld world = this.mc.getWorld();
            if (player == null || world == null) {
                return;
            }
            if (this.advancedRestoreSelected != -1 && System.currentTimeMillis() >= this.advancedRestoreAt) {
                player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.advancedRestoreSelected));
                this.advancedRestoreSelected = -1;
            }
            List<SlotSearchResult2> listMethod015 = findTotems(player);
            ArrayList<SlotSearchResult2> arrayList = new ArrayList();
            ArrayList<SlotSearchResult2> arrayList2 = new ArrayList();
            for (SlotSearchResult2 class329Var : listMethod015) {
                if (class329Var.stack().hasEnchantments()) {
                    arrayList2.add(class329Var);
                } else {
                    arrayList.add(class329Var);
                }
            }
            this.totemCount = arrayList.stream().mapToInt(class329Var2 -> {
                return class329Var2.stack().getCount();
            }).sum();
            this.enchantedTotemCount = arrayList2.stream().mapToInt(class329Var3 -> {
                return class329Var3.stack().getCount();
            }).sum();
            SlotSearchResult2 class329VarMethod006 = chooseTotem(arrayList, arrayList2);
            if (!shouldActivate(world, player) || class329VarMethod006 == null || !this.swapTimer.hasElapsed(300L)) {
                restoreOffhand(player);
                return;
            }
            if (hasTotemInHand(player) || !GrimDelayHandler.script.isFinished()) {
                return;
            }
            int iIncreasedSlot = class329VarMethod006.slotReference().increasedSlot();
            CombatPauseManager.INSTANCE.pauseAutoSwapForAtLeast(3);
            saveOffhand(player.getOffHandStack());
            // Пока игрок жуёт/пьёт, сервер держит предмет активным и SWAP-клик по оффхенду
            // может отбрасывается - тотем не встанет. Прерываем использование перед свапом.
            cancelUseIfEating(player);
            if (this.mode.isSelected(AutoTotemMode.ADVANCED)) {
                advancedSwap(player, class329VarMethod006.slotReference().slot(), iIncreasedSlot);
            } else {
                if (!canSwapNow(player, iIncreasedSlot)) {
                    return;
                }
                SwapUtil.swapToOffhand(iIncreasedSlot);
            }
            this.swapTimer.reset();
        });
    }

    public List<SlotSearchResult2> findTotems(ClientPlayerEntity clientPlayerEntity) {
        return Expensive.INSTANCE.inventoryService().searcher().findAllItems(itemStack -> {
            return itemStack.getItem() == Items.TOTEM_OF_UNDYING && !clientPlayerEntity.getItemCooldownManager().isCoolingDown(itemStack);
        }, InventoryScope.ALL);
    }

    public Optional<SlotSearchResult2> findSavedOffhandSlot() {
        return Expensive.INSTANCE.inventoryService().searcher().findItem(itemStack -> {
            return !itemStack.isEmpty() && itemStack.getItem() == this.savedOffhandItem && ItemStack.areItemsAndComponentsEqual(itemStack, this.savedOffhand);
        }, InventoryScope.HOTBAR, InventoryScope.INVENTORY);
    }

    @Nullable
    private SlotSearchResult2 chooseTotem(List<SlotSearchResult2> plain, List<SlotSearchResult2> enchanted) {
        if (!plain.isEmpty()) {
            return plain.getFirst();
        }
        if (this.saveEnchanted.isValue()) {
            return null;
        }
        return enchanted.isEmpty() ? null : enchanted.getFirst();
    }

    public boolean hasTotemInHand(ClientPlayerEntity clientPlayerEntity) {
        return clientPlayerEntity.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING || clientPlayerEntity.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING;
    }

    /**
     * Прерывает жевание или питье, если оно идёт. Пока предмет используется, сервер считает
     * его активным, и SWAP-клик по оффхенду может отбрасываться - тотем тогда не встаёт,
     * хотя клиент уже считает, что свап сделан. Клиент шлёт RELEASE_USE_ITEM и сам сбрасывает
     * активный предмет, после чего свап уходит в обычном потоке.
     */
    public void cancelUseIfEating(ClientPlayerEntity clientPlayerEntity) {
        if (!this.cancelEating.isValue() || !clientPlayerEntity.isUsingItem()) {
            return;
        }
        UseAction useAction = clientPlayerEntity.getActiveItem().getUseAction();
        if (useAction != UseAction.EAT && useAction != UseAction.DRINK) {
            return;
        }
        PacketSender.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
        clientPlayerEntity.stopUsingItem();
    }

    public boolean shouldActivate(ClientWorld clientWorld, ClientPlayerEntity clientPlayerEntity) {
        if (clientPlayerEntity.isCreative() || clientPlayerEntity.isSpectator() || clientPlayerEntity.isDead() || !clientPlayerEntity.isAlive()) {
            return false;
        }
        if (this.dontSwapSphere.isValue() && isHoldingSphere(clientPlayerEntity)) {
            return false;
        }
        float health = clientPlayerEntity.getHealth() + clientPlayerEntity.getAbsorptionAmount();
        if (health <= this.healthThreshold.currentValue()) {
            return true;
        }
        if (this.triggers.isSelected(AutoTotemTrigger.CRYSTAL) && isEntityNear(clientWorld, clientPlayerEntity, EndCrystalEntity.class, this.crystalRadius.currentValue())) {
            return true;
        }
        if (this.triggers.isSelected(AutoTotemTrigger.DYNAMITE) && isEntityNear(clientWorld, clientPlayerEntity, TntEntity.class, this.dynamiteRadius.currentValue())) {
            return true;
        }
        if (this.triggers.isSelected(AutoTotemTrigger.OBSIDIAN) && isBlockNear(clientWorld, clientPlayerEntity, this.obsidianRadius.currentValue(), Blocks.OBSIDIAN)) {
            return true;
        }
        if (this.triggers.isSelected(AutoTotemTrigger.ANCHOR) && isBlockNear(clientWorld, clientPlayerEntity, this.anchorRadius.currentValue(), Blocks.RESPAWN_ANCHOR)) {
            return true;
        }
        return this.triggers.isSelected(AutoTotemTrigger.FALL) && clientPlayerEntity.fallDistance >= this.fallDistance.currentValue();
    }

    public boolean isHoldingSphere(ClientPlayerEntity clientPlayerEntity) {
        ItemStack offHandStack = clientPlayerEntity.getOffHandStack();
        if (offHandStack.isEmpty()) {
            return false;
        }
        if (offHandStack.getItem() == Items.PLAYER_HEAD) {
            Text text = (Text) offHandStack.get(DataComponentTypes.CUSTOM_NAME);
            if (text == null) {
                return false;
            }
            String lowerCase = TextVisitFactory.removeFormattingCodes(text).toLowerCase();
            return lowerCase.contains("шар") || lowerCase.contains("сфера") || lowerCase.contains("голова") || lowerCase.contains("дед-мороз") || lowerCase.contains("яйцо");
        }
        return offHandStack.getItem() == Items.ENCHANTED_GOLDEN_APPLE && offHandStack.contains(DataComponentTypes.CUSTOM_DATA);
    }

    public boolean isEntityNear(ClientWorld clientWorld, ClientPlayerEntity clientPlayerEntity, Class<? extends Entity> cls, double d) {
        double d2 = d * d;
        for (Entity entity : clientWorld.getEntities()) {
            if (cls.isInstance(entity) && entity != clientPlayerEntity && entity.squaredDistanceTo(clientPlayerEntity) <= d2) {
                return true;
            }
        }
        return false;
    }

    public boolean isBlockNear(ClientWorld clientWorld, ClientPlayerEntity clientPlayerEntity, double d, Block block) {
        int iCeil = (int) Math.ceil(d);
        BlockPos blockPos = clientPlayerEntity.getBlockPos();
        for (BlockPos blockPos2 : BlockPos.iterate(blockPos.add(-iCeil, -iCeil, -iCeil), blockPos.add(iCeil, iCeil, iCeil))) {
            if (clientWorld.getBlockState(blockPos2).isOf(block)) {
                return true;
            }
        }
        return false;
    }

    public void advancedSwap(ClientPlayerEntity clientPlayerEntity, int i, int i2) {
        if (i < 9) {
            int i3 = clientPlayerEntity.getInventory().selectedSlot;
            clientPlayerEntity.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(i));
            clientPlayerEntity.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
            this.advancedRestoreSelected = i3;
            this.advancedRestoreAt = System.currentTimeMillis() + 200L;
        } else {
            SwapUtil.swapToOffhand(i2);
        }
    }

    public void restoreOffhand(ClientPlayerEntity clientPlayerEntity) {
        if (this.savedOffhand.isEmpty() || this.savedOffhandItem == Items.AIR) {
            this.savedOffhand = ItemStack.EMPTY;
            this.savedOffhandItem = Items.AIR;
            return;
        }
        if (ItemStack.areItemsAndComponentsEqual(clientPlayerEntity.getOffHandStack(), this.savedOffhand)) {
            this.savedOffhand = ItemStack.EMPTY;
            this.savedOffhandItem = Items.AIR;
            return;
        }
        if (!this.swapTimer.hasElapsed(300L) || !GrimDelayHandler.script.isFinished()) {
            return;
        }
        findSavedOffhandSlot().ifPresent(class329Var -> {
            int iIncreasedSlot = class329Var.slotReference().increasedSlot();
            if (!canSwapNow(clientPlayerEntity, iIncreasedSlot)) {
                return;
            }
            CombatPauseManager.INSTANCE.pauseAutoSwapForAtLeast(3);
            SwapUtil.swapToOffhand(iIncreasedSlot);
            this.swapTimer.reset();
            this.savedOffhand = ItemStack.EMPTY;
            this.savedOffhandItem = Items.AIR;
        });
    }

    public void saveOffhand(ItemStack itemStack) {
        if (itemStack.isEmpty() || itemStack.getItem() == Items.AIR) {
            return;
        }
        if (this.savedOffhandItem == Items.AIR || this.savedOffhand.isEmpty()) {
            this.savedOffhand = itemStack.copy();
            this.savedOffhandItem = this.savedOffhand.getItem();
        }
    }

    @Override
    public void deactivate() {
        this.savedOffhand = ItemStack.EMPTY;
        this.savedOffhandItem = Items.AIR;
        this.advancedRestoreSelected = -1;
        pendingSwapSlot = -1;
        slowdownTicksLeft = 0;
        pauseSprintTickAge = -1;
        pauseSprintResumeAge = -1;
        suppressSprintPacketTickAge = -1;
        super.deactivate();
    }

    // =========================================================================
    // РАБОТА СО СПРИНТОМ
    //
    // Сырой клик по оффхенду (SWAP, 40) уходит в том же тике, в котором игрок
    // бежит. Сервер в этот момент ещё считает нас бегущими, и на серверах с
    // Grim/NCP клик отбрасывается: STOP_SPRINTING приходит слишком поздно, он
    // формируется в sendMovementPackets(), а это уже после PlayerTickEvent.PRE,
    // где клик и отправляется. Отсюда и симптом "со спринтом не свапается".
    //
    // Решение: клик откладывается на один тик. В тике N спринт гасится в
    // tickMovement, поэтому STOP_SPRINTING уходит из sendMovementPackets() этого
    // же тика, а клик - из PlayerTickEvent.PRE следующего. Чтобы спринт не
    // прыгал рывком, на окно свапа вешается сглаживание ввода: множитель
    // 0.55 -> 0.35 -> 0.55 -> 0.8, то есть игрок идёт всё это время, просто
    // медленно, и плавно разгоняется обратно.
    // =========================================================================

    /** Длина окна сглаживания в тиках по умолчанию (Auto Totem): 2 тика на клик плюс 1 тик на возврат. */
    private static final int SLOWDOWN_TICKS = 4;

    /** Множители ввода по тикам окна по умолчанию (Auto Totem): торможение, минимум, отпускание, разгон. */
    private static final float[] SLOWDOWN_FACTORS = {0.55f, 0.35f, 0.55f, 0.8f};

    /** Слот, ради которого спринт уже погашен и клик ждёт следующего тика. -1 = не ждёт. */
    private static int pendingSwapSlot = -1;

    /** Сколько тиков окна сглаживания осталось. 0 = спринт идёт как обычно. */
    private static int slowdownTicksLeft = 0;

    /** Полная длина текущего окна сглаживания (задаётся в requestSprintPausedClick). */
    private static int slowdownTotalTicks = SLOWDOWN_TICKS;

    /** Множители текущего окна сглаживания (задаются в requestSprintPausedClick). */
    private static float[] slowdownFactors = SLOWDOWN_FACTORS;

    /** age тика паузы спринта. -1 = паузы нет, миксин ничего не делает. */
    public static int pauseSprintTickAge = -1;
    public static int pauseSprintResumeAge = -1;
    public static int suppressSprintPacketTickAge = -1;
    public static int sprintSuppressedCount = 0;

    /** Funtime: клик откладывается на тик, чтобы STOP_SPRINTING успел уйти. */
    private boolean canSwapNow(ClientPlayerEntity player, int slot) {
        if (!this.mode.isSelected(AutoTotemMode.FUNTIME)) {
            pendingSwapSlot = -1;
            return true;
        }
        return requestSprintPausedClick(player, slot);
    }

    /**
     * Общий для Auto Totem и Auto Swap запрос клика с обходом спринта. Если игрок
     * бежит, клик откладывается: в текущем тике спринт гасится в tickMovement
     * (STOP_SPRINTING уходит штатно из sendMovementPackets), а сам клик выполняется
     * на следующем тике - тогда сервер уже знает, что мы не бежим, и SWAP-клик не
     * отбрасывается. Повторный вызов с тем же слотом возвращает true и снимает
     * ожидание. Вызывать строго из PlayerTickEvent.PRE, иначе клик может уйти
     * раньше STOP_SPRINTING.
     */
    public static boolean requestSprintPausedClick(ClientPlayerEntity player, int slot) {
        return requestSprintPausedClick(player, slot, SLOWDOWN_TICKS, SLOWDOWN_FACTORS);
    }

    /**
     * Вариант с настраиваемым окном сглаживания: Auto Swap использует короткое и
     * мягкое окно, чтобы спринт не обрывался, а лишь немного замедлялся.
     */
    public static boolean requestSprintPausedClick(ClientPlayerEntity player, int slot, int ticks, float[] factors) {
        if (pendingSwapSlot != -1 && pendingSwapSlot != slot) {
            pendingSwapSlot = -1;
        }
        if (pendingSwapSlot == slot) {
            pendingSwapSlot = -1;
            return true;
        }
        if (player != null && player.isSprinting()) {
            pendingSwapSlot = slot;
            slowdownTicksLeft = ticks;
            slowdownTotalTicks = ticks;
            slowdownFactors = factors;
            return false;
        }
        return true;
    }

    /**
     * Множитель ввода на текущий тик и сдвиг окна на один тик. Вызывается из
     * ClientPlayerEntityMixin.tickMovement прямо перед super.tickMovement(),
     * то есть после того, как KeyboardInput уже собрал input, но до того, как
     * LivingEntity посчитает по нему перемещение.
     */
    public static float consumeSprintSlowdownTick(ClientPlayerEntity player) {
        if (slowdownTicksLeft <= 0) {
            pendingSwapSlot = -1;
            return 1.0f;
        }
        float f = slowdownFactors[Math.min(slowdownTotalTicks - slowdownTicksLeft, slowdownFactors.length - 1)];
        slowdownTicksLeft--;
        if (player != null && player.isSprinting()) {
            player.setSprinting(false);
        }
        return f;
    }

    /** Исходного модуля это поле не знает; держим 0, чтобы гард щит-брейкера ауры не срабатывал. */
    public int swapWatchTicks = 0;
    /** Исходного модуля нет срочного пути, поэтому всегда false. */
    public boolean urgentSwapPending = false;

    public boolean wantsSwap() {
        return false;
    }

    /**
     * Локальная пауза спринта ровно на тик клика, без пакета на сервер.
     * Нужна AutoPotionModule, где клик нельзя отложить на тик: сервер всё
     * время считает нас бегущими, поэтому STOP_SPRINTING в тике клика и не
     * нужен - достаточно не отправлять его вовсе. Миксин гасит спринт в
     * sendMovementPackets() и возвращает на следующем тике.
     */
    public static void pauseSprintForClick(ClientPlayerEntity clientPlayerEntity) {
        if (clientPlayerEntity == null || !clientPlayerEntity.isSprinting()) {
            return;
        }
        pauseSprintTickAge = clientPlayerEntity.age;
        pauseSprintResumeAge = clientPlayerEntity.age + 1;
        suppressSprintPacketTickAge = clientPlayerEntity.age;
        sprintSuppressedCount = 0;
    }
}
