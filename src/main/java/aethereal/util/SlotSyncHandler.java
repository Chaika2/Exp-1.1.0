package aethereal.util;
import aethereal.event.ClientListener;
import aethereal.Expensive;
import aethereal.type.Mc;
import aethereal.type.NotificationType;
import aethereal.event.PacketReceiveEvent;
import aethereal.event.PacketSendEvent;
import aethereal.module.WidgetsModule;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SlotSyncHandler implements ClientListener {
    public static final Mc mc = Mc.INSTANCE;
    public static boolean lastSprinting;
    public static int selectedSlot;

    public SlotSyncHandler() {
        Expensive.INSTANCE.eventDispatcher().register(PacketSendEvent.class, this::onPacketSend);
        Expensive.INSTANCE.eventDispatcher().register(PacketReceiveEvent.class, this::onPacketReceive);
    }

    public void onPacketSend(PacketSendEvent class037Var) {
        if (mc.isWorldLoaded()) {
            net.minecraft.network.packet.Packet<?> packet = class037Var.getPacket();
            Objects.requireNonNull(packet);
            if (packet instanceof ClientCommandC2SPacket) {
                lastSprinting = ((ClientCommandC2SPacket) packet).getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING;
                return;
            }
            if (packet instanceof UpdateSelectedSlotC2SPacket && MinecraftClient.getInstance().getCurrentServerEntry() != null) {
                UpdateSelectedSlotC2SPacket updateSelectedSlotC2SPacket = (UpdateSelectedSlotC2SPacket) packet;
                if (updateSelectedSlotC2SPacket.getSelectedSlot() == selectedSlot) {
                    class037Var.cancel();
                } else {
                    selectedSlot = updateSelectedSlotC2SPacket.getSelectedSlot();
                }
                return;
            }
        }
    }

    public void onPacketReceive(PacketReceiveEvent class051Var) throws MatchException {
        if (mc.isWorldLoaded()) {
            if ((class051Var.getPacket()) instanceof UpdateSelectedSlotS2CPacket packet ) {
                try {
                    selectedSlot = packet.slot();
                } catch (Throwable th) {
                    throw new MatchException(th.toString(), th);
                }
            }
            ServerUtil.packet(class051Var);
            handleItemPickup(class051Var);
        }
    }

    public void handleItemPickup(PacketReceiveEvent class051Var) {
        WidgetsModule class634Var = (WidgetsModule) Expensive.INSTANCE.moduleRepository().get(WidgetsModule.class);
        if (class634Var.isState() && class634Var.getNotificationWidget().getItemPickUp().isValue()) {
            ClientPlayerEntity player = mc.getPlayer();
            net.minecraft.network.packet.Packet<?> packet = class051Var.getPacket();
            Objects.requireNonNull(packet);
            if (packet instanceof ItemPickupAnimationS2CPacket) {
                ItemPickupAnimationS2CPacket itemPickupAnimationS2CPacket = (ItemPickupAnimationS2CPacket) packet;
                if (itemPickupAnimationS2CPacket.getCollectorEntityId() == player.getId()) {
                    if ((mc.getWorld().getEntityById(itemPickupAnimationS2CPacket.getEntityId())) instanceof ItemEntity entityById ) {
                        ItemStack stack = entityById.getStack();
                        ContainerComponent containerComponent = (ContainerComponent) stack.get(DataComponentTypes.CONTAINER);
                        if (containerComponent != null) {
                            containerComponent.stream().filter(this::isEmptyNamed).forEach(itemStack -> {
                                postPickupNotification("Поднят " + stack.getItem().getName().getString() + " с: ", itemStack.getName(), itemStack.getCount());
                            });
                            return;
                        } else {
                            if (isEmptyNamed(stack)) {
                                postPickupNotification("Поднят предмет: ", stack.getName(), stack.getCount());
                                return;
                            }
                            return;
                        }
                    }
                }
            } else if (packet instanceof ScreenHandlerSlotUpdateS2CPacket) {
                ScreenHandlerSlotUpdateS2CPacket screenHandlerSlotUpdateS2CPacket = (ScreenHandlerSlotUpdateS2CPacket) packet;
                if (screenHandlerSlotUpdateS2CPacket.getSyncId() == 0 && screenHandlerSlotUpdateS2CPacket.getSlot() < player.currentScreenHandler.slots.size()) {
                    ContainerComponent containerComponent2 = (ContainerComponent) screenHandlerSlotUpdateS2CPacket.getStack().get(DataComponentTypes.CONTAINER);
                    ContainerComponent containerComponent3 = (ContainerComponent) player.currentScreenHandler.getSlot(screenHandlerSlotUpdateS2CPacket.getSlot()).getStack().get(DataComponentTypes.CONTAINER);
                    if (containerComponent2 == null || containerComponent3 == null) {
                        return;
                    }
                    containerComponent2.stream().filter(outerStack -> {
                        return containerComponent3.stream().noneMatch(itemStack2 -> {
                            return stacksEqual(outerStack, itemStack2);
                        });
                    }).forEach(itemStack3 -> {
                        postPickupNotification("Сложен в " + screenHandlerSlotUpdateS2CPacket.getStack().getItem().getName().getString() + ": ", itemStack3.getName(), 1);
                    });
                    return;
                }
            }
        }
    }

    public boolean isEmptyNamed(ItemStack itemStack) {
        return itemStack.getName().getContent().toString().equals("empty");
    }

    public boolean stacksEqual(ItemStack itemStack, ItemStack itemStack2) {
        return Objects.equals(itemStack.getComponents(), itemStack2.getComponents()) && itemStack.toString().equals(itemStack2.toString());
    }

    public void postPickupNotification(String str, Text text, int i) {
        net.minecraft.text.MutableText textAppend = Text.empty().append(str).append(text);
        if (i > 1) {
            textAppend.append(String.valueOf(Formatting.RESET) + " [" + String.valueOf(Formatting.RED) + i + String.valueOf(Formatting.GRAY) + "x" + String.valueOf(Formatting.RESET) + "]");
        }
        Expensive.INSTANCE.notificationRepository().post(NotificationType.INFO, textAppend, 3000L);
    }
}
