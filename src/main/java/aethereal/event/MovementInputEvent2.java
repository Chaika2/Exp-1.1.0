package aethereal.event;

public class MovementInputEvent2 implements Event {
    public float movementForward;
    public float movementSideways;

    public float getMovementForward() {
        return this.movementForward;
    }

    public float getMovementSideways() {
        return this.movementSideways;
    }

    public void setMovementForward(float f) {
        this.movementForward = f;
    }

    public void setMovementSideways(float f) {
        this.movementSideways = f;
    }

    public MovementInputEvent2(float f, float f2) {
        this.movementForward = f;
        this.movementSideways = f2;
    }
}
