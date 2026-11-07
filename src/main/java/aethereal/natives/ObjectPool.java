package aethereal.natives;
import aethereal.util.ObjectFactory;

import java.util.ArrayDeque;
import java.util.Queue;

public class ObjectPool<T> {
    public final Queue<T> pool = new ArrayDeque();

    public final ObjectFactory<T> factory;

    public synchronized T get() {
        return !this.pool.isEmpty() ? this.pool.poll() : (T) this.factory.create();
    }

    public synchronized void free(T t) {
        this.pool.offer(t);
    }

    public ObjectPool(ObjectFactory<T> class379Var) {
        this.factory = class379Var;
    }
}
