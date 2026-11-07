package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.ui.setting.BooleanSetting;
import aethereal.Expensive;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.ModuleTab;
import aethereal.type.NotificationType;
import aethereal.event.PacketReceiveEvent;
import aethereal.event.PlayerInitEvent;
import aethereal.event.PlayerTickEvent;
import aethereal.type.ReallyWorldServer;
import aethereal.util.ServerUtil;

import com.mojang.authlib.GameProfile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;

@Aliases(aliases = {"Anti Bot", "Bot Detector", "Bot Blocker", "Detect Bots", "Block Bots"})
public class AntiBotModule extends Module {
    public final BooleanSetting removeFromWorld;
    public static final String validNameChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
    public final Mc mc;
    static Set<UUID> botUuids = ConcurrentHashMap.newKeySet();

    static Set<UUID> botsToRemoveFromWorld = ConcurrentHashMap.newKeySet();

    public AntiBotModule() {
        super(ModuleTab.COMBAT, "Anti Bot");
        this.removeFromWorld = new BooleanSetting(Lang.ANTIBOT_REMOVE_FROM_WORLD);
        this.mc = Mc.INSTANCE;
        addSettings(new ModeSetting(Lang.MODE).values(ReallyWorldServer.class), this.removeFromWorld);
        register(PlayerInitEvent.class, class125Var -> {
            reset();
        });
        register(PlayerTickEvent.class, class130Var -> {
            if (class130Var.isPre() && isState() && this.mc.isWorldLoaded() && ServerUtil.isConnectedToServer("reallyworld")) {
                ClientWorld world = this.mc.getWorld();
                if (!this.removeFromWorld.isValue() || botUuids.isEmpty()) {
                    return;
                }
                for (AbstractClientPlayerEntity clientPlayerEntity : new ArrayList<AbstractClientPlayerEntity>(world.getPlayers())) {
                    if (clientPlayerEntity != this.mc.getPlayer() && botsToRemoveFromWorld.contains(clientPlayerEntity.getUuid())) {
                        Expensive.INSTANCE.notificationRepository().post(NotificationType.SUCCESS, (Text) Text.literal("Бот %s удален из мира.".formatted(clientPlayerEntity.getName().getString())), 3L, TimeUnit.SECONDS);
                        world.removeEntity(clientPlayerEntity.getId(), Entity.RemovalReason.DISCARDED);
                    }
                }
            }
        });
        register(PacketReceiveEvent.class, class051Var -> {
            if (isState() && this.mc.isWorldLoaded() && ServerUtil.isConnectedToServer("reallyworld")) {
                Packet<?> packet = class051Var.getPacket();
                if (!(packet instanceof PlayerListS2CPacket)) {
                    if (packet instanceof PlayerRemoveS2CPacket) {
                        handlePlayerRemovePacket((PlayerRemoveS2CPacket) packet);
                    }
                } else {
                    PlayerListS2CPacket playerListS2CPacket = (PlayerListS2CPacket) packet;
                    if (playerListS2CPacket.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) {
                        handlePlayerListPacket(playerListS2CPacket);
                    }
                }
            }
        });
    }

    public boolean isDuplicateProfile(GameProfile gameProfile) {
        ClientPlayNetworkHandler networkHandler = Mc.INSTANCE.getNetworkHandler();
        if (networkHandler == null) {
            return false;
        }
        try {
            return List.copyOf(networkHandler.getPlayerList()).stream().filter(playerListEntry -> {
                return playerListEntry.getProfile().getName().equals(gameProfile.getName()) && !playerListEntry.getProfile().getId().equals(gameProfile.getId());
            }).count() == 1;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isBot(UUID uuid, boolean z) {
        if (isMissingFromPlayerList(uuid) && z && ((AntiBotModule) Expensive.INSTANCE.moduleRepository().get(AntiBotModule.class)).isState()) {
            return true;
        }
        return botUuids.contains(uuid);
    }

    public static boolean isBot(LivingEntity livingEntity) {
        if (livingEntity instanceof PlayerEntity) {
            return isBot(livingEntity.getUuid(), true);
        }
        return false;
    }

    public void reset() {
        botUuids.clear();
        botsToRemoveFromWorld.clear();
    }

    public void handlePlayerListPacket(PlayerListS2CPacket playerListS2CPacket) {
        Scoreboard scoreboard = MinecraftClient.getInstance().world.getScoreboard();
        UUID id = this.mc.getPlayer().getGameProfile().getId();
        playerListS2CPacket.getEntries().forEach(entry -> {
            GameProfile gameProfileProfile = entry.profile();
            if (gameProfileProfile == null || isLegitProfile(gameProfileProfile) || gameProfileProfile.getId().equals(id)) {
                return;
            }
            Team scoreHolderTeam = scoreboard.getScoreHolderTeam(gameProfileProfile.getId().toString());
            if (scoreHolderTeam == null || scoreHolderTeam.getPrefix() == null || !scoreHolderTeam.getPrefix().getString().contains("●")) {
                botUuids.add(gameProfileProfile.getId());
                botsToRemoveFromWorld.add(gameProfileProfile.getId());
            }
        });
    }

    public void handlePlayerRemovePacket(PlayerRemoveS2CPacket playerRemoveS2CPacket) {
        playerRemoveS2CPacket.profileIds().forEach(uuid -> {
            botUuids.remove(uuid);
        });
    }

    public boolean isLegitProfile(GameProfile gameProfile) {
        if (gameProfile.getProperties() == null || gameProfile.getProperties().isEmpty()) {
            return isInvalidName(gameProfile.getName());
        }
        return true;
    }

    public static boolean isMissingFromPlayerList(UUID uuid) {
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler == null) {
            return false;
        }
        try {
            return List.copyOf(networkHandler.getPlayerList()).stream().noneMatch(playerListEntry -> {
                return playerListEntry.getProfile().getId().equals(uuid);
            });
        } catch (Exception e) {
            return false;
        }
    }

    public UUID getOfflineUuid(String str) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + str).getBytes(StandardCharsets.UTF_8));
    }

    public boolean isInvalidName(String str) {
        if (str.length() < 3 || str.length() > 16) {
            return true;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!validNameChars.contains(String.valueOf(str.charAt(i)))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void deactivate() {
        reset();
        super.deactivate();
    }
}
