package aethereal.util;
import aethereal.math.Easings;
import aethereal.type.Mc;
import aethereal.model.StaffPlayerEntry;
import aethereal.type.StaffStatus;
import aethereal.event.TabListEntryEvent;
import aethereal.render.ToggleAnimator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import net.minecraft.world.GameMode;

public final class StaffDetector {
    public static Set<StaffPlayerEntry> staffPlayers = new LinkedHashSet();
    public static final Set<String> customStaffNames = new LinkedHashSet();
    public static final Pattern validNamePattern = Pattern.compile("^\\w{3,16}$");
    public static final Pattern staffKeywordPattern = Pattern.compile(".*(mod|der|adm|help|wne|хелп|адм|поддержка|кура|own|taf|curat|dev|supp|yt|staff|стафф|стаф|сотруд).*");
    public static final Map<String, AbstractTexture> headTextureCache = new HashMap();
    public static final Map<String, StaffMemory> staffMemory = new HashMap();
    public static final Set<String> spectatorNames = new LinkedHashSet();

    public static AbstractTexture defaultHeadTexture;

    public static void clearCaches() {
        headTextureCache.clear();
    }

    public static void clearStaffMemory() {
        staffMemory.clear();
        spectatorNames.clear();
    }

    public static void onPlayerListPacket(PlayerListS2CPacket playerListS2CPacket) {
        if (!playerListS2CPacket.getActions().contains(PlayerListS2CPacket.Action.UPDATE_GAME_MODE)) {
            return;
        }
        ClientPlayNetworkHandler networkHandler = Mc.INSTANCE.getNetworkHandler();
        for (PlayerListS2CPacket.Entry entry : playerListS2CPacket.getEntries()) {
            if (entry.gameMode() != GameMode.SPECTATOR) {
                continue;
            }
            UUID profileId = entry.profileId();
            String name = entry.profile() != null ? entry.profile().getName() : null;
            if (name == null && networkHandler != null && profileId != null) {
                for (PlayerListEntry playerListEntry : networkHandler.getPlayerList()) {
                    if (playerListEntry.getProfile() != null && profileId.equals(playerListEntry.getProfile().getId())) {
                        name = playerListEntry.getProfile().getName();
                        break;
                    }
                }
            }
            if (name != null) {
                spectatorNames.add(name.toLowerCase(Locale.ROOT));
            }
        }
    }

    public static void onPlayerListEvent(TabListEntryEvent class038Var) {
        if (Mc.INSTANCE.isWorldLoaded()) {
            PlayerListEntry playerListEntryCurrentEntry = class038Var.currentEntry();
            headTextureCache.put(playerListEntryCurrentEntry.getProfile().getName(), Mc.INSTANCE.getTextureManager().getTexture(playerListEntryCurrentEntry.getSkinTextures().texture()));
        }
    }

    public static AbstractTexture getDefaultHeadTexture() {
        if (defaultHeadTexture == null) {
            defaultHeadTexture = Mc.INSTANCE.getTextureManager().getTexture(Identifier.of("minecraft", "textures/entity/player/wide/steve.png"));
        }
        return defaultHeadTexture;
    }

