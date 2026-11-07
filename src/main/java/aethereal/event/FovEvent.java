package aethereal.event;

public class FovEvent extends CancellableEvent {
    public int fov;

    public int getFov() {
        return this.fov;
    }

    public void setFov(int i) {
        this.fov = i;
    }
}
