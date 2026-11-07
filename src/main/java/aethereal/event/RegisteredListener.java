package aethereal.event;
import aethereal.type.EventPriority;


public final class RegisteredListener<T extends Event> {
    public final EventCallback<T> callback;

    public final EventPriority priority;

    public RegisteredListener(EventCallback<T> class058Var, EventPriority class396Var) {
        this.callback = class058Var;
        this.priority = class396Var;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "callback=" + this.callback + ", " + "priority=" + this.priority + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.callback, this.priority);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RegisteredListener)) return false;
        RegisteredListener o = (RegisteredListener) obj;
        return java.util.Objects.equals(this.callback, o.callback) && java.util.Objects.equals(this.priority, o.priority);
    }
public EventCallback<T> callback() {
        return this.callback;
    }

    public EventPriority priority() {
        return this.priority;
    }
}
