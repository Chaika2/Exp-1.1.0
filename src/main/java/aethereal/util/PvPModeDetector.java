package aethereal.util;
import aethereal.type.Mc;
import aethereal.math.Stopwatch;

public class PvPModeDetector {
    public static final Stopwatch pvpStopwatch = new Stopwatch();

    public static boolean isPvPMode() {
        return !pvpStopwatch.hasElapsed(500L);
    }

    public static void tick() {
        if (hasPvpBossBar()) {
            pvpStopwatch.reset();
        }
    }

    public static boolean hasPvpBossBar() {
        return Mc.INSTANCE.getInGameHud().getBossBarHud().bossBars.values().stream().map(clientBossBar -> {
            return clientBossBar.getName().getString().toLowerCase();
        }).anyMatch(str -> {
            return str.contains("pvp") || str.contains("пвп");
        });
    }
}
