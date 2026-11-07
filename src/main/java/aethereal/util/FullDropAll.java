package aethereal.util;

import aethereal.type.Mc;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.Protocol1_21_2To1_21_4;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class FullDropAll {
    private static final long DELAY_MS = 50L;
    private static final String VIAFABRICPLUS_MOD_ID = "viafabricplus";

    private FullDropAll() {
    }

    public static void dropAll(ScreenHandler handler, Screen screen) {
        MinecraftClient client = Mc.INSTANCE.getMinecraft();
        ClientPlayerEntity player = client.player;
        if (player == null || client.interactionManager == null || player.currentScreenHandler != handler) {
            return;
        }
        if (FabricLoader.getInstance().isModLoaded(VIAFABRICPLUS_MOD_ID)
            && dropAllViaChain(client, player, handler, screen)) {
            return;
        }
        int containerSize = containerSize(handler);
        List<Integer> hotbarSlots = new ArrayList<>();
        List<Integer> allSlots = new ArrayList<>();
        for (int i = 0; i < handler.slots.size(); i++) {
            if (handler.slots.get(i).getStack().isEmpty()) {
                continue;
            }
            allSlots.add(i);
            if (hotbarInventoryIndex(handler, containerSize, i) >= 0) {
                hotbarSlots.add(i);
            }
        }
        int originalSlot = player.getInventory().selectedSlot;
        for (int slot : hotbarSlots) {
            client.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(
                hotbarInventoryIndex(handler, containerSize, slot)));
            client.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.DROP_ALL_ITEMS, BlockPos.ORIGIN, Direction.DOWN));
        }
        if (!hotbarSlots.isEmpty()) {
            client.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(originalSlot));
        }
        if (handler instanceof PlayerScreenHandler && allSlots.contains(45)) {
            client.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
            client.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.DROP_ALL_ITEMS, BlockPos.ORIGIN, Direction.DOWN));
        }
        if (allSlots.isEmpty()) {
            return;
        }
        Thread thread = new Thread(() -> {
            for (int slotId : allSlots) {
                if (client.currentScreen != screen || client.player == null || client.interactionManager == null
                    || client.player.currentScreenHandler != handler) {
                    break;
                }
                int id = slotId;
                client.execute(() -> {
                    if (client.player == null || client.interactionManager == null
                        || client.player.currentScreenHandler != handler
                        || handler.getSlot(id).getStack().isEmpty()) {
                        return;
                    }
                    client.interactionManager.clickSlot(handler.syncId, id, 1, SlotActionType.THROW, client.player);
                });
                try {
                    Thread.sleep(DELAY_MS);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "Expensive-FullDropAll");
        thread.setDaemon(true);
        thread.start();
    }

    private static boolean dropAllViaChain(MinecraftClient client, ClientPlayerEntity player, ScreenHandler handler,
        Screen screen) {
        if (!(handler instanceof PlayerScreenHandler)) {
            return false;
        }
        UserConnection connection;
        boolean pace;
        try {
            connection = ProtocolTranslator.getPlayNetworkUserConnection();
            if (connection == null
                || !connection.getProtocolInfo().getPipeline().contains(Protocol1_21_2To1_21_4.class)) {
                return false;
            }
            PacketWrapper preflight = PacketWrapper.create(ServerboundPackets1_21_2.PICK_ITEM, connection);
            preflight.write(Types.VAR_INT, 0);
            pace = connection.getProtocolInfo().protocolVersion().compareTo(ProtocolVersion.v1_17) >= 0;
        } catch (Throwable t) {
            return false;
        }
        List<Integer> throwSlots = new ArrayList<>();
        for (int i = 0; i < handler.slots.size(); i++) {
            if (handler.slots.get(i).getStack().isEmpty()) {
                continue;
            }
            if (i < 9) {
                throwSlots.add(i);
                continue;
            }
            sendDropAll(client, true);
            sendPickItem(connection, i > 35 && i <= 45 ? i - 36 : i);
        }
        sendDropAll(client, true);
        sendPickItem(connection, 0);
        if (!player.isSpectator()) {
            if (!player.getAbilities().creativeMode) {
                sendDropAll(client, false);
            }
            sendDropAll(client, true);
            throwSlots.add(45);
        }
        if (throwSlots.isEmpty()) {
            return true;
        }
        if (!pace) {
            for (int slot : throwSlots) {
                clickThrow(client, handler, screen, slot);
            }
            return true;
        }
        Thread thread = new Thread(() -> {
            for (int slot : throwSlots) {
                if (client.currentScreen != screen || client.player == null || client.interactionManager == null
                    || client.player.currentScreenHandler != handler) {
                    break;
                }
                client.execute(() -> clickThrow(client, handler, screen, slot));
                try {
                    Thread.sleep(DELAY_MS);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "Expensive-FullDropAll");
        thread.setDaemon(true);
        thread.start();
        return true;
    }

    private static void sendDropAll(MinecraftClient client, boolean swing) {
        client.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
            PlayerActionC2SPacket.Action.DROP_ALL_ITEMS, BlockPos.ORIGIN, Direction.DOWN));
        if (swing) {
            client.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
    }

    private static void sendPickItem(UserConnection connection, int inventoryIndex) {
        try {
            PacketWrapper wrapper = PacketWrapper.create(ServerboundPackets1_21_2.PICK_ITEM, connection);
            wrapper.write(Types.VAR_INT, inventoryIndex);
            wrapper.scheduleSendToServer(Protocol1_21_2To1_21_4.class);
        } catch (Throwable ignored) {
        }
    }

    private static void clickThrow(MinecraftClient client, ScreenHandler handler, Screen screen, int slot) {
        if (client.currentScreen != screen || client.player == null || client.interactionManager == null
            || client.player.currentScreenHandler != handler || handler.getSlot(slot).getStack().isEmpty()) {
            return;
        }
        client.interactionManager.clickSlot(handler.syncId, slot, 1, SlotActionType.THROW, client.player);
    }

    private static int containerSize(ScreenHandler handler) {
        if (handler instanceof GenericContainerScreenHandler) {
            return ((GenericContainerScreenHandler) handler).getInventory().size();
        }
        return 0;
    }

    private static int hotbarInventoryIndex(ScreenHandler handler, int containerSize, int slot) {
        if (handler instanceof PlayerScreenHandler) {
            return slot >= 36 && slot <= 44 ? slot - 36 : -1;
        }
        if (handler instanceof GenericContainerScreenHandler) {
            return slot >= containerSize + 27 && slot < containerSize + 36 ? slot - containerSize - 27 : -1;
        }
        return -1;
    }
}
