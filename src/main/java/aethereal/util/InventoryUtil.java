package aethereal.util;
import aethereal.math.DirectionalInput;
import aethereal.internal.EquipmentSlotIndexMap;
import aethereal.Expensive;
import aethereal.type.Mc;
import aethereal.event.MovementInputEvent;
import aethereal.net.PacketSender;
import aethereal.model.SlotSearchResult;
import aethereal.model.SwapResult;

import java.util.Iterator;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class InventoryUtil {
    public static final InventoryUtil INSTANCE = new InventoryUtil();
    boolean blockingMovement = false;
    int blockTicks = 0;

    public InventoryUtil() {
        Expensive.INSTANCE.eventDispatcher().register(MovementInputEvent.class, class040Var -> {
            if (this.blockingMovement) {
                class040Var.setInput(DirectionalInput.NONE);
                this.blockTicks++;
            }
        });
    }

    public boolean hasFreeSlotInHotbar(ClientPlayerEntity clientPlayerEntity) {
        for (int i = 0; i < 9; i++) {
            if (clientPlayerEntity.getInventory().getStack(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public String getPotionDuration(StatusEffectInstance statusEffectInstance) {
        int duration = statusEffectInstance.getDuration();
        return (statusEffectInstance.isInfinite() || duration < 0 || duration >= 1000000) ? "**:**" : MathUtil.tickToElapsedTime(duration);
    }

    public Hand getHandForItem(ClientPlayerEntity clientPlayerEntity, Item item) {
        return clientPlayerEntity.getOffHandStack().getItem() == item ? Hand.OFF_HAND : Hand.MAIN_HAND;
    }

    public void windowClick(SlotActionType slotActionType, int i, int i2, boolean z) {
        if (i == -1) {
            return;
        }
        Mc class815Var = Mc.INSTANCE;
        ClientPlayerEntity player = class815Var.getPlayer();
        class815Var.getInteractionManager().clickSlot(player.currentScreenHandler.syncId, i, i2, slotActionType, player);
        if (z) {
            player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(player.currentScreenHandler.syncId));
        }
    }

    public void windowClick(SlotActionType slotActionType, int i, int i2) {
        if (i == -1) {
            return;
        }
        Mc class815Var = Mc.INSTANCE;
        ClientPlayerEntity player = class815Var.getPlayer();
        class815Var.getInteractionManager().clickSlot(player.currentScreenHandler.syncId, i, i2, slotActionType, player);
    }

    public int getArmorSlotInventoryIndex(EquipmentSlot equipmentSlot) {
        switch (EquipmentSlotIndexMap.slotOrdinals[equipmentSlot.ordinal()]) {
            case 1:
                return 5;
            case 2:
                return 6;
            case 3:
                return 7;
            case 4:
                return 8;
            default:
                return -1;
        }
    }

    public void swapTo(int i, int i2) {
        PlayerInventory inventory = Mc.INSTANCE.getPlayer().getInventory();
        INSTANCE.windowClick(SlotActionType.SWAP, i, inventory.selectedSlot);
        INSTANCE.windowClick(SlotActionType.SWAP, i2, inventory.selectedSlot);
        INSTANCE.windowClick(SlotActionType.SWAP, i, inventory.selectedSlot, true);
    }

    public void switchHotBarSlot(ClientPlayerEntity clientPlayerEntity, int i, int i2, boolean z) {
        if (!z) {
            clientPlayerEntity.getInventory().selectedSlot = i;
        } else if (i2 != clientPlayerEntity.getInventory().selectedSlot) {
            PacketSender.sendPacket(new UpdateSelectedSlotC2SPacket(i));
        }
    }

    public int findItemSlot(Item item, boolean z, boolean z2) {
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        int i = z ? 9 : 0;
        int i2 = z ? 36 : 8;
        int i3 = i;
        while (i3 <= i2) {
            if (player.getInventory().getStack(i3).getItem() == item) {
                if (z2) {
                    return (i3 >= 9 || i3 == -1) ? i3 : i3 + 36;
                }
                return i3;
            }
            i3++;
        }
        return -1;
    }

    public int findItemSlot(Item item, boolean z) {
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        if (player == null) {
            return -1;
        }
        if (z) {
            Iterator it = player.getArmorItems().iterator();
            while (it.hasNext()) {
                if (((ItemStack) it.next()).getItem() == item) {
                    return -2;
                }
            }
        }
        int i = -1;
        for (int i2 = 0; i2 < 36; i2++) {
            if (player.getInventory().getStack(i2).getItem() == item) {
                i = i2;
                break;
            }
        }
        if (i < 9 && i != -1) {
            i += 36;
        }
        return i;
    }

    public SwapResult swapItemToMainHand(SlotSearchResult class105Var, SlotSearchResult class105Var2, ClientPlayerEntity clientPlayerEntity, boolean z) {
        if (!class105Var.found() && !class105Var2.found()) {
            return new SwapResult(false);
        }
        if (!class105Var.found() || class105Var2.found()) {
            clientPlayerEntity.getInventory().selectedSlot = class105Var2.slot();
        } else {
            this.blockingMovement = z;
            if (!this.blockingMovement) {
                windowClick(SlotActionType.SWAP, class105Var.slot(), clientPlayerEntity.getInventory().selectedSlot);
            } else if (this.blockTicks > 1) {
                windowClick(SlotActionType.SWAP, class105Var.slot(), clientPlayerEntity.getInventory().selectedSlot);
                this.blockTicks = 0;
                this.blockingMovement = false;
            }
        }
        return new SwapResult(true);
    }
}
