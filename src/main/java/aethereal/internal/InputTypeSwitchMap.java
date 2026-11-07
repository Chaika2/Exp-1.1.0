package aethereal.internal;

import net.minecraft.client.util.InputUtil;

public class InputTypeSwitchMap {
    public static final int[] typeSwitchMap = new int[InputUtil.Type.values().length];

    static {
        try {
            typeSwitchMap[InputUtil.Type.KEYSYM.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            typeSwitchMap[InputUtil.Type.MOUSE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
    }
}
