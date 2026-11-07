package aethereal.internal;

import net.minecraft.world.Difficulty;

public class DifficultyIndexMap {
    public static final int[] difficultyOrdinals = new int[Difficulty.values().length];

    static {
        try {
            difficultyOrdinals[Difficulty.PEACEFUL.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            difficultyOrdinals[Difficulty.EASY.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            difficultyOrdinals[Difficulty.HARD.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
    }
}
