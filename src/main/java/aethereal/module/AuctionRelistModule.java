package aethereal.module;
import aethereal.type.AuctionState;
import aethereal.Expensive;
import aethereal.util.GrimDelayHandler;
import aethereal.type.InventoryScope;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.util.MovementInputHelper;
import aethereal.type.NotificationType;
import aethereal.ui.setting.NumberSetting;
import aethereal.event.PacketReceiveEvent;
import aethereal.util.PlayerActionUtil;
import aethereal.event.PlayerTickEvent;
import aethereal.model.PricedItemStack;
import aethereal.util.ServerUtil;
import aethereal.type.SettingUnit;
import aethereal.model.SlotSearchResult2;
import aethereal.math.Stopwatch;
import aethereal.net.TooltipPriceReader;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;

public class AuctionRelistModule extends Module {
    static final int maxAttempts = 3;
    static final long actionTimeoutMs = 2000;
    static final long sellTimeoutMs = 5000;
    static final long slotClickDelayMs = 100;
    static final long closeDelayMs = 300;
    static final long emptyStorageTimeoutMs = 1000;
    public final NumberSetting cycleTimeSetting;

    public final Stopwatch actionStopwatch;

    public final Stopwatch cycleStopwatch;

    public final Stopwatch storageStopwatch;
    public final Deque<PricedItemStack> relistQueue;

    public final TooltipPriceReader priceReader;
    public AuctionState currentState;

    public PricedItemStack currentItem;
    public int attemptCount;
    public boolean selling;
    public boolean storageOpened;
    public boolean itemFound;
    public ItemStack lastProcessedStack;

