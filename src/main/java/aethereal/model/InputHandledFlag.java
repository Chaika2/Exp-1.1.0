package aethereal.model;

public final class InputHandledFlag {
    public boolean handled;

    public boolean handled() {
        return this.handled;
    }

    public void markHandled() {
        this.handled = true;
    }
}
