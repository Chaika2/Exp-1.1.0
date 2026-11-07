package aethereal.internal;
import aethereal.type.RotationDispatchMode;

public class RotationDispatchModeSwitchMap {
    public static final int[] dispatchModeSwitchMap = new int[RotationDispatchMode.values().length];

    static {
        try {
            dispatchModeSwitchMap[RotationDispatchMode.VANILLA.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            dispatchModeSwitchMap[RotationDispatchMode.GRIM.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            dispatchModeSwitchMap[RotationDispatchMode.DELAYED.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            dispatchModeSwitchMap[RotationDispatchMode.SEQUENTIAL.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
    }
}
