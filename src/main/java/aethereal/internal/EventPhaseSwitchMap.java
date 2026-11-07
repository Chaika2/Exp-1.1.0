package aethereal.internal;
import aethereal.type.EventPhase;

public class EventPhaseSwitchMap {
    public static final int[] phaseOrdinals = new int[EventPhase.values().length];

    static {
        try {
            phaseOrdinals[EventPhase.PRE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            phaseOrdinals[EventPhase.POST.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
    }
}
