package aethereal.event;

import net.minecraft.client.option.Perspective;

public class PerspectiveEvent implements Event {
    public Perspective perspective;

    public PerspectiveEvent perspective(Perspective perspective) {
        this.perspective = perspective;
        return this;
    }

    public Perspective perspective() {
        return this.perspective;
    }

    public PerspectiveEvent(Perspective perspective) {
        this.perspective = perspective;
    }
}
