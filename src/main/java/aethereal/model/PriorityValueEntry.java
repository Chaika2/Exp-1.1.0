package aethereal.model;

public class PriorityValueEntry<T> {
    public int expiresIn;
    public final int priority;
    public final Object provider;
    public final T value;

    public int getExpiresIn() {
        return this.expiresIn;
    }

    public int getPriority() {
        return this.priority;
    }

    public Object getProvider() {
        return this.provider;
    }

    public T getValue() {
        return this.value;
    }

    public void setExpiresIn(int i) {
        this.expiresIn = i;
    }

    public PriorityValueEntry(int i, int i2, Object obj, T t) {
        this.expiresIn = i;
        this.priority = i2;
        this.provider = obj;
        this.value = t;
    }
}