    public static void update() {
        if (Mc.INSTANCE.isWorldLoaded()) {
            ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
            ClientWorld world = Mc.INSTANCE.getWorld();
            ClientPlayNetworkHandler networkHandler = Mc.INSTANCE.getNetworkHandler();
            HashSet<String> hashSet = new HashSet();
            HashMap map = new HashMap();
            Set set = (Set) networkHandler.getPlayerList().stream().map(playerListEntry -> {
                return playerListEntry.getProfile().getName().toLowerCase(Locale.ROOT);
            }).collect(HashSet::new, (v0, v1) -> {
                v0.add(v1);
            }, (v0, v1) -> {
                v0.addAll(v1);
            });
            HashMap gameModes = new HashMap();
            for (PlayerListEntry listEntry : networkHandler.getPlayerList()) {
                if (listEntry.getProfile() != null && listEntry.getProfile().getName() != null) {
                    gameModes.put(listEntry.getProfile().getName().toLowerCase(Locale.ROOT), listEntry.getGameMode());
                }
            }
            world.getScoreboard().getTeams().stream().sorted(Comparator.comparing((v0) -> {
                return v0.getName();
            })).forEach(team -> {
                String string = team.getPrefix().getString();
                if (StringHelper.stripTextFormat(string.replace("●", "")).trim().equalsIgnoreCase("d.helper")) {
                    return;
                }
                boolean zMethod008 = isStaffPrefix(string);
                team.getPlayerList().forEach(str -> {
                    boolean zIsCustomStaffName = isCustomStaffName(str);
                    if (zMethod008 || zIsCustomStaffName) {
                        boolean z = !set.contains(str.toLowerCase(Locale.ROOT));
                        if (!validNamePattern.matcher(str).matches() || str.equals(player.getName().getString())) {
                            return;
                        }
                        AbstractTexture orDefault = headTextureCache.getOrDefault(str, getDefaultHeadTexture());
                        StaffStatus class119Var = z ? StaffStatus.SPEC : StaffStatus.PLAYING;
                        hashSet.add(str);
                        map.put(str, new StaffPlayerEntry(orDefault, string, str, class119Var, new ToggleAnimator(220, Easings.EASE_IN_OUT_CUBIC)));
                    }
                });
            });
            for (String strLive : hashSet) {
                StaffPlayerEntry liveEntry = (StaffPlayerEntry) map.get(strLive);
                if (liveEntry != null) {
                    String lowerName = strLive.toLowerCase(Locale.ROOT);
                    GameMode gameMode = (GameMode) gameModes.get(lowerName);
                    if (gameMode != null && gameMode != GameMode.SPECTATOR) {
                        spectatorNames.remove(lowerName);
                    }
                    boolean spectator = gameMode == GameMode.SPECTATOR || spectatorNames.contains(lowerName);
                    staffMemory.put(strLive, new StaffMemory(liveEntry.prefix(), liveEntry.headTexture(), spectator));
                }
            }
            for (Map.Entry<String, StaffMemory> memoryEntry : staffMemory.entrySet()) {
                if (hashSet.contains(memoryEntry.getKey())) {
                    continue;
                }
                StaffMemory memory = memoryEntry.getValue();
                if (!memory.spectator() && !spectatorNames.contains(memoryEntry.getKey().toLowerCase(Locale.ROOT))) {
                    continue;
                }
                hashSet.add(memoryEntry.getKey());
                map.put(memoryEntry.getKey(), new StaffPlayerEntry(memory.headTexture(), memory.prefix(), memoryEntry.getKey(), StaffStatus.VANISH, new ToggleAnimator(220, Easings.EASE_IN_OUT_CUBIC)));
            }
            for (StaffPlayerEntry class118Var : staffPlayers) {
                if (!hashSet.contains(class118Var.name())) {
                    class118Var.stateAnimation().state(false);
                }
            }
            staffPlayers.removeIf(class118Var2 -> {
                return !hashSet.contains(class118Var2.name()) && class118Var2.stateAnimation().isZero();
            });
            for (String str : hashSet) {
                Optional<StaffPlayerEntry> optionalFindFirst = staffPlayers.stream().filter(class118Var3 -> {
                    return class118Var3.name().equals(str);
                }).findFirst();
                StaffPlayerEntry class118Var4 = (StaffPlayerEntry) map.get(str);
                if (optionalFindFirst.isPresent()) {
                    StaffPlayerEntry class118Var5 = optionalFindFirst.get();
                    class118Var5.stateAnimation().state(true);
                    if (class118Var5.status() != class118Var4.status()) {
                        staffPlayers.remove(class118Var5);
                        staffPlayers.add(new StaffPlayerEntry(class118Var4.headTexture(), class118Var4.prefix(), class118Var5.name(), class118Var4.status(), class118Var5.stateAnimation()));
                    }
                } else {
                    class118Var4.stateAnimation().state(true);
                    staffPlayers.add(class118Var4);
                }
            }
        }
    }

    public static boolean isStaffPrefix(String str) {
        return staffKeywordPattern.matcher(str.toLowerCase(Locale.ROOT)).matches();
    }

    public static boolean addCustomStaffName(String str) {
        return customStaffNames.add(str.toLowerCase(Locale.ROOT));
    }

    public static boolean removeCustomStaffName(String str) {
        return customStaffNames.remove(str.toLowerCase(Locale.ROOT));
    }

    public static void clearCustomStaffNames() {
        customStaffNames.clear();
    }

    public static Set<String> getCustomStaffNames() {
        return Set.copyOf(customStaffNames);
    }

    public static boolean isCustomStaffName(String str) {
        return customStaffNames.contains(str.toLowerCase(Locale.ROOT));
    }

    public static boolean isKnownStaffName(String str) {
        if (str == null) {
            return false;
        }
        if (isCustomStaffName(str)) {
            return true;
        }
        for (String str2 : staffMemory.keySet()) {
            if (str2.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    public static String cleanPrefix(String str) {
        return str == null ? "" : StringHelper.stripTextFormat(str.replace("⚡", "").replace("●", "")).trim();
    }

    public static int getRankPriority(StaffPlayerEntry class118Var) {
        switch (cleanPrefix(class118Var.prefix()).toLowerCase(Locale.ROOT)) {
            case "admin":
            case "админ":
                return 0;
            case "ml.admin":
                return 1;
            case "gl.moder":
                return 2;
            case "st.moder":
            case "s.moder":
                return 3;
            case "moder":
            case "модератор":
            case "куратор":
                return 4;
            case "j.moder":
                return 5;
            case "st.helper":
                return 6;
            case "helper+":
                return 7;
            case "helper":
                return 8;
            case "yt+":
                return 9;
            case "yt":
                return 10;
            default:
                return 11;
        }
    }

    public static List<StaffPlayerEntry> sortedStaff() {
        return staffPlayers.stream().sorted(Comparator.comparingInt(StaffDetector::getRankPriority).thenComparing((v0) -> {
            return v0.name();
        }, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    public static final class StaffMemory {
        private final String prefix;
        private final AbstractTexture headTexture;
        private final boolean spectator;

        public StaffMemory(String str, AbstractTexture abstractTexture, boolean z) {
            this.prefix = str;
            this.headTexture = abstractTexture;
            this.spectator = z;
        }

        public String prefix() {
            return this.prefix;
        }

        public AbstractTexture headTexture() {
            return this.headTexture;
        }

        public boolean spectator() {
            return this.spectator;
        }
    }

    public StaffDetector() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
