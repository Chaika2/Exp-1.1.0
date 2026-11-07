package aethereal.internal;

import net.minecraft.util.math.Direction;

public class DirectionIndexMap {
    public static final int[] directionOrdinals = new int[Direction.values().length];

    static {
        try {
            directionOrdinals[Direction.UP.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            directionOrdinals[Direction.WEST.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            directionOrdinals[Direction.EAST.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
    }
}
