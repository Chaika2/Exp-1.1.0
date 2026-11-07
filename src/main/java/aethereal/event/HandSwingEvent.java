package aethereal.event;

public class HandSwingEvent extends CancellableEvent {
    public float swingSpeed;

    public float swingSpeed() {
        return this.swingSpeed;
    }

    public HandSwingEvent swingSpeed(float f) {
        this.swingSpeed = f;
        return this;
    }
}
