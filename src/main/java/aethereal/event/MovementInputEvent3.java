package aethereal.event;

public class MovementInputEvent3 extends CancellableEvent {
    public final float movementForward;
    public final float movementSideways;

    public float getMovementForward() {
        return this.movementForward;
    }

    public float getMovementSideways() {
        return this.movementSideways;
    }

    public MovementInputEvent3(float f, float f2) {
        this.movementForward = f;
        this.movementSideways = f2;
    }
}
