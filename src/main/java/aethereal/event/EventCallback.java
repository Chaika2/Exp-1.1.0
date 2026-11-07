package aethereal.event;


public interface EventCallback<T extends Event> {
    void call(T t);
}
