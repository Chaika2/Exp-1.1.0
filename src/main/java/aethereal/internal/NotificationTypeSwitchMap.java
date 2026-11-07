package aethereal.internal;
import aethereal.type.NotificationType;

public class NotificationTypeSwitchMap {
    public static final int[] ordinalToCase = new int[NotificationType.values().length];

    static {
        try {
            ordinalToCase[NotificationType.INFO.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            ordinalToCase[NotificationType.SUCCESS.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            ordinalToCase[NotificationType.MODULE_ENABLED.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            ordinalToCase[NotificationType.WARNING.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            ordinalToCase[NotificationType.ERROR.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            ordinalToCase[NotificationType.MODULE_DISABLED.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            ordinalToCase[NotificationType.EVENT.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
    }
}
