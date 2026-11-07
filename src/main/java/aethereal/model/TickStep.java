package aethereal.model;
import aethereal.type.PerformAction;

import java.util.function.BooleanSupplier;

public class TickStep implements Comparable<TickStep> {
    public final int ticks;
    public final PerformAction action;
    public final BooleanSupplier condition;
    public final int priority;

    public TickStep() {
        this.ticks = 0;
        this.action = null;
        this.condition = null;
        this.priority = 0;
    }

    public TickStep(int i, PerformAction class062Var, BooleanSupplier booleanSupplier, int i2) {
        this.ticks = i;
        this.action = class062Var;
        this.condition = booleanSupplier;
        this.priority = i2;
    }

    @Override
    public int compareTo(TickStep o) {
        return Integer.compare(this.priority, o.priority);
    }

    public PerformAction action() {
        return this.action;
    }

    public BooleanSupplier condition() {
        return this.condition;
    }

    public int ticks() {
        return this.ticks;
    }
}
