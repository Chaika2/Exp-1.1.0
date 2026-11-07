package aethereal.util;
import aethereal.module.Module;

import java.util.PriorityQueue;

public class TickScheduler<T> {
    public int tickCounter = 0;
    public final PriorityQueue<ScheduledTask<T>> activeTasks = new PriorityQueue<>((class411Var, class411Var2) -> {
        return Integer.compare(class411Var2.priority, class411Var.priority);
    });

    public void tick(int i) {
        this.tickCounter += i;
    }

    public void addTask(ScheduledTask<T> class411Var) {
        this.activeTasks.removeIf(class411Var2 -> {
            return class411Var2.provider.equals(class411Var.provider);
        });
        ((ScheduledTask) class411Var).expiresIn += this.tickCounter;
        this.activeTasks.add(class411Var);
    }

    public T getValue() {
        while (!this.activeTasks.isEmpty() && this.activeTasks.peek() != null && (((ScheduledTask) this.activeTasks.peek()).expiresIn <= this.tickCounter || !((ScheduledTask) this.activeTasks.peek()).provider.isState())) {
            this.activeTasks.poll();
        }
        if (this.activeTasks.isEmpty() || this.activeTasks.peek() == null) {
            return null;
        }
        return ((ScheduledTask<T>) this.activeTasks.peek()).value;
    }

    public void cancel(Module class605Var) {
        this.activeTasks.removeIf(class411Var -> {
            return class411Var.provider == class605Var;
        });
    }
}
