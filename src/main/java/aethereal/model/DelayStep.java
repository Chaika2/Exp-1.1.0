package aethereal.model;
import aethereal.type.PerformAction;

import java.util.function.BooleanSupplier;

public class DelayStep implements Comparable<DelayStep> {
    public final int delay;
    public final PerformAction action;
    public final BooleanSupplier condition;
    public final int priority;

    public DelayStep() {
        this.delay = 0;
        this.action = null;
        this.condition = null;
        this.priority = 0;
    }

    public DelayStep(int i, PerformAction class062Var, BooleanSupplier booleanSupplier, int i2) {
        this.delay = i;
        this.action = class062Var;
        this.condition = booleanSupplier;
        this.priority = i2;
    }

    @Override
    public int compareTo(DelayStep o) {
        return Integer.compare(this.priority, o.priority);
    }

    public PerformAction action() {
        return this.action;
    }

    public BooleanSupplier condition() {
        return this.condition;
    }

    public int delay() {
        return this.delay;
    }
}
