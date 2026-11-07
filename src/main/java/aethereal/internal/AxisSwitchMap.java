package aethereal.internal;

import net.minecraft.util.math.Direction;

public class AxisSwitchMap {
    public static final int[] switchMap = new int[Direction.Axis.values().length];

    static {
        try {
            switchMap[Direction.Axis.X.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            switchMap[Direction.Axis.Y.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            switchMap[Direction.Axis.Z.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
    }
}
