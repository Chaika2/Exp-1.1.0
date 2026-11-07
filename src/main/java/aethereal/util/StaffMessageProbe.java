package aethereal.util;

import aethereal.Expensive;
import aethereal.event.ClientListener;
import aethereal.event.PacketReceiveEvent;
import aethereal.type.Mc;

import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class StaffMessageProbe implements ClientListener {
    private static final String PROBE_MESSAGE = "1";
    private static final long TIMEOUT_MS = 4000L;

    private static volatile String pendingNick = null;
    private static volatile long pendingId = 0L;

    public StaffMessageProbe() {
        Expensive.INSTANCE.eventDispatcher().register(PacketReceiveEvent.class, this::onPacket);
    }

    public static void request(String str) {
        MinecraftClient minecraftClient = Mc.INSTANCE.getMinecraft();
        ClientPlayNetworkHandler networkHandler = minecraftClient.getNetworkHandler();
        if (networkHandler == null) {
            message("Нет соединения с сервером", Formatting.RED);
            return;
        }
        long j = pendingId + 1;
        pendingId = j;
        pendingNick = str;
        message("Проверяю " + str + " через /msg ...", Formatting.GRAY);
        networkHandler.sendChatCommand("msg " + str + " " + PROBE_MESSAGE);
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(TIMEOUT_MS);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
            if (pendingId == j && pendingNick != null) {
                String str2 = pendingNick;
                pendingNick = null;
                message("[" + str2 + "]: ОНЛАЙН и виден (сервер не вернул ошибку)", Formatting.GREEN);
            }
        }, "staff-msg-probe");
        thread.setDaemon(true);
        thread.start();
    }

    private void onPacket(PacketReceiveEvent packetReceiveEvent) {
        String str = pendingNick;
        if (str == null || !(packetReceiveEvent.getPacket() instanceof GameMessageS2CPacket)) {
            return;
        }
        String normalized = normalize(((GameMessageS2CPacket) packetReceiveEvent.getPacket()).content());
        if (normalized.contains("не найден") || normalized.contains("не найдена") || normalized.contains("не найдено")) {
            pendingNick = null;
            message("[" + str + "]: В ВАНИШЕ (сервер: игрок не найден)", Formatting.GOLD);
        } else if (normalized.contains("не в сети") || normalized.contains("не онлайн")) {
            pendingNick = null;
            message("[" + str + "]: НЕ В СЕТИ (оффлайн)", Formatting.RED);
        }
    }

    private static String normalize(Text text) {
        return text == null ? "" : text.getString().replaceAll("§.", "").toLowerCase(Locale.ROOT).trim();
    }

    private static void message(String str, Formatting formatting) {
        MinecraftClient minecraftClient = Mc.INSTANCE.getMinecraft();
        if (minecraftClient != null) {
            minecraftClient.execute(() -> ChatUtil.addChatMessage((Text) Text.literal(str).formatted(formatting)));
        }
    }
}
