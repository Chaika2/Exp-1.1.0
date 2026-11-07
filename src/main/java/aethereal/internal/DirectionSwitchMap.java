package aethereal.internal;

import net.minecraft.util.math.Direction;

public class DirectionSwitchMap {
    public static final int[] directionOrdinals = new int[Direction.values().length];

    static {
        try {
            directionOrdinals[Direction.WEST.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            directionOrdinals[Direction.EAST.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            directionOrdinals[Direction.SOUTH.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            directionOrdinals[Direction.NORTH.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
    }
}
