package aethereal.util;
import aethereal.model.PriorityTaskEntry;

import java.util.PriorityQueue;

public class PriorityTaskQueue<T> {
    public int tickCounter = 0;
    public PriorityQueue<PriorityTaskEntry<T>> activeTasks = new PriorityQueue<>((class194Var, class194Var2) -> {
        return Integer.compare(class194Var2.priority, class194Var.priority);
    });

    public void tick(int i) {
        this.tickCounter += i;
    }

    public void addTask(PriorityTaskEntry<T> class194Var) {
        this.activeTasks.removeIf(class194Var2 -> {
            return class194Var2.module.equals(class194Var.module);
        });
        class194Var.expiresAt += this.tickCounter;
        this.activeTasks.add(class194Var);
    }

    public T fetchActiveTaskValue() {
        while (!this.activeTasks.isEmpty() && this.activeTasks.peek() != null && (this.activeTasks.peek().expiresAt <= this.tickCounter || !this.activeTasks.peek().module.isState())) {
            this.activeTasks.poll();
        }
        if (this.activeTasks.isEmpty() || this.activeTasks.peek() == null) {
            return null;
        }
        return (T) this.activeTasks.peek().module;
    }
}
