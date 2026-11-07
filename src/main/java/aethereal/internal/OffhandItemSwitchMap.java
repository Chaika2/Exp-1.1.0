package aethereal.internal;
import aethereal.type.AutoDuelOffhandItem;

public class OffhandItemSwitchMap {
    public static final int[] ordinalToCase = new int[AutoDuelOffhandItem.values().length];

    static {
        try {
            ordinalToCase[AutoDuelOffhandItem.TOTEM.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            ordinalToCase[AutoDuelOffhandItem.SPHERE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            ordinalToCase[AutoDuelOffhandItem.ANY.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
    }
}
