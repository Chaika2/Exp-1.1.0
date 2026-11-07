package aethereal.internal;
import aethereal.ui.ModuleTab;

public class ModuleTabSwitchMap {
    public static final int[] tabSwitchMap = new int[ModuleTab.values().length];

    static {
        try {
            tabSwitchMap[ModuleTab.COMBAT.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            tabSwitchMap[ModuleTab.MOVEMENT.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            tabSwitchMap[ModuleTab.PLAYER.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            tabSwitchMap[ModuleTab.RENDER.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            tabSwitchMap[ModuleTab.MISC.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
    }
}
