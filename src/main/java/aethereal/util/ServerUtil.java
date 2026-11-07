package aethereal.util;
import aethereal.Expensive;
import aethereal.type.Mc;
import aethereal.event.PacketReceiveEvent;
import aethereal.type.ServerType;
import aethereal.math.Stopwatch;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.session.Session;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import org.apache.commons.lang3.StringUtils;

public final class ServerUtil {
    public static int anarchy;
    public static String server = "Vanilla";
    public static Stopwatch timer = new Stopwatch();
    public static float TPS = 20.0f;

    public static void tick() {
        if (Mc.INSTANCE.isWorldLoaded()) {
            anarchy = parseAnarchyNumber();
            server = getServer();
        }
    }

    public static Optional<Integer> valid1_17() {
        return Optional.of(Integer.valueOf(getProtocolVersion())).filter(num -> {
            return Mc.INSTANCE.isSingleplayer() || (num.intValue() >= 755 && num.intValue() <= 769);
        });
    }

    public static void packet(PacketReceiveEvent class051Var) {
        if (class051Var.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            long elapsedTime = timer.getElapsedTime(TimeUnit.NANOSECONDS);
            if (elapsedTime > 0) {
                TPS = MathUtil.clamp(20.0f * (1.0E9f / elapsedTime), 0.0f, 20.0f);
            }
            timer.reset();
        }
    }

    

    public static int getProtocolVersion() {
        return 769;
    }

    public static String getWorldType() {
        return !Mc.INSTANCE.isWorldLoaded() ? "null" : Mc.INSTANCE.getWorld().getRegistryKey().getValue().getPath();
    }

    public static int parseAnarchyNumber() {
        String strSubstringBetween;
        Scoreboard scoreboard = Mc.INSTANCE.getWorld().getScoreboard();
        ScoreboardObjective objectiveForSlot = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        switch (server) {
            case "FunTime":
                if (objectiveForSlot == null) {
                    return -1;
                }
                String[] strArrSplit = objectiveForSlot.getDisplayName().getString().split("-");
                if (strArrSplit.length > 1) {
                    return Integer.parseInt(strArrSplit[1]);
                }
                return -1;
            case "HolyWorld":
                Pattern patternCompile = Pattern.compile("^(.*?)#(\\d+)(?:\\s*\\(1\\.20\\))?");
                for (ScoreboardEntry scoreboardEntry : scoreboard.getScoreboardEntries(objectiveForSlot)) {
                    String string = Team.decorateName(scoreboard.getScoreHolderTeam(scoreboardEntry.owner()), scoreboardEntry.name()).getString();
                    if (!string.isEmpty() && (strSubstringBetween = StringUtils.substringBetween(string, "-◆-", " -◆-")) != null && !strSubstringBetween.isEmpty()) {
                        Matcher matcher = patternCompile.matcher(strSubstringBetween.trim());
                        if (matcher.find()) {
                            return Integer.parseInt(matcher.group(2));
                        }
                    }
                }
                return -1;
            default:
                return -1;
        }
    }

    public static String getServer() {
        if (!Mc.INSTANCE.isWorldLoaded() || Mc.INSTANCE.getNetworkHandler().getBrand() == null || Mc.INSTANCE.getServerInfo() == null) {
            return "Vanilla";
        }
        String lowerCase = Mc.INSTANCE.getServerInfo().address.toLowerCase();
        String strRemoveSymbols = StringUtil.removeSymbols(Mc.INSTANCE.getNetworkHandler().getBrand().toLowerCase());
        if (strRemoveSymbols.contains("botfilter")) {
            return "FunTime";
        }
        if (lowerCase.contains("funtime") || lowerCase.contains("skytime") || lowerCase.contains("space-times") || lowerCase.contains("funsky")) {
            return "CopyTime";
        }
        if (strRemoveSymbols.contains("holywоrld") || strRemoveSymbols.contains("vk.com/idwok")) {
            return "HolyWorld";
        }
        if (lowerCase.contains("reallyworld") || strRemoveSymbols.contains("reallyworld")) {
            return "ReallyWorld";
        }
        if (lowerCase.contains("spookytime") || strRemoveSymbols.contains("spookytime")) {
            return "SpookyTime";
        }
        if (lowerCase.contains("aresmine") || strRemoveSymbols.contains("Folia (Velocity)")) {
            return "AresMine";
        }
        if (lowerCase.contains("cakeworld") || strRemoveSymbols.contains("cakeworld")) {
            return "RwCopy";
        }
        return (lowerCase.contains("dexland") || lowerCase.contains("mineblaze")) ? "DexLand" : "Vanilla";
    }

    public static boolean isConnectedToServer(String str) {
        return server.toLowerCase().contains(str);
    }

    public static boolean isConnectedToAllFuntimeServers() {
        return server.equalsIgnoreCase("funtime") || server.equalsIgnoreCase("copytime");
    }

    public static String getServerIp() {
        if (Mc.INSTANCE.isSingleplayer() || !Mc.INSTANCE.isWorldLoaded()) {
            return "localhost";
        }
        SocketAddress address = Mc.INSTANCE.getNetworkHandler().getConnection().getAddress();
        return address instanceof InetSocketAddress ? ((InetSocketAddress) address).getHostString() : "localhost";
    }

    public static void applySessionNickname(String str) {
        MinecraftClient minecraftClient;
        if (str == null || str.isBlank() || (minecraftClient = MinecraftClient.getInstance()) == null || minecraftClient.getSession() == null) {
            return;
        }
        Session session = minecraftClient.getSession();
        if (str.equals(session.getUsername())) {
            return;
        }
        minecraftClient.session = new Session(str, UUID.nameUUIDFromBytes(("OfflinePlayer:" + str).getBytes(StandardCharsets.UTF_8)), session.getAccessToken(), session.getXuid(), session.getClientId(), Session.AccountType.LEGACY);
    }

    public static void reconnectToServer() {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        if (minecraftClient == null || minecraftClient.isInSingleplayer()) {
            return;
        }
        ServerInfo currentServerEntry = minecraftClient.getCurrentServerEntry();
        if (currentServerEntry == null) {
            currentServerEntry = new ServerInfo("ReallyWorld", "mc.reallyworld.ru:25565", ServerInfo.ServerType.OTHER);
        }
        ServerAddress serverAddress = ServerAddress.parse(currentServerEntry.address);
        ServerInfo serverInfo = currentServerEntry;
        minecraftClient.execute(() -> {
            if (minecraftClient.currentScreen != null) {
                minecraftClient.currentScreen.close();
            }
            ConnectScreen.connect(minecraftClient.currentScreen, minecraftClient, serverAddress, serverInfo, false, (CookieStorage) null);
        });
    }

    public ServerUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static int getAnarchy() {
        return anarchy;
    }
}
