package aethereal.util;

import aethereal.Expensive;
import aethereal.event.ClientListener;
import aethereal.event.PacketReceiveEvent;
import aethereal.type.Mc;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

public final class StaffPacketDebug implements ClientListener {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final Set<String> NOISE = Set.of(
            "CommonPingS2CPacket", "WorldTimeUpdateS2CPacket", "EntityTrackerUpdateS2CPacket",
            "EntityVelocityUpdateS2CPacket", "EntitySetHeadYawS2CPacket", "EntityPositionSyncS2CPacket",
            "MoveRelative", "RotateAndMoveRelative", "ParticleS2CPacket", "ChunkLoadS2CPacket",
            "UnloadChunkS2CPacket", "ChunkDeltaUpdateS2CPacket", "LightUpdateS2CPacket", "EntityAnimationS2CPacket",
            "BlockUpdateS2CPacket", "BlockEventS2CPacket");

    private static volatile boolean enabled = false;
    private static PrintWriter writer;

    public StaffPacketDebug() {
        Expensive.INSTANCE.eventDispatcher().register(PacketReceiveEvent.class, this::onPacket);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static synchronized boolean toggle() {
        setEnabled(!enabled);
        return enabled;
    }

    public static synchronized void setEnabled(boolean value) {
        if (value == enabled) {
            return;
        }
        enabled = value;
        if (value) {
            openWriter();
        } else {
            closeWriter();
        }
    }

    public static Path getLogPath() {
        return FabricLoader.getInstance().getGameDir().resolve("staff-packets.log");
    }

    private static void openWriter() {
        closeWriter();
        try {
            Path path = getLogPath();
            writer = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE), true);
            writer.println("=== Staff packet capture started " + LocalTime.now().format(TIME_FORMAT) + " ===");
            ClientPlayNetworkHandler networkHandler = Mc.INSTANCE.getNetworkHandler();
            if (networkHandler != null) {
                writer.println("brand=" + networkHandler.getBrand());
            }
            writer.println("file=" + path.toAbsolutePath());
            writer.println();
            writeTabSnapshot("initial");
        } catch (Exception exception) {
            Expensive.LOGGER.error("Failed to open staff packet log", exception);
            writer = null;
            enabled = false;
        }
    }

    private static void closeWriter() {
        if (writer != null) {
            writer.println("=== Staff packet capture stopped " + LocalTime.now().format(TIME_FORMAT) + " ===");
            writer.flush();
            writer.close();
            writer = null;
        }
    }

    private void onPacket(PacketReceiveEvent event) {
        if (!enabled || writer == null) {
            return;
        }
        try {
            Packet<?> packet = event.getPacket();
            String time = LocalTime.now().format(TIME_FORMAT);
            if (packet instanceof PlayerListS2CPacket playerListPacket) {
                logPlayerList(time, playerListPacket);
                return;
            }
            if (packet instanceof PlayerRemoveS2CPacket playerRemovePacket) {
                logLine(time + " PlayerRemove uuids=" + playerRemovePacket.profileIds());
                scheduleTabSnapshot("after remove");
                return;
            }
            if (packet instanceof TeamS2CPacket teamPacket) {
                logTeam(time, teamPacket);
                return;
            }
            if (packet instanceof PlayerListHeaderS2CPacket headerPacket) {
                logLine(time + " TAB-HEADER header='" + clean(headerPacket.header()) + "'");
                logLine("           footer='" + clean(headerPacket.footer()) + "'");
                return;
            }
            if (packet instanceof EntitySpawnS2CPacket spawnPacket) {
                boolean player = spawnPacket.getEntityType() == EntityType.PLAYER;
                logLine(time + " EntitySpawn" + (player ? " [PLAYER]" : "") + " id=" + spawnPacket.getEntityId() + " uuid=" + spawnPacket.getUuid() + " type=" + spawnPacket.getEntityType());
                if (player) {
                    logPlayerPresence(spawnPacket.getUuid());
                }
                return;
            }
            if (packet instanceof EntitiesDestroyS2CPacket destroyPacket) {
                logLine(time + " EntitiesDestroy ids=" + destroyPacket.getEntityIds());
                return;
            }
            if (packet instanceof GameMessageS2CPacket messagePacket) {
                logLine(time + " Chat" + (messagePacket.overlay() ? "(actionbar)" : "") + ": " + clean(messagePacket.content()));
                return;
            }
            if (packet instanceof CommandSuggestionsS2CPacket suggestionsPacket) {
                logSuggestions(time, suggestionsPacket);
                return;
            }
            String simple = packet.getClass().getSimpleName();
            if (!NOISE.contains(simple)) {
                logLine(time + " " + simple);
            }
        } catch (Exception exception) {
            logLine("!! failed to log packet: " + exception);
        }
    }

    private void logPlayerList(String time, PlayerListS2CPacket packet) {
        logLine(time + " PlayerList actions=" + packet.getActions() + " entries=" + packet.getEntries().size());
        Set<UUID> listedUuids = getListedUuids();
        Set<UUID> knownUuids = getKnownUuids();
        boolean addAction = packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER);
        for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
            UUID id = entry.profileId();
            String name = entry.profile() != null ? entry.profile().getName() : resolveName(id);
            boolean listed = id != null && listedUuids.contains(id);
            StringBuilder flags = new StringBuilder();
            if (!addAction) {
                if (id == null) {
                    flags.append(" [NO-UUID]");
                } else if (!knownUuids.contains(id)) {
                    flags.append(" [UNKNOWN-UUID]");
                }
                if (!listed) {
                    flags.append(" [NOT-IN-LISTED-MAP]");
                }
            }
            if (entry.gameMode() == GameMode.SPECTATOR) {
                flags.append(" [SPECTATOR]");
            }
            if (!entry.listed() && packet.getActions().contains(PlayerListS2CPacket.Action.UPDATE_LISTED)) {
                flags.append(" [UPDATE_LISTED=false]");
            }
            logLine("    - " + (name == null ? "<unknown:" + id + ">" : name)
                    + " uuid=" + id
                    + " listed=" + entry.listed()
                    + flags
                    + " display=" + (entry.displayName() == null ? "null" : clean(entry.displayName()))
                    + " gamemode=" + entry.gameMode()
                    + " latency=" + entry.latency());
            if (!addAction && id != null && !knownUuids.contains(id) && (entry.gameMode() == GameMode.SPECTATOR || !entry.listed())) {
                logLine("    !! VANISH-JOIN-CANDIDATE: update-only packet for unknown uuid " + id + " gamemode=" + entry.gameMode() + " listed=" + entry.listed());
            }
        }
        scheduleTabSnapshot("after playerlist");
    }

    private void logTeam(String time, TeamS2CPacket packet) {
        StringBuilder builder = new StringBuilder();
        builder.append(time).append(" Team name=").append(packet.getTeamName())
                .append(" op=").append(packet.getTeamOperation())
                .append(" players=").append(packet.getPlayerNames());
        packet.getTeam().ifPresent(team -> builder.append(" prefix='").append(clean(team.getPrefix())).append("'"));
        logLine(builder.toString());
    }

    private void logPlayerPresence(UUID uuid) {
        ClientPlayNetworkHandler networkHandler = Mc.INSTANCE.getNetworkHandler();
        boolean inTab = false;
        if (networkHandler != null) {
            for (PlayerListEntry entry : networkHandler.getPlayerList()) {
                if (entry.getProfile().getId().equals(uuid)) {
                    inTab = true;
                    break;
                }
            }
        }
        logLine("    " + (inTab ? "player is in player-list" : "!! VANISH-CANDIDATE: player entity NOT in player-list: " + uuid));
    }

    private static Set<UUID> getListedUuids() {
        Set<UUID> set = new HashSet<>();
        ClientPlayNetworkHandler networkHandler = Mc.INSTANCE.getNetworkHandler();
        if (networkHandler != null) {
            for (PlayerListEntry entry : networkHandler.getListedPlayerListEntries()) {
                set.add(entry.getProfile().getId());
            }
        }
        return set;
    }

    private static Set<UUID> getKnownUuids() {
        Set<UUID> set = new HashSet<>();
        ClientPlayNetworkHandler networkHandler = Mc.INSTANCE.getNetworkHandler();
        if (networkHandler != null) {
            for (PlayerListEntry entry : networkHandler.getPlayerList()) {
                if (entry.getProfile() != null) {
                    set.add(entry.getProfile().getId());
                }
            }
        }
        return set;
    }

    private static Set<String> getListedNamesLower() {
        Set<String> set = new HashSet<>();
        ClientPlayNetworkHandler networkHandler = Mc.INSTANCE.getNetworkHandler();
        if (networkHandler != null) {
            for (PlayerListEntry entry : networkHandler.getListedPlayerListEntries()) {
                if (entry.getProfile() != null && entry.getProfile().getName() != null) {
                    set.add(entry.getProfile().getName().toLowerCase(Locale.ROOT));
                }
            }
        }
        return set;
    }

    private static void logSuggestions(String time, CommandSuggestionsS2CPacket packet) {
        List<CommandSuggestionsS2CPacket.Suggestion> list = packet.suggestions();
        List<String> texts = new ArrayList<>();
        for (CommandSuggestionsS2CPacket.Suggestion suggestion : list) {
            texts.add(suggestion.text());
        }
        logLine(time + " Suggestions id=" + packet.id() + " count=" + list.size() + " -> " + texts);
        Set<String> tab = getListedNamesLower();
        List<String> extras = new ArrayList<>();
        for (String text : texts) {
            if (!tab.contains(text.toLowerCase(Locale.ROOT))) {
                extras.add(text);
            }
        }
        if (!extras.isEmpty()) {
            logLine("    !! SUGGESTION-NOT-IN-TAB (possible vanished staff): " + extras);
        }
    }

    private static String resolveName(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        ClientPlayNetworkHandler networkHandler = Mc.INSTANCE.getNetworkHandler();
        if (networkHandler != null) {
            for (PlayerListEntry entry : networkHandler.getPlayerList()) {
                if (entry.getProfile() != null && uuid.equals(entry.getProfile().getId())) {
                    return entry.getProfile().getName();
                }
            }
        }
        return null;
    }

    private static void scheduleTabSnapshot(String reason) {
        try {
            Mc.INSTANCE.getMinecraft().execute(() -> writeTabSnapshot(reason));
        } catch (Exception exception) {
            writeTabSnapshot(reason);
        }
    }

    private static void writeTabSnapshot(String reason) {
        if (writer == null) {
            return;
        }
        ClientPlayNetworkHandler networkHandler = Mc.INSTANCE.getNetworkHandler();
        if (networkHandler == null) {
            return;
        }
        Collection<PlayerListEntry> all = networkHandler.getPlayerList();
        Set<UUID> listedUuids = getListedUuids();
        logLine("    [tab " + reason + "] total=" + all.size());
        for (PlayerListEntry entry : List.copyOf(all)) {
            try {
                boolean listed = listedUuids.contains(entry.getProfile().getId());
                logLine("        " + (listed ? "  " : "!!") + " " + entry.getProfile().getName() + " " + entry.getProfile().getId());
            } catch (Exception entryException) {
                logLine("        ??? <entry failed: " + entryException + ">");
            }
        }
    }

    private static String clean(Text text) {
        return text == null ? "null" : text.getString().replaceAll("§.", "");
    }

    private static void logLine(String line) {
        if (writer != null) {
            writer.println(line);
        }
    }
}
