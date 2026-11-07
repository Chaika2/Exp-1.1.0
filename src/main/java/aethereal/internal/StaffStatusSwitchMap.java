package aethereal.internal;
import aethereal.type.StaffStatus;

public class StaffStatusSwitchMap {
    public static final int[] statusSwitchMap = new int[StaffStatus.values().length];

    static {
        try {
            statusSwitchMap[StaffStatus.SPEC.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            statusSwitchMap[StaffStatus.PLAYING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            statusSwitchMap[StaffStatus.VANISH.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
    }
}
