package aethereal.type;
import aethereal.util.BlockUtil;
import aethereal.module.CreeperFarmModule;
import aethereal.Expensive;
import aethereal.event.PacketReceiveEvent;
import aethereal.util.PlayerActionUtil;
import aethereal.model.ScopedSlot;
import aethereal.model.SlotSearchResult2;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class CreeperFarmUnloadPhase {
    public final CreeperFarmModule module;
    public BlockPos targetChest;
    public final Mc mc = Mc.INSTANCE;

    public CreeperFarmUnloadTarget unloadTarget = CreeperFarmUnloadTarget.NONE;
    public boolean awaitingResponse = false;
    public long commandSentTime = 0;
    public boolean noChestsAvailable = false;

    public CreeperFarmUnloadPhase(CreeperFarmModule class597Var) {
        this.module = class597Var;
    }

    public boolean tryStartUnload() {
        if (this.noChestsAvailable || countGunpowder() <= 64) {
            return false;
        }
        if (this.awaitingResponse) {
            return true;
        }
        this.mc.getNetworkHandler().sendChatMessage("/clan storage");
        this.awaitingResponse = true;
        this.commandSentTime = System.currentTimeMillis();
        return true;
    }

    public void handlePacket(PacketReceiveEvent class051Var) {
        if (this.awaitingResponse) {
            net.minecraft.network.packet.Packet<?> packet = class051Var.getPacket();
            Objects.requireNonNull(packet);
            if (packet instanceof GameMessageS2CPacket) {
                String string = ((GameMessageS2CPacket) packet).content().getString();
                if (string.contains("Помощь по Кланам")) {
                    Expensive.INSTANCE.notificationRepository().post(NotificationType.INFO, (Text) Text.literal("Клановое хранилище недоступно. Переключаюсь на эндер сундук."), 3L, TimeUnit.SECONDS);
                    this.awaitingResponse = false;
                    this.unloadTarget = CreeperFarmUnloadTarget.ENDER_CHEST;
                }
                if (string.contains("Эту команду могут писать только донатеры")) {
                    Expensive.INSTANCE.notificationRepository().post(NotificationType.INFO, (Text) Text.literal("Эндер-сундук недоступен. Переключаюсь на обычный."), 3L, TimeUnit.SECONDS);
                    this.awaitingResponse = false;
                    this.unloadTarget = CreeperFarmUnloadTarget.CHEST;
                }
            } else if (packet instanceof OpenScreenS2CPacket) {
                String string2 = (String) (((OpenScreenS2CPacket) packet).getName().getString());
                if (string2.contains("Клан: Хранилище")) {
                    this.awaitingResponse = false;
                    this.unloadTarget = CreeperFarmUnloadTarget.CLAN;
                }
                if (string2.contains("Ender Chest")) {
                    this.awaitingResponse = false;
                    this.unloadTarget = CreeperFarmUnloadTarget.ENDER_CHEST;
                }
            }
        }
    }

    public void tickUnloadPhase(ClientPlayerEntity clientPlayerEntity) {
        if (System.currentTimeMillis() - this.commandSentTime > 3000 && this.awaitingResponse) {
            this.awaitingResponse = false;
            this.unloadTarget = CreeperFarmUnloadTarget.CHEST;
        }
        switch (this.unloadTarget.ordinal()) {
            case 0:
                if (!(this.mc.getCurrentScreen() instanceof GenericContainerScreen) && !this.awaitingResponse) {
                    this.mc.getNetworkHandler().sendChatMessage("/clan storage");
                    this.awaitingResponse = true;
                    this.commandSentTime = System.currentTimeMillis();
                    break;
                }
                break;
            case 1:
                if (!(this.mc.getCurrentScreen() instanceof GenericContainerScreen) && !this.awaitingResponse) {
                    this.mc.getNetworkHandler().sendChatMessage("/ec");
                    this.awaitingResponse = true;
                    this.commandSentTime = System.currentTimeMillis();
                    break;
                }
                break;
            case 2:
                if (this.targetChest == null || isChestFull(this.targetChest)) {
                    this.targetChest = findAvailableChest();
                    if (this.targetChest == null) {
                        Expensive.INSTANCE.notificationRepository().post(NotificationType.WARNING, (Text) Text.literal("Нет свободных сундуков. Переход к патрулю."), 3L, TimeUnit.SECONDS);
                        this.noChestsAvailable = true;
                        this.module.getPhaseManager().setPhase(TpLootStage.LOADING_CHUNKS);
                    }
                }
                if (clientPlayerEntity.getPos().distanceTo(Vec3d.ofCenter(this.targetChest)) <= 4.5d && !(this.mc.getCurrentScreen() instanceof GenericContainerScreen)) {
                    this.mc.getInteractionManager().interactBlock(clientPlayerEntity, Hand.MAIN_HAND, BlockUtil.createHitResult(this.targetChest, Direction.UP));
                }
                break;
        }
    }

    public void handleContainer(GenericContainerScreenHandler genericContainerScreenHandler, String str) {
        if (str.contains("Клан: Хранилище") && this.unloadTarget == CreeperFarmUnloadTarget.CLAN) {
            if (!hasEmptySlot(genericContainerScreenHandler)) {
                Expensive.INSTANCE.notificationRepository().post(NotificationType.INFO, (Text) Text.literal("Клановое хранилище заполнено. Пробуем /ec."), 3L, TimeUnit.SECONDS);
                this.mc.getPlayer().closeHandledScreen();
                this.unloadTarget = CreeperFarmUnloadTarget.ENDER_CHEST;
                this.awaitingResponse = true;
                this.mc.getNetworkHandler().sendChatMessage("/ec");
                this.commandSentTime = System.currentTimeMillis();
                return;
            }
            transferGunpowder(genericContainerScreenHandler);
        }
        if (str.contains("Ender Chest") && this.unloadTarget == CreeperFarmUnloadTarget.ENDER_CHEST) {
            if (!hasEmptySlot(genericContainerScreenHandler)) {
                Expensive.INSTANCE.notificationRepository().post(NotificationType.INFO, (Text) Text.literal("Эндер-сундук заполнен. Переключаюсь на обычный."), 3L, TimeUnit.SECONDS);
                this.mc.getPlayer().closeHandledScreen();
                this.unloadTarget = CreeperFarmUnloadTarget.CHEST;
                return;
            }
            transferGunpowder(genericContainerScreenHandler);
        }
        if (str.contains("Chest") && this.unloadTarget == CreeperFarmUnloadTarget.CHEST) {
            if (hasEmptySlot(genericContainerScreenHandler)) {
                transferGunpowder(genericContainerScreenHandler);
            } else {
                this.mc.getPlayer().closeHandledScreen();
                this.targetChest = null;
            }
        }
    }

    public void transferGunpowder(GenericContainerScreenHandler genericContainerScreenHandler) {
        Iterator<SlotSearchResult2> it = Expensive.INSTANCE.inventoryService().searcher().findAllItems(itemStack -> {
            return itemStack.getItem() == Items.GUNPOWDER;
        }, InventoryScope.ALL).stream().filter((v0) -> {
            return v0.found();
        }).toList().iterator();
        while (it.hasNext()) {
            ScopedSlot class246VarSlotReference = it.next().slotReference();
            int size = class246VarSlotReference.slot() < 9 ? (genericContainerScreenHandler.slots.size() - 9) + class246VarSlotReference.slot() : (genericContainerScreenHandler.slots.size() - 36) + (class246VarSlotReference.slot() - 9);
            if (size >= 0 && size < genericContainerScreenHandler.slots.size()) {
                PlayerActionUtil.INSTANCE.windowClick(SlotActionType.QUICK_MOVE, size, 1);
            }
        }
        this.mc.getPlayer().closeHandledScreen();
        if (this.unloadTarget == CreeperFarmUnloadTarget.CHEST) {
            this.targetChest = null;
        } else {
            this.module.getPhaseManager().setPhase(TpLootStage.LOADING_CHUNKS);
            this.unloadTarget = CreeperFarmUnloadTarget.NONE;
        }
    }

    public boolean hasEmptySlot(GenericContainerScreenHandler genericContainerScreenHandler) {
        return genericContainerScreenHandler.slots.stream().filter(slot -> {
            return slot.inventory != this.mc.getPlayer().getInventory();
        }).anyMatch(slot2 -> {
            return slot2.getStack().isEmpty();
        });
    }

    public boolean isChestFull(BlockPos blockPos) {
        if (!(this.mc.getWorld().getBlockEntity(blockPos) instanceof Inventory inventory)) {
            return true;
        }
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public BlockPos findAvailableChest() {
        return BlockUtil.getAllBlockPositions(this.module.getRegionMin(), this.module.getRegionMax()).stream().filter(blockPos -> {
            Block block = this.mc.getWorld().getBlockState(blockPos).getBlock();
            if (!(block.getName().getString().toLowerCase().contains("сундук") || block.getTranslationKey().toLowerCase().contains("chest"))) {
                return false;
            }
            if (!(this.mc.getWorld().getBlockEntity(blockPos) instanceof Inventory inventory)) {
                return false;
            }
            for (int i = 0; i < inventory.size(); i++) {
                if (inventory.getStack(i).isEmpty()) {
                    return true;
                }
            }
            return false;
        }).min(Comparator.comparingDouble(blockPos2 -> {
            return blockPos2.getSquaredDistance(this.mc.getPlayer().getPos());
        })).orElse(null);
    }

    public int countGunpowder() {
        return Expensive.INSTANCE.inventoryService().searcher().findAllItems(itemStack -> {
            return itemStack.getItem() == Items.GUNPOWDER;
        }, InventoryScope.ALL).stream().filter((v0) -> {
            return v0.found();
        }).mapToInt(class329Var -> {
            return class329Var.stack().getCount();
        }).sum();
    }

    public void reset() {
        this.unloadTarget = CreeperFarmUnloadTarget.NONE;
        this.targetChest = null;
        this.noChestsAvailable = false;
        this.awaitingResponse = false;
    }

    public CreeperFarmUnloadTarget getUnloadTarget() {
        return this.unloadTarget;
    }
}
