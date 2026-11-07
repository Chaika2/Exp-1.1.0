package aethereal.util;
import aethereal.model.PriorityValueEntry;

import java.util.PriorityQueue;

public class PriorityValueQueue<T> {
    public int tickCounter = 0;
    public final PriorityQueue<PriorityValueEntry<T>> queue = new PriorityQueue<>((class413Var, class413Var2) -> {
        return Integer.compare(class413Var2.priority, class413Var.priority);
    });

    public void tick(int i) {
        this.tickCounter += i;
    }

    public void addTask(PriorityValueEntry<T> class413Var) {
        this.queue.removeIf(class413Var2 -> {
            return class413Var2.provider == class413Var.provider;
        });
        ((PriorityValueEntry) class413Var).expiresIn += this.tickCounter;
        this.queue.add(class413Var);
    }

    public T getValue() {
        while (!this.queue.isEmpty()) {
            PriorityValueEntry<T> class413VarPeek = this.queue.peek();
            if (((PriorityValueEntry<T>) class413VarPeek).expiresIn > this.tickCounter) {
                return ((PriorityValueEntry<T>) class413VarPeek).value;
            }
            this.queue.poll();
        }
        return null;
    }
}
