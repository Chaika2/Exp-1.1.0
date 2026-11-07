package aethereal.event;

import net.minecraft.client.gui.screen.Screen;

public class CloseScreenEvent extends CancellableEvent {
    public Screen screen;

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CloseScreenEvent)) {
            return false;
        }
        CloseScreenEvent class270Var = (CloseScreenEvent) obj;
        if (!class270Var.canEqual(this) || !super.equals(obj)) {
            return false;
        }
        Screen screen = getScreen();
        Screen screen2 = class270Var.getScreen();
        if (screen == null) {
            return screen2 == null;
        }
        return screen.equals(screen2);
    }

    public boolean canEqual(Object obj) {
        return obj instanceof CloseScreenEvent;
    }

    public int hashCode() {
        int iHashCode = super.hashCode();
        Screen screen = getScreen();
        return (iHashCode * 59) + (screen == null ? 43 : screen.hashCode());
    }

    public Screen getScreen() {
        return this.screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    public String toString() {
        return "CloseScreenEvent(screen=" + String.valueOf(getScreen()) + ")";
    }

    public CloseScreenEvent(Screen screen) {
        this.screen = screen;
    }
}
