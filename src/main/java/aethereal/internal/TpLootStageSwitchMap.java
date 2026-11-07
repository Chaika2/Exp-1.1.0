package aethereal.internal;
import aethereal.type.TpLootStage;

public class TpLootStageSwitchMap {
    public static final int[] ordinalMap = new int[TpLootStage.values().length];

    static {
        try {
            ordinalMap[TpLootStage.APPROACH.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            ordinalMap[TpLootStage.LOADING_CHUNKS.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            ordinalMap[TpLootStage.LOOTING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            ordinalMap[TpLootStage.UNLOADING.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
    }
}
