package aethereal.event;

public final class PlayerTravelEvent implements Event {
    public float speed;

    public PlayerTravelEvent(float speed) {
        this.speed = speed;
    }

    public float speed() {
        return this.speed;
    }

    public void speed(float speed) {
        this.speed = speed;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[speed=" + this.speed + "]";
    }
}
