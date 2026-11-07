package aethereal.internal;
import aethereal.type.BlockUpdateType;

public class BlockUpdateTypeSwitchMap {
    public static final int[] switchMap = new int[BlockUpdateType.values().length];

    static {
        try {
            switchMap[BlockUpdateType.LOAD.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            switchMap[BlockUpdateType.UPDATE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            switchMap[BlockUpdateType.UNLOAD.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
    }
}