    public AuctionRelistModule() {
        super(ModuleTab.MISC, "Auction Relist");
        this.cycleTimeSetting = new NumberSetting(Lang.AUCTION_RELIST_CYCLE_TIME, Lang.AUCTION_RELIST_CYCLE_TIME_DESC).currentValue(10.0f).step(1.0f).range(1.0f, 60.0f).unit(SettingUnit.SECONDS);
        this.actionStopwatch = new Stopwatch();
        this.cycleStopwatch = new Stopwatch();
        this.storageStopwatch = new Stopwatch();
        this.relistQueue = new ArrayDeque();
        this.priceReader = new TooltipPriceReader();
        this.currentState = AuctionState.IDLE;
        this.currentItem = null;
        this.attemptCount = 0;
        this.selling = false;
        this.storageOpened = false;
        this.itemFound = false;
        this.lastProcessedStack = ItemStack.EMPTY;
        addSettings(this.cycleTimeSetting);
        register(PacketReceiveEvent.class, class051Var -> {
            if ((class051Var.getPacket()) instanceof GameMessageS2CPacket packet ) {
                String lowerCase = StringHelper.stripTextFormat(packet.content().getString()).toLowerCase();
                if (lowerCase.contains("выставлен на продажу")) {
                    this.selling = false;
                    this.currentItem = null;
                }
                if (lowerCase.contains("вы не можете продать воздух")) {
                    this.selling = false;
                }
                if (lowerCase.contains("слишком дешево") || lowerCase.contains("слишком дорого")) {
                    resendSellCommand();
                }
                if (lowerCase.contains("после входа на режим необходимо немного подождать перед использованием аукциона. подождите")) {
                    setState(false);
                }
            }
        });
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded() && isOnFuntimeAnarchy()) {
                ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
                Screen currentScreen = Mc.INSTANCE.getCurrentScreen();
                switch (this.currentState.ordinal()) {
                    case 0:
                        handleIdle();
                        break;
                    case 1:
                        handleOpenAuction(player);
                        break;
                    case 2:
                        handleClickStorageTab(player, currentScreen);
                        break;
                    case maxAttempts:
                        handleCollectItems(player, currentScreen);
                        break;
                    case 4:
                        handleRelistItems(player, currentScreen);
                        break;
                }
            }
        });
    }

    public void resendSellCommand() {
        ClientPlayerEntity player;
        if (this.currentItem == null || (player = Mc.INSTANCE.getPlayer()) == null) {
            return;
        }
        player.networkHandler.sendChatCommand("ah sell " + this.currentItem.price());
    }

    public boolean isOnFuntimeAnarchy() {
        if (ServerUtil.isConnectedToServer("funtime") && ServerUtil.getAnarchy() != -1) {
            return true;
        }
        notifyError("[Auction Relist] Вы должны быть подключены к " + String.valueOf(Formatting.RED) + "FunTime!");
        setState(false);
        return false;
    }

    public void handleIdle() {
        if (this.cycleStopwatch.hasElapsed((long) this.cycleTimeSetting.currentValue(), TimeUnit.SECONDS)) {
            this.cycleStopwatch.reset();
            setAuctionState(AuctionState.OPEN_AH);
        }
    }

    public void handleOpenAuction(ClientPlayerEntity clientPlayerEntity) {
        clientPlayerEntity.networkHandler.sendChatCommand("ah");
        setAuctionState(AuctionState.CLICK_STORAGE_TAB);
    }

    public void handleClickStorageTab(ClientPlayerEntity clientPlayerEntity, Screen screen) {
        if (screen instanceof GenericContainerScreen) {
            GenericContainerScreen genericContainerScreen = (GenericContainerScreen) screen;
            if (genericContainerScreen.getTitle().getString().toLowerCase().contains("аукцион")) {
                if (MovementInputHelper.hasPlayerMovement()) {
                    return;
                }
                PlayerActionUtil.INSTANCE.clickSlot(genericContainerScreen.getScreenHandler().syncId, 46, 0, SlotActionType.QUICK_MOVE, true);
                resetStorageState();
                setAuctionState(AuctionState.COLLECT_ITEMS);
                return;
            }
        }
        if (this.actionStopwatch.hasElapsed(actionTimeoutMs)) {
            retryOpenAuction();
        }
    }

    public void handleCollectItems(ClientPlayerEntity clientPlayerEntity, Screen screen) {
        if (screen instanceof GenericContainerScreen) {
            GenericContainerScreen genericContainerScreen = (GenericContainerScreen) screen;
            if (genericContainerScreen.getTitle().getString().toLowerCase().contains("хранилище")) {
                if (!this.storageOpened) {
                    this.storageStopwatch.reset();
                    this.storageOpened = true;
                    this.lastProcessedStack = ItemStack.EMPTY;
                }
                Slot slot = genericContainerScreen.getScreenHandler().getSlot(0);
                if (slot.hasStack() && this.actionStopwatch.hasElapsed(slotClickDelayMs)) {
                    processStorageItem(genericContainerScreen, slot.getStack());
                    return;
                }
                if (this.itemFound && this.actionStopwatch.hasElapsed(closeDelayMs)) {
                    clientPlayerEntity.closeScreen();
                    setAuctionState(AuctionState.RELIST_ITEMS);
                    return;
                } else {
                    if (!this.itemFound && this.storageOpened && this.storageStopwatch.hasElapsed(emptyStorageTimeoutMs)) {
                        clientPlayerEntity.closeScreen();
                        notifyError("[Auction Relist] Не найдено предметов для релистинга!");
                        setAuctionState(AuctionState.IDLE);
                        return;
                    }
                    return;
                }
            }
        }
        this.storageOpened = false;
        if (this.actionStopwatch.hasElapsed(actionTimeoutMs)) {
            setAuctionState(AuctionState.CLICK_STORAGE_TAB);
        }
    }

    public void processStorageItem(GenericContainerScreen genericContainerScreen, ItemStack itemStack) {
        if (this.lastProcessedStack.isEmpty() || !ItemStack.areItemsAndComponentsEqual(itemStack, this.lastProcessedStack)) {
            ItemStack itemStackCopy = itemStack.copy();
            int price = this.priceReader.getPrice(itemStackCopy);
            this.lastProcessedStack = itemStackCopy;
            if (price > 0) {
                this.relistQueue.addLast(new PricedItemStack(price, itemStackCopy));
            } else {
                notifyError("[Auction Relist] Некорректная цена у предмета, пропускаю.");
            }
            PlayerActionUtil.INSTANCE.clickSlot(genericContainerScreen.getScreenHandler().syncId, 0, 0, SlotActionType.QUICK_MOVE, true);
            this.actionStopwatch.reset();
            this.itemFound = true;
        }
    }

    public void handleRelistItems(ClientPlayerEntity clientPlayerEntity, Screen screen) {
        if (this.selling) {
            if (this.actionStopwatch.hasElapsed(sellTimeoutMs)) {
                handleSellTimeout();
            }
        } else {
            if (this.relistQueue.isEmpty() && this.currentItem == null) {
                setAuctionState(AuctionState.IDLE);
                return;
            }
            if (screen != null) {
                clientPlayerEntity.closeScreen();
                return;
            }
            if (this.currentItem == null) {
                this.currentItem = this.relistQueue.pollFirst();
                if (this.currentItem == null) {
                    setAuctionState(AuctionState.IDLE);
                    return;
                }
            }
            sellCurrentItem(clientPlayerEntity);
        }
    }

    public void handleSellTimeout() {
        this.selling = false;
        if (this.currentItem != null) {
            this.relistQueue.addFirst(this.currentItem);
            this.currentItem = null;
            this.attemptCount++;
            if (this.attemptCount > maxAttempts) {
                notifyError("[Auction Relist] Превышено количество попыток, останавливаюсь.");
                setState(false);
            }
        }
    }

    public void sellCurrentItem(ClientPlayerEntity clientPlayerEntity) {
        Optional<SlotSearchResult2> optionalFindItem = Expensive.INSTANCE.inventoryService().searcher().findItem(itemStack -> {
            return !itemStack.isEmpty() && ItemStack.areItemsEqual(itemStack, this.currentItem.stack());
        }, InventoryScope.HOTBAR, InventoryScope.INVENTORY);
        if (optionalFindItem.isEmpty()) {
            this.currentItem = null;
            return;
        }
        if (GrimDelayHandler.script.isFinished()) {
            SlotSearchResult2 class329Var = optionalFindItem.get();
            PricedItemStack class473Var = this.currentItem;
            this.selling = true;
            this.actionStopwatch.reset();
            GrimDelayHandler.script.addTickStep(0, GrimDelayHandler::disableMoveKeys).addTickStep(1, () -> {
                PlayerActionUtil.INSTANCE.clickSlot(clientPlayerEntity.currentScreenHandler.syncId, class329Var.slotReference().increasedSlot(), clientPlayerEntity.getInventory().selectedSlot, SlotActionType.SWAP, true);
                PlayerActionUtil.INSTANCE.updateSlots(true);
            }).addTickStep(2, () -> {
                clientPlayerEntity.networkHandler.sendChatCommand("ah sell " + class473Var.price());
                GrimDelayHandler.enableMoveKeys();
            });
        }
    }

    public void setAuctionState(AuctionState class474Var) {
        this.currentState = class474Var;
        this.actionStopwatch.reset();
        if (class474Var == AuctionState.IDLE) {
            this.cycleStopwatch.reset();
            this.attemptCount = 0;
        }
    }

    public void resetStorageState() {
        this.storageOpened = false;
        this.itemFound = false;
        this.lastProcessedStack = ItemStack.EMPTY;
    }

    public void retryOpenAuction() {
        this.attemptCount++;
        if (this.attemptCount <= maxAttempts) {
            setAuctionState(AuctionState.OPEN_AH);
        } else {
            notifyError("[Auction Relist] Не удалось открыть аукцион после 3 попыток.");
            setState(false);
        }
    }

    public void notifyError(String str) {
        Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, Text.of(str), 3L, TimeUnit.SECONDS);
    }

    @Override
    public void deactivate() {
        this.relistQueue.clear();
        this.currentState = AuctionState.IDLE;
        this.selling = false;
        this.actionStopwatch.reset();
        resetStorageState();
        this.currentItem = null;
        this.attemptCount = 0;
        this.cycleStopwatch.setElapsedTime((long) this.cycleTimeSetting.currentValue(), TimeUnit.SECONDS);
        super.deactivate();
    }
}
